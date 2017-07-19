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

package com.lastsoft.plog.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lastsoft.plog.GamesFragment;
import com.lastsoft.plog.MainActivity;
import com.lastsoft.plog.R;
import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GameGroup;
import com.lastsoft.plog.db.GamesPerPlay;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.ViewHolder> {
    private static final String TAG = "GameAdapter";

    private List<Game> games;
    private DisplayImageOptions options;
    private Activity mActivity;
    private Fragment mFragment;
    private boolean fromDrawer;
    int mPosition;
    int playListType;
    int currentYear;
    private String gameGroup = null;
    private String fragmentName = "";
    private long mLastClickTime = 0;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView, bucketDateView, gamePlaysView, recentPlayView, nickDimeView;
        private final ImageView imageView;
        private final LinearLayout overflowLayout;
        private final LinearLayout clickLayout;
        private final View myView;

        public ViewHolder(View v) {
            super(v);

            textView = (TextView) v.findViewById(R.id.gameName);
            nickDimeView = (TextView) v.findViewById(R.id.alert_bubble);
            gamePlaysView = (TextView) v.findViewById(R.id.gamePlays);
            recentPlayView = (TextView) v.findViewById(R.id.recentPlay);
            bucketDateView = (TextView) v.findViewById(R.id.bucketDate);
            imageView = (ImageView) v.findViewById(R.id.imageView1);
            overflowLayout = (LinearLayout) v.findViewById(R.id.overflowLayout);
            clickLayout = (LinearLayout) v.findViewById(R.id.clickLayout);
            myView = v;
        }

        public ImageView getImageView() {
            return imageView;
        }
        public TextView getTextView() {
            return textView;
        }
        public TextView getNickDimeView() {
            return nickDimeView;
        }
        public TextView getBucketDateView() {
            return bucketDateView;
        }
        public TextView getRecentPlayView() {
            return recentPlayView;
        }
        public TextView getGamePlaysView() {
            return gamePlaysView;
        }
        public LinearLayout getOverflowLayout() {
            return overflowLayout;
        }
        public View getView() {
            return myView;
        }
        public LinearLayout getClickLayout() {
            return clickLayout;
        }
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

    public void updateData(List<Game> updatedGames){
        this.games = updatedGames;
        this.notifyDataSetChanged();
    }

    public List<Game> generateGameList(String mSearchQuery, int playListType, int sortType, int currentYear){
        /*
        SortType
        0 = Alpha AZ
        1 = Alpha ZA
        2 = Plays X0
        3 = Plays 0X
         */

        Calendar calendar = Calendar.getInstance();
        int year = currentYear;

        List<Game> games_out;
        switch (playListType) {
            case 0:
                games_out = Game.findBaseGames(mSearchQuery, sortType, year);
                break;
            case 1:
                if (mSearchQuery.equals("0")){
                    games_out = Game.getUniqueGames(sortType, year);
                }else {
                    gameGroup = mSearchQuery;
                    games_out = Game.getUniqueGames_GameGroup(GameGroup.findById(GameGroup.class, Long.parseLong(mSearchQuery)), sortType, year);
                }
                break;
            case 2:
                games_out = Game.getBucketList();
                break;
            case 3:
                if (mSearchQuery.equals("0") && !fromDrawer){
                    games_out = Game.findAllGames("", sortType, false);
                } else if (mSearchQuery.equals("")) {
                    games_out = Game.findAllGames("", sortType, true);
                }else if (fromDrawer) {
                    games_out = Game.findAllGames(mSearchQuery, sortType, true);
                }else{
                    long groupId =  Long.parseLong(mSearchQuery);
                    gameGroup = mSearchQuery;
                    games_out = Game.findAllGames_GameGroup(GameGroup.findById(GameGroup.class, groupId), sortType);
                }
                break;
            case 4:
                if (mSearchQuery.equals("0")){
                    games_out = Game.getUnplayedGames(sortType, false, year);
                }else {
                    gameGroup = mSearchQuery;
                    games_out = Game.getUnplayedGames_GameGroup(GameGroup.findById(GameGroup.class, Long.parseLong(mSearchQuery)), sortType, false, year);
                }
                break;
            case 5:
                if (mSearchQuery.equals("0")){
                    games_out = Game.getUnplayedGames(sortType, true, year);
                }else {
                    gameGroup = mSearchQuery;
                    games_out = Game.getUnplayedGames_GameGroup(GameGroup.findById(GameGroup.class, Long.parseLong(mSearchQuery)), sortType, true, year);
                }
                break;
            default:
                games_out = Game.findBaseGames(mSearchQuery, sortType, year);
                break;
        }

        return games_out;
    }

    public GameAdapter(Fragment theFragment, Activity theActivity, String mSearchQuery, boolean theFromDrawer, int mPlayListType, int mSortType, String mFragmentName, int mCurrentYear) {
        mFragment = theFragment;
        mActivity = theActivity;
        playListType = mPlayListType;
        fromDrawer = theFromDrawer;
        fragmentName = mFragmentName;
        currentYear = mCurrentYear;

        games = generateGameList(mSearchQuery, mPlayListType, mSortType, mCurrentYear);

        options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(false)
                .resetViewBeforeLoading(true)
                .build();
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.game_row_item, viewGroup, false);

        return new ViewHolder(v);
    }

    public List<Game> getGames(){
        return games;
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the contents of the view
        // with that element

        DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");

        if (games.get(position).tbtCount >= 5){
            viewHolder.getNickDimeView().setVisibility(View.VISIBLE);
            if (games.get(position).tbtCount >= 25){
                viewHolder.getNickDimeView().setText("25¢");
            }else if (games.get(position).tbtCount >= 10){
                viewHolder.getNickDimeView().setText("10¢");
            }else{
                viewHolder.getNickDimeView().setText("5¢");
            }
        }else{
            viewHolder.getNickDimeView().setVisibility(View.GONE);
        }
        viewHolder.getTextView().setText(games.get(position).gameName);
        if (games.get(position).gameThumb != null  && !games.get(position).gameThumb.equals("")) {
            if (games.get(position).gameThumb.contains("http")) {
                Picasso.with(mActivity).load(games.get(position).gameThumb).into(viewHolder.getImageView());
            }else {
                Picasso.with(mActivity).load("http:" + games.get(position).gameThumb).into(viewHolder.getImageView());
            }
        } else {
            viewHolder.getImageView().setImageDrawable(null);
        }

        if (games.get(position).recentPlay > 0) {
            Date theDate = new Date((long)games.get(position).recentPlay);
            long diff = new Date().getTime() - theDate.getTime();
            long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            String output_date2;
            if (days == 0){
                output_date2 = mActivity.getString( R.string.less_than_a_day_ago);
            }else if (days == 1){
                output_date2 = mActivity.getString( R.string.last_play_label) + days +  mActivity.getString(R.string.day_ago_label);
            }else if (days <= 6){
                output_date2 = mActivity.getString( R.string.last_play_label) + days + mActivity.getString( R.string.days_ago_label);
            }else {
                output_date2 = mActivity.getString( R.string.last_play_label) + outputFormatter.format(theDate); // Output : 01/20/2012
            }
            viewHolder.getRecentPlayView().setText(output_date2);
            viewHolder.getRecentPlayView().setVisibility(View.VISIBLE);
        }else{
            viewHolder.getRecentPlayView().setVisibility(View.INVISIBLE);
        }

        if (games.get(position).taggedToPlay > 0 && playListType == 2){
            Date theDate = new Date(((long)games.get(position).taggedToPlay)*1000L);
            long diff = new Date().getTime() - theDate.getTime();
            long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            String output_date;
            if (days == 0){
                output_date = mActivity.getString( R.string.added) + mActivity.getString(R.string.less_than_a_day_ago);
            }else if (days == 1){
                output_date = mActivity.getString( R.string.added) + days +  mActivity.getString(R.string.day_ago_label);
            }else if (days <= 6){
                output_date = mActivity.getString( R.string.added) + days + mActivity.getString( R.string.days_ago_label);
            }else {
                output_date = mActivity.getString( R.string.added) + outputFormatter.format(theDate); // Output : 01/20/2012
            }
            //String output_date = outputFormatter.format(theDate); // Output : 01/20/2012
            viewHolder.getBucketDateView().setText(output_date);
            viewHolder.getBucketDateView().setVisibility(View.VISIBLE);
        }else{
            viewHolder.getBucketDateView().setVisibility(View.GONE);
        }

        if (playListType != 1) {
            viewHolder.getOverflowLayout().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playPopup(view, position);
                }
            });
            if (games.get(position).expansionFlag == false) {
                viewHolder.getClickLayout().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();
                        ((MainActivity) mActivity).openAddPlay(mFragment, games.get(position).gameName, -1, false);
                    }
                });
            }else{
                viewHolder.getClickLayout().setOnClickListener(null);
            }
        }else{
            viewHolder.getOverflowLayout().setVisibility(View.GONE);
        }

        viewHolder.getGamePlaysView().setText(mActivity.getString(R.string.plays) + games.get(position).playCount);
        //}

    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return games.size();
    }

    public void playPopup(View v, final int position) {

        try{
            InputMethodManager inputManager = (InputMethodManager)
                    mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception ignored){}

        PopupMenu popup = new PopupMenu(mActivity, v);

        MenuInflater inflater = popup.getMenuInflater();

        if (games.get(position).expansionFlag == true){
            inflater.inflate(R.menu.game_expansion_overflow, popup.getMenu());
        }else {
            inflater.inflate(R.menu.game_overflow, popup.getMenu());
        }
        if(games.get(position).gameBGGID == null || games.get(position).gameBGGID.equals("")) {
            popup.getMenu().removeItem(R.id.update_bgg);
            popup.getMenu().removeItem(R.id.open_bgg);
            popup.getMenu().removeItem(R.id.add_bgg);
        }
        if (games.get(position).gameBoxImage == null || games.get(position).gameBoxImage.equals("")){
            popup.getMenu().removeItem(R.id.view_box_photo);
        }
        if (games.get(position).taggedToPlay <= 0){
            popup.getMenu().removeItem(R.id.remove_bucket_list);
        }else{
            popup.getMenu().removeItem(R.id.add_bucket_list);
        }

        SharedPreferences app_preferences;
        app_preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        long currentDefaultPlayer = app_preferences.getLong("defaultPlayer", -1);
        if (games.get(position).collectionFlag || currentDefaultPlayer == -1){
            popup.getMenu().removeItem(R.id.add_bgg);
        }

        //check if this game has been played
        //if so, can't delete
        if (GamesPerPlay.hasGameBeenPlayed(games.get(position))) {
            popup.getMenu().removeItem(R.id.delete_game);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
             @Override
             public boolean onMenuItemClick(MenuItem item) {
                 switch (item.getItemId()) {
                     case R.id.delete_game:
                         ((MainActivity) mActivity).deleteGame(games.get(position).getId());
                         return true;
                     case R.id.add_tenbyten:
                         ((MainActivity) mActivity).addToTenXTen(games.get(position).getId());
                         return true;
                     case R.id.view_plays:
                         if (games.get(position).expansionFlag == true) {
                             ((MainActivity) mActivity).openPlays(games.get(position).gameName, false, 9, fragmentName, currentYear);
                         }else{
                             ((MainActivity) mActivity).openPlays(games.get(position).gameName, false, 0, fragmentName, currentYear);
                         }
                         return true;
                     case R.id.open_bgg:
                         Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bgg.cc/boardgame/" + games.get(position).gameBGGID));
                         mActivity.startActivity(browserIntent);
                         return true;
                     case R.id.update_bgg:
                         mPosition = position;
                         if (games.get(position).expansionFlag == true) {
                             ((MainActivity) mActivity).searchGameViaBGG(games.get(position).gameName, false, true, -1);
                         }else{
                             ((MainActivity) mActivity).searchGameViaBGG(games.get(position).gameName, false, false, -1);
                         }
                         return true;
                     case R.id.add_bgg:
                         mPosition = position;
                         ((MainActivity) mActivity).updateGameViaBGG(games.get(position).gameName, games.get(position).gameBGGID, "", true, false);
                         return true;
                     case R.id.add_box_photo:
                         ((GamesFragment) mFragment).captureBox(games.get(position));
                         return true;
                     case R.id.view_box_photo:
                         String[] photoParts = games.get(position).gameBoxImage.split("/");
                         File newFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/Plog/",photoParts[photoParts.length-1]);
                         Uri contentUri = FileProvider.getUriForFile(mActivity.getApplicationContext(), "com.lastsoft.plog.fileprovider", newFile);
                         Intent intent = new Intent();
                         intent.setAction(Intent.ACTION_VIEW);
                         intent.setDataAndType(contentUri, "image/*");
                         intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                         mActivity.startActivity(intent);
                         return true;
                     case R.id.add_bucket_list:
                         String[] ids = TimeZone.getAvailableIDs(-5 * 60 * 60 * 1000);
                         // create a Eastern Standard Time time zone
                         SimpleTimeZone pdt = new SimpleTimeZone(-5 * 60 * 60 * 1000, ids[0]);

                         // set up rules for daylight savings time
                         pdt.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
                         pdt.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);

                         // create a GregorianCalendar with the Pacific Daylight time zone
                         // and the current date and time
                         Calendar calendar = new GregorianCalendar(pdt);
                         Date trialTime = new Date();
                         calendar.setTime(trialTime);
                         int i = (int) (calendar.getTime().getTime()/1000);
                         games.get(position).taggedToPlay = i;
                         games.get(position).save();

                         Snackbar
                                 .make(((GamesFragment) mFragment).mCoordinatorLayout,
                                         games.get(position).gameName + mActivity.getString(R.string.added_to_bl),
                                         Snackbar.LENGTH_LONG)
                                 .setAction(mActivity.getString(R.string.undo), new View.OnClickListener() {
                                     @Override
                                     public void onClick(View view) {
                                         games.get(position).taggedToPlay = 0;
                                         games.get(position).save();
                                         if (playListType == 2) {
                                             ((MainActivity) mActivity).onFragmentInteraction("refresh_games");
                                         }
                                     }
                                 })
                                 .show(); // Do not forget to show!

                         return true;
                     case R.id.remove_bucket_list:
                         final int taggedToPlay = games.get(position).taggedToPlay;
                         final Game gameToUndo = games.get(position);
                         games.get(position).taggedToPlay = 0;
                         games.get(position).save();

                         Snackbar
                                 .make(((GamesFragment) mFragment).mCoordinatorLayout,
                                         games.get(position).gameName + mActivity.getString(R.string.removed_from_bl),
                                         Snackbar.LENGTH_LONG)
                                 .setAction(mActivity.getString(R.string.undo), new View.OnClickListener() {
                                     @Override
                                     public void onClick(View view) {
                                         gameToUndo.taggedToPlay = taggedToPlay;
                                         gameToUndo.save();
                                         if (playListType == 2) {
                                             ((MainActivity) mActivity).onFragmentInteraction("refresh_games");
                                         }
                                     }
                                 })
                                 .show(); // Do not forget to show!
                         if (playListType == 2) {
                             ((MainActivity) mActivity).onFragmentInteraction("refresh_games");
                         }
                         return true;
                     default:
                         return false;
                 }
             }
         }

        );
        popup.show();
    }
}
