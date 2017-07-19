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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
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
import android.widget.TextView;

import com.lastsoft.plog.adapter.GameAdapter;
import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.util.LoadGamesTask;
import com.lastsoft.plog.util.MyRecyclerScroll;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

//import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class GamesFragment extends Fragment{

    private static final String TAG = "GamesFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private static final int DATASET_COUNT = 60;
    private float x,y;
    public boolean fromDrawer;
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
    private int currentYear = 0;
    private int playListType_Holder = 0;
    private int sortType = 0;
    FloatingActionButton addPlayer;
    private String fragmentName = "";
    int fabMargin;
    //VerticalRecyclerViewFastScroller fastScroller;


    public static GamesFragment newInstance(boolean fromDrawer, String searchQuery, int playListType, String fragmentName, int currentYear) {
        GamesFragment fragment = new GamesFragment();
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
        final View rootView = inflater.inflate(R.layout.fragment_games, container, false);

        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinatorLayout);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setBackgroundColor(getResources().getColor(R.color.cardview_initial_background));
        pullToRefreshView = (SwipeRefreshLayout) rootView.findViewById(R.id.pull_to_refresh_listview);
        pullToRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                initDataset(true);
            }
        });



        RecyclerFastScroller fastScroller = (RecyclerFastScroller) rootView.findViewById(R.id.fastscroller);
        fastScroller.attachRecyclerView(mRecyclerView);
        //fastScroller = (VerticalRecyclerViewFastScroller) rootView.findViewById(R.id.fastscroller);

        // Connect the recycler to the scroller (to let the scroller scroll the list)
        //fastScroller.setRecyclerView(mRecyclerView, pullToRefreshView);

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        //mRecyclerView.setOnScrollListener(fastScroller.getOnScrollListener());

        addPlayer = (FloatingActionButton) rootView.findViewById(R.id.add_game);
        if (fromDrawer && playListType != 2) {
            //fastScroller.setRecyclerView(mRecyclerView, pullToRefreshView);
            mRecyclerView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    boolean enable = false;
                    boolean firstItemVisiblePull = recyclerView.getChildPosition(recyclerView.getChildAt(0)) == 0;
                    boolean topOfFirstItemVisiblePull = recyclerView.getChildAt(0).getTop() == recyclerView.getChildAt(0).getTop();;
                    enable = firstItemVisiblePull && topOfFirstItemVisiblePull;
                    pullToRefreshView.setEnabled(enable);
                }
            });
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
            if (!fromDrawer) {
                RelativeLayout gamesLayout = (RelativeLayout) rootView.findViewById(R.id.gamesLayout);
                final SwipeDismissBehavior<LinearLayout> behavior = new SwipeDismissBehavior();
                behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);
                behavior.setStartAlphaSwipeDistance(1.0f);
                behavior.setSensitivity(0.15f);
                behavior.setListener(new SwipeDismissBehavior.OnDismissListener() {
                    @Override
                    public void onDismiss(final View view) {
                        GamesFragment myFragC1 = (GamesFragment) getFragmentManager().findFragmentByTag("games");
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

                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) gamesLayout.getLayoutParams();
                params.setBehavior(behavior);

            }
            //fastScroller.setRecyclerView(mRecyclerView, null);
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
        mAdapter = new GameAdapter(this, mActivity,mSearchQuery, fromDrawer, playListType, sortType, fragmentName, currentYear);
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);

        if (mSearch != null) {
            mSearch.setHint(getString(R.string.filter) + mAdapter.getItemCount() + getString(R.string.filter_games));
        }

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
                    mAdapter = new GameAdapter(GamesFragment.this, mActivity, mSearchQuery, fromDrawer, playListType, sortType, fragmentName, currentYear);
                    // Set CustomAdapter as the adapter for RecyclerView.
                    mRecyclerView.setAdapter(mAdapter);

                    if (mSearch != null) {
                        mSearch.setHint(getString(R.string.filter) + mAdapter.getItemCount() + getString(R.string.filter_games));
                    }
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

                    //fastScroller.scrollHider();

                    InputMethodManager inputManager = (InputMethodManager)
                            mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    mSearch.clearFocus();
                    mRecyclerView.requestFocus();

                    initDataset(false);

                    if (mSearch != null) {
                        mSearch.setHint(getString(R.string.filter) + mAdapter.getItemCount() + getString(R.string.filter_games));
                    }
                }
            });
        }

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        if (Game.findBaseGames("", sortType, year).size() == 0){
            initDataset(false);
        }else {
            mText.setVisibility(View.GONE);
            mProgress.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }

        // END_INCLUDE(initializeRecyclerView)
        return rootView;
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

    private MenuItem menuItem0;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        if (playListType != 2  && !((MainActivity) mActivity).mNavigationDrawerFragment.isDrawerOpen() && ((MainActivity) mActivity).currentFragmentCode != 0) {
            inflater.inflate(R.menu.games, menu);
            menuItem0 = menu.getItem(0);
            if (showExpansions) {
                menuItem0.setTitle(getString(R.string.hide_expansions));
                menuItem0.setIcon(R.drawable.ic_visibility_off);
            } else {
                menuItem0.setTitle(getString(R.string.show_expansions));
                menuItem0.setIcon(R.drawable.ic_visibility);
            }
        }else if (playListType == 2  && !((MainActivity) mActivity).mNavigationDrawerFragment.isDrawerOpen() && ((MainActivity) mActivity).currentFragmentCode != 0) {
            inflater.inflate(R.menu.bucketlist, menu);
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
            case R.id.random_game:
                //select random bucket list game
                SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);

                List<Game> theGames = mAdapter.getGames();
                int min = 0;
                int max = theGames.size();
                Random r = new Random();
                int randomGame;

                if (!app_preferences.getBoolean("bucket_list_weight", false)) {
                    randomGame = r.nextInt(max - min) + min;
                }else{
                    //build weights
                    int[] weightHash = new int[max];
                    int weightedMax = 0;
                    for (int i = 1; i <= max; i++) {
                        weightedMax = weightedMax + i;
                        weightHash[i - 1] = weightedMax;
                    }

                    randomGame = r.nextInt(weightedMax - min) + min;

                    for (int j = max - 1; j >= 0; j--) {
                        if (randomGame < weightHash[j]) {
                            //it may fall in this range
                            //see if it's larger than the next one
                            //if this one is zero, we found it
                            if (j == 0) {
                                randomGame = j;
                                break;
                            } else if (randomGame > weightHash[j - 1]) {
                                randomGame = j;
                                break;
                            } else if (randomGame == weightHash[j - 1]) {
                                randomGame = j - 1;
                                break;
                            }
                        }
                    }
                }

                Snackbar
                        .make(mCoordinatorLayout,
                                getString(R.string.random_game_to_play) + theGames.get(randomGame).gameName,
                                Snackbar.LENGTH_LONG)
                    .show(); // Do not forget to show!
                return true;
            case R.id.sort:
                View menuItemView = mActivity.findViewById(R.id.sort);
                PopupMenu popup = new PopupMenu(mActivity, menuItemView);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.sort_az:
                                sortType = 0;
                                mAdapter.updateData(mAdapter.generateGameList(mSearchQuery, playListType, sortType, currentYear));
                                mRecyclerView.scrollToPosition(0);
                                if (mSearch != null) {
                                    mSearch.setHint(getString(R.string.filter) + mAdapter.getItemCount() + getString(R.string.filter_games));
                                }
                                return true;
                            case R.id.sort_za:
                                sortType = 1;
                                mAdapter.updateData(mAdapter.generateGameList(mSearchQuery, playListType, sortType, currentYear));
                                mRecyclerView.scrollToPosition(0);
                                return true;
                            case R.id.sort_plays_x0:
                                sortType = 2;
                                mAdapter.updateData(mAdapter.generateGameList(mSearchQuery, playListType, sortType, currentYear));
                                mRecyclerView.scrollToPosition(0);
                                return true;
                            case R.id.sort_plays_0x:
                                sortType = 3;
                                mAdapter.updateData(mAdapter.generateGameList(mSearchQuery, playListType, sortType, currentYear));
                                mRecyclerView.scrollToPosition(0);
                                return true;
                            case R.id.sort_last_played_newold:
                                sortType = 4;
                                mAdapter.updateData(mAdapter.generateGameList(mSearchQuery, playListType, sortType, currentYear));
                                mRecyclerView.scrollToPosition(0);
                                return true;
                            case R.id.sort_last_played_oldnew:
                                sortType = 5;
                                mAdapter.updateData(mAdapter.generateGameList(mSearchQuery, playListType, sortType, currentYear));
                                mRecyclerView.scrollToPosition(0);
                                return true;
                        }
                        return false;
                    }
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.games_sort, popup.getMenu());
                if (playListType == 4){
                    //remove sort by plays
                    popup.getMenu().getItem(2).setVisible(false);;
                    popup.getMenu().getItem(3).setVisible(false);;
                }
                popup.show();
                return true;
            case R.id.show_expansions:
                if (showExpansions) {
                    //currently showing expansions
                    //trying to hide them
                    //make it say show expansions
                    item.setTitle(getString(R.string.show_expansions));
                    item.setIcon(R.drawable.ic_visibility);
                    playListType = playListType_Holder;
                    mAdapter.updateData(mAdapter.generateGameList(mSearchQuery, playListType, sortType, currentYear));
                    if (mSearch != null) {
                        mSearch.setHint(getString(R.string.filter) + mAdapter.getItemCount() + getString(R.string.filter_games));
                    }
                    showExpansions = false;
                }else{
                    //currently hiding expansions
                    //trying to show them
                    //make it say hide expansions
                    item.setTitle(getString(R.string.hide_expansions));
                    item.setIcon(R.drawable.ic_visibility_off);
                    playListType_Holder = playListType;
                    if (playListType == 4) {
                        playListType = 5;
                    }else {
                        playListType = 3;
                    }
                    mAdapter.updateData(mAdapter.generateGameList(mSearchQuery, playListType, sortType, currentYear));
                    if (mSearch != null) {
                        mSearch.setHint(getString(R.string.filter) + mAdapter.getItemCount() + getString(R.string.filter_games));
                    }
                    showExpansions = true;
                }
                return true;
        }
        return false;
    }


    @Override
    public void onStart () {
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
            if (!fromDrawer){
                ((MainActivity) mActivity).setUpActionBar(7);
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


    GamesLoader myTask;
    private void initDataset(boolean notify) {
        myTask = new GamesLoader(mActivity, notify);
        try {
            myTask.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void refreshDataset(boolean reInit){
        if (reInit) {
            initDataset(true);
        }
        updateDataset();
    }

    protected void updateDataset(){
        if (mAdapter != null) {
            mAdapter.updateData(mAdapter.generateGameList(mSearchQuery, playListType, sortType, currentYear));
            if (mSearch != null) {
                mSearch.setHint(getString(R.string.filter) + mAdapter.getItemCount() + getString(R.string.filter_games));
            }
        }else{
            initDataset(false);
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String id, float x, float y);
    }

    public class GamesLoader extends LoadGamesTask {
        private boolean notify;
        public GamesLoader(Context context, boolean notify) {
            super(context);
            this.notify = notify;
        }

        @Override
        protected void onPostExecute(final String result) {
            pullToRefreshView.setRefreshing(false);
            if (result.equals("derp")) {
                Snackbar
                        .make(mCoordinatorLayout,
                                theContext.getString(R.string.no_default_player),
                                Snackbar.LENGTH_LONG)
                        .show(); // Do not forget to show!
            }else if (result.equals("true") && notify) {
                Snackbar
                        .make(mCoordinatorLayout,
                                theContext.getString(R.string.bgg_process_notice),
                                Snackbar.LENGTH_LONG)
                        .show(); // Do not forget to show!
            }else {
                myTask = null;

                mAdapter = new GameAdapter(GamesFragment.this, mActivity, mSearchQuery, fromDrawer, playListType, sortType, fragmentName, currentYear);
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
        String fileName = "PLAY_" + timeStamp + "_";
        String imageFileName = fileName;
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File plogDir = new File(storageDir, "/Plog/");
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                plogDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/Plog/" + image.getName();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == -1) {
            //captured
            theGame.gameBoxImage = mCurrentPhotoPath;
            theGame.save();
        }
    }
}