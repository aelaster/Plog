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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GameGroup;
import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class PlayAdapter extends RecyclerView.Adapter<PlayAdapter.ViewHolder> {
    private static final String TAG = "PlayAdapter";

    List<Play> plays;
    private DisplayImageOptions options;
    private String searchQuery;
    private Activity myActivity;
    private Fragment myFragment;
    private boolean fromDrawer;
    int mPosition;
    int playListType;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView gameNameView;
        private final TextView playDateView;
        private final ImageView imageView;
        private final ImageView overflowView;
        private final LinearLayout clickLayout;
        private final View myView;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            gameNameView = (TextView) v.findViewById(R.id.gameName);
            playDateView = (TextView) v.findViewById(R.id.playDate);
            imageView = (ImageView) v.findViewById(R.id.imageView1);
            overflowView = (ImageView) v.findViewById(R.id.overflowMenu);
            clickLayout = (LinearLayout) v.findViewById(R.id.clickLayout);
            myView = v;
        }

        public ImageView getImageView() {
            return imageView;
        }
        public TextView getPlayDateView() {
            return playDateView;
        }
        public TextView getGameNameView() {
            return gameNameView;
        }
        public ImageView getOverflowView() {
            return overflowView;
        }
        public View getView() {
            return myView;
        }
        public LinearLayout getClickLayout() {
            return clickLayout;
        }
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

    public PlayAdapter(Activity mActivity, Fragment mFragment, String mSearchQuery, boolean mFromDrawer, int mPlayListType) {
        myActivity = mActivity;
        myFragment = mFragment;
        playListType = mPlayListType;
        //fromDrawer = mFromDrawer;
        //plays = Play.listPlaysNewOld();

        /*
        playListType:
        0 - listPlaysNewOld
        1 - list total plays for groupID, which is passed in via mSearchQuery
        2 - a players total regular wins, passed in the search query.  it is group and then player id, split with a dollar sign
        3 - a players total asterisk wins, passed in the search query.  it is group and then player id, split with a dollar sign
        4 - a players total wins, passed in the search query.  it is group and then player id, split with a dollar sign
        5 - total group shared wins
        6 - total group losses
        7 - plays for a ten by ten game.  Group, then game, then year.
         */

        switch (playListType) {
            case 0:
                plays = Play.listPlaysNewOld(mSearchQuery, fromDrawer);
                break;
            case 1:
                plays = Play.listPlaysNewOld_GameGroup(mSearchQuery);
                break;
            case 2:
                String[] query = mSearchQuery.split("\\$");
                plays = Play.totalWins_GameGroup_Player(GameGroup.findById(GameGroup.class, Long.parseLong(query[0])), Player.findById(Player.class, Long.parseLong(query[1])));
                break;
            case 3:
                String[] query2 = mSearchQuery.split("\\$");
                plays = Play.totalAsteriskWins_GameGroup_Player(GameGroup.findById(GameGroup.class, Long.parseLong(query2[0])), Player.findById(Player.class, Long.parseLong(query2[1])));
                break;
            case 4:
                String[] query3 = mSearchQuery.split("\\$");
                List<Play> wins = Play.totalWins_GameGroup_Player(GameGroup.findById(GameGroup.class, Long.parseLong(query3[0])), Player.findById(Player.class, Long.parseLong(query3[1])));
                plays = Play.totalAsteriskWins_GameGroup_Player(GameGroup.findById(GameGroup.class, Long.parseLong(query3[0])), Player.findById(Player.class, Long.parseLong(query3[1])));
                plays.addAll(wins);
                Collections.sort(plays, new Comparator<Play>() {
                    public int compare(Play left, Play right)  {
                        return right.playDate.compareTo(left.playDate); // The order depends on the direction of sorting.
                    }
                });
                break;
            case 5:
                plays = Play.totalSharedWins(GameGroup.findById(GameGroup.class, Long.parseLong(mSearchQuery)));
                break;
            case 6:
                plays = Play.totalGroupLosses(GameGroup.findById(GameGroup.class, Long.parseLong(mSearchQuery)));
                break;
            case 7:
                String[] query4 = mSearchQuery.split("\\$");
                plays = Play.gameTenByTen_GameGroup(GameGroup.findById(GameGroup.class, Long.parseLong(query4[0])), Game.findById(Game.class, Long.parseLong(query4[1])), Integer.parseInt(query4[2]));
                break;
            default:
                plays = Play.listPlaysNewOld(mSearchQuery, fromDrawer);
        }

        options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .considerExifParams(true)
                .resetViewBeforeLoading(true)
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
            String output_date = outputFormatter.format(plays.get(position).playDate); // Output : 01/20/2012
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
                    ((MainActivity) myActivity).onPlayClicked(plays.get(position), myFragment, viewHolder.getImageView(), viewHolder.getGameNameView(), viewHolder.getPlayDateView(), position, fromDrawer, playListType);
                }
            });
            viewHolder.getOverflowView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playPopup(view, position);
                }
            });
            if(plays.get(position).playPhoto != null && plays.get(position).playPhoto.equals("")) {
                String gameThumb = GamesPerPlay.getBaseGame(plays.get(position)).gameThumb;
                if (gameThumb != null && !gameThumb.equals("")) {
                    ImageLoader.getInstance().displayImage("http:" + GamesPerPlay.getBaseGame(plays.get(position)).gameThumb, viewHolder.getImageView(), options);
                    ImageLoader.getInstance().loadImage("http:" + GamesPerPlay.getBaseGame(plays.get(position)).gameThumb, options, null);
                }else{
                    viewHolder.getImageView().setImageDrawable(null);
                }
            }else{
                String thumbPathCheck = plays.get(position).playPhoto.substring(7, plays.get(position).playPhoto.length() - 4) + "_thumb.jpg";
                String thumbPath = plays.get(position).playPhoto.substring(0, plays.get(position).playPhoto.length() - 4) + "_thumb.jpg";
                if (new File(thumbPathCheck).exists()) {
                    ImageLoader.getInstance().displayImage(thumbPath, viewHolder.getImageView(), options);
                }else{
                    ImageLoader.getInstance().displayImage(plays.get(position).playPhoto, viewHolder.getImageView(), options);

                    // make a thumb
                    String fixedPath = plays.get(position).playPhoto.substring(6, plays.get(position).playPhoto.length());
                    String thumbPath2 = fixedPath.substring(0, fixedPath.length() - 4) + "_thumb.jpg";
                    try {
                        FileInputStream fis;
                        fis = new FileInputStream(fixedPath);
                        Bitmap imageBitmap = BitmapFactory.decodeStream(fis);
                        Bitmap b = resizeImageForImageView(imageBitmap, 500);

                        if (b != null) {
                            try {
                                b.compress(Bitmap.CompressFormat.JPEG,100, new FileOutputStream(new File(thumbPath2)));
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
                ImageLoader.getInstance().loadImage(plays.get(position).playPhoto, options, null);
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
        PopupMenu popup = new PopupMenu(myActivity, v);

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.play_overflow, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mPosition = position;
                switch (item.getItemId()) {
                    case R.id.edit_play:
                        ((MainActivity) myActivity).openAddPlay(myFragment, GamesPerPlay.getBaseGame(plays.get(position)).gameName, plays.get(position).getId());
                        return true;
                    case R.id.delete_play:
                        //delete the play
                        //refresh play list
                        ((MainActivity) myActivity).deletePlay(plays.get(position).getId());
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }

}
