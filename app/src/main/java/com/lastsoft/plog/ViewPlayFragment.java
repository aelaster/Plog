package com.lastsoft.plog;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerPlay;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewPlayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ViewPlayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewPlayFragment extends Fragment {
    // TODO: Rename and change types of parameters
    private long playID;
    String imageTransID;
    String nameTransID;
    String dateTransID;
    int adapterPosition;
    private OnFragmentInteractionListener mListener;
    private ViewGroup mContainerView_Players;
    private ViewGroup mContainerView_Expansions;
    ImageView playImage;

    public static ViewPlayFragment newInstance(long playID, String transID, String transID2, String transID3, int position) {
        ViewPlayFragment fragment = new ViewPlayFragment();
        Bundle args = new Bundle();
        args.putLong("playID", playID);
        args.putString("imageTransID", transID);
        args.putString("nameTransID", transID2);
        args.putString("dateTransID", transID3);
        args.putInt("adapterPosition", position);
        fragment.setArguments(args);
        return fragment;
    }

    public ViewPlayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playID = getArguments().getLong("playID");
            imageTransID = getArguments().getString("imageTransID");
            nameTransID = getArguments().getString("nameTransID");
            dateTransID = getArguments().getString("dateTransID");
            adapterPosition = getArguments().getInt("adapterPosition");
        }

        /*
        Log.d("V1", "playID=" + playID);
        Log.d("V1", "imageTransID=" + imageTransID);
        Log.d("V1", "nameTransID=" + nameTransID);
        Log.d("V1", "dateTransID=" + dateTransID);
        */

        ((MainActivity)mActivity).getSupportActionBar().setDisplayShowCustomEnabled(false);

        setHasOptionsMenu(true);
    }


    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    public ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    View viewPlayLayout;

    public int isSwiping = ViewPager.SCROLL_STATE_IDLE;
    public boolean pendingBack = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewPlayLayout = inflater.inflate(R.layout.fragment_view_play_swipe, container, false);
        viewPlayLayout.setBackgroundColor(getResources().getColor(R.color.cardview_initial_background));

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) viewPlayLayout.findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(adapterPosition);
        //mPager.setOffscreenPageLimit(4);
        mPager.setPageTransformer(true, new DepthPageTransformer());
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When changing pages, reset the action bar actions since they are dependent
                // on which page is currently active. An alternative approach is to have each
                // fragment expose actions itself (rather than the activity exposing actions),
                // but for simplicity, the activity provides the actions in this sample.
                mActivity.invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                isSwiping = state;
                if (pendingBack){
                    mActivity.onBackPressed();
                }
            }
        });
        return viewPlayLayout;
    }



    public void refreshPager() {
        mPagerAdapter.notifyDataSetChanged();
    }

    public void resetPager(){
        mPager.setCurrentItem(adapterPosition);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.view_play, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        long menuPlayId = ((MainActivity)mActivity).mPlayAdapter.plays.get(mPager.getCurrentItem()).getId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.edit_play) {
            mPager.getCurrentItem();
            ((MainActivity) mActivity).openAddPlay(this, GamesPerPlay.getBaseGame(Play.findById(Play.class, menuPlayId)).gameName, menuPlayId);
            return true;
        }else if (id == R.id.delete_play) {
            ((MainActivity) mActivity).deletePlay(menuPlayId, true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    Activity mActivity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        ((MainActivity) mActivity).onSectionAttached(7);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mActivity != null) {
            ((MainActivity) mActivity).onSectionAttached(3);
            ((MainActivity) mActivity).getSupportActionBar().setDisplayShowCustomEnabled(true);
            mActivity = null;
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        //Log.d("V1", "on destroy");
        super.onDestroy();
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


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            return ViewPlayFragment_Pages.newInstance(((MainActivity)mActivity).mPlayAdapter.plays.get(position).getId(), "imageTrans"+position, "nameTrans"+position, "dateTrans"+position);
            //return ViewPlayFragment_Pages.newInstance(mAdapter.plays.get(position).getId(), "imageTrans"+position, "nameTrans"+position, "dateTrans"+position);
        }

        @Override
        public int getCount() {
            if (mActivity != null) {
                return ((MainActivity) mActivity).mPlayAdapter.plays.size();
            }else{
                return 0;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
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
