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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerPlay;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Demonstrates the use of {@link RecyclerView} with a {@link LinearLayoutManager} and a
 * {@link GridLayoutManager}.
 */
public class PlayersFragment extends Fragment{

    private static final String TAG = "PlayersFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private static final int DATASET_COUNT = 60;
    private float x,y;


    private enum LayoutManagerType {
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected RecyclerView mRecyclerView;
    protected PlayerAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected String[] mDataset;
    private SwipeRefreshLayout pullToRefreshView;

    private OnFragmentInteractionListener mListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize dataset, this data would usually come from a local content provider or
        // remote server.
        initDataset();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.players_view_frag, container, false);
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        FloatingActionButton addPlayer = (FloatingActionButton) rootView.findViewById(R.id.add_player);
        addPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int viewXY[] = new int[2];
                v.getLocationOnScreen(viewXY);

                if (mListener != null) {
                    mListener.onFragmentInteraction("add_player", viewXY[0], viewXY[1]);
                }
            }
        });

        FloatingActionButton addGroup = (FloatingActionButton) rootView.findViewById(R.id.add_group);
        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int viewXY[] = new int[2];
                v.getLocationOnScreen(viewXY);

                String next[] = {};
                try {
                    CSVReader reader = new CSVReader(new InputStreamReader(mActivity.getAssets().open("GamesImport.csv")));
                    while(true) {
                        next = reader.readNext();
                        if(next != null) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                            Date date1 = dateFormat.parse(next[0]);

                            Play newPlay = new Play(date1, next[3].trim());
                            newPlay.save();

                            int totalPlayers = 2;
                            if (!next[5].equals("")) {totalPlayers++; }
                            if (!next[6].equals("")) {totalPlayers++; }
                            if (!next[7].equals("")) {totalPlayers++; }
                            if (!next[8].equals("")) {totalPlayers++; }

                            /*
                                First, trim off asterisks from players 1-4 and hold in it's own variable, so the asterisk is still in the next array
                                If Player 1-4 isn't blank, check to see if that player's name exists
                                if not, add the player and get the id
                                otherwise, get the player id

                                Next, for each player 1-4 that has an asterisk, make their score 1 for the game
                             */

                            if (!next[5].equals("")){ //Player 1
                                addPlayer(next[5], newPlay, totalPlayers);
                            }
                            if (!next[6].equals("")){ //Player 2
                                addPlayer(next[6], newPlay, totalPlayers);
                            }
                            if (!next[7].equals("")){ //Player 3
                                addPlayer(next[7], newPlay, totalPlayers);
                            }
                            if (!next[8].equals("")){ //Player 4
                                addPlayer(next[8], newPlay, totalPlayers);
                            }

                           /*

                            Next, check "winner".

                            I AM 1, SHE IS 2 (player ids)

                            if "AEL", get My playerID and make my score 1 and make her score 0
                            if "SKG", get her playerID and make her score 1 and make my score 0
                            if "AEL and SKG" get our player ids and make our scores 1
                            if "Big Fat Losers" get out playerids and make our scores 0

                             */

                            int score;
                            if (next[4].trim().contains("AEL and SKG")) {
                                PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, (long) 1), newPlay, totalPlayers, totalPlayers);
                                newPlayer.save();
                                PlayersPerPlay newPlayer2 = new PlayersPerPlay(Player.findById(Player.class, (long) 2), newPlay, totalPlayers, totalPlayers);
                                newPlayer2.save();
                            }else if (next[4].trim().contains("AEL")){
                                if (next[4].trim().endsWith("*")){
                                    PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, (long) 1), newPlay, totalPlayers-1, totalPlayers);
                                    newPlayer.save();
                                }else {
                                    PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, (long) 1), newPlay, totalPlayers, totalPlayers);
                                    newPlayer.save();
                                }
                                PlayersPerPlay newPlayer2 = new PlayersPerPlay(Player.findById(Player.class, (long)2), newPlay, 0, totalPlayers);
                                newPlayer2.save();
                            }else if (next[4].trim().contains("SKG")){
                                if (next[4].trim().endsWith("*")){
                                    PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, (long) 2), newPlay, totalPlayers-1, totalPlayers);
                                    newPlayer.save();
                                }else {
                                    PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, (long) 2), newPlay, totalPlayers, totalPlayers);
                                    newPlayer.save();
                                }
                                PlayersPerPlay newPlayer2 = new PlayersPerPlay(Player.findById(Player.class, (long)1), newPlay, 0, totalPlayers);
                                newPlayer2.save();
                            }else{
                                PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, (long)1), newPlay, 0, totalPlayers);
                                newPlayer.save();
                                PlayersPerPlay newPlayer2 = new PlayersPerPlay(Player.findById(Player.class, (long)2), newPlay, 0, totalPlayers);
                                newPlayer2.save();
                            }

                            //last, add game to play
                            //if i don't have the game, add it

                            Game addedGame = Game.findGameByName_NoCase(URLDecoder.decode(next[1].trim(), "UTF-8"));
                            if (addedGame == null){
                                Log.d("V1", "New Game = " + next[1].trim());
                                addedGame = new Game(next[1].trim());
                                addedGame.save();
                            }

                            GamesPerPlay newBaseGame = new GamesPerPlay(newPlay, addedGame, false);
                            newBaseGame.save();

                            //}
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                /*if (mListener != null) {
                    mListener.onFragmentInteraction("add_group", viewXY[0], viewXY[1]);
                }*/
            }
        });


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

        mAdapter = new PlayerAdapter(mActivity, this);
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);

        /*pullToRefreshView = (SwipeRefreshLayout) rootView.findViewById(R.id.pull_to_refresh_listview);
        pullToRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                // TODO Auto-generated method stub
                pullToRefreshView.setRefreshing(false);
            }
        });*/


        // END_INCLUDE(initializeRecyclerView)
        return rootView;
    }

    private void addPlayer(String playerName, Play newPlay, int totalPlayers){
        String playerHolder = playerName.trim();
        long playerID;
        boolean winnerFlag = false;
        int score;
        if (playerHolder.endsWith("*")){
            winnerFlag = true;
            playerHolder = playerHolder.substring(0, playerHolder.length()-1);
        }
        playerID = Player.playerExists_ID(playerHolder);
        if (playerID == -1) {
            Log.d("V1", "playerName= " + playerName);
            Log.d("V1", "playerHolder= " + playerHolder);
            //player doesn't exist.  Add them
            Player player = new Player(playerHolder);
            player.save();
            playerID = player.getId();
        }

        if (winnerFlag){
            score = totalPlayers;
        }else{
            score = 0;
        }

        PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, playerID), newPlay, score, totalPlayers);
        newPlayer.save();
    }

    Activity mActivity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        ((MainActivity) mActivity).onSectionAttached(2);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mActivity = null;
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private void initDataset() {
        List<Player> players = Player.listPlayersAZ();
        if (players != null) {
            mDataset = new String[players.size()];
            int i = 0;
            for(Player player:players){
                mDataset[i] = player.playerName;
                //Log.d("V1", "player name = " + player.playerName);
                i++;
            }
        }
    }

    protected void refreshDataset(){
        initDataset();
        mAdapter = new PlayerAdapter(mActivity, this);
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id, float x, float y);
    }
}
