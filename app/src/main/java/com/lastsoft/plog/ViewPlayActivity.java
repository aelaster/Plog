package com.lastsoft.plog;

import android.app.ActionBar;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.PlayersPerPlay;
import com.lastsoft.plog.db.PlaysPerGameGroup;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.lastsoft.plog.MainActivity.EXTRA_CURRENT_ITEM_POSITION;
import static com.lastsoft.plog.MainActivity.EXTRA_OLD_ITEM_POSITION;

public class ViewPlayActivity extends AppCompatActivity implements AddPlayFragment.OnFragmentInteractionListener {
    // TODO: Rename and change types of parameters
    private long playID;
    String imageTransID;
    String nameTransID;
    String dateTransID;
    private OnFragmentInteractionListener mListener;
    private ViewGroup mContainerView_Players;
    private ViewGroup mContainerView_Expansions;
    ImageView playImage;
    PlayAdapter mPlayAdapter;
    String searchQuery = "";

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
            if (mIsReturning) {
                View sharedImageView = mPagerAdapter.getCurrentDetailsFragment().getSharedImageElement();
                View sharedNameView = mPagerAdapter.getCurrentDetailsFragment().getSharedNameElement();
                View sharedDateView = mPagerAdapter.getCurrentDetailsFragment().getSharedDateElement();
                //Log.d("V1", "mCurrentPosition = " + mCurrentPosition);
                //Log.d("V1", "mOriginalPosition = " + mOriginalPosition);
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
                    //names.add(sharedNameView.getTransitionName());
                    //names.add(sharedDateView.getTransitionName());
                    sharedElements.put(sharedImageView.getTransitionName(), sharedImageView);
                    //sharedElements.put(sharedNameView.getTransitionName(), sharedNameView);
                    //sharedElements.put(sharedDateView.getTransitionName(), sharedDateView);
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


        // Play a circular reveal animation starting beneath the shared element.
        /*Transition circularReveal = new CircularReveal(sharedElement);
        circularReveal.addTarget(rootView.findViewById(R.id.imageView1));
        enterTransition.addTransition(circularReveal);*/

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
        enterTransition.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionStart(Transition transition) {
                gameName.animate().alpha(1f).setDuration(res.getInteger(R.integer.text_background_fade_millis));
                gameDate.animate().alpha(1f).setDuration(res.getInteger(R.integer.text_background_fade_millis));
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                //gameName.animate().alpha(1f).setDuration(res.getInteger(R.integer.text_background_fade_millis));
                //gameDate.animate().alpha(1f).setDuration(res.getInteger(R.integer.text_background_fade_millis));
            }
        });

        enterTransition.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        return enterTransition;
    }

    private Transition makeReturnTransition() {
        View rootView = mPagerAdapter.getCurrentDetailsFragment().getView();
        assert rootView != null;

        TransitionSet returnTransition = new TransitionSet();

        // Slide and fade the circular reveal container off the top of the screen.
        /*TransitionSet slideFade = new TransitionSet();
        slideFade.addTarget(rootView.findViewById(R.id.imageView1));
        slideFade.addTransition(new Slide(Gravity.TOP));
        slideFade.addTransition(new Fade());
        returnTransition.addTransition(slideFade);*/

        /*returnTransition.setOrdering(TransitionSet.ORDERING_TOGETHER);

        Transition recolor = new Recolor();
        recolor.addTarget(mPagerAdapter.getCurrentDetailsFragment().getSharedNameElement());
        recolor.addTarget(mPagerAdapter.getCurrentDetailsFragment().getSharedDateElement());
        returnTransition.addTransition(recolor);

        Transition changeBounds = new ChangeBounds();
        changeBounds.addTarget(mPagerAdapter.getCurrentDetailsFragment().getSharedNameElement());
        changeBounds.addTarget(mPagerAdapter.getCurrentDetailsFragment().getSharedDateElement());
        returnTransition.addTransition(changeBounds);

        Transition textSize = new TextSizeTransition();
        textSize.addTarget(mPagerAdapter.getCurrentDetailsFragment().getSharedNameElement());
        textSize.addTarget(mPagerAdapter.getCurrentDetailsFragment().getSharedDateElement());
        returnTransition.addTransition(textSize);*/
        try {
            // Slide the cards off the bottom of the screen.
            Transition cardSlide = new Slide(Gravity.BOTTOM);
            cardSlide.addTarget(rootView.findViewById(R.id.container));
            returnTransition.addTransition(cardSlide);
        }catch (Exception ignored){}
        returnTransition.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        return returnTransition;
    }

    Toolbar toolbar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO THE MENU STUFF, OR NOT...MAYBE JUST A VIEWER?
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            searchQuery = getIntent().getExtras().getString("searchQuery");
            playID =  getIntent().getExtras().getLong("playID");
            imageTransID =  getIntent().getExtras().getString("imageTransID");
            nameTransID =  getIntent().getExtras().getString("nameTransID");
            dateTransID =  getIntent().getExtras().getString("dateTransID");
            mCurrentPosition =  getIntent().getExtras().getInt("adapterPosition");
            mOriginalPosition = mCurrentPosition;
        } else {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_POSITION);
            mOriginalPosition = savedInstanceState.getInt(STATE_OLD_POSITION);
            searchQuery = savedInstanceState.getString("searchQuery");
        }

        setContentView(R.layout.fragment_view_play_swipe);

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
                if (id == R.id.edit_play) {
                    toolbar.setVisibility(View.GONE);
                    openAddPlay(GamesPerPlay.getBaseGame(Play.findById(Play.class, menuPlayId)).gameName, menuPlayId);
                    return true;
                } else if (id == R.id.delete_play) {
                    deletePlay(menuPlayId, true);
                    return true;
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


        //setBackgroundColor(getResources().getColor(R.color.cardview_initial_background));
        mPlayAdapter = new PlayAdapter(this, null, searchQuery);
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (CustomViewPager) findViewById(R.id.pager);
       //mPager.setBackgroundColor(getResources().getColor(R.color.cardview_initial_background));
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(mCurrentPosition);
        //mPager.setOffscreenPageLimit(4);
        mPager.setPageTransformer(true, new DepthPageTransformer());
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                invalidateOptionsMenu();
                mCurrentPosition = position;
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
    }

    @Override
    public void finishAfterTransition() {
        Log.d("V1" ,"in here");
        mIsReturning = true;
        getWindow().setReturnTransition(makeReturnTransition());
        Intent data = new Intent();
        data.putExtra(EXTRA_OLD_ITEM_POSITION, getIntent().getExtras().getInt("adapterPosition"));
        data.putExtra(EXTRA_CURRENT_ITEM_POSITION, mCurrentPosition);
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
        // TODO Auto-generated method stub
        //Log.d("V1", "on destroy");
        super.onDestroy();
    }

    @Override
    public void onFragmentInteraction(String string) {
        mPager.setPagingEnabled(true);
        //mPagerAdapter.notifyDataSetChanged();
        mPagerAdapter.getCurrentDetailsFragment().redrawLayout();
        // in here we should recreate the frag
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name

        void onFragmentInteraction(String string);
    }

    AddPlayFragment mAddPlayFragment;
    public void openAddPlay(String game_name, long playID){

        mPager.setPagingEnabled(false);
        //mTitle = game_name;

        try{
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception ignored){}

        //mFragment.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_top));



        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        mAddPlayFragment = AddPlayFragment.newInstance(0, 0, true, game_name, playID);
        //mAddPlayFragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_bottom));
        //mAddPlayFragment.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_top));
        ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
        ft.add(R.id.swipeholder, mAddPlayFragment, "add_play");
        ft.addToBackStack("add_play");
        ft.commitAllowingStateLoss();
    }


    public void deletePlay(long playID, boolean backFlag){
        Play deleteMe = Play.findById(Play.class, playID);

        //delete PlayersPerPlay
        List<PlayersPerPlay> players = PlayersPerPlay.getPlayers(deleteMe);
        for(PlayersPerPlay player:players){
            player.delete();
        }
        //delete GamesPerPay
        List<GamesPerPlay> games = GamesPerPlay.getGames(deleteMe);
        for(GamesPerPlay game:games){
            game.delete();
        }

        //delete plays_per_game_group
        List<PlaysPerGameGroup> plays = PlaysPerGameGroup.getPlays(deleteMe);
        for(PlaysPerGameGroup play:plays){
            play.delete();
        }

        //delete play image
        Log.d("V1", "play image = " + deleteMe.playPhoto);
        if(deleteMe.playPhoto != null && !deleteMe.playPhoto.equals("")) {
            File deleteImage = new File(deleteMe.playPhoto.substring(7, deleteMe.playPhoto.length()));
            if (deleteImage.exists()) {
                deleteImage.delete();
            }

            //delete play image thumb
            File deleteImage_thumb = new File(deleteMe.playPhoto.substring(7, deleteMe.playPhoto.length() - 4) + "_thumb.jpg");
            if (deleteImage_thumb.exists()) {
                deleteImage_thumb.delete();
            }
        }

        //delete play
        deleteMe.delete();

        mPlayAdapter = new PlayAdapter(this, null, searchQuery);
        mPagerAdapter.notifyDataSetChanged();
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
