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
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerPlay;
import com.lastsoft.plog.db.TenByTen;
import com.lastsoft.plog.db.TenByTen_Stats;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatsFragment_TenByTen extends Fragment {

    List<Player> groupPlayers;
    View statsView;
    private ViewGroup mContainerView_Players;


    // TODO: Rename and change types and number of parameters
    public static StatsFragment_TenByTen newInstance() {
        StatsFragment_TenByTen fragment = new StatsFragment_TenByTen();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public StatsFragment_TenByTen() {
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

    public class LoadStatsTask extends AsyncTask<Long, Void, Integer[]> {

        ArrayList<String> filteredGames;
        Context theContext;
        int filteredPlays = 0;
        int uniqueFilter = 0;
        boolean loserFlag = true;
        boolean sharedFlag = true;
        int sharedCounter = 0;
        int loserCounter = 0;


        private final ProgressDialog mydialog = new ProgressDialog(mActivity);

        public LoadStatsTask(Context context) {
            this.theContext = context;
        }

        List<TenByTen> gamesToCheck;

        // can use UI thread here
        @Override
        protected void onPreExecute() {
            gamesToCheck = TenByTen.tenByTens_Group(GameGroup.findById(GameGroup.class, (long) 1));
            mydialog.setMessage(getString(R.string.calculating));
            mydialog.setCancelable(false);
            try{
                mydialog.show();
            }catch (Exception e){}
        }

        // automatically done on worker thread (separate from UI thread)
        @Override
        protected Integer[] doInBackground(final Long... args) {
            Integer[] output = new Integer[gamesToCheck.size()];
            DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
            try {
                int gameCounter = 0;
                for (TenByTen gameToCheck : gamesToCheck) {
                    List<TenByTen_Stats> tenByTenOut = TenByTen_Stats.getUniquePlays_GameGroup(gameToCheck.game.gameName, GameGroup.findById(GameGroup.class, (long) 1));
                    int scoreIndex = 0;
                    int gamePlays = 0;
                    long playCounter = -1;
                    TenByTen_Stats currentStat = null;
                    for (TenByTen_Stats eachStat : tenByTenOut) {
                        if (playCounter == -1) {
                            //first time in
                            //set play counter to current play
                            playCounter = eachStat.play.getId();
                            currentStat = eachStat;
                        } else if (eachStat.play.getId() != playCounter) {
                            //output
                            if (scoreIndex == groupPlayers.size()) {
                                //this means we both played ths game, so it's part of our TenByTen, so log that shit
                                String output_date = outputFormatter.format(currentStat.playDate); // Output : 01/20/2012
                                if (output_date.endsWith(""+gameToCheck.year)) {
                                    gamePlays++;
                                }
                            }
                            playCounter = eachStat.play.getId();
                            currentStat = eachStat;
                            scoreIndex = 0;
                        }

                        for (Player eachPlayer : groupPlayers) {
                            if (eachStat.player.getId() == eachPlayer.getId()) {
                                scoreIndex++;
                                break;
                            }
                        }
                    }
                    if (scoreIndex == groupPlayers.size()) {
                        //this means we both played ths game, so it's part of our TenByTen, so log that shit
                        String output_date = outputFormatter.format(currentStat.playDate); // Output : 01/20/2012
                        if (output_date.endsWith(""+gameToCheck.year)) {
                            gamePlays++;
                        }
                    }
                    output[gameCounter] = gamePlays;
                    gameCounter++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return output;
        }

        @Override
        protected void onPostExecute ( final Integer[] result){
            int totalPlayed = 0;
            for(int x = 0; x < result.length; x++) {
                //addStat(gamesToCheck.get(x).game.gameName + " Plays:", result[x]+"");
                addStat(gamesToCheck.get(x).game.gameName + ":", result[x]+"");
                totalPlayed = totalPlayed + result[x];
            }
            addStat("Percent Completed:", ((int) (totalPlayed * 100.0 / 100 + 0.5)) + "%");
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
