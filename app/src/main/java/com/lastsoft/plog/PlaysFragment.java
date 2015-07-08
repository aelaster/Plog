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
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.lastsoft.plog.adapter.PlayAdapter;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class PlaysFragment extends Fragment{

    private static final String TAG = "PlaysFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private static final int DATASET_COUNT = 60;
    private float x,y;



    private enum LayoutManagerType {
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected RecyclerView mRecyclerView;
    protected PlayAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected String[] mDataset;
    private SwipeRefreshLayout pullToRefreshView;
    private ImageView mCancel;
    private EditText mSearch;
    private String mSearchQuery = "";
    private int playListType = 0;
    private boolean fromDrawer;

    public static PlaysFragment newInstance(boolean fromDrawer, String searchQuery, int playListType) {
        PlaysFragment fragment = new PlaysFragment();
        Bundle args = new Bundle();
        args.putBoolean("fromDrawer", fromDrawer);
        args.putString("searchQuery", searchQuery);
        args.putInt("playListType", playListType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fromDrawer = getArguments().getBoolean("fromDrawer");
            mSearchQuery = getArguments().getString("searchQuery");
            playListType = getArguments().getInt("playListType");
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
        }else{
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.plays_view_frag, container, false);
        rootView.setBackgroundColor(getResources().getColor(R.color.cardview_initial_background));
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller) rootView.findViewById(R.id.fastscroller);

        // Connect the recycler to the scroller (to let the scroller scroll the list)
        fastScroller.setRecyclerView(mRecyclerView, null);

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        mRecyclerView.setOnScrollListener(fastScroller.getOnScrollListener());

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
        mAdapter = ((MainActivity)mActivity).mPlayAdapter;
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // END_INCLUDE(initializeRecyclerView)


        if (mSearch != null) {
            mSearch.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                    // When user changed the Text
                    mSearchQuery = cs.toString();
                    //initDataset();
                    //mAdapter = new GameAdapter(PlaysFragment.this, mActivity,mSearchQuery);
                    mAdapter = ((MainActivity) mActivity).initPlayAdapter(mSearchQuery, fromDrawer, playListType);
                    // Set CustomAdapter as the adapter for RecyclerView.
                    mRecyclerView.setAdapter(mAdapter);
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
                        ((MainActivity) mActivity).initPlayAdapter(mSearchQuery, fromDrawer, playListType);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get item selected and deal with it
        switch (item.getItemId()) {
            case android.R.id.home:
                //called when the up affordance/carat in actionbar is pressed
                mActivity.onBackPressed();
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
        int firstVisible = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                .findFirstCompletelyVisibleItemPosition();
        //int current = ((PlayAdapter)mRecyclerView.getAdapter()).mPosition;

        mAdapter = ((MainActivity)mActivity).initPlayAdapter(mSearchQuery, fromDrawer, playListType);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.scrollToPosition(firstVisible);
    }

}
