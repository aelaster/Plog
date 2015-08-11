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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
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
import android.widget.TextView;

import com.lastsoft.plog.adapter.GameAdapter;
import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.util.LoadGamesTask;
import com.lastsoft.plog.util.MyRecyclerScroll;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class GamesFragment extends Fragment{

    private static final String TAG = "GamesFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private static final int DATASET_COUNT = 60;
    private float x,y;
    private boolean fromDrawer;
    Uri photoUri;
    File photoFile;
    String mCurrentPhotoPath = "";
    boolean showExpansions = false;



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
    public CoordinatorLayout mCoordinatorLayout;
    private boolean releaseFocus = false;
    private int playListType = 0;
    private int playListType_Holder = 0;
    FloatingActionButton addPlayer;
    int fabMargin;


    public static GamesFragment newInstance(boolean fromDrawer, String searchQuery, int playListType) {
        GamesFragment fragment = new GamesFragment();
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
        if (fromDrawer && playListType != 2) {
            try {
                ActionBar actionBar = ((MainActivity) mActivity).getSupportActionBar();
                //actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setCustomView(R.layout.search_bar);
                mSearch = (EditText) actionBar.getCustomView()
                        .findViewById(R.id.etSearch);
                mCancel = (ImageView) actionBar.getCustomView()
                        .findViewById(R.id.closeButton);
            } catch (Exception e) {
            }
        }
        setHasOptionsMenu(true);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.games_view_frag, container, false);
        rootView.setBackgroundColor(getResources().getColor(R.color.cardview_initial_background));
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinatorLayout);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        pullToRefreshView = (SwipeRefreshLayout) rootView.findViewById(R.id.pull_to_refresh_listview);
        pullToRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                initDataset();
            }
        });

        VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller) rootView.findViewById(R.id.fastscroller);

        // Connect the recycler to the scroller (to let the scroller scroll the list)
        //fastScroller.setRecyclerView(mRecyclerView, pullToRefreshView);

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        mRecyclerView.setOnScrollListener(fastScroller.getOnScrollListener());

        addPlayer = (FloatingActionButton) rootView.findViewById(R.id.add_game);
        if (fromDrawer && playListType != 2) {
            fastScroller.setRecyclerView(mRecyclerView, pullToRefreshView);
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
        }else{
            fastScroller.setRecyclerView(mRecyclerView, null);
            pullToRefreshView.setEnabled(false);
            addPlayer.setVisibility(View.GONE);
        }

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
        mAdapter = new GameAdapter(this, mActivity,mSearchQuery, fromDrawer, playListType);
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);

        fabMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
        mRecyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void show() {
                addPlayer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            }

            @Override
            public void hide() {
                addPlayer.animate().translationY(addPlayer.getHeight() + fabMargin).setInterpolator(new AccelerateInterpolator(2)).start();
            }
        });

        if (mSearch != null) {
            mSearch.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                    // When user changed the Text
                    mSearchQuery = cs.toString();
                    //initDataset();
                    mAdapter = new GameAdapter(GamesFragment.this, mActivity, mSearchQuery, fromDrawer, playListType);
                    // Set CustomAdapter as the adapter for RecyclerView.
                    mRecyclerView.setAdapter(mAdapter);
                }

                @Override
                public void afterTextChanged(Editable editable) {

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
        }

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
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (fromDrawer && playListType != 2) {
            inflater.inflate(R.menu.games, menu);
            if (showExpansions) {
                menu.getItem(0).setTitle(getString(R.string.hide_expansions));
            }else{
                menu.getItem(0).setTitle(getString(R.string.show_expansions));
            }
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
            case R.id.show_expansions:
                if (showExpansions) {
                    //currently showing expansions
                    //trying to hide them
                    //make it say show expansions
                    item.setTitle(getString(R.string.show_expansions));
                    playListType = playListType_Holder;
                    mAdapter.updateData(mAdapter.generateGameList(mSearchQuery, playListType));
                    showExpansions = false;
                }else{
                    //currently hiding expansions
                    //trying to show them
                    //make it say hide expansions
                    item.setTitle(getString(R.string.hide_expansions));
                    playListType_Holder = playListType;
                    playListType = 3;
                    mAdapter.updateData(mAdapter.generateGameList(mSearchQuery, playListType));
                    showExpansions = true;
                }
                return true;
        }
        return false;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (fromDrawer) {
            if (mActivity != null) {
                if(playListType != 2) {
                    ((MainActivity) mActivity).setUpActionBar(4);
                }else{
                    ((MainActivity) mActivity).setUpActionBar(12);
                }
            }
        }else{
            if (mActivity != null) {
                ((MainActivity) mActivity).setUpActionBar(11);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
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
            //((MainActivity) mActivity).getSupportActionBar().setDisplayShowCustomEnabled(false);
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
        super.onDestroy();
        ((MainActivity)mActivity).unbindDrawables(mRecyclerView);
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
        //mAdapter = new GameAdapter(this, mActivity,mSearchQuery, fromDrawer, playListType);
        // Set CustomAdapter as the adapter for RecyclerView.
        //mRecyclerView.setAdapter(mAdapter);
        mAdapter.updateData(mAdapter.generateGameList(mSearchQuery, playListType));
    }

    protected void updateDataset(){
        //int current = ((GameAdapter)mRecyclerView.getAdapter()).mPosition;
        /*int firstVisible = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                .findFirstCompletelyVisibleItemPosition();
        refreshDataset(false);
        mRecyclerView.scrollToPosition(firstVisible);*/
        mAdapter.updateData(mAdapter.generateGameList(mSearchQuery, playListType));
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

    public class GamesLoader extends LoadGamesTask {
        public GamesLoader(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(final String result) {
            if (result.equals("true")) {
                //Toast.makeText(theContext, theContext.getString(R.string.bgg_process_notice), Toast.LENGTH_LONG).show();
                //go again
                //wait a few seconds before tryi
                initDataset();
            }else {
                pullToRefreshView.setRefreshing(false);
                myTask = null;

                //mAdapter = new CustomAdapter(mDataset, mDataset_Thumb);
                mAdapter = new GameAdapter(GamesFragment.this, mActivity, mSearchQuery, fromDrawer, playListType);
                // Set CustomAdapter as the adapter for RecyclerView.
                mRecyclerView.setAdapter(mAdapter);

                mText.setVisibility(View.GONE);
                mProgress.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    Game theGame;
    public void captureBox(Game game){
        theGame = game;
        try{
            InputMethodManager inputManager = (InputMethodManager)
                    mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception ignored){}

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(mActivity.getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoUri);
                startActivityForResult(takePictureIntent, 0);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PLAY_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file://" + image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(false)
                .considerExifParams(true)
                .build();
        if (requestCode == 0 && resultCode == -1) {
            //captured
            theGame.gameBoxImage = mCurrentPhotoPath;
            theGame.save();
        }
    }
}