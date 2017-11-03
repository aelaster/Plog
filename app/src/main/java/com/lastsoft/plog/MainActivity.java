package com.lastsoft.plog;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.TextView;

import com.lastsoft.plog.adapter.PlayAdapter;
import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GameGroup;
import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerGameGroup;
import com.lastsoft.plog.db.PlayersPerPlay;
import com.lastsoft.plog.db.PlaysPerGameGroup;
import com.lastsoft.plog.db.TenByTen;
import com.lastsoft.plog.dialogs.DeleteGameDialogFragment;
import com.lastsoft.plog.dialogs.DeleteGroupDialogFragment;
import com.lastsoft.plog.dialogs.DeletePlayDialogFragment;
import com.lastsoft.plog.dialogs.DeletePlayerDialogFragment;
import com.lastsoft.plog.dialogs.GameChooserDialogFragment;
import com.lastsoft.plog.dialogs.TenByTenDialogFragment;
import com.lastsoft.plog.util.AppUtils;
import com.lastsoft.plog.util.BGGLogInHelper;
import com.lastsoft.plog.util.DeletePlayTask;
import com.lastsoft.plog.util.NotificationFragment;
import com.lastsoft.plog.util.PostMortemReportExceptionHandler;
import com.lastsoft.plog.util.SearchBGGTask;
import com.lastsoft.plog.util.UpdateBGGTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        AddGroupFragment.OnFragmentInteractionListener,
        AddPlayFragment.OnFragmentInteractionListener,
        AddPlayerFragment.OnFragmentInteractionListener,
        AddGameFragment.OnFragmentInteractionListener,
        PlayersFragment.OnFragmentInteractionListener,
        GamesFragment.OnFragmentInteractionListener,
        StatsFragment.OnFragmentInteractionListener,
        BGGLogInHelper.LogInListener,
        DeleteGroupDialogFragment.OnDialogButtonClickListener,
        DeleteGameDialogFragment.OnDialogButtonClickListener,
        DeletePlayerDialogFragment.OnDialogButtonClickListener,
        DeletePlayDialogFragment.OnDialogButtonClickListener,
        GameChooserDialogFragment.OnDialogButtonClickListener
        {

    static final String EXTRA_CURRENT_ITEM_POSITION = "extra_current_item_position";
    static final String EXTRA_OLD_ITEM_POSITION = "extra_old_item_position";
    static final String CURRENT_FRAGMENT_CODE = "current_fragment_code";

    private Boolean fragUp = false;
    public Boolean usedFAB = false;
    private AddPlayerFragment mAddPlayerFragment;
    private AddGameFragment mAddGameFragment;
    private AddPlayFragment mAddPlayFragment;
    private AddGroupFragment mAddGroupFragment;
    public static final int CurrentYear = 0;
    PlaysFragment mPlaysFragment;
    GamesFragment mGamesFragment;
    StatsFragment mStatsFragment;
    PlayAdapter mPlayAdapter;
    protected PostMortemReportExceptionHandler mDamageReport = new PostMortemReportExceptionHandler(this);

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    public NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Bundle mTmpState;
    private boolean mIsReentering;
    public BGGLogInHelper mLogInHelper;
    public int currentFragmentCode = 4;

    private final SharedElementCallback mCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            try {
                if (mIsReentering) {
                    int oldPosition = mTmpState.getInt(EXTRA_OLD_ITEM_POSITION);
                    int currentPosition = mTmpState.getInt(EXTRA_CURRENT_ITEM_POSITION);

                    if (currentPosition != oldPosition) {
                        // If currentPosition != oldPosition the user must have swiped to a different
                        // page in the DetailsActivity. We must update the shared element so that the
                        // correct one falls into place.
                        String newImageTransitionName = "imageTrans" + currentPosition;
                        View newImageSharedView = mPlaysFragment.mRecyclerView.findViewWithTag(newImageTransitionName);
                        if (newImageSharedView != null) {
                            names.clear();
                            names.add(newImageTransitionName);;
                            sharedElements.clear();
                            sharedElements.put(newImageTransitionName, newImageSharedView);
                        }
                    }
                    mTmpState = null;
                }
            }catch (Exception ignored){
                //we don't care if you can't see the transition once
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);

        setExitSharedElementCallback(mCallback);
        mDamageReport.initialize();

        mLogInHelper = new BGGLogInHelper(this, this);

        BackupManager bm = new BackupManager(this);
        bm.dataChanged();

        if (savedInstanceState != null) {
            currentFragmentCode = savedInstanceState.getInt(CURRENT_FRAGMENT_CODE);
            mSearchQuery = savedInstanceState.getString("searchQuery");
        }


        if (!doesDatabaseExist(this, "SRX.db")) {
            setContentView(R.layout.activity_main0);
            mTitle = "Welcome!";
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new SetupWizardFragment(), "wizard")
                    .commitAllowingStateLoss();
            restoreActionBar();
        }else{
            setContentView(R.layout.activity_main);

            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
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
                    R.id.left_drawer,
                    mDrawerLayout);

            PackageManager pm = getPackageManager();
            PackageInfo pi;
            try {
                pi = pm.getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException nnfe) {
                //doubt this will ever run since we want info about our own package
                pi = new PackageInfo();
                pi.versionName = "unknown";
                pi.versionCode = 699999;
            }

            TextView versionNumber = (TextView) findViewById(R.id.version_label);
            versionNumber.setText(getString(R.string.version_label) + pi.versionName);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_FRAGMENT_CODE, currentFragmentCode);
        outState.putString("searchQuery", mSearchQuery);
    }

    @Override
    protected void onResume() {
        onFragmentInteraction("refresh_games");
        onFragmentInteraction("refresh_plays");
        logIntoBGG();
        super.onResume();
    }

    public void logIntoBGG(){
        logOutOfBGG();
        mLogInHelper.logIn();
    }

    public void logOutOfBGG() {
        mLogInHelper.logOut();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
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
    public PlayAdapter initPlayAdapter(String searchQuery, boolean fromDrawer, int playListType, int currentYear){
        mSearchQuery = searchQuery;
        return new PlayAdapter(this, mPlaysFragment, searchQuery, fromDrawer, playListType, 0, currentYear);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        forceBack = false;
        usedFAB = false;
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragUp = false;
            }

            String positionName = mNavigationDrawerFragment.getPositionHeading(position);
            if (positionName.equals(getString(R.string.title_plays))) {
                openPlays("", true, 0, getString(R.string.title_plays), CurrentYear);
            } else if (positionName.equals(getString(R.string.title_players))) {
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new PlayersFragment(), "players")
                        .commitAllowingStateLoss();
            } else if (positionName.equals(getString(R.string.title_games))) {
                openGames("", true, 0, getString(R.string.title_games), CurrentYear);
            } else if (positionName.equals(getString(R.string.title_bucket_list))) {
                openGames("BucketList", true, 2, getString(R.string.title_bucket_list), CurrentYear);
            } else if (positionName.equals(getString(R.string.title_statistics))) {
                openStats();
            } else if (positionName.equals(getString(R.string.title_export_db))) {
                exportDB();
            } else if (positionName.equals(getString(R.string.title_import_db))) {
                importDB();
            } else if (positionName.equals(getString(R.string.title_settings))) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, 1);
            } else {
                super.onBackPressed();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1) {
            //refresh games
            GamesFragment collectionFrag = (GamesFragment)
                    getSupportFragmentManager().findFragmentByTag("games");
            if (collectionFrag != null) {
                collectionFrag.refreshDataset(true);
            }
        }
    }

    public void importDB(){
        try{
            File oldDb = getDatabasePath("SRX.db");
            File newDb = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SRX_export.db");
            if (newDb.exists()) {
                if(oldDb.exists()){

                }
                else{
                    //This'll create the directories you wanna write to, so you
                    //can put the DB in the right spot.
                    oldDb.getParentFile().mkdirs();
                }
                Log.d("V1", "importing database");
                FileInputStream src_input = new FileInputStream(newDb);
                FileOutputStream dst_input = new FileOutputStream(oldDb);
                FileChannel src = src_input.getChannel();
                FileChannel dst = dst_input.getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                src_input.close();
                dst_input.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportDB(){
        try{
            File currentDB = getDatabasePath("SRX.db");
            String backupDBPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SRX_export.db";
            File backupDB = new File(backupDBPath);

            if (currentDB.exists()) {
                FileInputStream src_input = new FileInputStream(currentDB);
                FileOutputStream dst_input = new FileOutputStream(backupDB);
                FileChannel src = src_input.getChannel();
                FileChannel dst = dst_input.getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                src_input.close();
                dst_input.close();
            }
            notifyUser(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openStats(){
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (mStatsFragment != null){
            mStatsFragment = null;
        }

        mStatsFragment = new StatsFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mStatsFragment, "stats")
                .commitAllowingStateLoss();
    }

    public void openGames(String searchQuery, boolean fromDrawer, int playListType, String fragmentName, int currentYear){
        invalidateOptionsMenu();
        if(!fromDrawer){
            mNavigationDrawerFragment.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mNavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(false);
        }
        forceBack = !fromDrawer;
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (mGamesFragment != null){
            mGamesFragment = null;
        }

        mGamesFragment = GamesFragment.newInstance(fromDrawer, searchQuery, playListType, fragmentName, currentYear);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (!fromDrawer){
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
            ft.addToBackStack("games");
        }
        if (!fromDrawer){
            ft.add(R.id.container, mGamesFragment, "games");
        }else {
            ft.replace(R.id.container, mGamesFragment, "games");
        }
        ft.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
    }

    public void openPlays(String searchQuery, boolean fromDrawer, int playListType, String fragmentName, int currentYear){
        invalidateOptionsMenu();
        if(!fromDrawer){
            mNavigationDrawerFragment.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mNavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(false);
        }
        forceBack = !fromDrawer;
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (mPlaysFragment != null){
            mPlaysFragment = null;
        }

        mPlaysFragment = PlaysFragment.newInstance(fromDrawer, searchQuery, playListType, fragmentName, currentYear);
        mPlayAdapter = initPlayAdapter(searchQuery, fromDrawer, playListType, currentYear);

        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (!fromDrawer){
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
            ft.addToBackStack("plays");
        }

        if (!fromDrawer) {
            ft.add(R.id.container, mPlaysFragment, "plays");
        }else {
            ft.replace(R.id.container, mPlaysFragment, "plays");
        }
        ft.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (mTitle.equals(getString(R.string.title_statistics))) {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            } else {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            }
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

    public void setTitle(String title){
        mTitle = title;
    }

    public void setUpActionBar(int fragmentCode){

        try{
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception ignored){}

        currentFragmentCode = fragmentCode;

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            if (fragmentCode == 0) {
                //addgame
                mNavigationDrawerFragment.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                mNavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(false);
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                mTitle = getString(R.string.title_games);
                actionBar.setTitle(mTitle);
            } else if (fragmentCode == 1) {
                //addgroup
                mNavigationDrawerFragment.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                mNavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(false);
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setTitle(getString(R.string.groups_header));
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            } else if (fragmentCode == 2) {
                //addplayer
                mNavigationDrawerFragment.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                mNavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(false);
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                mTitle = getString(R.string.title_players);
                actionBar.setTitle(mTitle);
            } else if (fragmentCode == 3) {
                //addplay
                mNavigationDrawerFragment.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                mNavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(false);
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                actionBar.setTitle(mTitle);
            } else if (fragmentCode == 4) {
                //games
                mNavigationDrawerFragment.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                mNavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(true);
                actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                mTitle = getString(R.string.title_games);
                actionBar.setTitle(mTitle);
            } else if (fragmentCode == 5) {
                //players
                mNavigationDrawerFragment.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                mNavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(true);
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                mTitle = getString(R.string.title_players);
                actionBar.setTitle(mTitle);
            } else if (fragmentCode == 6) {
                //plays
                initPlayAdapter(mSearchQuery, false, 0, CurrentYear);
                mNavigationDrawerFragment.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                mNavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(true);
                actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                mTitle = getString(R.string.title_plays);
                actionBar.setTitle(mTitle);
                PlaysFragment playsFrag = (PlaysFragment)
                    getSupportFragmentManager().findFragmentByTag("plays");
                if (playsFrag != null) {
                    mPlaysFragment = playsFrag;
                }
            } else if (fragmentCode == 7) {
                //stats
                mNavigationDrawerFragment.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                mNavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(true);
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                mTitle = getString(R.string.title_statistics);
                actionBar.setTitle(mTitle);
            } else if (fragmentCode == 8) {
                //viewplay
                mNavigationDrawerFragment.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                mNavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(true);
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                mTitle = getString(R.string.title_plays);
            } else if (fragmentCode == 9) {
                //set up
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                mTitle = getString(R.string.title_settings);
                actionBar.setTitle("Set Up");
            } else if (fragmentCode == 10) {
                //plays, filtered by games
                mNavigationDrawerFragment.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                mNavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(false);
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                mTitle = getString(R.string.title_plays);
                actionBar.setTitle(mTitle);
            } else if (fragmentCode == 11) {
                //plays, filtered by player
                mNavigationDrawerFragment.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                mNavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(false);
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                mTitle = getString(R.string.title_games);
                actionBar.setTitle(mTitle);
            } else if (fragmentCode == 12) {
                //bucket list
                mNavigationDrawerFragment.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                mNavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(true);
                actionBar.setDisplayShowCustomEnabled(false);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                mTitle = getString(R.string.title_bucket_list);
                actionBar.setTitle(mTitle);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mNavigationDrawerFragment != null && !mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.

            setUpActionBar(currentFragmentCode);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onFragmentInteraction(String id) {
        if (id.contains("refresh_players_add")){
            PlayersFragment playersFrag = (PlayersFragment)
                    getSupportFragmentManager().findFragmentByTag("players");
            if (playersFrag != null) {
                playersFrag.refreshDataset(true);
            }
        }else if (id.contains("refresh_players_drop")){
            PlayersFragment playersFrag = (PlayersFragment)
                    getSupportFragmentManager().findFragmentByTag("players");
            if (playersFrag != null) {
                playersFrag.refreshDataset(false);
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
        }
    }


    public void hidePlayerFAB(){
        PlayersFragment playersFrag = (PlayersFragment)
                getSupportFragmentManager().findFragmentByTag("players");
        if (playersFrag != null) {
            playersFrag.hideFAB();
        }
    }

    public void showPlayerFAB(){
        PlayersFragment playersFrag = (PlayersFragment)
                getSupportFragmentManager().findFragmentByTag("players");
        if (playersFrag != null) {
            playersFrag.showFAB();
        }
    }

    public void openAddPlayer(Fragment mFragment, long playerID){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        fragUp = true;
        mAddPlayerFragment = AddPlayerFragment.newInstance( 0,  0, true, playerID);

        ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
        ft.replace(R.id.container, mAddPlayerFragment, "add_player");
        ft.addToBackStack("add_play");
        ft.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions(); //Prevents the flashing.
    }

    public void openAddGroup(Fragment mFragment, long groupID){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        fragUp = true;
        mAddGroupFragment = AddGroupFragment.newInstance( 0,  0, true, groupID);

        ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
        ft.replace(R.id.container, mAddGroupFragment, "add_group");
        ft.addToBackStack("add_group");
        ft.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions(); //Prevents the flashing.
    }


    int firstVisible, lastVisible;
    public void onPlayClicked(Play clickedPlay, Fragment mFragment, final View view, final View nameView, final View dateView, int position, boolean fromDrawer, int playListType, int sortType, String theSearchQuery, int currentYear){
        mIsReentering = false;
        firstVisible = ((LinearLayoutManager) mPlaysFragment.mRecyclerView.getLayoutManager())
                .findFirstCompletelyVisibleItemPosition();
        lastVisible = ((LinearLayoutManager) mPlaysFragment.mRecyclerView.getLayoutManager())
                .findLastCompletelyVisibleItemPosition();

        Intent intent = new Intent(MainActivity.this, ViewPlayActivity.class);
        intent.putExtra("searchQuery", theSearchQuery);
        intent.putExtra("playID", clickedPlay.getId());
        intent.putExtra("imageTransID", view.getTransitionName());
        intent.putExtra("nameTransID", nameView.getTransitionName());
        intent.putExtra("dateTransID", dateView.getTransitionName());
        intent.putExtra("adapterPosition", position);
        intent.putExtra("fromDrawer", fromDrawer);
        intent.putExtra("playListType", playListType);
        intent.putExtra("sortType", sortType);
        intent.putExtra("currentYear", currentYear);

        View decor = getWindow().getDecorView();
        View statusBar = decor.findViewById(android.R.id.statusBarBackground);
        View navBar = decor.findViewById(android.R.id.navigationBarBackground);
        View actionBar = decor.findViewById(R.id.action_bar_container);
        actionBar.setTransitionName("actionBar");

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(view, view.getTransitionName())
                ,Pair.create(statusBar, statusBar.getTransitionName())
                ,Pair.create(navBar, navBar.getTransitionName())
        );
        startActivity(intent, options.toBundle());
    }


    @Override
    public void onActivityReenter(int requestCode, Intent data) {
        super.onActivityReenter(requestCode, data);
        mIsReentering = true;
        mTmpState = new Bundle(data.getExtras());
        int oldPosition = mTmpState.getInt(EXTRA_OLD_ITEM_POSITION);
        int currentPosition = mTmpState.getInt(EXTRA_CURRENT_ITEM_POSITION);
        if (mPlaysFragment != null) {
            mPlaysFragment.refreshDataset();
            if (oldPosition != currentPosition) {
                if (!(firstVisible <= currentPosition && currentPosition <= lastVisible)) {
                    mPlaysFragment.mRecyclerView.scrollToPosition(currentPosition);
                }
            }
            postponeEnterTransition();

            mPlaysFragment.mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mPlaysFragment.mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    mPlaysFragment.mRecyclerView.requestLayout();
                    startPostponedEnterTransition();
                    return true;
                }
            });
        }
    }

    public void openAddPlay(Fragment mFragment, String game_name, long playID, boolean copyPlay){

        mTitle = game_name;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mTitle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        fragUp = true;

        mAddPlayFragment = AddPlayFragment.newInstance( 0,  0, true, game_name, playID, copyPlay);
        ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
        ft.add(R.id.container, mAddPlayFragment, "add_play");
        ft.addToBackStack("add_play");
        ft.commitAllowingStateLoss();
    }

    public void addToTenXTen(long gameId){
        //prsent dialog of all available groups
        //just like expansions
        //this is also a way to remove them from the table
        TenByTenDialogFragment newFragment = new TenByTenDialogFragment().newInstance(gameId, -1);
        newFragment.show(getSupportFragmentManager(), "tenByTenPicker");
    }

    public void notifyUser(int notificationId){
        NotificationFragment newFragment = new NotificationFragment().newInstance(notificationId);
        newFragment.show(getSupportFragmentManager(), "notifyUser");
    }

    public void deleteGame(long gameId){
        DeleteGameDialogFragment newFragment = new DeleteGameDialogFragment().newInstance(gameId);
        newFragment.show(getSupportFragmentManager(), "deleteGame");
    }

    public void deleteGroup(long groupId){
        DeleteGroupDialogFragment newFragment = new DeleteGroupDialogFragment().newInstance(groupId);
        newFragment.show(getSupportFragmentManager(), "deleteGroup");
    }

    @Override
    public void onLogInSuccess() {

    }

    @Override
    public void onLogInError(String errorMessage) {
        if (errorMessage.equals("credentials")) {
            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = app_preferences.edit();
            editor.putLong("defaultPlayer", -1);
            editor.commit();
            logOutOfBGG();
            notifyUser(2);
        }
    }

    @Override
    public void onNeedCredentials() {

    }

    // START DELETE GROUP DIALOG FRAGMENT
    @Override
    public void onPositiveClick(long groupId) {
        RemoveGroupTask removeGroup = new RemoveGroupTask(MainActivity.this, GameGroup.findById(GameGroup.class, groupId));
        try {
            removeGroup.execute();
        } catch (Exception ignored) {

        }
    }

    @Override
    public void onNegativeClick() {
        if (mAddGroupFragment != null) mAddGroupFragment.enableDelete();
    }
    // END DELETE GROUP DIALOG FRAGMENT

    @Override
    public void onPositiveClick_DeleteGame(long gameId, boolean deleteBGGFlag) {
        Game deleteMe = Game.findById(Game.class, gameId);
        //check if this game has been played
        //if so, can't delete
        if (!GamesPerPlay.hasGameBeenPlayed(deleteMe)){
            deleteMe.delete();

            //delete from BGG
            if (deleteBGGFlag) {
                updateGameViaBGG(deleteMe.gameName, deleteMe.gameBGGID, deleteMe.gameBGGCollectionID, false, true);
            }
        }else{
            Snackbar
                    .make(mGamesFragment.mCoordinatorLayout,
                            getString(R.string.unable_remove_game),
                            Snackbar.LENGTH_LONG)
                    .show(); // Do not forget to show!
        }
        onFragmentInteraction("refresh_games");
    }

    @Override
    public void onPositiveClick_DeletePlayer(long playerId) {
        Player deleteMe = Player.findById(Player.class, playerId);

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
        onFragmentInteraction("refresh_players_drop");
        onBackPressed();
    }

    @Override
    public void onNegativeClick_DeletePlayer() {
        if (mAddPlayerFragment != null) mAddPlayerFragment.enableDelete();
    }

    @Override
    public void onPositiveClick_DeletePlay(long playId) {
        AppUtils.deletePlay(this, playId);
        onFragmentInteraction("refresh_plays");
    }

    @Override
    public void onItemSelected_GameChooser(String gameName, String bggID, String bggCollectionID, boolean addToCollection, boolean deleteFromBGG) {
        updateGameViaBGG(gameName, bggID, "", addToCollection, false);
    }

    @Override
    public void onNegativeClick_GameChooser(long gameId) {
        if (gameId >= 0) {
            Game updateMe = Game.findById(Game.class, gameId);
            updateMe.delete();
            onFragmentInteraction("update_games");
        }
    }


    public class RemoveGroupTask extends AsyncTask<Long, Void, Long[]> {

        Context theContext;
        GameGroup theGroup;

        private final ProgressDialog mydialog = new ProgressDialog(MainActivity.this);

        public RemoveGroupTask(Context context, GameGroup gameGroup) {
            this.theGroup = gameGroup;
            this.theContext = context;
        }

        // can use UI thread here
        @Override
        protected void onPreExecute() {
            mydialog.setMessage(getString(R.string.removeGroup) + theGroup.groupName);
            mydialog.setCancelable(false);
            try{
                mydialog.show();
            }catch (Exception ignored){}
        }

        // automatically done on worker thread (separate from UI thread)
        @Override
        protected Long[] doInBackground(final Long... args) {

            List<PlayersPerGameGroup> players =  PlayersPerGameGroup.getPlayers(theGroup);
            for (PlayersPerGameGroup player : players) {
                player.delete();
            }

            //delete the plays
            List<PlaysPerGameGroup> plays = PlaysPerGameGroup.getPlays(theGroup);
            for(PlaysPerGameGroup play:plays){
                play.delete();
            }

            theGroup.delete();
            return null;
        }

        @Override
        protected void onPostExecute ( final Long[] result){
            mydialog.dismiss();
            onFragmentInteraction("refresh_players_drop");
            onBackPressed();
        }
    }

    GameUpdater gameUpdate;
    public void updateGameViaBGG(String gameName, String bggID, String bggCollectionID, boolean addToCollection, boolean deleteFromBGG){
        gameUpdate = new GameUpdater(this, addToCollection, deleteFromBGG);
        try {
            gameUpdate.execute(bggID, gameName, bggCollectionID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    GameList gameList;
    public void searchGameViaBGG(String gameName, boolean addToCollection, boolean expansionFlag, long gameID){
        gameList = new GameList(this, addToCollection, expansionFlag, gameID);
        try {
            gameList.execute(gameName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deletePlayer(long playerID){
        DeletePlayerDialogFragment newFragment = new DeletePlayerDialogFragment().newInstance(playerID);
        newFragment.show(getSupportFragmentManager(), "deletePlayer");

    }

    public void deletePlay(long playID){
        DeletePlayDialogFragment newFragment = new DeletePlayDialogFragment().newInstance(playID);
        newFragment.show(getSupportFragmentManager(), "deletePlay");
    }



    @Override
    public void onFragmentInteraction(String id, float x, float y) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (id.equals("add_player")) {
            fragUp = true;
            mAddPlayerFragment = AddPlayerFragment.newInstance((int) x, (int) y, true, -1);
            ft.add(R.id.container, mAddPlayerFragment, id);
            ft.addToBackStack(id);
            ft.commitAllowingStateLoss();
        }else if (id.equals("add_group")) {
            fragUp = true;
            mAddGroupFragment = AddGroupFragment.newInstance((int) x, (int) y, true, -1);
            ft.add(R.id.container, mAddGroupFragment, id);
            ft.addToBackStack(id);
            ft.commitAllowingStateLoss();
        }else if (id.equals("add_game")) {
            fragUp = true;
            mAddGameFragment = AddGameFragment.newInstance((int) x, (int) y, true);
            ft.add(R.id.container, mAddGameFragment, id);
            ft.addToBackStack(id);
            ft.commitAllowingStateLoss();
        }
    }

    public void scrollPlays(int position){
        if (mPlaysFragment != null){
            mPlaysFragment.mRecyclerView.scrollToPosition(position);
            mPlaysFragment.mLayoutManager.scrollToPosition(position);
        }else{
            Log.d("V1", "plays fragment is null");
        }
    }

    public boolean forceBack = false;
    @Override
    public void onBackPressed(){
        onFragmentInteraction("refresh_games");
        onFragmentInteraction("refresh_plays");
        if (mNavigationDrawerFragment != null) {
            mNavigationDrawerFragment.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mNavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            if (mNavigationDrawerFragment != null && !mNavigationDrawerFragment.isDrawerOpen()) {
                if (fragUp) {
                    if (mAddPlayerFragment != null) removeFragment(mAddPlayerFragment.getView());
                    if (mAddGameFragment != null) removeFragment(mAddGameFragment.getView());
                    if (mAddPlayFragment != null) removeFragment(mAddPlayFragment.getView());
                    if (mAddGroupFragment != null) removeFragment(mAddGroupFragment.getView());
                } else {
                    if (forceBack) {
                        forceBack = false;
                        super.onBackPressed();
                    } else {
                        PlayersFragment playersFrag = (PlayersFragment)
                                getSupportFragmentManager().findFragmentByTag("players");
                        if (playersFrag != null) {
                            if (playersFrag.fabMenu.isExpanded()) {
                                playersFrag.fabMenu.collapse();
                            } else {
                                if (usedFAB){
                                    usedFAB = false;
                                    openPlays("", true, 0, getString(R.string.title_plays), CurrentYear);
                                }else {
                                    super.onBackPressed();
                                }
                            }
                        } else {
                            if (usedFAB){
                                usedFAB = false;
                                openPlays("", true, 0, getString(R.string.title_plays), CurrentYear);
                            }else {
                                super.onBackPressed();
                            }
                        }
                    }

                }
            } else {
                if (mNavigationDrawerFragment != null) {
                    mNavigationDrawerFragment.closeDrawer();
                } else {
                    super.onBackPressed();
                }
            }
        }else{
            super.onBackPressed();
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
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            try{
                ((ViewGroup) view).removeAllViews();
            } catch (Exception e){}
        }
        System.gc();
    }


    public class GameUpdater extends UpdateBGGTask {
        public GameUpdater(Context context, boolean addToCollection, boolean deleteFromCollection) {
            super(context, addToCollection, false, deleteFromCollection);
        }

        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);
            onFragmentInteraction("update_games");
        }
    }

    public class GameList extends SearchBGGTask {
        boolean addToCollection;
        boolean expansionFlag;
        long gameId;
        public GameList(Context context, boolean addToCollection, boolean expansionFlag, long gameId) {
            super(context, expansionFlag);
            this.addToCollection = addToCollection;
            this.expansionFlag = expansionFlag;
            this.gameId = gameId;
        }

        @Override
        protected void onPostExecute(final ArrayList<GameInfo> result) {
            super.onPostExecute(result);
            if (result.size() > 0) {
                if (result.size() == 1){
                    //this is the only game returned, so just go ahead and run update
                    updateGameViaBGG(result.get(0).gameName, result.get(0).gameBGGID, "", addToCollection, false);
                }else {
                    //more than one choice, so give the user a dialog and let them pick
                    ArrayList<String> theGames = new ArrayList<>();
                    ArrayList<String> theItems = new ArrayList<>();
                    ArrayList<String> theIDs = new ArrayList<>();
                    for (GameInfo aGame : result) {
                        theGames.add(aGame.gameName);
                        theItems.add(aGame.gameName + " (" + aGame.yearPublished + ")");
                        theIDs.add(aGame.gameBGGID);
                    }

                    GameChooserDialogFragment newFragment = new GameChooserDialogFragment().newInstance(theGames, theItems, theIDs, addToCollection, gameId);
                    newFragment.show(getSupportFragmentManager(), "gamePicker");
                }
            }else{
                Game deleteMe = Game.findById(Game.class, gameId);
                deleteMe.delete();
                onFragmentInteraction("refresh_games");
                Snackbar
                        .make(mGamesFragment.mCoordinatorLayout,
                                getString(R.string.unable_find_game),
                                Snackbar.LENGTH_LONG)
                        .show(); // Do not forget to show!
            }
        }
    }
}
