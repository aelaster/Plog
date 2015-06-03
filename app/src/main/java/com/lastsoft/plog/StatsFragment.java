package com.lastsoft.plog;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.lastsoft.plog.db.GameGroup;
import com.lastsoft.plog.db.TenByTen;

import java.util.ArrayList;
import java.util.List;


public class StatsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {
        // Get the same strings provided for the drop-down's ArrayAdapter
        //String[] strings = mActivity.getResources().getStringArray(R.array.color_choices);

        @Override
        public boolean onNavigationItemSelected(int position, long itemId) {

            return true;
        }
    };


    public ViewPager mPager;


    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    View viewPlayLayout;
    long groupToPoll = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewPlayLayout = inflater.inflate(R.layout.fragment_view_stats_swipe, container, false);
        viewPlayLayout.setBackgroundColor(getResources().getColor(R.color.cardview_initial_background));


        ActionBar mActionBar = ((MainActivity)mActivity).getSupportActionBar();

        List<String> gameGroupNames = new ArrayList<>();
        final List<GameGroup> gameGroups = GameGroup.listAll(GameGroup.class);
        int i = 0;
        for(GameGroup group:gameGroups){
            gameGroupNames.add(group.groupName);
            i++;
        }

        ArrayAdapter<String> mSpinnerAdapter = null;
        if (mActionBar != null) {
            mSpinnerAdapter = new ArrayAdapter<>(mActionBar.getThemedContext(), android.R.layout.simple_list_item_1, gameGroupNames);
        }
        mActionBar.setListNavigationCallbacks(mSpinnerAdapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                GameGroup checker = gameGroups.get(position);
                groupToPoll = checker.getId();
                Log.d("V1", "group to poll = " + groupToPoll);
                mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager(), groupToPoll);
                mPager.setAdapter(mPagerAdapter);
                return true;
            }
        });

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) viewPlayLayout.findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager(), 0);
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When changing pages, reset the action bar actions since they are dependent
                // on which page is currently active. An alternative approach is to have each
                // fragment expose actions itself (rather than the activity exposing actions),
                // but for simplicity, the activity provides the actions in this sample.
                mActivity.invalidateOptionsMenu();
            }
        });
        return viewPlayLayout;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String string) {
        if (mListener != null) {
            mListener.onFragmentInteraction(string);
        }
    }

    Activity mActivity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        try {
            mListener = (OnFragmentInteractionListener) activity;
            ((MainActivity) mActivity).onSectionAttached(4);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mActivity = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String string);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        long theGroup;
        public ScreenSlidePagerAdapter(FragmentManager fm, long theGroup) {
            super(fm);
            this.theGroup = theGroup;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0){
                return StatsFragment_Wins.newInstance(theGroup);
            }else {
                return StatsFragment_TenByTen.newInstance(theGroup);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0){
                return getString(R.string.tab0);
            }else{
                return getString(R.string.tab1);
            }
        }

    }
}
