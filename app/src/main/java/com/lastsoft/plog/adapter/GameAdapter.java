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
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lastsoft.plog.GamesFragment;
import com.lastsoft.plog.MainActivity;
import com.lastsoft.plog.R;
import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GameGroup;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.ViewHolder> {
    private static final String TAG = "GameAdapter";

    private List<Game> games;
    private DisplayImageOptions options;
    private Activity mActivity;
    private Fragment myFragment;
    private boolean fromDrawer;
    int mPosition;
    int playListType;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView, bucketDateView;
        private final ImageView imageView;
        private final LinearLayout overflowLayout;
        private final LinearLayout clickLayout;
        private final View myView;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Element " + getPosition() + " clicked.");
                }
            });
            textView = (TextView) v.findViewById(R.id.gameName);
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
        public TextView getBucketDateView() {
            return bucketDateView;
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

    public GameAdapter(Fragment mFragment, Activity theActivity, String mSearchQuery, boolean mFromDrawer, int mPlayListType) {
        //games = Game.listAll(Game.class);
        //find(Class<T> type, String whereClause, String[] whereArgs, String groupBy, String orderBy, String limit)
        myFragment = mFragment;
        mActivity = theActivity;
        playListType = mPlayListType;

        switch (playListType) {
            case 0:
                games = Game.findBaseGames(mSearchQuery);
                break;
            case 1:
                if (mSearchQuery.equals("0")){
                    games = Game.getUniqueGames();
                }else {
                    games = Game.getUniqueGames_GameGroup(GameGroup.findById(GameGroup.class, Long.parseLong(mSearchQuery)));
                }
                break;
            case 2:
                games = Game.getBucketList();
                break;
            default:
                games = Game.findBaseGames(mSearchQuery);
                break;
        }

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
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        //Log.d(TAG, "Element " + position + " set.");

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        //if (searchQuery.equals("") || (games.get(position).gameName.toLowerCase().contains(searchQuery.toLowerCase()))) {
            viewHolder.getTextView().setText(games.get(position).gameName);
            if (games.get(position).gameThumb != null  && !games.get(position).gameThumb.equals("")) {
                ImageLoader.getInstance().displayImage("http:" + games.get(position).gameThumb, viewHolder.getImageView(), options);
            } else {
                viewHolder.getImageView().setImageDrawable(null);
            }

            if (games.get(position).taggedToPlay > 0 && playListType == 2){
                Date theDate = new Date(((long)games.get(position).taggedToPlay)*1000L);
                DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
                String output_date = outputFormatter.format(theDate); // Output : 01/20/2012
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
                viewHolder.getClickLayout().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) mActivity).openAddPlay(myFragment, games.get(position).gameName, -1);
                    }
                });
            }else{
                viewHolder.getOverflowLayout().setVisibility(View.GONE);
            }
        //}

    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return games.size();
    }

    public void playPopup(View v, final int position) {
        PopupMenu popup = new PopupMenu(mActivity, v);

        MenuInflater inflater = popup.getMenuInflater();

        inflater.inflate(R.menu.game_overflow, popup.getMenu());
        if(games.get(position).gameBGGID == null || games.get(position).gameBGGID.equals("")) {
            popup.getMenu().removeItem(R.id.update_bgg);
        }
        if (games.get(position).gameBoxImage == null || games.get(position).gameBoxImage.equals("")){
            popup.getMenu().removeItem(R.id.view_box_photo);
        }
        if (games.get(position).taggedToPlay <= 0){
            popup.getMenu().removeItem(R.id.remove_bucket_list);
        }else{
            popup.getMenu().removeItem(R.id.add_bucket_list);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_game:
                        //delete the play
                        //refresh play list
                        ((MainActivity) mActivity).deleteGame(games.get(position).getId());
                        return true;
                    case R.id.add_tenbyten:
                        //delete the play
                        //refresh play list
                        ((MainActivity) mActivity).addToTenXTen(games.get(position).getId());
                        return true;
                    case R.id.view_plays:
                        //delete the play
                        //refresh play list
                        ((MainActivity) mActivity).openPlays(games.get(position).gameName, false, 0);
                        return true;
                    case R.id.update_bgg:
                        //delete the play
                        //refresh play list
                        mPosition = position;
                        ((MainActivity) mActivity).updateGameViaBGG(games.get(position).gameName, false);
                        return true;
                    case R.id.add_box_photo:
                        ((GamesFragment) myFragment).captureBox(games.get(position));
                        return true;
                    case R.id.view_box_photo:
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(games.get(position).gameBoxImage), "image/*");
                        mActivity.startActivity(intent);
                        return true;
                    case R.id.add_bucket_list:
                        String[] ids = TimeZone.getAvailableIDs(-5 * 60 * 60 * 1000);
                        // if no ids were returned, something is wrong. get out.
                        if (ids.length == 0)
                            System.exit(0);

                        // begin output
                        //System.out.println("Current Time");

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
                        return true;
                    case R.id.remove_bucket_list:
                        games.get(position).taggedToPlay = 0;
                        games.get(position).save();
                        ((MainActivity) mActivity).onFragmentInteraction("refresh_games");
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
