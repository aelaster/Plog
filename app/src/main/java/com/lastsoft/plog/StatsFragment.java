package com.lastsoft.plog;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.gson.Gson;
import com.lastsoft.plog.db.GameGroup;

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


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            if (mActivity != null) {
                ((MainActivity) mActivity).setUpActionBar(7);
            }
        }
    }

    public ViewPager mPager;


    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    View viewPlayLayout;
    long groupToPoll = 0;
    GameGroup theGroup = new GameGroup();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewPlayLayout = inflater.inflate(R.layout.fragment_view_stats_swipe, container, false);
        viewPlayLayout.setBackgroundColor(getResources().getColor(R.color.cardview_initial_background));


        ActionBar mActionBar = ((MainActivity)mActivity).getSupportActionBar();

        List<String> gameGroupNames = new ArrayList<>();
        final List<GameGroup> gameGroups = GameGroup.listAll_AZ(true);
        int i = 0;
        gameGroupNames.add(getString(R.string.select_group));
        gameGroupNames.add(getString(R.string.everyone));
        for(GameGroup group:gameGroups){
            gameGroupNames.add(group.groupName);
            i++;
        }

        ArrayAdapter<String> mSpinnerAdapter = null;
        if (mActionBar != null) {
            mSpinnerAdapter = new ArrayAdapter<>(mActionBar.getThemedContext(), R.layout.simple_list_item_1, gameGroupNames);
        }
        if (mActionBar != null) {
            mActionBar.setListNavigationCallbacks(mSpinnerAdapter, new ActionBar.OnNavigationListener() {
                @Override
                public boolean onNavigationItemSelected(int position, long itemId) {
                    if (position > 0) {

                        if (position - 2 == -2) {
                            theGroup.setId((long)-1);
                            //groupToPoll = -1; //dont do anything
                        }else if (position - 2 == -1) {
                            //groupToPoll = 0; //this is the everyone group
                            theGroup.setId((long)0);
                        } else {
                            theGroup = gameGroups.get(position - 2);
                           // groupToPoll = theGroup.getId();
                        }


                        mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager(), theGroup);
                        mPager.setAdapter(mPagerAdapter);
                    }
                    return true;
                }
            });
        }

        GameGroup init = new GameGroup();
        init.setId((long)-1);
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) viewPlayLayout.findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager(), init);
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


    Activity mActivity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mActivity != null) {
            ((MainActivity) mActivity).setUpActionBar(7);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mActivity = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String string);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        GameGroup theGroup;
        public ScreenSlidePagerAdapter(FragmentManager fm, GameGroup theGroup) {
            super(fm);
            this.theGroup = theGroup;
        }

        @Override
        public Fragment getItem(int position) {
            Gson gson = new Gson();
            String json = gson.toJson(theGroup);
            if (position == 0){
                return StatsFragment_Wins.newInstance(json);
            }else {
                return StatsFragment_TenByTen.newInstance(json);
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
