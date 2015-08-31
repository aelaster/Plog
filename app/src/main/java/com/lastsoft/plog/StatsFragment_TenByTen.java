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

import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GameGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatsFragment_TenByTen extends Fragment {

    View statsView;
    private ViewGroup mContainerView_Players;
    long gameGroup;
    int year;
    boolean boot = true;

    public static StatsFragment_TenByTen newInstance(long gameGroup) {
        StatsFragment_TenByTen fragment = new StatsFragment_TenByTen();
        Bundle args = new Bundle();
        args.putLong("gameGroup", gameGroup);
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
            gameGroup = getArguments().getLong("gameGroup");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        statsView = inflater.inflate(R.layout.fragment_statistics, container, false);
        if (gameGroup > 0) {
            mContainerView_Players = (ViewGroup) statsView.findViewById(R.id.container_players);
            //create dropdown




            final ViewGroup newView = (ViewGroup) LayoutInflater.from(mActivity).inflate(
                    R.layout.stats_viewstat_spinner, mContainerView_Players, false);

            TextView statTypeView = (TextView) newView.findViewById(R.id.statType);
            Spinner spinnerValueView = (Spinner) newView.findViewById(R.id.spinnerValue);
            statTypeView.setText(getString(R.string.year_label));
            statTypeView.setTextSize(24);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayList<Integer> theYears = new ArrayList<Integer>();

            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            for (int i = year; i >= 2015; i--){
                theYears.add(i);
            }
            ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(mActivity,R.layout.stats_viewstat_spinner_item, theYears);
            spinnerValueView.setAdapter(adapter);
            spinnerValueView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (!boot) {
                        mContainerView_Players.removeViewsInLayout(1, mContainerView_Players.getChildCount()-1);
                        getTenByTen((Integer) adapterView.getItemAtPosition(i));
                    }else{
                        boot = false;
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
        List<Game> tbts = Game.totalTenByTen_GameGroup(GameGroup.findById(GameGroup.class, gameGroup), year);
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
                        ((MainActivity) mActivity).openPlays(gameGroup + "^" + gameValue + "^" + theYear, false, 7, mActivity.getString(R.string.title_statistics));
                    }
                });
            }else{
                newView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) mActivity).openPlays(gameGroup + "^" + theYear, false, 10, mActivity.getString(R.string.title_statistics));

                    }
                });
            }

            mContainerView_Players.addView(newView);
        }catch (Exception ignored){}
    }
}
