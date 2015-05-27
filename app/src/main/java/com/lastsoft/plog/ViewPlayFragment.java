package com.lastsoft.plog;

import android.animation.Animator;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerPlay;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewPlayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ViewPlayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewPlayFragment extends Fragment {
    // TODO: Rename and change types of parameters
    private long playID;
    String imageTransID;
    String nameTransID;
    String dateTransID;
    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration;
    private OnFragmentInteractionListener mListener;

    // TODO: Rename and change types and number of parameters
    public static ViewPlayFragment newInstance(long playID, String transID, String transID2, String transID3) {
        ViewPlayFragment fragment = new ViewPlayFragment();
        Bundle args = new Bundle();
        args.putLong("playID", playID);
        args.putString("imageTransID", transID);
        args.putString("nameTransID", transID2);
        args.putString("dateTransID", transID3);
        fragment.setArguments(args);
        return fragment;
    }

    public ViewPlayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playID = getArguments().getLong("playID");
            imageTransID = getArguments().getString("imageTransID");
            nameTransID = getArguments().getString("nameTransID");
            dateTransID = getArguments().getString("dateTransID");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View viewPlayLayout = inflater.inflate(R.layout.fragment_view_play, container, false);
        viewPlayLayout.setBackgroundColor(getResources().getColor(R.color.cardview_initial_background));
        LinearLayout linLayout = (LinearLayout) viewPlayLayout.findViewById(R.id.linearLayout);

        Play thisPlay = Play.findById(Play.class, playID);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .considerExifParams(true)
                .build();

        ImageView playImage = (ImageView) viewPlayLayout.findViewById(R.id.imageView1);
        if (!thisPlay.playPhoto.equals("")){
            playImage.setImageDrawable(Drawable.createFromPath(thisPlay.playPhoto.substring(7, thisPlay.playPhoto.length())));
            playImage.setTransitionName(imageTransID);
        }

        //ImageLoader.getInstance().displayImage(thisPlay.playPhoto, playImage, options);

        //get Play object


        //get game for this play
        Game thisBaseGame = GamesPerPlay.getBaseGame(thisPlay);

        //add a textview with the game name
        TextView gameName = (TextView) viewPlayLayout.findViewById(R.id.gameName);
        gameName.setText(thisBaseGame.gameName);
        gameName.setTransitionName(nameTransID);

        //expansions
        List<GamesPerPlay> expansions = GamesPerPlay.getExpansions(thisPlay);
        for(GamesPerPlay expansion:expansions){
            TextView showPlayer = new TextView(getActivity());
            showPlayer.setText(expansion.game.gameName);
            linLayout.addView(showPlayer);
        }

        //date
        TextView playDate = (TextView) viewPlayLayout.findViewById(R.id.gameDate);
        DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
        String output = outputFormatter.format(thisPlay.playDate); // Output : 01/20/2010
        playDate.setText(output);
        playDate.setTransitionName(dateTransID);

        //players
        List<PlayersPerPlay> players = PlayersPerPlay.getPlayers(thisPlay);
        List<PlayersPerPlay> winners = PlayersPerPlay.getWinners(thisPlay);

        for(PlayersPerPlay player:players){
            Player thisPlayer = player.player;
            TextView showPlayer = new TextView(getActivity());
            showPlayer.setText(thisPlayer.playerName + " - Score=" + player.score);
            linLayout.addView(showPlayer);
        }

        //output winner
        if (winners.size() > 1){
            for(PlayersPerPlay winner:winners){
                Player thisPlayer = winner.player;
                TextView showWinners = new TextView(getActivity());
                showWinners.setText("Winners = " + winner.player.playerName);
                linLayout.addView(showWinners);
            }
        }else {
            TextView showWinner = new TextView(getActivity());
            showWinner.setText("Winner = " + winners.get(0).player.playerName);
            linLayout.addView(showWinner);
        }

        //output note
        TextView showNote = new TextView(getActivity());
        showNote.setText("Note = " + thisPlay.playNotes);
        linLayout.addView(showNote);

        return viewPlayLayout;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String string) {
        if (mListener != null) {
            mListener.onFragmentInteraction(string);
        }
    }


    public void setImageId(String id){
        id = imageTransID;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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

}
