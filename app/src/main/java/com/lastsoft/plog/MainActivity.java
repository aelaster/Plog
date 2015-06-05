package com.lastsoft.plog;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GameGroup;
import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerGameGroup;
import com.lastsoft.plog.db.PlayersPerPlay;
import com.lastsoft.plog.db.TenByTen;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        AddGroupFragment.OnFragmentInteractionListener,
        AddPlayFragment.OnFragmentInteractionListener,
        AddPlayerFragment.OnFragmentInteractionListener,
        AddGameFragment.OnFragmentInteractionListener,
        PlaysFragment.OnFragmentInteractionListener,
        PlayersFragment.OnFragmentInteractionListener,
        GamesFragment.OnFragmentInteractionListener,
        ViewPlayFragment.OnFragmentInteractionListener,
        StatsFragment.OnFragmentInteractionListener,
        View.OnClickListener {

    static final String EXTRA_CURRENT_ITEM_POSITION = "extra_current_item_position";
    static final String EXTRA_OLD_ITEM_POSITION = "extra_old_item_position";

    private Boolean fragUp = false;
    private AddPlayerFragment mAddPlayerFragment;
    private AddGameFragment mAddGameFragment;
    private AddPlayFragment mAddPlayFragment;
    private AddGroupFragment mAddGroupFragment;
    public ViewPlayFragment mViewPlayFragment;
    PlaysFragment mPlaysFragment;
    PlayAdapter mPlayAdapter;
    protected PostMortemReportExceptionHandler mDamageReport = new PostMortemReportExceptionHandler(this);

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Bundle mTmpState;
    private boolean mIsReentering;

    private final SharedElementCallback mCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (mIsReentering) {
                int oldPosition = mTmpState.getInt(EXTRA_OLD_ITEM_POSITION);
                int currentPosition = mTmpState.getInt(EXTRA_CURRENT_ITEM_POSITION);

                if (currentPosition != oldPosition) {
                    // If currentPosition != oldPosition the user must have swiped to a different
                    // page in the DetailsActivity. We must update the shared element so that the
                    // correct one falls into place.
                    String newImageTransitionName = "imageTrans" + currentPosition;
                    String newNameTransitionName = "nameTrans" + currentPosition;
                    String newDateTransitionName = "dateTrans" + currentPosition;
                    View newImageSharedView = mPlaysFragment.mRecyclerView.findViewWithTag(newImageTransitionName);
                    //View newNameSharedView = mPlaysFragment.mRecyclerView.findViewWithTag(newNameTransitionName);
                    //View newDateSharedView = mPlaysFragment.mRecyclerView.findViewWithTag(newDateTransitionName);
                    if (newImageSharedView != null) {
                        names.clear();
                        names.add(newImageTransitionName);
                        //names.add(newNameTransitionName);
                        //names.add(newDateTransitionName);
                        sharedElements.clear();
                        sharedElements.put(newImageTransitionName, newImageSharedView);
                        //sharedElements.put(newNameTransitionName, newNameSharedView);
                        //sharedElements.put(newDateTransitionName, newDateSharedView);
                    }
                    //if (newNameSharedView == null ){ Log.d("V1", "newNameSharedView is null");}
                    //if (newDateSharedView == null ){ Log.d("V1", "newDateSharedView is null");}
                }
                mTmpState = null;
            }
            /*
            if (!mIsReentering) {
                View navigationBar = findViewById(android.R.id.navigationBarBackground);
                View statusBar = findViewById(android.R.id.statusBarBackground);
                int actionBarId = getResources().getIdentifier("action_bar_container", "id", "android");
                View actionBar = findViewById(actionBarId);

                if (navigationBar != null) {
                    names.add(navigationBar.getTransitionName());
                    sharedElements.put(navigationBar.getTransitionName(), navigationBar);
                }
                if (statusBar != null) {
                    names.add(statusBar.getTransitionName());
                    sharedElements.put(statusBar.getTransitionName(), statusBar);
                }
                if (actionBar != null) {
                    actionBar.setTransitionName("actionBar");
                    names.add(actionBar.getTransitionName());
                    sharedElements.put(actionBar.getTransitionName(), actionBar);
                }
            } else {
                names.remove(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);
                sharedElements.remove(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);
                names.remove(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME);
                sharedElements.remove(Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME);
                names.remove("actionBar");
                sharedElements.remove("actionBar");
            }
            */
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);

        setExitSharedElementCallback(mCallback);
        mDamageReport.initialize();

        if (!doesDatabaseExist(this, "SRX.db")) {
            setContentView(R.layout.activity_main0);
            mTitle = "Welcome!";
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new SetupWizardFragment(), "wizard")
                    .commit();
            restoreActionBar();
        }else{
            setContentView(R.layout.activity_main);

            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                    .memoryCacheSize(41943040)
                    .diskCacheSize(104857600)
                    .threadPoolSize(10)
                    .build();
            ImageLoader.getInstance().init(config);

            mNavigationDrawerFragment = (NavigationDrawerFragment)
                    getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
            mTitle = getTitle();


            // Set up the drawer.
            DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mNavigationDrawerFragment.setUp(
                    R.id.navigation_drawer,
                    mDrawerLayout);


            ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                    this, mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
            );

            /*LoadGamesTask initDb = new LoadGamesTask(this);
            try {
                initDb.execute();
            } catch (Exception e) {

            }*/
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDamageReport != null) {
            mDamageReport.restoreOriginalHandler();
            mDamageReport = null;
        }
    }

    private static boolean doesDatabaseExist(ContextWrapper context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

    private String mSearchQuery;
    public PlayAdapter initPlayAdapter(String searchQuery){
        mSearchQuery = searchQuery;
        mPlayAdapter = new PlayAdapter(this, mPlaysFragment, searchQuery);
        return mPlayAdapter;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        //Log.d("V1", ""+position);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0){
            //int backStackCount = manager.getBackStackEntryCount();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            //fragmentManager.popBackStack(fragmentManager.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragUp = false;
        }
        if (position == 1){
            mPlaysFragment = new PlaysFragment();
            mPlayAdapter = initPlayAdapter("");
            //initPlayAdapter();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mPlaysFragment, "plays")
                    .commit();
        }else if (position == 2){
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new PlayersFragment(), "players")
                    .commit();
        }else if (position == 0){
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new GamesFragment(), "games")
                    .commit();
        }else if (position == 3){
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new StatsFragment(), "stats")
                    .commit();
        }else if (position == 4){
            /*fragmentManager.beginTransaction()
                    .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                    .commit();*/
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new SetupWizardFragment(), "wizard")
                    .commit();
        }else{
            super.onBackPressed();
            //android.os.Process.killProcess(android.os.Process.myPid());
        }
    }





    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section3);
                break;
            case 2:
                mTitle = getString(R.string.title_section1);
                break;
            case 3:
                mTitle = getString(R.string.title_section2);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
            case 6:
                restoreActionBar();
                break;
            case 7:
                mTitle = getString(R.string.title_section7);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (mTitle.equals(getString(R.string.title_section4))) {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            } else {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            }
            actionBar.setDisplayShowTitleEnabled(true);
            /*if(mTitle.equals(getString(R.string.title_section3)) || mTitle.equals(getString(R.string.title_section1))) {
                //actionBar.setDisplayShowCustomEnabled(true);
            }else{
                actionBar.setDisplayShowCustomEnabled(false);
            }*/
            //actionBar.setTitle(mTitle);
        }
    }

    public int currentFragmentCode = 4;
    public void setUpActionBar(int fragmentCode){
        currentFragmentCode = fragmentCode;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (fragmentCode == 0) {
                //addgame
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                onSectionAttached(1);
            } else if (fragmentCode == 1) {
                //addgroup
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.groups));
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            } else if (fragmentCode == 2) {
                //addplayer
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                onSectionAttached(3);
            } else if (fragmentCode == 3) {
                //addplay
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                actionBar.setTitle(mTitle);
            } else if (fragmentCode == 4) {
                //games
                actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
                onSectionAttached(1);
                actionBar.setTitle(mTitle);
            } else if (fragmentCode == 5) {
                //players
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                onSectionAttached(3);
                actionBar.setTitle(mTitle);
            } else if (fragmentCode == 6) {
                //plays
                actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                onSectionAttached(2);
                actionBar.setTitle(mTitle);
            } else if (fragmentCode == 7) {
                //stats
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                onSectionAttached(4);
                actionBar.setTitle(mTitle);
            } else if (fragmentCode == 8) {
                //viewplay
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                onSectionAttached(2);
            } else if (fragmentCode == 9) {
                //set up
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                onSectionAttached(5);
                actionBar.setTitle("Set Up");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mNavigationDrawerFragment != null && !mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.

            //restoreActionBar();
            setUpActionBar(currentFragmentCode);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String id) {
        if (id.contains("refresh_players")){
            PlayersFragment playersFrag = (PlayersFragment)
                    getSupportFragmentManager().findFragmentByTag("players");
            if (playersFrag != null) {
                playersFrag.refreshDataset();
            }
        }else if (id.contains("refresh_games")){
            GamesFragment collectionFrag = (GamesFragment)
                    getSupportFragmentManager().findFragmentByTag("games");
            if (collectionFrag != null) {
                collectionFrag.refreshDataset(false);
            }
        }else if (id.contains("update_games")){
            GamesFragment collectionFrag = (GamesFragment)
                    getSupportFragmentManager().findFragmentByTag("games");
            if (collectionFrag != null) {
                collectionFrag.updateDataset();
            }
        }else if (id.contains("refresh_plays")){
            PlaysFragment playsFrag = (PlaysFragment)
                    getSupportFragmentManager().findFragmentByTag("plays");
            if (playsFrag != null) {
                playsFrag.refreshDataset();
            }

            ViewPlayFragment viewPlaysFrag = (ViewPlayFragment)
                    getSupportFragmentManager().findFragmentByTag("view_play");
            if (viewPlaysFrag != null) {
                viewPlaysFrag.refreshPager();
            }
        }
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(this, "Floating Action Clicked in " + mTitle, Toast.LENGTH_SHORT).show();
    }

    public void onListItemClicked(String id){
        Toast.makeText(this, id + " List Item Clicked", Toast.LENGTH_SHORT).show();
    }

    public void openAddPlayer(Fragment mFragment, long playerID){
        try{
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception ignored){}

        //mFragment.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_top));

        //mFragment.setSharedElementReturnTransition(null);
        //mFragment.setExitTransition(null);


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        fragUp = true;
        mAddPlayerFragment = AddPlayerFragment.newInstance( 0,  0, true, playerID);
        //mAddPlayFragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_bottom));
        //mAddPlayFragment.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_top));

        ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
        ft.replace(R.id.container, mAddPlayerFragment, "add_player");
        ft.addToBackStack("add_play");
        ft.commit();
        fragmentManager.executePendingTransactions(); //Prevents the flashing.
    }

    public void onPlayClicked(Play clickedPlay, Fragment mFragment, final View view, final View nameView, final View dateView, int position){
        try{
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception ignored){}

        //mFragment.setSharedElementReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform));
        //mFragment.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.move));

        /*FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        mViewPlayFragment = ViewPlayFragment.newInstance(clickedPlay.getId(),view.getTransitionName(), nameView.getTransitionName(), dateView.getTransitionName(), position);
        mViewPlayFragment.setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform));
        mViewPlayFragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_bottom));



        //Log.d("V1", "nameView.getTransitionName() = " + nameView.getTransitionName());

        ft.addSharedElement(view, view.getTransitionName());
        ft.addSharedElement(nameView, nameView.getTransitionName());
        ft.addSharedElement(dateView, dateView.getTransitionName());
        ft.replace(R.id.container, mViewPlayFragment, "view_play");
        ft.addToBackStack(null);
        ft.commit();
        fragmentManager.executePendingTransactions(); //Prevents the flashing.*/
        mIsReentering = false;



        Intent intent = new Intent(MainActivity.this, ViewPlayActivity.class);
        intent.putExtra("searchQuery", mSearchQuery);
        intent.putExtra("playID", clickedPlay.getId());
        intent.putExtra("imageTransID", view.getTransitionName());
        intent.putExtra("nameTransID", nameView.getTransitionName());
        intent.putExtra("dateTransID", dateView.getTransitionName());
        intent.putExtra("adapterPosition", position);

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(view, view.getTransitionName()));
                //Pair.create(nameView,  nameView.getTransitionName()),
                //Pair.create(dateView,  dateView.getTransitionName()));

        startActivity(intent, options.toBundle());
    }

    @Override
    public void onActivityReenter(int requestCode, Intent data) {
        super.onActivityReenter(requestCode, data);
        mIsReentering = true;
        mTmpState = new Bundle(data.getExtras());
        int oldPosition = mTmpState.getInt(EXTRA_OLD_ITEM_POSITION);
        int currentPosition = mTmpState.getInt(EXTRA_CURRENT_ITEM_POSITION);
        if (oldPosition != currentPosition) {
            mPlaysFragment.mRecyclerView.scrollToPosition(currentPosition);
        }
        postponeEnterTransition();
        mPlaysFragment.mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mPlaysFragment.mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                // TODO: hack! not sure why, but requesting a layout pass is necessary in order to fix re-mapping + scrolling glitches!
                mPlaysFragment.mRecyclerView.requestLayout();
                startPostponedEnterTransition();
                return true;
            }
        });
    }

    public void openAddPlay(Fragment mFragment, String game_name, long playID){

        mTitle = game_name;

        try{
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception ignored){}

        //mFragment.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_top));

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        fragUp = true;

        mAddPlayFragment = AddPlayFragment.newInstance( 0,  0, true, game_name, playID);
        //mAddPlayFragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_bottom));
        //mAddPlayFragment.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_top));
        ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
        ft.add(R.id.container, mAddPlayFragment, "add_play");
        ft.addToBackStack("add_play");
        ft.commit();




        /*Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //System.gc();
                //Log.d("V1", "background status allocated=" + Long.toString(Debug.getNativeHeapAllocatedSize()));
                //Log.d("V1", "background status free=" + Long.toString(Debug.getNativeHeapFreeSize()));
                GamesFragment collectionFrag = (GamesFragment)
                        getSupportFragmentManager().findFragmentByTag("games");
                if (collectionFrag != null && !collectionFrag.getQuery().equals("")) {
                    collectionFrag.clearQuery();
                    collectionFrag.refreshDataset(false);
                }
            }
        }, 1000);*/

    }

    public void addToTenXTen(long gameId){
        //prsent dialog of all available groups
        //just like expansions
        //this is also a way to remove them from the table
        TenByTenDialogFragment newFragment = new TenByTenDialogFragment().newInstance(gameId);
        newFragment.show(getSupportFragmentManager(), "tenByTenPicker");
        //onBackPressed();
    }

    public void deleteGame(long gameId){
        Game deleteMe = Game.findById(Game.class, gameId);

        //Log.d("V1", "has game been played? = " + GamesPerPlay.hasGameBeenPlayed(deleteMe));
        //check if this game has been played
        //if so, can't delete
        if (!GamesPerPlay.hasGameBeenPlayed(deleteMe)){
            deleteMe.delete();
        }

        onFragmentInteraction("refresh_games");
        //onBackPressed();
    }

    GameUpdater gameUpdate;
    public void updateGameViaBGG(String gameName){
        gameUpdate = new GameUpdater(this);
        try {
            gameUpdate.execute(gameName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deletePlayer(long playerID){
        Player deleteMe = Player.findById(Player.class, playerID);

        //delete PlayersPerPlay
        List<PlayersPerPlay> players = PlayersPerPlay.getPlayer(deleteMe);
        for(PlayersPerPlay player:players){
            player.delete();
        }
        //delete PlayersPerGameGroup
        List<PlayersPerGameGroup> groupers = PlayersPerGameGroup.getPlayer(deleteMe);
        for(PlayersPerGameGroup grouper:groupers){
            grouper.delete();
        }

        //delete player
        deleteMe.delete();
        onFragmentInteraction("refresh_players");
        onBackPressed();
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

        //delete play image
        Log.d("V1", "play image = " + deleteMe.playPhoto);
        if(deleteMe.playPhoto != null && deleteMe.playPhoto.equals("")) {
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

        onFragmentInteraction("refresh_plays");
    }

    @Override
    public void onFragmentInteraction(String id, float x, float y) {
        //Log.d("V1", "x = " + x);
        //Log.d("V1", "y = " + y);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (id.equals("add_player")) {
            fragUp = true;
            mAddPlayerFragment = AddPlayerFragment.newInstance((int) x, (int) y, true, -1);
            ft.add(R.id.container, mAddPlayerFragment, id);
            ft.addToBackStack(id);
            ft.commit();
            //mTitle = getString(R.string.add_player);
            //restoreActionBar();
        }else if (id.equals("add_play")) {
            //no longer used
            /*fragUp = true;
            mAddPlayFragment = AddPlayFragment.newInstance((int) x, (int) y, true, "", -1);
            ft.add(R.id.container, mAddPlayFragment, id);
            ft.addToBackStack(id);
            ft.commit();
            //mTitle = getString(R.string.add_player);
            //restoreActionBar();*/
        }else if (id.equals("add_group")) {
            fragUp = true;
            mAddGroupFragment = AddGroupFragment.newInstance((int) x, (int) y, true);
            ft.add(R.id.container, mAddGroupFragment, id);
            ft.addToBackStack(id);
            ft.commit();
            //mTitle = getString(R.string.add_player);
            //restoreActionBar();
        }else if (id.equals("add_game")) {
            fragUp = true;
            mAddGameFragment = AddGameFragment.newInstance((int) x, (int) y, true);
            ft.add(R.id.container, mAddGameFragment, id);
            ft.addToBackStack(id);
            ft.commit();
            //mTitle = getString(R.string.add_player);
            //restoreActionBar();
        }
        //Log.d("V1", id);
    }

    public void scrollPlays(int position){
        if (mPlaysFragment != null){
            mPlaysFragment.mRecyclerView.scrollToPosition(position);
            mPlaysFragment.mLayoutManager.scrollToPosition(position);
        }else{
            Log.d("V1", "plays fragment is null");
        }
    }

    @Override
    public void onBackPressed(){
        if (mNavigationDrawerFragment != null && !mNavigationDrawerFragment.isDrawerOpen()) {
            if (fragUp) {
                if (mAddPlayerFragment != null) removeFragment(mAddPlayerFragment.getView());
                if (mAddGameFragment != null) removeFragment(mAddGameFragment.getView());
                if (mAddPlayFragment != null) removeFragment(mAddPlayFragment.getView());
                if (mAddGroupFragment != null) removeFragment(mAddGroupFragment.getView());
            } else {
                //super.onBackPressed();
                if (mViewPlayFragment != null){
                    if (mViewPlayFragment.isSwiping == ViewPager.SCROLL_STATE_IDLE) {
                        //mViewPlayFragment.setSharedElementReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform));
                        //int holder = mViewPlayFragment.mPager.getCurrentItem();
                        super.onBackPressed();
                        mViewPlayFragment = null;
                    }else{
                        mViewPlayFragment.pendingBack = true;
                    }
                    //mPlaysFragment.mRecyclerView.scrollToPosition(holder);
                }else {
                    PlayersFragment playersFrag = (PlayersFragment)
                            getSupportFragmentManager().findFragmentByTag("players");
                    if (playersFrag != null){
                        if (playersFrag.fabMenu.isExpanded()){
                            playersFrag.fabMenu.collapse();
                        }else{
                            mNavigationDrawerFragment.openDrawer();
                        }
                    }else {
                        mNavigationDrawerFragment.openDrawer();
                    }
                }
            }
        }else{
            if (mNavigationDrawerFragment != null) {
                mNavigationDrawerFragment.closeDrawer();
            }else{
                super.onBackPressed();
            }
        }
    }

    /*
    Called by the back button in fragment_main.xml
     */
    public void removeFragment(View v){
        fragUp = false;
        if (mAddPlayerFragment != null){
            mAddPlayerFragment.removeYourself();
            mAddPlayerFragment = null;
        }
        if (mAddGameFragment != null){
            mAddGameFragment.removeYourself();
            mAddGameFragment = null;
        }
        if (mAddPlayFragment != null){
            mAddPlayFragment.removeYourself();
            mAddPlayFragment = null;
        }
        if (mAddGroupFragment != null){
            mAddGroupFragment.removeYourself();
            mAddGroupFragment = null;
        }
    }

    protected void unbindDrawables(View view) {
        //Log.d("V1", "unbinding drawables");
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                //Log.d("V1", "unbinding drawables " + i);
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            try{
                ((ViewGroup) view).removeAllViews();
                //Log.d("V1", "unbinding drawables x");
            } catch (Exception e){
                //Log.d("V1", "exception");
            }
        }
        System.gc();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public class GameUpdater extends UpdateBGGTask {
        public GameUpdater(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);
            onFragmentInteraction("update_games");
        }
    }

    public class TenByTenDialogFragment extends DialogFragment {

        ArrayList<GameGroup> addedGroups;

        public TenByTenDialogFragment newInstance(long gameId) {
            TenByTenDialogFragment frag = new TenByTenDialogFragment();
            Bundle args = new Bundle();
            args.putLong("gameId", gameId);

            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            addedGroups = new ArrayList<>();
            List<String> gameGroupNames = new ArrayList<>();

            final long gameId = getArguments().getLong("gameId");
            final Game theGame = Game.findById(Game.class, gameId);
            final List<GameGroup> gameGroups = GameGroup.listAll(GameGroup.class);

            boolean checkedItems[] = new boolean[gameGroups.size()];
            int i = 0;
            for(GameGroup group:gameGroups){
                gameGroupNames.add(group.groupName);
                if (TenByTen.isGroupAdded(group, theGame)){
                    checkedItems[i] = true;
                }
                i++;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            // Set the dialog title
            builder.setTitle(R.string.choose_expansions)
                    // Specify the list array, the items to be selected by default (null for none),
                    // and the listener through which to receive callbacks when items are selected
                    .setMultiChoiceItems(gameGroupNames.toArray(new CharSequence[gameGroupNames.size()]), checkedItems,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which,
                                                    boolean isChecked) {
                                    //checkedItems[which] = isChecked;
                                    GameGroup checked = gameGroups.get(which);
                                    if (isChecked) {
                                        addedGroups.add(checked);
                                    } else {
                                        addedGroups.remove(checked);
                                    }
                                    //Log.d("V1", checked.gameName);
                                    //Log.d("V1", "isChecked="+isChecked);
                                }
                            })
                            // Set the action buttons
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            TenByTen.deleteTenByTen(gameId);
                            Calendar calendar = Calendar.getInstance();
                            int year = calendar.get(Calendar.YEAR);
                            for (int i = 0; i < addedGroups.size(); i++) {
                                TenByTen addMe = new TenByTen(theGame, addedGroups.get(i), year);
                                addMe.save();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            return builder.create();
        }
    }

}
