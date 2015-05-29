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

import com.lastsoft.plog.db.GameGroup;
import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerPlay;

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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StatsFragment.
     */
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

    View statsView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        statsView = inflater.inflate(R.layout.fragment_statistics, container, false);
        LoadStatsTask initStats = new LoadStatsTask(mActivity);
        try {
            initStats.execute();
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

    public int totalWins(Player player){
        long totalPlays = Play.count(Play.class, null, null);
        int totalWins = 0;
        List<PlayersPerPlay> playerTotalPlays = PlayersPerPlay.totalPlays(player);
        for(PlayersPerPlay eachPlay:playerTotalPlays){
            int highScore = PlayersPerPlay.getHighScore(eachPlay.play);
            if (eachPlay.score == highScore && highScore != 0){
                totalWins++;
            }
        }
        return totalWins;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mActivity = null;
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
        public void onFragmentInteraction(String string);
    }

    public class LoadStatsTask extends AsyncTask<String, Void, Long[]> {

        Context theContext;
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
        protected Long[] doInBackground(final String... args) {

            Long[] output = new Long[6];
            try {


                long totalPlays = Play.count(Play.class, null, null);
                long uniquePlays  = GamesPerPlay.getUniquePlays();


                output[0] = totalPlays;
                output[1] = uniquePlays;


                long adam_regularTotalWins = 0;
                long adam_asteriskTotalWins = 0;
                long sher_regularTotalWins = 0;
                long sher_asteriskTotalWins = 0;

                /*List<PlayersPerPlay> playerTotalPlays = PlayersPerPlay.totalPlays(Player.findById(Player.class, (long) 1));
                for(PlayersPerPlay eachPlay:playerTotalPlays){
                    //int highScore = PlayersPerPlay.getHighScore(eachPlay.play);
                    if (eachPlay.score == eachPlay.playHighScore && eachPlay.score != 0){
                        regularTotalWins++;
                    }
                    int sherScore = PlayersPerPlay.getScoreByPlayer(Player.findById(Player.class, (long)2), eachPlay.play);
                    if (eachPlay.score >= sherScore && eachPlay.score != 0){
                        asteriskTotalWins++;
                    }
                }*/

                List<PlayersPerPlay> groupTotalPlays = PlayersPerPlay.totalPlays_GameGroup(GameGroup.findById(GameGroup.class, (long) 1));
                long playCounter = -1;
                int highScore = 0;
                int adamScore = 0;
                int sherScore = 0;
                for(PlayersPerPlay eachPlay:groupTotalPlays){
                    if (playCounter == -1){
                        //first time in
                        //set play counter to current play
                        playCounter = eachPlay.play.getId();
                        //set high score for play
                        highScore = eachPlay.playHighScore;
                    }else if (eachPlay.play.getId() != playCounter){
                        //we've moved on to the next play
                        //calculate winner from past playCounter play
                        if (adamScore == highScore && adamScore != 0){
                            adam_regularTotalWins++;
                        }
                        if (sherScore == highScore && sherScore != 0){
                            sher_regularTotalWins++;
                        }
                        if (adamScore >= sherScore && adamScore != 0) {
                            adam_asteriskTotalWins++;
                        }
                        if (sherScore >= adamScore && sherScore != 0){
                            sher_asteriskTotalWins++;
                        }
                        //set playCounter to new play
                        playCounter = eachPlay.play.getId();
                        //set high score for new play
                        highScore = eachPlay.playHighScore;
                        //zero out scores
                        adamScore = 0;
                        sherScore = 0;
                    }
                    if (eachPlay.player.getId() == 1){
                        adamScore = eachPlay.score;
                    }else if (eachPlay.player.getId() == 2){
                        sherScore = eachPlay.score;
                    }
                }

                //calculate the last winner
                if (adamScore == highScore && adamScore != 0){
                    adam_regularTotalWins++;
                }
                if (sherScore == highScore && sherScore != 0){
                    sher_regularTotalWins++;
                }
                if (adamScore >= sherScore && adamScore != 0) {
                    adam_asteriskTotalWins++;
                }
                if (sherScore >= adamScore && sherScore != 0){
                    sher_asteriskTotalWins++;
                }

                output[2] = adam_regularTotalWins;
                output[3] = adam_asteriskTotalWins;
                output[4] = sher_regularTotalWins;
                output[5] = sher_asteriskTotalWins;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return output;
        }

        @Override
        protected void onPostExecute(final Long[] result) {
            TextView totalPlaysView = (TextView) statsView.findViewById(R.id.total_games_played);
            TextView uniquePlaysView = (TextView) statsView.findViewById(R.id.unique_games_played);
            TextView totalWins = (TextView) statsView.findViewById(R.id.adam_total_wins);
            TextView percentTotalWins = (TextView) statsView.findViewById(R.id.adam_total_wins_percentage);
            TextView asteriskWins = (TextView) statsView.findViewById(R.id.adam_asterisk_wins);
            TextView percentAsteriskWins = (TextView) statsView.findViewById(R.id.adam_asterisk_wins_percentage);
            TextView stotalWins = (TextView) statsView.findViewById(R.id.sher_total_wins);
            TextView percentStotalWins = (TextView) statsView.findViewById(R.id.sher_total_wins_percentage);
            TextView sasteriskWins = (TextView) statsView.findViewById(R.id.sher_asterisk_wins);
            TextView percentSasteriskWins = (TextView) statsView.findViewById(R.id.sher_asterisk_wins_percentage);

            totalPlaysView.setText("Total Plays: " + result[0]);
            uniquePlaysView.setText("Unique Games: " + result[1]);
            totalWins.setText("Adam Total Wins: " + result[2]);
            percentTotalWins.setText("Adam Total Wins Percentage: " + ((int)(result[2] * 100.0 / result[0] + 0.5)) + "%");
            asteriskWins.setText("Adam Asterisk Wins:" + result[3]);
            percentAsteriskWins.setText("Adam Asterisk Wins Percentage: " + ((int)(result[3] * 100.0 / result[0] + 0.5)) + "%");
            stotalWins.setText("Sheralyn Total Wins: " + result[4]);
            percentStotalWins.setText("Sheralyn Total Wins Percentage: " + ((int)(result[4] * 100.0 / result[0] + 0.5)) + "%");
            sasteriskWins.setText("Sheralyn Asterisk Wins:" + result[5]);
            percentSasteriskWins.setText("Sheralyn Asterisk Wins Percentage: " + ((int)(result[5] * 100.0 / result[0] + 0.5)) + "%");

            mydialog.dismiss();
        }
    }

}
