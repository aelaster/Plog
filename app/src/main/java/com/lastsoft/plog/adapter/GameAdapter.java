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

import com.lastsoft.plog.MainActivity;
import com.lastsoft.plog.R;
import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GameGroup;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.ViewHolder> {
    private static final String TAG = "GameAdapter";

    private List<Game> games;
    private DisplayImageOptions options;
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
        private final TextView textView;
        private final ImageView imageView;
        private final ImageView overflowView;
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
            imageView = (ImageView) v.findViewById(R.id.imageView1);
            overflowView = (ImageView) v.findViewById(R.id.overflowMenu);
            clickLayout = (LinearLayout) v.findViewById(R.id.clickLayout);
            myView = v;
        }

        public ImageView getImageView() {
            return imageView;
        }
        public TextView getTextView() {
            return textView;
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

    public GameAdapter(Fragment mFragment, Activity mActivity, String mSearchQuery, boolean mFromDrawer, int mPlayListType) {
        //games = Game.listAll(Game.class);
        //find(Class<T> type, String whereClause, String[] whereArgs, String groupBy, String orderBy, String limit)
        myFragment = mFragment;
        myActivity = mActivity;
        playListType = mPlayListType;

        switch (playListType) {
            case 0:
                games = Game.findBaseGames(mSearchQuery);
                break;
            case 1:
                games = Game.getUniqueGames_GameGroup(GameGroup.findById(GameGroup.class, Long.parseLong(mSearchQuery)));
                break;
            default:
                games = Game.findBaseGames(mSearchQuery);
                break;
        }

        options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
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
            if (playListType != 1) {
                viewHolder.getOverflowView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playPopup(view, position);
                    }
                });
                viewHolder.getClickLayout().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) myActivity).openAddPlay(myFragment, games.get(position).gameName, -1);
                    }
                });
            }else{
                viewHolder.getOverflowView().setVisibility(View.GONE);
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
        PopupMenu popup = new PopupMenu(myActivity, v);

        MenuInflater inflater = popup.getMenuInflater();
        if(games.get(position).gameBGGID.equals("")){
            inflater.inflate(R.menu.game_overflow_update, popup.getMenu());
        }else {
            inflater.inflate(R.menu.game_overflow, popup.getMenu());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_game:
                        //delete the play
                        //refresh play list
                        ((MainActivity) myActivity).deleteGame(games.get(position).getId());
                        return true;
                    case R.id.add_tenbyten:
                        //delete the play
                        //refresh play list
                        ((MainActivity) myActivity).addToTenXTen(games.get(position).getId());
                        return true;
                    case R.id.view_plays:
                        //delete the play
                        //refresh play list
                        ((MainActivity) myActivity).openPlays(games.get(position).gameName, false, 0);
                        return true;
                    case R.id.update_bgg:
                        //delete the play
                        //refresh play list
                        mPosition = position;
                        ((MainActivity) myActivity).updateGameViaBGG(games.get(position).gameName);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }
}
