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
import com.lastsoft.plog.R;
import com.lastsoft.plog.db.Player;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.ViewHolder> {
    private static final String TAG = "PlayerAdapter";

    private DisplayImageOptions options;
    private Activity mActivity;
    private Fragment mFragment;
    public List<Player> players;
    private long mLastClickTime = 0;
    int currentYear = 0;


    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView winsView;
        private final TextView playsView;
        private final ImageView imageView;
        private final LinearLayout overflowLayout;
        private final View myView;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.d(TAG, "Element " + getPosition() + " clicked.");
                }
            });
            myView = v;
            nameView = (TextView) v.findViewById(R.id.playerName);
            winsView = (TextView) v.findViewById(R.id.playerWins);
            playsView = (TextView) v.findViewById(R.id.playerPlays);
            overflowLayout = (LinearLayout) v.findViewById(R.id.overflowLayout);
            imageView = (ImageView) v.findViewById(R.id.imageView1);
        }

        public ImageView getImageView() {
            return imageView;
        }
        public TextView getNameView() {
            return nameView;
        }
        public TextView getWinsView() {
            return winsView;
        }
        public TextView getPlaysView() {
            return playsView;
        }
        public LinearLayout getOverflowLayout() {
            return overflowLayout;
        }
        public View getView() {
            return myView;
        }
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

    public PlayerAdapter(Activity theActivity, Fragment theFragment, int mCurrentYear) {
        mActivity = theActivity;
        mFragment = theFragment;
        currentYear = mCurrentYear;
        players = Player.listPlayersAZ(currentYear);
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
                .inflate(R.layout.player_row_item, viewGroup, false);

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        String drawable = "R.drawable." + players.get(position).defaultColor;
        int id;
        if (players.get(position).playerPhoto == null) {
            if (players.get(position).defaultColor == null) {
                id = mActivity.getResources().getIdentifier("none", "drawable", mActivity.getPackageName());
            } else {
                id = mActivity.getResources().getIdentifier(players.get(position).defaultColor.toLowerCase(), "drawable", mActivity.getPackageName());
            }
            viewHolder.getImageView().setImageResource(id);
        }
        viewHolder.getNameView().setText(players.get(position).playerName);
        viewHolder.getWinsView().setText(mActivity.getString(R.string.wins) + players.get(position).totalWins);
        viewHolder.getPlaysView().setText(mActivity.getString(R.string.plays) + players.get(position).totalPlays);

        if (players.get(position).playerPhoto != null) {
            ImageLoader.getInstance().displayImage(players.get(position).playerPhoto, viewHolder.getImageView(), options);
        }
        viewHolder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                ((MainActivity)mActivity).openAddPlayer(mFragment, players.get(position).getId());
            }
        });
        viewHolder.getOverflowLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerPopup(view, position);
            }
        });
    }

    public void playerPopup(View v, final int position) {

        try{
            InputMethodManager inputManager = (InputMethodManager)
                    mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception ignored){}
        
        PopupMenu popup = new PopupMenu(mActivity, v);

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.player_overflow, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.view_plays:
                        ((MainActivity) mActivity).openPlays("0^" + players.get(position).getId(), false, 8, mActivity.getString(R.string.title_players), currentYear);
                        return true;
                    case R.id.view_wins:
                        ((MainActivity) mActivity).openPlays("0^" + players.get(position).getId(), false, 2, mActivity.getString(R.string.title_players), currentYear);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return players.size();
    }
}
