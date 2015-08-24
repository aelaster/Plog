package com.lastsoft.plog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GameGroup;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerPlay;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        if (savedInstanceState == null) {
            statsView = inflater.inflate(R.layout.fragment_statistics, container, false);
            mContainerView_Players = (ViewGroup) statsView.findViewById(R.id.container_players);
            if (gameGroup >= 0) {
                if (gameGroup > 0) {
                    groupPlayers = GameGroup.getGroupPlayers(GameGroup.findById(GameGroup.class, gameGroup));
                } else {
                    //errybody
                    groupPlayers = Player.listPlayersAZ();
                }
                LoadStatsTask initStats = new LoadStatsTask(mActivity, gameGroup);
                try {
                    initStats.execute();
                } catch (Exception ignored) {

                }
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


    public class WinStats {
        public Player player;
        public int regularWins;
        public int asteriskWins;
        public long totalPlays;
        public long totalUnique;
        public long totalUnplayed;

        public WinStats(Player player, int regularWins, int asteriskWins, long totalPlays, long totalUnique, long totalUnplayed) {
            this.player = player;
            this.regularWins = regularWins;
            this.asteriskWins = asteriskWins;
            this.totalPlays = totalPlays;
            this.totalUnique = totalUnique;
            this.totalUnplayed = totalUnplayed;
        }
    }

    public class LoadStatsTask extends AsyncTask<Long, Void, ArrayList<WinStats>> {

        Context theContext;
        int sharedCounter = 0;
        int loserCounter = 0;
        long theGroup;
        ArrayList<WinStats> theStats;


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
        protected ArrayList<WinStats> doInBackground(final Long... args) {

            ArrayList<WinStats> output = new ArrayList<>();
            //Long[] output = new Long[outputBounds];
            //times 2 because each player needs regular and asterisk totals
            //plus two because we put total and unique plays on the top
            long totalPlays, totalUnique, totalUnplayed;
            if (theGroup == 0){
                totalPlays = ((long) Play.listPlaysNewOld(0).size());
                totalUnique = Game.getUniqueGames(0).size();
                totalUnplayed = Game.getUnplayedGames(0, false).size();
                try {
                    for (int i = 0; i < groupPlayers.size(); i++){
                        Player thisPlayer = Player.findById(Player.class, groupPlayers.get(i).getId());
                        //regular wins
                        int regularWins = Play.totalWins_Player(thisPlayer, 0).size();
                        //asterisk wins
                        int asteriskWins = Play.totalAsteriskWins_Player(thisPlayer, 0).size();
                        //output[arrayBounds+1] = (long)(regularWins + asteriskWins);
                        output.add(new WinStats(thisPlayer, regularWins, asteriskWins, totalPlays, totalUnique, totalUnplayed));
                    }
                    Collections.sort(output, new Comparator<WinStats>() {
                        public int compare(WinStats left, WinStats right) {
                            return Integer.compare(right.regularWins, left.regularWins); // The order depends on the direction of sorting.
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                totalPlays = ((long) PlayersPerPlay.totalPlays_GameGroup(GameGroup.findById(GameGroup.class, theGroup)).size() / (long) groupPlayers.size());
                totalUnique = Game.getUniqueGames_GameGroup(GameGroup.findById(GameGroup.class, theGroup), 0).size();
                totalUnplayed = Game.getUnplayedGames_GameGroup(GameGroup.findById(GameGroup.class, theGroup), 0, false).size();
                try {
                    for (int i = 0; i < groupPlayers.size(); i++){
                        GameGroup thisGroup = GameGroup.findById(GameGroup.class, theGroup);
                        Player thisPlayer = Player.findById(Player.class, groupPlayers.get(i).getId());
                        //regular wins
                        int regularWins = Play.totalWins_GameGroup_Player(thisGroup, thisPlayer, 0).size();
                        //asterisk wins
                        int asteriskWins = Play.totalAsteriskWins_GameGroup_Player(thisGroup, thisPlayer, 0).size();
                        //output[arrayBounds+1] = (long)(regularWins + asteriskWins);
                        output.add(new WinStats(thisPlayer, regularWins, asteriskWins, totalPlays, totalUnique, totalUnplayed));
                    }
                    Collections.sort(output, new Comparator<WinStats>() {
                        public int compare(WinStats left, WinStats right) {
                            return Integer.compare(right.regularWins, left.regularWins); // The order depends on the direction of sorting.
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return output;
        }

        @Override
        protected void onPostExecute ( final ArrayList<WinStats> result){
            if (result.size() > 0) {
                addStat(0, getString(R.string.stats_total_plays), result.get(0).totalPlays + "", "");
                addStat(1, getString(R.string.stats_unique_plays), result.get(0).totalUnique + "", "");
                addStat(8, getString(R.string.stats_unplayed_games), result.get(0).totalUnplayed + "", "");
                for (int x = 0; x < result.size(); x++) {

                    if (theGroup == 0){
                        addStat(4, result.get(x).player.playerName + " " + getString(R.string.stats_regular_wins), (result.get(x).regularWins) + "", result.get(x).player.getId() + "");
                        //addStat(2, result.get(x).player.playerName + " " + getString(R.string.stats_regular_wins), result.get(x).regularWins + "", result.get(x).player.getId() + "");
                        //addStat(3, result.get(x).player.playerName + " " + getString(R.string.stats_asterisk_wins), result.get(x).asteriskWins + "", result.get(x).player.getId() + "");
                    }else {
                        addStat(4, result.get(x).player.playerName + " " + getString(R.string.stats_total_wins), (result.get(x).asteriskWins + result.get(x).regularWins) + "", result.get(x).player.getId() + "");
                        addPieChart(result.get(x).player.playerName, result.get(x).regularWins, result.get(x).asteriskWins, result.get(x).player.getId() + "");
                    }
                }
                if (theGroup == 0){
                    //sharedCounter = Play.totalSharedWins().size();
                    //loserCounter = Play.totalGroupLosses().size();
                }else {
                    sharedCounter = Play.totalSharedWins(GameGroup.findById(GameGroup.class, theGroup), 0).size();
                    loserCounter = Play.totalGroupLosses(GameGroup.findById(GameGroup.class, theGroup), 0).size();

                    addStat(5, getString(R.string.stats_shared_wins), sharedCounter + "", "");
                    addStat(6, getString(R.string.stats_total_losses), loserCounter + "", "");
                }
                for (int x = 0; x < result.size(); x++) {
                    if (theGroup == 0) {
                        addStat(7, result.get(x).player.playerName + " " + getString(R.string.stats_regular_wins) + getString(R.string.percentage), roundTwoDecimals((((result.get(x).regularWins) * 100.0 / result.get(0).totalPlays))) + "%", result.get(x).player.getId() + "");
                    } else {
                        addStat(-1, result.get(x).player.playerName + " " + getString(R.string.stats_total_wins) + getString(R.string.percentage), roundTwoDecimals((((result.get(x).asteriskWins + result.get(x).regularWins) * 100.0 / result.get(0).totalPlays))) + "%", result.get(x).player.getId() + "");
                        addStat(7, result.get(x).player.playerName + " " + getString(R.string.stats_regular_wins) + getString(R.string.percentage), roundTwoDecimals(((result.get(x).regularWins * 100.0 / result.get(0).totalPlays))) + "%", result.get(x).player.getId() + "");
                        if (result.get(x).asteriskWins > 0) {
                            addStat(-1, result.get(x).player.playerName + " " + getString(R.string.stats_asterisk_wins) + getString(R.string.percentage), roundTwoDecimals((((result.get(x).asteriskWins) * 100.0 / result.get(0).totalPlays))) + "%", result.get(x).player.getId() + "");
                        }
                    }


                    if (sharedCounter > 0) {
                        addStat(-1, getString(R.string.stats_shared_wins) + getString(R.string.percentage), roundTwoDecimals(((sharedCounter * 100.0 / result.get(0).totalPlays))) + "%", "");
                    }
                    if (loserCounter > 0) {
                        addStat(-1, getString(R.string.stats_total_losses) + getString(R.string.percentage), roundTwoDecimals(((loserCounter * 100.0 / result.get(0).totalPlays))) + "%", "");
                    }
                }
            }
            mydialog.dismiss();
        }
    }

    double roundTwoDecimals(double d)
    {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }

    private void addPieChart(String centerLabel, long regular, long asterisk, final String playerValue ){
        try {
            final ViewGroup newView = (ViewGroup) LayoutInflater.from(mActivity).inflate(
                    R.layout.stats_viewpie_item, mContainerView_Players, false);
            PieChart mChart = (PieChart) newView.findViewById(R.id.mmmPie);

            mChart.setUsePercentValues(false);
            mChart.setDescription("");
            mChart.setDragDecelerationFrictionCoef(0.95f);
            mChart.setDrawSliceText(false);
            mChart.setDrawHoleEnabled(true);
            mChart.setHoleColorTransparent(true);
            mChart.setTransparentCircleColor(Color.WHITE);
            mChart.setHoleRadius(58f);
            mChart.setTransparentCircleRadius(61f);
            mChart.setDrawCenterText(true);
            mChart.setRotationAngle(0);
            mChart.setRotationEnabled(false);
            mChart.setCenterText(centerLabel);
            mChart.setHardwareAccelerationEnabled(true);

            mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                    switch (e.getXIndex()) {
                        case 0:
                            ((MainActivity) mActivity).openPlays(gameGroup + "^" + playerValue, false, 2);
                            break;
                        case 1:
                            ((MainActivity) mActivity).openPlays(gameGroup + "^" + playerValue, false, 3);
                            break;
                    }
                }

                @Override
                public void onNothingSelected() {

                }
            });



            ArrayList<Entry> percent = new ArrayList<Entry>();
            percent.add(0, new Entry((long) regular, 0));
            percent.add(1, new Entry((long) asterisk, 1));
            ArrayList<String> types = new ArrayList<String>();
            types.add(0, getString(R.string.stats_regular_wins));
            types.add(1, getString(R.string.stats_asterisk_wins));

            setData(mChart, percent, types);

            Legend l = mChart.getLegend();
            l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);

            mChart.animateY(1500, Easing.EasingOption.EaseInOutQuad);

            mContainerView_Players.addView(newView);
        }catch (Exception ignored){}

    }
    private void setData(PieChart mChart, ArrayList<Entry> percent, ArrayList<String> types) {



        PieDataSet dataSet = new PieDataSet(percent, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(0f);


        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(getResources().getColor(R.color.pie_chart_regular));
        colors.add(getResources().getColor(R.color.pie_chart_asterisk));
        dataSet.setColors(colors);

        PieData data = new PieData(types, dataSet);
        data.setValueFormatter(new MyValueFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    public class MyValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value) {
            return mFormat.format(value);
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
                                ((MainActivity) mActivity).openGames(gameGroup+"", false, 1);
                                break;
                            case 2:
                                //((MainActivity) mActivity).openPlays(games.get(position).gameName, false);
                                ((MainActivity) mActivity).openPlays(gameGroup+"^"+playerValue, false, 2);
                                break;
                            case 3:
                                //((MainActivity) mActivity).openPlays(games.get(position).gameName, false);
                                ((MainActivity) mActivity).openPlays(gameGroup+"^"+playerValue, false, 3);
                                break;
                            case 4:
                                //((MainActivity) mActivity).openPlays(games.get(position).gameName, false);
                                ((MainActivity) mActivity).openPlays(gameGroup+"^"+playerValue, false, 4);
                                break;
                            case 5:
                                //((MainActivity) mActivity).openPlays(games.get(position).gameName, false);
                                ((MainActivity) mActivity).openPlays(gameGroup+"", false, 5);
                                break;
                            case 6:
                                //((MainActivity) mActivity).openPlays(games.get(position).gameName, false);
                                ((MainActivity) mActivity).openPlays(gameGroup+"", false, 6);
                                break;
                            case 7:
                                //regular wins percentage
                                //just using this to get a list of total plays for a player
                                //using it because it's part of the "Everyone" list too
                                ((MainActivity) mActivity).openGames(gameGroup+"^"+playerValue, false, 8);
                                break;
                            case 8:
                                ((MainActivity) mActivity).openGames(gameGroup + "", false, 4);
                                break;
                        }
                    }
                });
            }

            mContainerView_Players.addView(newView);
        }catch (Exception ignored){}
    }
}
