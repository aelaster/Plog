package com.lastsoft.plog;

import android.app.Dialog;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.lastsoft.plog.adapter.PlayAdapter;
import com.lastsoft.plog.adapter.TransitionListenerAdapter;
import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.PlayersPerPlay;
import com.lastsoft.plog.db.PlaysPerGameGroup;
import com.lastsoft.plog.dialogs.DeletePlayDialogFragment;
import com.lastsoft.plog.util.AppUtils;
import com.lastsoft.plog.util.CustomViewPager;
import com.lastsoft.plog.util.DeletePlayTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.lastsoft.plog.MainActivity.EXTRA_CURRENT_ITEM_POSITION;
import static com.lastsoft.plog.MainActivity.EXTRA_OLD_ITEM_POSITION;

public class ViewPlayActivity extends AppCompatActivity implements
        AddPlayFragment.OnFragmentInteractionListener,
        DeletePlayDialogFragment.OnDialogButtonClickListener{
    private long playID;
    String imageTransID;
    String nameTransID;
    String dateTransID;
    PlayAdapter mPlayAdapter;
    String searchQuery = "";
    private int playListType = 0;
    private int currentYear = 0;
    private int sortType = 0;
    private boolean fromDrawer;

    Toolbar toolbar;

    private static final String STATE_CURRENT_POSITION = "state_current_position";
    private static final String STATE_OLD_POSITION = "state_old_position";


    public ViewPlayActivity() {
        // Required empty public constructor
    }

    private int mCurrentPosition;
    private int mOriginalPosition;
    private boolean mIsReturning;

    private final SharedElementCallback mCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (mIsReturning && mPlayAdapter.getItemCount() > 0) {
                View sharedImageView = mPagerAdapter.getCurrentDetailsFragment().getSharedImageElement();
                if (sharedImageView == null) {
                    // If shared view is null, then it has likely been scrolled off screen and
                    // recycled. In this case we cancel the shared element transition by
                    // removing the shared elements from the shared elements map.
                    names.clear();
                    sharedElements.clear();
                } else if (mCurrentPosition != mOriginalPosition) {
                    names.clear();
                    sharedElements.clear();
                    names.add(sharedImageView.getTransitionName());
                    sharedElements.put(sharedImageView.getTransitionName(), sharedImageView);
                }
            }
        }

        @Override
        public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements,
                                         List<View> sharedElementSnapshots) {
            if (!mIsReturning) {
                getWindow().setEnterTransition(makeEnterTransition());
            }
        }

        @Override
        public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements,
                                       List<View> sharedElementSnapshots) {
            if (mIsReturning) {
                getWindow().setReturnTransition(makeReturnTransition());
            }
        }

        private View getSharedElement(List<View> sharedElements) {
            for (final View view : sharedElements) {
                if (view instanceof ImageView) {
                    return view;
                }
            }
            return null;
        }
    };



    private Transition makeEnterTransition() {
        View rootView = mPagerAdapter.getCurrentDetailsFragment().getView();
        assert rootView != null;

        TransitionSet enterTransition = new TransitionSet();


        // Slide the cards in through the bottom of the screen.
        Transition cardSlide = new Slide(Gravity.BOTTOM);
        cardSlide.addTarget(rootView.findViewById(R.id.container));
        enterTransition.addTransition(cardSlide);

        // Don't fade the navigation/status bars.
        Transition fade = new Fade();
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        enterTransition.addTransition(fade);

        final Resources res = getResources();
        final TextView gameName = (TextView) rootView.findViewById(R.id.gameName);
        final TextView gameDate = (TextView) rootView.findViewById(R.id.gameDate);
        gameName.setAlpha(0f);
        gameDate.setAlpha(0f);
        toolbar.setAlpha(0f);
        enterTransition.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionStart(Transition transition) {
                gameName.animate().alpha(1f).setDuration(res.getInteger(R.integer.text_background_fade_millis));
                gameDate.animate().alpha(1f).setDuration(res.getInteger(R.integer.text_background_fade_millis));
                toolbar.animate().alpha(1f).setDuration(res.getInteger(R.integer.text_background_fade_millis));
            }
        });

        enterTransition.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        return enterTransition;
    }

    private Transition makeReturnTransition() {
        if (mPlayAdapter.getItemCount() > 0) {
            View rootView = mPagerAdapter.getCurrentDetailsFragment().getView();
            assert rootView != null;

            TransitionSet returnTransition = new TransitionSet();

            try {
                // Slide the cards off the bottom of the screen.
                Transition cardSlide = new Slide(Gravity.BOTTOM);
                cardSlide.addTarget(rootView.findViewById(R.id.container));
                returnTransition.addTransition(cardSlide);
            } catch (Exception ignored) {
            }
            returnTransition.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
            return returnTransition;
        }
        return null;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            searchQuery = getIntent().getExtras().getString("searchQuery");
            playID =  getIntent().getExtras().getLong("playID");
            imageTransID =  getIntent().getExtras().getString("imageTransID");
            nameTransID =  getIntent().getExtras().getString("nameTransID");
            dateTransID =  getIntent().getExtras().getString("dateTransID");
            mCurrentPosition =  getIntent().getExtras().getInt("adapterPosition");
            fromDrawer =  getIntent().getExtras().getBoolean("fromDrawer");
            playListType =  getIntent().getExtras().getInt("playListType");
            currentYear =  getIntent().getExtras().getInt("currentYear");
            sortType =  getIntent().getExtras().getInt("sortType");
            mOriginalPosition = mCurrentPosition;
        } else {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_POSITION);
            mOriginalPosition = savedInstanceState.getInt(STATE_OLD_POSITION);
            searchQuery = savedInstanceState.getString("searchQuery");
            playListType = savedInstanceState.getInt("playListType");
            currentYear = savedInstanceState.getInt("currentYear");
            sortType =  savedInstanceState.getInt("sortType");
        }

        setContentView(R.layout.fragment_view_play_swipe);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheSize(41943040)
                .diskCacheSize(104857600)
                .threadPoolSize(10)
                .build();
        if (!ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().init(config);
        }


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_action_cancel);
        toolbar.inflateMenu(R.menu.view_play); // this does nothing at all
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                long menuPlayId = mPlayAdapter.plays.get(mPager.getCurrentItem()).getId();
                if (id == R.id.view_image) {
                    String[] photoParts = mPlayAdapter.plays.get(mPager.getCurrentItem()).playPhoto.split("/");
                    File newFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/Plog/",photoParts[photoParts.length-1]);
                    Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.lastsoft.plog.fileprovider", newFile);
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(contentUri, "image/*");
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.edit_play) {
                    toolbar.setVisibility(View.GONE);
                    openAddPlay(GamesPerPlay.getBaseGame(Play.findById(Play.class, menuPlayId)).gameName, menuPlayId);
                    return true;
                } else if (id == R.id.delete_play) {
                    deletePlay(menuPlayId);
                    return true;
                } else if (id == R.id.share_play) {
                    ShareActionProvider mShareActionProvider = new ShareActionProvider(ViewPlayActivity.this);
                    MenuItemCompat.setActionProvider(item, mShareActionProvider);

                    String playPhoto;
                    playPhoto = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES) + "/Plog/" + mPagerAdapter.getCurrentDetailsFragment().thisPlay.playPhoto;

                    Uri imageUri = Uri.parse("file://" + playPhoto);
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    shareIntent.setType("image/*");
                    mShareActionProvider.setShareIntent(shareIntent);
                }
                return false;
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        postponeEnterTransition();

        mPlayAdapter = new PlayAdapter(this, null, searchQuery, fromDrawer, playListType, sortType, currentYear);
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (CustomViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(mCurrentPosition);
        toggleMenuShare();
        mPager.setOffscreenPageLimit(3);
        mPager.setPageTransformer(true, new DepthPageTransformer());
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                invalidateOptionsMenu();
                mCurrentPosition = position;
                toggleMenuShare();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                isSwiping = state;
                if (pendingBack) {
                    onBackPressed();
                }
            }
        });

        getWindow().getSharedElementEnterTransition().setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        setEnterSharedElementCallback(mCallback);
    }

    private void toggleMenuShare(){
        long menuPlayId = mPlayAdapter.plays.get(mCurrentPosition).getId();
        String photo = Play.findById(Play.class, menuPlayId).playPhoto;
        if (photo == null || photo.equals("")){
            toolbar.getMenu().getItem(0).setVisible(false);
        }else{
            toolbar.getMenu().getItem(0).setVisible(true);
        }
    }

    @Override
    public void onBackPressed(){
        if (mAddPlayFragment != null){
            toolbar.setVisibility(View.VISIBLE);
            mAddPlayFragment.removeYourself();
            mAddPlayFragment = null;
            mPager.setPagingEnabled(true);
            //mPagerAdapter.notifyDataSetChanged();
            mPagerAdapter.updateCurrentFragment();
        }else{
            toolbar.setVisibility(View.GONE);
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_POSITION, mCurrentPosition);
        outState.putInt(STATE_OLD_POSITION, mOriginalPosition);
        outState.putString("searchQuery", searchQuery);
        outState.putInt("playListType", playListType);
        outState.putInt("sortType", sortType);
        outState.putInt("currentYear", currentYear);
    }

    @Override
    public void finishAfterTransition() {
        mIsReturning = true;
        getWindow().setReturnTransition(makeReturnTransition());
        Intent data = new Intent();
        data.putExtra(EXTRA_OLD_ITEM_POSITION, getIntent().getExtras().getInt("adapterPosition"));
        data.putExtra(EXTRA_CURRENT_ITEM_POSITION, mCurrentPosition);
        data.putExtra("mSearchQuery", searchQuery);
        data.putExtra("playListType", playListType);
        data.putExtra("currentYear", currentYear);
        data.putExtra("sortType", sortType);
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }


    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    public CustomViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private ScreenSlidePagerAdapter mPagerAdapter;

    public int isSwiping = ViewPager.SCROLL_STATE_IDLE;
    public boolean pendingBack = false;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onFragmentInteraction(String string) {
        mPager.setPagingEnabled(true);
        //mPagerAdapter.notifyDataSetChanged();
        mPagerAdapter.getCurrentDetailsFragment().redrawLayout();
        // in here we should recreate the frag
    }


    AddPlayFragment mAddPlayFragment;
    public void openAddPlay(String game_name, long playID){

        mPager.setPagingEnabled(false);

        try{
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception ignored){}

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        mAddPlayFragment = AddPlayFragment.newInstance(0, 0, true, game_name, playID, false);
        ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
        ft.add(R.id.swipeholder, mAddPlayFragment, "add_play");
        ft.addToBackStack("add_play");
        ft.commitAllowingStateLoss();
    }


    public void deletePlay(long playID){
        DeletePlayDialogFragment newFragment = new DeletePlayDialogFragment().newInstance(playID);
        newFragment.show(getSupportFragmentManager(), "deletePlay");
    }

    @Override
    public void onPositiveClick_DeletePlay(long playId) {
        AppUtils.deletePlay(this, playId);
        mPlayAdapter = new PlayAdapter(this, null, searchQuery, fromDrawer, playListType, sortType, currentYear);
        mPagerAdapter.notifyDataSetChanged();
        if (mPlayAdapter.getItemCount() == 0){
            onBackPressed();
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        ViewPlayFragment_Pages mCurrentFragment;
        ViewGroup mCurrentContainer;
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ViewPlayFragment_Pages.newInstance(mPlayAdapter.plays.get(position).getId(), "imageTrans"+position, "nameTrans"+position, "dateTrans"+position);
        }

        @Override
        public int getCount() {
            return mPlayAdapter.plays.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mCurrentFragment = (ViewPlayFragment_Pages) object;
            mCurrentContainer = container;
        }

        public void updateCurrentFragment(){
            mCurrentFragment.redrawLayout();
        }

        public ViewPlayFragment_Pages getCurrentDetailsFragment() {
            return mCurrentFragment;
        }
    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
