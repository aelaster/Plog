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
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lastsoft.plog.db.Game;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

public class GamesFragment extends Fragment{

    private static final String TAG = "GamesFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private static final int DATASET_COUNT = 60;
    private float x,y;



    private enum LayoutManagerType {
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected RecyclerView mRecyclerView;
    protected GameAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;


    private OnFragmentInteractionListener mListener;
    private SwipeRefreshLayout pullToRefreshView;
    private LinearLayout mProgress;
    private TextView mText;
    private ImageView mCancel;
    private EditText mSearch;
    String mSearchQuery = "";
    private boolean releaseFocus = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = ((MainActivity)mActivity).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.search_bar);
        mSearch = (EditText) actionBar.getCustomView()
                .findViewById(R.id.etSearch);
        mCancel = (ImageView) actionBar.getCustomView()
                .findViewById(R.id.closeButton);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.collection_view_frag, container, false);
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        FloatingActionButton addPlayer = (FloatingActionButton) rootView.findViewById(R.id.add_game);
        addPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int viewXY[] = new int[2];
                v.getLocationOnScreen(viewXY);

                if (mListener != null) {
                    mListener.onFragmentInteraction("add_game", viewXY[0], viewXY[1]);
                }
            }
        });

        mProgress = (LinearLayout) rootView.findViewById(R.id.progressContainer);
        mText = (TextView) rootView.findViewById(R.id.LoadingText);

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

        //mAdapter = new CustomAdapter(mDataset, mDataset_Thumb);
        mAdapter = new GameAdapter(this, mActivity,mSearchQuery);
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);

        pullToRefreshView = (SwipeRefreshLayout) rootView.findViewById(R.id.pull_to_refresh_listview);
        pullToRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                // TODO Auto-generated method stub
                initDataset();
            }
        });

        mSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                mSearchQuery = cs.toString();
                //initDataset();
                mAdapter = new GameAdapter(GamesFragment.this, mActivity,mSearchQuery);
                // Set CustomAdapter as the adapter for RecyclerView.
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }
        });



        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mSearch.getText().toString().equals("")) {
                    mSearchQuery = "";
                    mSearch.setText(mSearchQuery);
                    //mActivity.onBackPressed();
                }

                InputMethodManager inputManager = (InputMethodManager)
                        mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                mSearch.clearFocus();
                mRecyclerView.requestFocus();

                initDataset();
            }
        });

        if (Game.findBaseGames("").size() == 0){
            initDataset();
        }else {
            mText.setVisibility(View.GONE);
            mProgress.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }

        // END_INCLUDE(initializeRecyclerView)
        return rootView;
    }

    public void clearQuery(){
        mSearchQuery = "";
        mSearch.setText(mSearchQuery);
    }

    public String getQuery(){
        return mSearchQuery;
    }

    Activity mActivity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        ((MainActivity) mActivity).onSectionAttached(1);
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
        try{
            InputMethodManager inputManager = (InputMethodManager)
                    mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception e){}
        if (mActivity != null) {
            ((MainActivity) mActivity).getSupportActionBar().setDisplayShowCustomEnabled(false);
            mActivity = null;
        }
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

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
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        ((MainActivity)mActivity).unbindDrawables(mRecyclerView);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    GamesLoader myTask;
    private void initDataset() {
        myTask = new GamesLoader(mActivity);
        try {
            myTask.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void refreshDataset(boolean reInit){
        if (reInit) {
            initDataset();
        }
        mAdapter = new GameAdapter(this, mActivity,mSearchQuery);
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
    }

    protected void updateDataset(){
        //int current = ((GameAdapter)mRecyclerView.getAdapter()).mPosition;
        int firstVisible = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                .findFirstCompletelyVisibleItemPosition();
        refreshDataset(false);
        mRecyclerView.scrollToPosition(firstVisible);
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
        public void onFragmentInteraction(String id, float x, float y);
    }

    public class GamesLoader extends LoadGamesTask {
        public GamesLoader(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(final String result) {
            pullToRefreshView.setRefreshing(false);
            myTask = null;

            //mAdapter = new CustomAdapter(mDataset, mDataset_Thumb);
            mAdapter = new GameAdapter(GamesFragment.this, mActivity,mSearchQuery);
            // Set CustomAdapter as the adapter for RecyclerView.
            mRecyclerView.setAdapter(mAdapter);

            mText.setVisibility(View.GONE);
            mProgress.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}