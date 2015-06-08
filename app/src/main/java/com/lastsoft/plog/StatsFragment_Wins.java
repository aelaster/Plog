package com.lastsoft.plog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GameGroup;
import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerPlay;

import java.util.ArrayList;
import java.util.List;

public class StatsFragment_Wins extends Fragment {

    List<Player> groupPlayers;
    View statsView;
    private ViewGroup mContainerView_Players;
    long gameGroup;


    // TODO: Rename and change types and number of parameters
    public static StatsFragment_Wins newInstance(long gameGroup) {
        StatsFragment_Wins fragment = new StatsFragment_Wins();
        Bundle args = new Bundle();
        args.putLong("gameGroup", gameGroup);
        fragment.setArguments(args);
        return fragment;
    }

    public StatsFragment_Wins() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gameGroup = getArguments().getLong("gameGroup");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        statsView = inflater.inflate(R.layout.fragment_statistics, container, false);
        if (gameGroup > 0) {
            groupPlayers = GameGroup.getGroupPlayers(GameGroup.findById(GameGroup.class, gameGroup));
            mContainerView_Players = (ViewGroup) statsView.findViewById(R.id.container_players);
            LoadStatsTask initStats = new LoadStatsTask(mActivity, gameGroup);
            try {
                initStats.execute();
            } catch (Exception ignored) {

            }
        }
        return statsView;
    }


    Activity mActivity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    public class LoadStatsTask extends AsyncTask<Long, Void, Long[]> {

        ArrayList<String> filteredGames;
        Context theContext;
        int filteredPlays = 0;
        int uniqueFilter = 0;
        boolean loserFlag = true;
        boolean sharedFlag = true;
        int sharedCounter = 0;
        int loserCounter = 0;
        long theGroup;

        private final ProgressDialog mydialog = new ProgressDialog(mActivity);

        public LoadStatsTask(Context context, long gameGroup) {
            this.theGroup = gameGroup;
            this.theContext = context;
        }

        // can use UI thread here
        @Override
        protected void onPreExecute() {

            mydialog.setMessage(getString(R.string.calculating));
            mydialog.setCancelable(false);
            try{
                mydialog.show();
            }catch (Exception ignored){}
        }

        // automatically done on worker thread (separate from UI thread)
        @Override
        protected Long[] doInBackground(final Long... args) {

            filteredGames = new ArrayList<>();
            int outputBounds = ((groupPlayers.size() * 2) + 2);
            Long[] output = new Long[outputBounds];
            //times 2 because each player needs regular and asterisk totals
            //plus two because we put total and unique plays on the top
            int[] playerScoreHolder = new int[groupPlayers.size()];
            try {

                List<PlayersPerPlay> groupTotalPlays = PlayersPerPlay.totalPlays_GameGroup(GameGroup.findById(GameGroup.class, theGroup));
                //long uniquePlays  = GamesPerPlay.getUniquePlays_GameGroup(GameGroup.findById(GameGroup.class, theGroup));
                long uniquePlays = Game.getUniqueGames_GameGroup(GameGroup.findById(GameGroup.class, theGroup)).size();
                //Log.d("V1", "groupTotalPlays = " + groupTotalPlays.size());

                output[0] = ((long)groupTotalPlays.size()/(long)groupPlayers.size());
                output[1] = uniquePlays;
                long playCounter = -1;
                PlayersPerPlay playHolder = null;
                int highScore = 0;
                int scoreIndex = 0;
                for(PlayersPerPlay eachPlay:groupTotalPlays){
                    playHolder = eachPlay;
                    if (playCounter == -1){
                        //first time in
                        //set play counter to current play
                        playCounter = eachPlay.play.getId();
                        //set high score for play
                        highScore = eachPlay.playHighScore;
                    }else if (eachPlay.play.getId() != playCounter){
                        //we've moved on to the next play
                        //calculate winner from past playCounter play
                        //Log.d("V1", "game  = " + GamesPerPlay.getBaseGame(Play.findById(Play.class, eachPlay.play.getId())).gameName);
                        //Log.d("V1", "scoreIndex = " + scoreIndex);
                        //Log.d("V1", "groupPlayers.size() = " + groupPlayers.size());
                        if (scoreIndex == groupPlayers.size()) {
                            //only included if all of the group has been scored
                            int max = playerScoreHolder[0];

                            for (int aPlayerScoreHolder : playerScoreHolder) {
                                if (aPlayerScoreHolder > max) {
                                    max = aPlayerScoreHolder;
                                }
                            }

                            for (int x = 0; x < playerScoreHolder.length; x++) {
                                int arrayBounds = 2 + (x * 2);
                                //Log.d("V1", "arrayBounds = " + arrayBounds);
                                if (playerScoreHolder[x] == highScore && playerScoreHolder[x] != 0) {
                                    if (output[(arrayBounds)] == null) {
                                        output[(arrayBounds)] = (long) 1;
                                    } else {
                                        output[arrayBounds] = output[arrayBounds] + (long) 1;
                                    }
                                    loserFlag = false;
                                }else{
                                    sharedFlag = false;
                                }
                                if (playerScoreHolder[x] >= max && playerScoreHolder[x] != 0) {
                                    if (output[(arrayBounds + 1)] == null) {
                                        output[(arrayBounds + 1)] = (long) 1;
                                    } else {
                                        output[(arrayBounds + 1)] = output[(arrayBounds + 1)] + (long) 1;
                                    }
                                    loserFlag = false;
                                }

                            }
                            if (sharedFlag){
                                sharedCounter++;
                            }
                            if (loserFlag){
                                loserCounter++;
                            }
                        }else{
                            filteredPlays++;
                       }


                        loserFlag = true;
                        sharedFlag = true;


                        //set playCounter to new play
                        playCounter = eachPlay.play.getId();
                        //set high score for new play
                        highScore = eachPlay.playHighScore;
                        //zero out scores
                        playerScoreHolder = new int[groupPlayers.size()];
                        scoreIndex = 0;
                    }

                    for(Player eachPlayer:groupPlayers){
                        //Log.d("V1", "player name = " + eachPlayer.playerName);
                        if (eachPlay.player.getId() == eachPlayer.getId()){
                            //Log.d("V1", "MATCHED!");
                            playerScoreHolder[scoreIndex] = eachPlay.score;
                            scoreIndex++;
                            break;
                        }
                    }
                    //Log.d("V1", "not matched");
                }
                //Log.d("V1", "scoreIndex = " + scoreIndex);
                if (scoreIndex == groupPlayers.size()) {
                    //calculate the last winner
                    int max = playerScoreHolder[0];

                    for (int aPlayerScoreHolder : playerScoreHolder) {
                        if (aPlayerScoreHolder > max) {
                            max = aPlayerScoreHolder;
                        }
                    }

                    for (int x = 0; x < playerScoreHolder.length; x++) {
                        int arrayBounds = 2 + (x * 2);
                        if (playerScoreHolder[x] == highScore && playerScoreHolder[x] != 0) {
                            if (output[(arrayBounds)] == null) {
                                output[(arrayBounds)] = (long) 1;
                            } else {
                                output[arrayBounds] = output[arrayBounds] + (long) 1;
                            }
                            loserFlag = false;
                        }else{
                            sharedFlag = false;
                        }
                        if (playerScoreHolder[x] >= max) {
                            if (output[(arrayBounds + 1)] == null) {
                                output[(arrayBounds + 1)] = (long) 1;
                            } else {
                                output[(arrayBounds + 1)] = output[(arrayBounds + 1)] + (long) 1;
                            }
                            loserFlag = false;
                        }
                    }
                    if (sharedFlag){
                        sharedCounter++;
                    }
                    if (loserFlag){
                        loserCounter++;
                    }
                }else{
                    filteredPlays++;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return output;
        }

        @Override
        protected void onPostExecute ( final Long[] result){
            long totalPlays = result[0];
            Log.d("V1", "filteredPlays=" + filteredPlays );
            long totalUnique = result[1];
            addStat("Total Plays: ", totalPlays + "");
            addStat("Unique Games: ", totalUnique + "");
            for(int x = 0; x < groupPlayers.size(); x++) {
                int arrayBounds = 2 + (x * 2);
                long result_out, result_out2;
                if (result[arrayBounds] == null){
                    result_out = 0;
                }else{
                    result_out = result[arrayBounds];
                }
                if (result[arrayBounds + 1] == null) {
                    result_out2 = 0;
                } else {
                    result_out2 = result[arrayBounds+1];
                }
                addStat(groupPlayers.get(x).playerName + " Total Wins:", result_out+"");
                addStat(groupPlayers.get(x).playerName + " Asterisk Wins:", result_out2+"");
            }
            addStat("Shared Wins: ", sharedCounter + "");
            addStat("Total Losses: ", loserCounter + "");
            for(int x = 0; x < groupPlayers.size(); x++) {
                int arrayBounds = 2 + (x * 2);
                long result_out, result_out2;
                if (result[arrayBounds] == null){
                    result_out = 0;
                }else{
                    result_out = result[arrayBounds];
                }
                if (result[arrayBounds+1] == null){
                    result_out2 = 0;
                }else{
                    result_out2 = result[arrayBounds+1];
                }
                addStat(groupPlayers.get(x).playerName + " Total Wins Percentage:", ((int) (result_out * 100.0 / totalPlays + 0.5)) + "%");
                addStat(groupPlayers.get(x).playerName + " Asterisk Wins Percentage:", ((int) (result_out2 * 100.0 / totalPlays + 0.5)) + "%");
            }
            addStat("Shared Wins Percentage: ", ((int) (sharedCounter * 100.0 / totalPlays + 0.5)) + "%");
            addStat("Total Losses Percentage: ", ((int) (loserCounter * 100.0 / totalPlays + 0.5)) + "%");
            mydialog.dismiss();
            }
    }

    private void addStat(String statType, String statValue) {
        // Instantiate a new "row" view.
        try {
            final ViewGroup newView = (ViewGroup) LayoutInflater.from(mActivity).inflate(
                    R.layout.stats_viewstat_item, mContainerView_Players, false);

            TextView statTypeView = (TextView) newView.findViewById(R.id.statType);
            TextView statValueView = (TextView) newView.findViewById(R.id.statValue);
            statTypeView.setText(statType);
            statValueView.setText(statValue);
            statTypeView.setTextSize(16);
            statValueView.setTextSize(16);
            mContainerView_Players.addView(newView);
        }catch (Exception ignored){}
    }
}
