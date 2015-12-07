package com.lastsoft.plog;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lastsoft.plog.db.GameGroup;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.TenByTen;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatsFragment_TenByTen extends Fragment {

    View statsView;
    private ViewGroup mContainerView_Players;
    GameGroup gameGroup;
    int year;
    boolean boot = true;

    public static StatsFragment_TenByTen newInstance(String gameGroup) {
        StatsFragment_TenByTen fragment = new StatsFragment_TenByTen();
        Bundle args = new Bundle();
        args.putString("gameGroup", gameGroup);
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
            gameGroup = new Gson().fromJson(getArguments().getString("gameGroup"), GameGroup.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        statsView = inflater.inflate(R.layout.fragment_statistics, container, false);
        if (gameGroup.getId() > 0) {
            mContainerView_Players = (ViewGroup) statsView.findViewById(R.id.container_players);
            //create dropdown




            final ViewGroup newView = (ViewGroup) LayoutInflater.from(mActivity).inflate(
                    R.layout.stats_viewstat_spinner, mContainerView_Players, false);

            TextView statTypeView = (TextView) newView.findViewById(R.id.statType);
            final Spinner spinnerValueView = (Spinner) newView.findViewById(R.id.spinnerValue);
            statTypeView.setText(getString(R.string.year_label));
            statTypeView.setTextSize(24);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayList<Integer> theYears = new ArrayList<Integer>();

            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            for (int i = year+1; i >= 2015; i--){
                theYears.add(i);
            }
            final ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(mActivity,R.layout.stats_viewstat_spinner_item, theYears);
            spinnerValueView.setAdapter(adapter);
            spinnerValueView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (!boot) {
                        mContainerView_Players.removeViewsInLayout(1, mContainerView_Players.getChildCount()-1);
                        getTenByTen((Integer) adapterView.getItemAtPosition(i));
                    }else{
                        boot = false;
                        int spinnerPosition = adapter.getPosition(year);
                        spinnerValueView.setSelection(spinnerPosition);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            mContainerView_Players.addView(newView);

            getTenByTen(year);
        }
        return statsView;
    }

    private void getTenByTen(int year){
        List<TenByTen> tens = TenByTen.tenByTens_Group(gameGroup, year);
        List<TBTInfo> info = new ArrayList<TBTInfo>();
        int tbtCounts = 0;
        for (TenByTen ten : tens){
            //gameTenByTen_GameGroup(GameGroup group, Game game, int year, int sortType){
            List<Play> plays = Play.gameTenByTen_GameGroup(gameGroup, ten.game, year, 0);
            if (plays.size() <= 10) { //count only uses the value if it's less than or equal to 10
                tbtCounts = tbtCounts + plays.size();
            }else{//otherwise, it adds 10
                tbtCounts = tbtCounts + 10;
            }
            info.add(new TBTInfo(year, ten.game.getId(), plays.size(), ten.game.gameName));
            //addStat(ten.game.gameName, plays.size()+"", ten.game.getId()+"", year);
        }
        Collections.sort(info, new Comparator<TBTInfo>() {
            public int compare(TBTInfo left, TBTInfo right) {
                return Integer.compare(right.playsCount, left.playsCount); // The order depends on the direction of sorting.
            }
        });
        for (TBTInfo ten2 : info){
            //addStat(ten.game.gameName, plays.size()+"", ten.game.getId()+"", year);
            addStat(ten2.gameName, ""+ten2.playsCount, ten2.gameID+"", ten2.year);
        }
        addStat(getString(R.string.percent_completed), ((int) (tbtCounts * 100.0 / 100 + 0.5)) + "%", "", year);
        /*
        List<Game> tbts = Game.totalTenByTen_GameGroup(gameGroup, year);
        int tbtCounts = 0;
        for (Game tbt : tbts){
            if (tbt.tbtCount <= 10) { //count only uses the value if it's less than or equal to 10
                tbtCounts = tbtCounts + tbt.tbtCount;
            }else{//otherwise, it adds 10
                tbtCounts = tbtCounts + 10;
            }
            addStat(tbt.gameName, tbt.tbtCount+"", tbt.getId()+"", year);
        }
        addStat(getString(R.string.percent_completed), ((int) (tbtCounts * 100.0 / 100 + 0.5)) + "%", "", year);
        */
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

    private void addStat(String statType, String statValue, final String gameValue, final int theYear) {
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
            if (!gameValue.equals("")) {
                newView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) mActivity).openPlays(gameGroup.getId() + "^" + gameValue + "^" + theYear, false, 7, mActivity.getString(R.string.title_statistics), year);
                    }
                });
            }else{
                newView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) mActivity).openPlays(gameGroup.getId() + "^" + theYear, false, 10, mActivity.getString(R.string.title_statistics), year);

                    }
                });
            }

            mContainerView_Players.addView(newView);
        }catch (Exception ignored){}
    }

    //addStat(ten.game.gameName, plays.size()+"", ten.game.getId()+"", year);
    public class TBTInfo {
        public int year;
        public long gameID;
        public int playsCount;
        public String gameName;

        public TBTInfo(int year, long gameID, int playsCount, String gameName) {
            this.year = year;
            this.gameID = gameID;
            this.playsCount = playsCount;
            this.gameName = gameName;
        }
    }
}
