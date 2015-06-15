package com.lastsoft.plog;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GameGroup;

import java.util.Calendar;
import java.util.List;

public class StatsFragment_TenByTen extends Fragment {

    View statsView;
    private ViewGroup mContainerView_Players;
    long gameGroup;
    int year;


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
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            List<Game> tbts = Game.totalTenByTen_GameGroup(GameGroup.findById(GameGroup.class, gameGroup), year);
            int tbtCounts = 0;
            for (Game tbt : tbts){
                if (tbt.tbtCount <= 10) { //count only uses the value if it's less than or equal to 10
                    tbtCounts = tbtCounts + tbt.tbtCount;
                }else{//otherwise, it adds 10
                    tbtCounts = tbtCounts + 10;
                }
                addStat(tbt.gameName + ":", tbt.tbtCount+"", tbt.getId()+"");
            }
            addStat("Percent Completed:", ((int) (tbtCounts * 100.0 / 100 + 0.5)) + "%", "");
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

    private void addStat(String statType, String statValue, final String gameValue) {
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
                        ((MainActivity) mActivity).openPlays(gameGroup + "$" + gameValue + "$" + year, false, 7);
                    }
                });
            }

            mContainerView_Players.addView(newView);
        }catch (Exception ignored){}
    }
}
