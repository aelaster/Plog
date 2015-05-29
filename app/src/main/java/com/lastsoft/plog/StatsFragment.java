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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    List<Player> groupPlayers;
    View statsView;
    private ViewGroup mContainerView_Players;


    // TODO: Rename and change types and number of parameters
    public static StatsFragment newInstance(String param1, String param2) {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        statsView = inflater.inflate(R.layout.fragment_statistics, container, false);
        groupPlayers = GameGroup.getGroupPlayers(GameGroup.findById(GameGroup.class, (long) 1));
        mContainerView_Players = (ViewGroup) statsView.findViewById(R.id.container_players);
        LoadStatsTask initStats = new LoadStatsTask(mActivity);
        try {
            initStats.execute((long)1);
        } catch (Exception e) {

        }
        return statsView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String string) {
        if (mListener != null) {
            mListener.onFragmentInteraction(string);
        }
    }

    Activity mActivity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        try {
            mListener = (OnFragmentInteractionListener) activity;
            ((MainActivity) mActivity).onSectionAttached(4);
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String string);
    }

    public class LoadStatsTask extends AsyncTask<Long, Void, Long[]> {

        ArrayList<String> filteredGames;
        Context theContext;
        int filteredPlays = 0;
        int uniqueFilter = 0;
        private final ProgressDialog mydialog = new ProgressDialog(mActivity);

        public LoadStatsTask(Context context) {
            this.theContext = context;
        }

        // can use UI thread here
        @Override
        protected void onPreExecute() {

            mydialog.setMessage(getString(R.string.calculating));
            mydialog.setCancelable(false);
            try{
                mydialog.show();
            }catch (Exception e){}
        }

        // automatically done on worker thread (separate from UI thread)
        @Override
        protected Long[] doInBackground(final Long... args) {

            filteredGames = new ArrayList<String>();
            int outputBounds = ((groupPlayers.size() * 2) + 2);
            Long[] output = new Long[outputBounds];
            //times 2 because each player needs regular and asterisk totals
            //plus two because we put total and unique plays on the top
            int[] playerScoreHolder = new int[groupPlayers.size()];
            try {

                List<PlayersPerPlay> groupTotalPlays = PlayersPerPlay.totalPlays_GameGroup(GameGroup.findById(GameGroup.class, args[0]));
                long uniquePlays  = GamesPerPlay.getUniquePlays_GameGroup(GameGroup.findById(GameGroup.class, args[0]));
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
                        //Log.d("V1", "scoreIndex = " + scoreIndex);
                        if (scoreIndex == groupPlayers.size()) {
                            //only included if all of the group has been scored
                            int max = playerScoreHolder[0];

                            for (int i = 0; i < playerScoreHolder.length; i++) {
                                if (playerScoreHolder[i] > max) {
                                    max = playerScoreHolder[i];
                                }
                            }

                            for (int x = 0; x < playerScoreHolder.length; x++) {
                                int arrayBounds = 2 + (x * groupPlayers.size());
                                if (playerScoreHolder[x] == highScore && playerScoreHolder[x] != 0) {
                                    if (output[(arrayBounds)] == null) {
                                        output[(arrayBounds)] = (long) 1;
                                    } else {
                                        output[arrayBounds] = output[arrayBounds] + (long) 1;
                                    }
                                }
                                if (playerScoreHolder[x] >= max && playerScoreHolder[x] != 0) {
                                    if (output[(arrayBounds + 1)] == null) {
                                        output[(arrayBounds + 1)] = (long) 1;
                                    } else {
                                        output[(arrayBounds + 1)] = output[(arrayBounds + 1)] + (long) 1;
                                    }
                                }
                            }
                        }else{
                            filteredPlays++;
                            //ths is filtred.  is this a unique game being filtered?
                            Game getGame = GamesPerPlay.getBaseGame(eachPlay.play);
                            if (!filteredGames.contains(getGame.gameName)) {
                                boolean uniqueFlag;
                                for (Player eachPlayer : groupPlayers) {
                                    if (Player.hasPlayerPlayedGame(eachPlayer, GamesPerPlay.getBaseGame(eachPlay.play)) == false) {
                                        //nope, one of us hasn't played, so it's not a unique game for us
                                        uniqueFilter++;
                                        filteredGames.add(getGame.gameName);
                                        break;
                                    }
                                }
                            }
                        }

                        //set playCounter to new play
                        playCounter = eachPlay.play.getId();
                        //set high score for new play
                        highScore = eachPlay.playHighScore;
                        //zero out scores
                        playerScoreHolder = new int[groupPlayers.size()];
                        scoreIndex = 0;
                    }

                    for(Player eachPlayer:groupPlayers){
                        if (eachPlay.player.getId() == eachPlayer.getId()){
                            playerScoreHolder[scoreIndex] = eachPlay.score;
                            scoreIndex++;
                            break;
                        }
                    }
                }
                //Log.d("V1", "scoreIndex = " + scoreIndex);
                if (scoreIndex == groupPlayers.size()) {
                    //calculate the last winner
                    int max = playerScoreHolder[0];

                    for (int i = 0; i < playerScoreHolder.length; i++) {
                        if (playerScoreHolder[i] > max) {
                            max = playerScoreHolder[i];
                        }
                    }

                    for (int x = 0; x < playerScoreHolder.length; x++) {
                        int arrayBounds = 2 + (x * groupPlayers.size());
                        if (playerScoreHolder[x] == highScore && playerScoreHolder[x] != 0) {
                            if (output[(arrayBounds)] == null) {
                                output[(arrayBounds)] = (long) 1;
                            } else {
                                output[arrayBounds] = output[arrayBounds] + (long) 1;
                            }
                        }
                        if (playerScoreHolder[x] >= max) {
                            if (output[(arrayBounds + 1)] == null) {
                                output[(arrayBounds + 1)] = (long) 1;
                            } else {
                                output[(arrayBounds + 1)] = output[(arrayBounds + 1)] + (long) 1;
                            }
                        }
                    }
                }else{
                    filteredPlays++;
                    Game getGame = GamesPerPlay.getBaseGame(playHolder.play);
                    if (!filteredGames.contains(getGame.gameName)) {
                        boolean uniqueFlag;
                        for (Player eachPlayer : groupPlayers) {
                            if (Player.hasPlayerPlayedGame(eachPlayer, getGame) == false) {
                                uniqueFilter++;
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return output;
        }

        @Override
        protected void onPostExecute ( final Long[] result){
            long totalPlays = (result[0] - (filteredPlays/groupPlayers.size()));
            long totalUnique = (result[1] - (uniqueFilter));
            addStat("Total Plays: ", totalPlays + "");
            addStat("Unique Games: ", totalUnique + "");
                for(int x = 0; x < groupPlayers.size(); x++) {
                int arrayBounds = 2 + (x * groupPlayers.size());
                addStat(groupPlayers.get(x).playerName + " Total Wins:", result[arrayBounds]+"");
                addStat(groupPlayers.get(x).playerName + " Total Wins Percentage:", ((int) (result[arrayBounds] * 100.0 / totalPlays + 0.5)) + "%");
                addStat(groupPlayers.get(x).playerName + " Asterisk Wins:", result[arrayBounds+1]+"");
                addStat(groupPlayers.get(x).playerName + " Asterisk Wins Percentage:", ((int) (result[arrayBounds+1] * 100.0 / totalPlays + 0.5)) + "%");
            }
            mydialog.dismiss();
            }
    }

    private void addStat(String statType, String statValue) {
        // Instantiate a new "row" view.
        final ViewGroup newView = (ViewGroup) LayoutInflater.from(mActivity).inflate(
                R.layout.stats_viewstat_item, mContainerView_Players, false);

        TextView statTypeView = (TextView) newView.findViewById(R.id.statType);
        TextView statValueView = (TextView) newView.findViewById(R.id.statValue);
        statTypeView.setText(statType);
        statValueView.setText(statValue);
        statTypeView.setTextSize(16);
        statValueView.setTextSize(16);
        mContainerView_Players.addView(newView);
    }
}
