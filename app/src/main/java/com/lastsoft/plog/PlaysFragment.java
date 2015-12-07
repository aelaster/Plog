/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.lastsoft.plog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.lastsoft.plog.adapter.PlayAdapter;
import com.lastsoft.plog.util.MyRecyclerScroll;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class PlaysFragment extends Fragment{

    private static final String TAG = "PlaysFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private static final int DATASET_COUNT = 60;
    private float x,y;
    private int sortType = 0;
    FloatingActionButton addPlay;
    int fabMargin;


    private enum LayoutManagerType {
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;
    //private OnFragmentInteractionListener mListener;

    protected RecyclerView mRecyclerView;
    protected PlayAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    private ImageView mCancel;
    private EditText mSearch;
    private String mSearchQuery = "";
    private String fragmentName = "";
    private int playListType = 0;
    private int currentYear = 0;
    private boolean fromDrawer;

    public static PlaysFragment newInstance(boolean fromDrawer, String searchQuery, int playListType, String fragmentName, int currentYear) {
        PlaysFragment fragment = new PlaysFragment();
        Bundle args = new Bundle();
        args.putBoolean("fromDrawer", fromDrawer);
        args.putString("searchQuery", searchQuery);
        args.putInt("playListType", playListType);
        args.putInt("currentYear", currentYear);
        args.putString("fragmentName", fragmentName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fromDrawer = getArguments().getBoolean("fromDrawer");
            mSearchQuery = getArguments().getString("searchQuery");
            fragmentName = getArguments().getString("fragmentName");
            playListType = getArguments().getInt("playListType");
            currentYear = getArguments().getInt("currentYear");
        }
        ActionBar actionBar = ((MainActivity) mActivity).getSupportActionBar();
        if (fromDrawer) {
            try {

                if (actionBar != null) {
                    //actionBar.setDisplayShowCustomEnabled(true);
                    actionBar.setCustomView(R.layout.search_bar_plays);
                    mSearch = (EditText) actionBar.getCustomView()
                            .findViewById(R.id.etSearch);
                    mCancel = (ImageView) actionBar.getCustomView()
                            .findViewById(R.id.closeButton);
                }
            } catch (Exception ignored) {
            }
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_plays, container, false);

        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setBackgroundColor(getResources().getColor(R.color.cardview_initial_background));
        VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller) rootView.findViewById(R.id.fastscroller);

        // Connect the recycler to the scroller (to let the scroller scroll the list)
        fastScroller.setRecyclerView(mRecyclerView, null);

        addPlay = (FloatingActionButton) rootView.findViewById(R.id.add_play);
        if (fromDrawer && playListType != 2) {
            addPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int viewXY[] = new int[2];
                    v.getLocationOnScreen(viewXY);
                    /*if (mListener != null) {
                        mListener.onFragmentInteraction("add_play", viewXY[0], viewXY[1]);
                    }*/
                    ((MainActivity)mActivity).usedFAB = true;
                    ((MainActivity)mActivity).openGames("", true, 0, getString(R.string.title_games), MainActivity.CurrentYear);
                }
            });
        }else{
            addPlay.setVisibility(View.GONE);
        }

        fabMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
        mRecyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void show() {
                addPlay.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            }

            @Override
            public void hide() {
                addPlay.animate().translationY(addPlay.getHeight() + fabMargin).setInterpolator(new AccelerateInterpolator(2)).start();
            }
        });

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        mRecyclerView.addOnScrollListener(fastScroller.getOnScrollListener());

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(mActivity);

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        //mAdapter = new PlayAdapter(mActivity, this);

        if (((MainActivity)mActivity).mPlayAdapter != null) {
            mAdapter = ((MainActivity) mActivity).mPlayAdapter;
        }else{
            mAdapter = ((MainActivity) mActivity).initPlayAdapter(mSearchQuery, fromDrawer, playListType, currentYear);
        }
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // END_INCLUDE(initializeRecyclerView)

        if (mSearch != null) {
            mSearch.setHint(getString(R.string.filter) + mAdapter.getItemCount() + getString(R.string.filter_plays));
        }

        //boolean pauseOnScroll = true; // or true
        //boolean pauseOnFling = true; // or false
        //NewPauseOnScrollListener listener = new NewPauseOnScrollListener(ImageLoader.getInstance(), pauseOnScroll, pauseOnFling);
        //mRecyclerView.addOnScrollListener(listener);

        if (!fromDrawer){
            RelativeLayout playsLayout = (RelativeLayout) rootView.findViewById(R.id.playsLayout);
            final SwipeDismissBehavior<LinearLayout> behavior = new SwipeDismissBehavior();
            behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);
            behavior.setStartAlphaSwipeDistance(1.0f);
            behavior.setSensitivity (0.15f);
            behavior.setListener(new SwipeDismissBehavior.OnDismissListener() {
                @Override
                public void onDismiss(final View view) {
                    PlaysFragment myFragC1 = (PlaysFragment) getFragmentManager().findFragmentByTag("plays");
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.remove(myFragC1);
                    transaction.commitAllowingStateLoss();
                    getFragmentManager().executePendingTransactions();
                    mActivity.onBackPressed();
                }

                @Override
                public void onDragStateChanged(int i) {

                }
            });

            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) playsLayout.getLayoutParams();
            params.setBehavior(behavior);
        }


        if (mSearch != null) {
            mSearch.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                    // When user changed the Text
                    if (mActivity != null) {
                        mSearchQuery = cs.toString();
                        //initDataset();
                        //mAdapter = new GameAdapter(PlaysFragment.this, mActivity,mSearchQuery);
                        mAdapter = ((MainActivity) mActivity).initPlayAdapter(mSearchQuery, fromDrawer, playListType, currentYear);
                        // Set CustomAdapter as the adapter for RecyclerView.
                        mRecyclerView.setAdapter(mAdapter);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {}

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                              int arg3) {}
            });


            mCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mSearch.getText().toString().equals("")) {
                        mSearchQuery = "";
                        ((MainActivity) mActivity).initPlayAdapter(mSearchQuery, fromDrawer, playListType, currentYear);
                        mSearch.setText(mSearchQuery);

                        //mActivity.onBackPressed();
                    }

                    InputMethodManager inputManager = (InputMethodManager)
                            mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    mSearch.clearFocus();
                    mRecyclerView.requestFocus();

                    refreshDataset();
                }
            });
        }

        return rootView;
    }

    public void setSearchText(String theText){
        if (mSearch.getText().toString().equals(theText)){
            mSearch.setText("");
        }else {
            mSearch.setText(theText);
        }
    }

    private MenuItem menuItem0;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        if (!((MainActivity) mActivity).mNavigationDrawerFragment.isDrawerOpen()) {
            inflater.inflate(R.menu.plays, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get item selected and deal with it
        switch (item.getItemId()) {
            case android.R.id.home:
                //called when the up affordance/carat in actionbar is pressed
                mActivity.onBackPressed();
                return true;
            case R.id.sort_plays:
                View menuItemView = mActivity.findViewById(R.id.sort_plays);
                PopupMenu popup = new PopupMenu(mActivity, menuItemView);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.sort_date_newold:
                                sortType = 0;
                                mAdapter.updateData(mAdapter.generatePlayData(mSearchQuery, playListType, sortType, currentYear));
                                mRecyclerView.scrollToPosition(0);
                                return true;
                            case R.id.sort_date_oldnew:
                                sortType = 1;
                                mAdapter.updateData(mAdapter.generatePlayData(mSearchQuery, playListType, sortType, currentYear));
                                mRecyclerView.scrollToPosition(0);
                                return true;
                            case R.id.sort_az:
                                sortType = 2;
                                mAdapter.updateData(mAdapter.generatePlayData(mSearchQuery, playListType, sortType, currentYear));
                                mRecyclerView.scrollToPosition(0);
                                return true;
                            case R.id.sort_za:
                                sortType = 3;
                                mAdapter.updateData(mAdapter.generatePlayData(mSearchQuery, playListType, sortType, currentYear));
                                mRecyclerView.scrollToPosition(0);
                                return true;
                        }
                        return false;
                    }
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.plays_sort, popup.getMenu());
                popup.show();
                return true;
        }
        return false;
    }

    Activity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (fromDrawer) {
            if (mActivity != null) {
                ((MainActivity) mActivity).setUpActionBar(6);

            }
        }else {
            if (mActivity != null) {
                ((MainActivity) mActivity).setUpActionBar(10);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mActivity != null) {

            /*
              playListType:
        0 - listPlaysNewOld
        1 - list total plays for groupID, which is passed in via mSearchQuery
        2 - a players total regular wins, passed in the search query.  it is group and then player id, split with a caret
        3 - a players total asterisk wins, passed in the search query.  it is group and then player id, split with a caret
        4 - a players total wins, passed in the search query.  it is group and then player id, split with a caret
        5 - total group shared wins
        6 - total group losses
        7 - plays for a ten by ten game.  Group, then game, then year.
        8 - plays for a specific player
        9 - listPlaysNewOld, allowing expansions
        10 - plays for all ten by ten games.  Group, then year.
         */
            //Log.d("V1", "fragmentName = " + fragmentName);

            if (fragmentName.equals(getString(R.string.title_bucket_list))){
                ((MainActivity) mActivity).setUpActionBar(12);
            }else if (fragmentName.equals(getString(R.string.title_players))){
                ((MainActivity) mActivity).setUpActionBar(5);
            }else if (fragmentName.equals(getString(R.string.title_games))){
                ((MainActivity) mActivity).setUpActionBar(4);
            }else if (fragmentName.equals(getString(R.string.title_statistics))){
                ((MainActivity) mActivity).setUpActionBar(7);
            }else if (fragmentName.equals(getString(R.string.title_plays))){
                ((MainActivity) mActivity).setUpActionBar(6);
            }

            mActivity = null;
        }
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        //int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        /*if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }*/

        switch (layoutManagerType) {
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(mActivity);
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(mActivity);
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        //mRecyclerView.scrollToPosition(scrollPosition);

        //int duration = getResources().getInteger(R.integer.scroll_duration);
        //mRecyclerView.setLayoutManager(new ScrollingLinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false, duration));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity)mActivity).unbindDrawables(mRecyclerView);
    }


    protected void refreshDataset(){
        /*int firstVisible = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                .findFirstCompletelyVisibleItemPosition();
        //int current = ((PlayAdapter)mRecyclerView.getAdapter()).mPosition;

        mAdapter = ((MainActivity)mActivity).initPlayAdapter(mSearchQuery, fromDrawer, playListType);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.scrollToPosition(firstVisible);*/
        if (mAdapter != null) {
            mAdapter.updateData(mAdapter.generatePlayData(mSearchQuery, playListType, sortType, currentYear));
            if (mSearch != null) {
                mSearch.setHint(getString(R.string.filter) + mAdapter.getItemCount() + getString(R.string.filter_plays));
            }
        }else{
            mAdapter = ((MainActivity) mActivity).initPlayAdapter(mSearchQuery, fromDrawer, playListType, currentYear);
        }
    }

}
