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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
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

import com.lastsoft.plog.MainActivity;
import com.lastsoft.plog.PlaysFragment;
import com.lastsoft.plog.R;
import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GameGroup;
import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class PlayAdapter extends RecyclerView.Adapter<PlayAdapter.ViewHolder> {
    private static final String TAG = "PlayAdapter";

    public List<Play> plays;
    private DisplayImageOptions options;
    private String searchQuery;
    private Activity mActivity;
    private Fragment mFragment;
    private boolean fromDrawer;
    int mPosition;
    int playListType;
    int currentYear;
    int sortType;
    private long mLastClickTime = 0;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView gameNameView;
        private final TextView playDateView, playWinnerView;
        private final ImageView imageView;
        private final LinearLayout overflowLayout;
        private final LinearLayout clickLayout;
        private final View myView;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            gameNameView = (TextView) v.findViewById(R.id.gameName);
            playDateView = (TextView) v.findViewById(R.id.playDate);
            playWinnerView = (TextView) v.findViewById(R.id.playWinner);
            imageView = (ImageView) v.findViewById(R.id.imageView1);
            overflowLayout = (LinearLayout) v.findViewById(R.id.overflowLayout);
            clickLayout = (LinearLayout) v.findViewById(R.id.clickLayout);
            myView = v;
        }

        public ImageView getImageView() {
            return imageView;
        }
        public TextView getPlayDateView() {
            return playDateView;
        }
        public TextView getPlayWinnerView() {
            return playWinnerView;
        }
        public TextView getGameNameView() {
            return gameNameView;
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

    public void updateData(List<Play> updatedPlays){
        this.plays = updatedPlays;
        this.notifyDataSetChanged();
    }

    public List<Play> generatePlayData(String mSearchQuery, int playListType, int sortType, int currentYear){
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

        this.sortType = sortType;
        List<Play> plays_out;
        switch (playListType) {
            case 0:
                plays_out = Play.listPlaysNewOld(mSearchQuery, fromDrawer, false, sortType, currentYear);
                break;
            case 1:
                if (mSearchQuery.equals("0")){
                    plays_out = Play.listPlaysNewOld(sortType, currentYear);
                }else {
                    plays_out = Play.listPlaysNewOld_GameGroup(mSearchQuery, sortType, currentYear);
                }
                break;
            case 2:
                String[] query = mSearchQuery.split("\\^");
                if (query[0].equals("0")){
                    plays_out = Play.totalWins_Player(Player.findById(Player.class, Long.parseLong(query[1])), sortType, currentYear);
                }else {
                    plays_out = Play.totalWins_GameGroup_Player(GameGroup.findById(GameGroup.class, Long.parseLong(query[0])), Player.findById(Player.class, Long.parseLong(query[1])), sortType, currentYear);
                }
                break;
            case 3:
                String[] query2 = mSearchQuery.split("\\^");
                if (query2[0].equals("0")){
                    plays_out = Play.totalAsteriskWins_Player(Player.findById(Player.class, Long.parseLong(query2[1])), sortType, currentYear);
                }else {
                    plays_out = Play.totalAsteriskWins_GameGroup_Player(GameGroup.findById(GameGroup.class, Long.parseLong(query2[0])), Player.findById(Player.class, Long.parseLong(query2[1])), sortType, currentYear);
                }
                break;
            case 4:
                String[] query3 = mSearchQuery.split("\\^");
                List<Play> wins;
                if (query3[0].equals("0")){
                    wins = Play.totalWins_Player(Player.findById(Player.class, Long.parseLong(query3[1])), sortType, currentYear);
                    plays_out = Play.totalAsteriskWins_Player(Player.findById(Player.class, Long.parseLong(query3[1])), sortType, currentYear);
                }else {
                    wins = Play.totalWins_GameGroup_Player(GameGroup.findById(GameGroup.class, Long.parseLong(query3[0])), Player.findById(Player.class, Long.parseLong(query3[1])), sortType, currentYear);
                    plays_out = Play.totalAsteriskWins_GameGroup_Player(GameGroup.findById(GameGroup.class, Long.parseLong(query3[0])), Player.findById(Player.class, Long.parseLong(query3[1])), sortType, currentYear);
                }
                plays_out.addAll(wins);
                Collections.sort(plays_out, new Comparator<Play>() {
                    public int compare(Play left, Play right)  {
                        return right.playDate.compareTo(left.playDate); // The order depends on the direction of sorting.
                    }
                });
                break;
            case 5:
                if (mSearchQuery.equals("0")){
                    plays_out = Play.totalSharedWins(sortType, currentYear);
                }else {
                    plays_out = Play.totalSharedWins(GameGroup.findById(GameGroup.class, Long.parseLong(mSearchQuery)), sortType, currentYear);
                }
                break;
            case 6:
                if (mSearchQuery.equals("0")){
                    plays_out = Play.totalGroupLosses(sortType, currentYear);
                }else {
                    plays_out = Play.totalGroupLosses(GameGroup.findById(GameGroup.class, Long.parseLong(mSearchQuery)), sortType, currentYear);
                }
                break;
            case 7:
                String[] query4 = mSearchQuery.split("\\^");
                plays_out = Play.gameTenByTen_GameGroup(GameGroup.findById(GameGroup.class, Long.parseLong(query4[0])), Game.findById(Game.class, Long.parseLong(query4[1])), Integer.parseInt(query4[2]), sortType);
                break;
            case 8:
                String[] query8 = mSearchQuery.split("\\^");
                if (query8[0].equals("0")){
                    plays_out = Play.totalPlays_Player(Player.findById(Player.class, Long.parseLong(query8[1])), sortType, currentYear);
                }else {
                    plays_out = Play.totalPlays_Player_GameGroup(Player.findById(Player.class, Long.parseLong(query8[1])), GameGroup.findById(GameGroup.class, Long.parseLong(query8[0])), sortType, currentYear);

                }
                break;
            case 9:
                plays_out = Play.listPlaysNewOld(mSearchQuery, fromDrawer, true, sortType, currentYear);
                break;
            case 10:
                String[] query10 = mSearchQuery.split("\\^");
                plays_out = Play.totalPlays_TenByTen_GameGroup(GameGroup.findById(GameGroup.class, Long.parseLong(query10[0])), Integer.parseInt(query10[1]), sortType);
                break;
            default:
                plays_out = Play.listPlaysNewOld(mSearchQuery, fromDrawer, false, sortType, currentYear);
        }
        return plays_out;
    }

    public PlayAdapter(Activity theActivity, Fragment theFragment, String mSearchQuery, boolean mFromDrawer, int mPlayListType, int mSortType, int mCurrentYear) {
        mActivity = theActivity;
        mFragment = theFragment;
        playListType = mPlayListType;
        sortType = mSortType;
        fromDrawer = mFromDrawer;
        currentYear = mCurrentYear;

        plays = generatePlayData(mSearchQuery, mPlayListType, mSortType, mCurrentYear);

        options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .resetViewBeforeLoading(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.play_row_item, viewGroup, false);

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        //Log.d(TAG, "Element " + position + " set.");

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        //if (searchQuery.equals("") || (games.get(position).gameName.toLowerCase().contains(searchQuery.toLowerCase()))) {

            DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
            Date theDate = plays.get(position).playDate;
            long diff = new Date().getTime() - theDate.getTime();
            long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            String output_date;
            if (days == 0){
                output_date = mActivity.getString( R.string.played_label) + mActivity.getString(R.string.less_than_a_day_ago);
            }else if (days == 1){
                output_date = mActivity.getString( R.string.played_label) + days + mActivity.getString(R.string.day_ago_label);
            }else if (days <= 6){
                output_date = mActivity.getString( R.string.played_label) + days + mActivity.getString( R.string.days_ago_label);
            }else {
                output_date = mActivity.getString( R.string.played_label) + outputFormatter.format(theDate); // Output : 01/20/2012
            }
            //String output_date = outputFormatter.format(theDate); // Output : 01/20/2012

            viewHolder.getGameNameView().setText(GamesPerPlay.getBaseGame(plays.get(position)).gameName);
            viewHolder.getPlayDateView().setText(output_date);
            viewHolder.getImageView().setTransitionName("imageTrans" + position);
            viewHolder.getImageView().setTag("imageTrans" + position);
            viewHolder.getGameNameView().setTransitionName("nameTrans" + position);
            viewHolder.getGameNameView().setTag("nameTrans" + position);
            viewHolder.getPlayDateView().setTransitionName("dateTrans" + position);
            viewHolder.getPlayDateView().setTag("dateTrans" + position);
            viewHolder.getClickLayout().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 2000){
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    ((MainActivity) mActivity).onPlayClicked(plays.get(position), mFragment, viewHolder.getImageView(), viewHolder.getGameNameView(), viewHolder.getPlayDateView(), position, fromDrawer, playListType, sortType);
                }
            });
            viewHolder.getOverflowLayout().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playPopup(view, position);
                }
            });

            String playPhoto;
            playPhoto = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES) + "/Plog/" + plays.get(position).playPhoto;

            if(plays.get(position).playPhoto != null && (plays.get(position).playPhoto.equals("") || new File(playPhoto).exists() == false)) {
                String gameThumb = GamesPerPlay.getBaseGame(plays.get(position)).gameThumb;
                if (gameThumb != null && !gameThumb.equals("")) {
                    //ImageLoader.getInstance().displayImage("http:" + GamesPerPlay.getBaseGame(plays.get(position)).gameThumb, viewHolder.getImageView(), options);
                    //ImageLoader.getInstance().loadImage("http:" + GamesPerPlay.getBaseGame(plays.get(position)).gameThumb, options, null);
                    Picasso.with(mActivity).load("http:" + GamesPerPlay.getBaseGame(plays.get(position)).gameThumb).into(viewHolder.getImageView());
                }else{
                    viewHolder.getImageView().setImageDrawable(null);
                }
            }else{
                String thumbPath =  playPhoto.substring(0, playPhoto.length() - 4) + "_thumb3.jpg";
                if (new File(thumbPath).exists()) {
                    //ImageLoader.getInstance().displayImage(thumbPath, viewHolder.getImageView(), options);
                    Picasso.with(mActivity).load("file://" + thumbPath).into(viewHolder.getImageView());
                }else{
                    //ImageLoader.getInstance().displayImage(playPhoto, viewHolder.getImageView(), options);
                    //Picasso.with(mActivity).load(playPhoto).fit().into(viewHolder.getImageView());
                    Picasso.with(mActivity)
                            .load(playPhoto)
                            .resize(100, 100)
                            .centerCrop()
                            .into(viewHolder.getImageView());
                    // make a thumb
                    String thumbPath2 = playPhoto.substring(0, playPhoto.length() - 4) + "_thumb3.jpg";
                    try {
                        FileInputStream fis;
                        fis = new FileInputStream(thumbPath2);
                        Bitmap imageBitmap = BitmapFactory.decodeStream(fis);
                        Bitmap b = resizeImageForImageView(imageBitmap, 100);

                        if (b != null) {
                            try {
                                b.compress(Bitmap.CompressFormat.JPEG,50, new FileOutputStream(new File(thumbPath2)));
                            } catch (Exception ignored) {
                            }
                            b = null;
                        }
                        if (imageBitmap != null){
                            imageBitmap = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //still use the og picture.  next time there will be a thumb
                }
                //Picasso.with(mActivity).load(playPhoto).fetch();
                //ImageLoader.getInstance().loadImage(playPhoto, options, null);
            }


            viewHolder.getPlayWinnerView().setTypeface(null, Typeface.ITALIC);
            if (plays.get(position).winners != null) {
                viewHolder.getPlayWinnerView().setText(mActivity.getString(R.string.winners) + plays.get(position).winners);
            }else{
                viewHolder.getPlayWinnerView().setText(mActivity.getString(R.string.winners) + mActivity.getString(R.string.none));
            }

    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    public Bitmap resizeImageForImageView(Bitmap bitmap, int size) {
        Bitmap resizedBitmap;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = -1;
        int newHeight = -1;
        float multFactor;
        if(originalHeight > originalWidth) {
            newHeight = size;
            multFactor = (float) originalWidth/(float) originalHeight;
            newWidth = (int) (newHeight*multFactor);
        } else if(originalWidth > originalHeight) {
            newWidth = size;
            multFactor = (float) originalHeight/ (float)originalWidth;
            newHeight = (int) (newWidth*multFactor);
        } else if(originalHeight == originalWidth) {
            newHeight = size;
            newWidth = size;
        }
        resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        return resizedBitmap;
    }



    @Override
    public int getItemCount() {
        return plays.size();
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
        inflater.inflate(R.menu.play_overflow, popup.getMenu());

        if (!fromDrawer){
            popup.getMenu().removeItem(R.id.view_plays);
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mPosition = position;
                switch (item.getItemId()) {
                    case R.id.view_plays:
                        ((PlaysFragment)mFragment).setSearchText(GamesPerPlay.getBaseGame(plays.get(position)).gameName);
                        return true;
                    case R.id.edit_play:
                        ((MainActivity) mActivity).openAddPlay(mFragment, GamesPerPlay.getBaseGame(plays.get(position)).gameName, plays.get(position).getId(), false);
                        return true;
                    case R.id.copy_play:
                        ((MainActivity) mActivity).openAddPlay(mFragment, GamesPerPlay.getBaseGame(plays.get(position)).gameName, plays.get(position).getId(), true);
                        return true;
                    case R.id.delete_play:
                        //delete the play
                        //refresh play list
                        ((MainActivity) mActivity).deletePlay(plays.get(position).getId());
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }

}
