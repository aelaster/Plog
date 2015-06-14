package com.lastsoft.plog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GameGroup;
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
                long uniquePlays = Game.getUniqueGames_GameGroup(GameGroup.findById(GameGroup.class, theGroup)).size();
                output[0] = ((long)groupTotalPlays.size()/(long)groupPlayers.size());
                output[1] = uniquePlays;

                for (int i = 0; i < groupPlayers.size(); i++){
                    int arrayBounds = 2 + (i * 2);
                    //regular wins
                    int regularWins = Play.totalWins_GameGroup_Player(GameGroup.findById(GameGroup.class, theGroup), Player.findById(Player.class, groupPlayers.get(i).getId())).size();
                    output[arrayBounds] = (long)regularWins;
                    //asterisk wins
                    int asteriskWins = Play.totalAsteriskWins_GameGroup_Player(GameGroup.findById(GameGroup.class, theGroup), Player.findById(Player.class, groupPlayers.get(i).getId())).size();
                    output[arrayBounds+1] = (long)(regularWins + asteriskWins);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return output;
        }

        @Override
        protected void onPostExecute ( final Long[] result){
            long totalPlays = result[0];
            long totalUnique = result[1];
            addStat(0, "Total Plays: ", totalPlays + "", "");
            addStat(1, "Unique Games: ", totalUnique + "", "");
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
                addStat(2, groupPlayers.get(x).playerName + " Regular Wins:", result_out+"", groupPlayers.get(x).getId()+"");
                addStat(3, groupPlayers.get(x).playerName + " Asterisk Wins:", (result_out2-result_out)+"", groupPlayers.get(x).getId()+"");
                addStat(4, groupPlayers.get(x).playerName + " Total Wins:", result_out2+"", groupPlayers.get(x).getId()+"");
            }
            //addStat(4, "Shared Wins: ", sharedCounter + "");totalSharedWins
            sharedCounter = Play.totalSharedWins(GameGroup.findById(GameGroup.class, theGroup)).size();
            addStat(5, "Shared Wins: ", sharedCounter + "", "");
            //addStat(5, "Total Losses: ", loserCounter + "");
            loserCounter = Play.totalGroupLosses(GameGroup.findById(GameGroup.class, theGroup)).size();
            addStat(6, "Total Losses: ", loserCounter + "", "");
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
                addStat(-1, groupPlayers.get(x).playerName + " Regular Wins Percentage:", ((int) (result_out * 100.0 / totalPlays + 0.5)) + "%", groupPlayers.get(x).getId()+"");
                addStat(-1, groupPlayers.get(x).playerName + " Asterisk Wins Percentage:", ((int) ((result_out2-result_out) * 100.0 / totalPlays + 0.5)) + "%", groupPlayers.get(x).getId()+"");
                addStat(-1, groupPlayers.get(x).playerName + " Total Wins Percentage:", ((int) (result_out2 * 100.0 / totalPlays + 0.5)) + "%", groupPlayers.get(x).getId()+"");
            }
            addStat(-1, "Shared Wins Percentage: ", ((int) (sharedCounter * 100.0 / totalPlays + 0.5)) + "%", "");
            addStat(-1, "Total Losses Percentage: ", ((int) (loserCounter * 100.0 / totalPlays + 0.5)) + "%", "");
            mydialog.dismiss();
            }
    }

    private void addStat(final int statType, String statHeader, String statValue, final String playerValue) {
        // Instantiate a new "row" view.
        try {
            final ViewGroup newView = (ViewGroup) LayoutInflater.from(mActivity).inflate(
                    R.layout.stats_viewstat_item, mContainerView_Players, false);

            TextView statTypeView = (TextView) newView.findViewById(R.id.statType);
            TextView statValueView = (TextView) newView.findViewById(R.id.statValue);
            statTypeView.setText(statHeader);
            statValueView.setText(statValue);
            statTypeView.setTextSize(16);
            statValueView.setTextSize(16);
            if (statType >= 0){
                newView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (statType) {
                            case 0:
                                //((MainActivity) mActivity).openPlays(games.get(position).gameName, false);
                                ((MainActivity) mActivity).openPlays(gameGroup+"", false, 1);
                                break;
                            case 1:
                                //((MainActivity) mActivity).openPlays(games.get(position).gameName, false);
                                //((MainActivity) mActivity).openPlays(gameGroup+"", false, 1);
                                break;
                            case 2:
                                //((MainActivity) mActivity).openPlays(games.get(position).gameName, false);
                                ((MainActivity) mActivity).openPlays(gameGroup+"$"+playerValue, false, 2);
                                break;
                            case 3:
                                //((MainActivity) mActivity).openPlays(games.get(position).gameName, false);
                                ((MainActivity) mActivity).openPlays(gameGroup+"$"+playerValue, false, 3);
                                break;
                            case 4:
                                //((MainActivity) mActivity).openPlays(games.get(position).gameName, false);
                                ((MainActivity) mActivity).openPlays(gameGroup+"$"+playerValue, false, 4);
                                break;
                            case 5:
                                //((MainActivity) mActivity).openPlays(games.get(position).gameName, false);
                                ((MainActivity) mActivity).openPlays(gameGroup+"", false, 5);
                                break;
                            case 6:
                                //((MainActivity) mActivity).openPlays(games.get(position).gameName, false);
                                ((MainActivity) mActivity).openPlays(gameGroup+"", false, 6);
                                break;
                        }
                    }
                });
            }

            mContainerView_Players.addView(newView);
        }catch (Exception ignored){}
    }
}
