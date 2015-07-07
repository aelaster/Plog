package com.lastsoft.plog;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;
import net.i2p.android.ext.floatingactionbutton.FloatingActionsMenu;


public class PlayersFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    FloatingActionsMenu fabMenu;

    public PlayersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            if (mActivity != null) {
                ((MainActivity) mActivity).setUpActionBar(6);
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewPlayLayout = inflater.inflate(R.layout.fragment_players_swipe, container, false);
        viewPlayLayout.setBackgroundColor(getResources().getColor(R.color.cardview_initial_background));

        fabMenu = (FloatingActionsMenu) viewPlayLayout.findViewById(R.id.multiple_actions);

        FloatingActionButton addPlayer = (FloatingActionButton) viewPlayLayout.findViewById(R.id.add_player);
        addPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.toggle();
                int viewXY[] = new int[2];
                v.getLocationOnScreen(viewXY);

                if (mListener != null) {
                    mListener.onFragmentInteraction("add_player", viewXY[0], viewXY[1]);
                }
            }
        });

        FloatingActionButton addGroup = (FloatingActionButton) viewPlayLayout.findViewById(R.id.add_group);
        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.toggle();
                int viewXY[] = new int[2];
                v.getLocationOnScreen(viewXY);
                if (mListener != null) {
                    mListener.onFragmentInteraction("add_group", viewXY[0], viewXY[1]);
                }
            }
        });

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) viewPlayLayout.findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager(), -1);
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
            ((MainActivity) mActivity).setUpActionBar(6);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mActivity = null;
    }

    public void refreshDataset(boolean addFlag){
        ((ScreenSlidePagerAdapter)mPagerAdapter).setAddDropFlag(addFlag);
        mPagerAdapter.notifyDataSetChanged();
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        long theGroup;
        boolean addFlag;
        public ScreenSlidePagerAdapter(FragmentManager fm, long theGroup) {
            super(fm);
            this.theGroup = theGroup;
        }

        public void setAddDropFlag(boolean addDropFlag){
            addFlag = addDropFlag;
        }

        public void refreshPages(){
            if (this.getItem(0) instanceof PlayersFragment_Players) {
                ((PlayersFragment_Players)this.getItem(0)).updateDataset();
            }
            if (this.getItem(1) instanceof PlayersFragment_Groups) {
                ((PlayersFragment_Groups)this.getItem(1)).updateDataset();
            }
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0){
                return PlayersFragment_Players.newInstance();
            }else {
                return PlayersFragment_Groups.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public int getItemPosition(Object object) {
            if (addFlag) {
                return POSITION_NONE;
            }else{
                return POSITION_UNCHANGED;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0){
                return getString(R.string.title_players);
            }else{
                return getString(R.string.groups_header);
            }
        }

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
        void onFragmentInteraction(String id, float x, float y);
    }
}
