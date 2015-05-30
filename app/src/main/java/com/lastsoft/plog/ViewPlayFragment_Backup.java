package com.lastsoft.plog;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewPlayFragment_Backup.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ViewPlayFragment_Backup#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewPlayFragment_Backup extends Fragment {
    // TODO: Rename and change types of parameters
    private long playID;
    String imageTransID;
    String nameTransID;
    String dateTransID;
    private OnFragmentInteractionListener mListener;
    private ViewGroup mContainerView_Players;
    private ViewGroup mContainerView_Expansions;
    ImageView playImage;

    // TODO: Rename and change types and number of parameters
    public static ViewPlayFragment_Backup newInstance(long playID, String transID, String transID2, String transID3) {
        ViewPlayFragment_Backup fragment = new ViewPlayFragment_Backup();
        Bundle args = new Bundle();
        args.putLong("playID", playID);
        args.putString("imageTransID", transID);
        args.putString("nameTransID", transID2);
        args.putString("dateTransID", transID3);
        fragment.setArguments(args);
        return fragment;
    }

    public ViewPlayFragment_Backup() {
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
        setHasOptionsMenu(true);
    }

    View viewPlayLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewPlayLayout = inflater.inflate(R.layout.fragment_view_play, container, false);
        viewPlayLayout.setBackgroundColor(getResources().getColor(R.color.cardview_initial_background));
        LinearLayout linLayout = (LinearLayout) viewPlayLayout.findViewById(R.id.linearLayout);

        Play thisPlay = Play.findById(Play.class, playID);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .considerExifParams(true)
                .build();

        mContainerView_Players = (ViewGroup) viewPlayLayout.findViewById(R.id.container_players);
        mContainerView_Expansions = (ViewGroup) viewPlayLayout.findViewById(R.id.container_expansions);

        playImage = (ImageView) viewPlayLayout.findViewById(R.id.imageView1);
        if (thisPlay.playPhoto != null && !thisPlay.playPhoto.equals("")){
            //playImage.setImageDrawable(Drawable.createFromPath(thisPlay.playPhoto.substring(7, thisPlay.playPhoto.length())));
            ImageLoader.getInstance().displayImage(thisPlay.playPhoto, playImage, options);
            //final BitmapWorkerTask task = new BitmapWorkerTask(playImage);
            //task.execute(thisPlay.playPhoto.substring(7, thisPlay.playPhoto.length()));
            playImage.setTransitionName(imageTransID);
        }else{
            if (GamesPerPlay.getBaseGame(thisPlay).gameThumb != null) {
                ImageLoader.getInstance().displayImage("http:" + GamesPerPlay.getBaseGame(thisPlay).gameThumb, playImage, options);
                playImage.setTransitionName(imageTransID);
            }
        }

        //ImageLoader.getInstance().displayImage(thisPlay.playPhoto, playImage, options);

        //get Play object


        //get game for this play
        Game thisBaseGame = GamesPerPlay.getBaseGame(thisPlay);

        //add a textview with the game name
        TextView gameName = (TextView) viewPlayLayout.findViewById(R.id.gameName);
        gameName.setText(thisBaseGame.gameName);
        gameName.setTransitionName(nameTransID);



        //date
        TextView playDate = (TextView) viewPlayLayout.findViewById(R.id.gameDate);
        DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
        String output = outputFormatter.format(thisPlay.playDate); // Output : 01/20/2010
        playDate.setText(output);
        playDate.setTransitionName(dateTransID);

        //expansions
        List<GamesPerPlay> expansions = GamesPerPlay.getExpansions(thisPlay);
        for(GamesPerPlay expansion:expansions){
            addGame(expansion.game);
        }

        List<PlayersPerPlay> players = PlayersPerPlay.getPlayers_Winners(thisPlay);
        int highScore = PlayersPerPlay.getHighScore(thisPlay);
        for(PlayersPerPlay player:players){
            Player thisPlayer = player.player;
            if (player.score < highScore) {
                addPlayer(thisPlayer.playerName, "" + player.score, false);
            } else {
                addPlayer(thisPlayer.playerName, "" + player.score, true);
            }
        }

        //output note
        TextView showNote = (TextView) viewPlayLayout.findViewById(R.id.notesText);
        if (!thisPlay.playNotes.equals("")) {
            showNote.setText("\"" + thisPlay.playNotes + "\"");
            showNote.setTextSize(24);
            showNote.setTypeface(null, Typeface.ITALIC);
        }else{
            showNote.setVisibility(View.GONE);
        }
        return viewPlayLayout;
    }

    private void addPlayer(String playerName, String score, boolean winnerFlag) {
        // Instantiate a new "row" view.
        final ViewGroup newView = (ViewGroup) LayoutInflater.from(mActivity).inflate(
                R.layout.play_viewplayer_item, mContainerView_Players, false);

        TextView playerView = (TextView) newView.findViewById(R.id.player);
        TextView scoreView = (TextView) newView.findViewById(R.id.score);
        playerView.setText(playerName);
        scoreView.setText(score);
        if (winnerFlag){
            playerView.setTextSize(20);
            scoreView.setTextSize(20);
            playerView.setTypeface(null, Typeface.BOLD);
            scoreView.setTypeface(null, Typeface.BOLD);
        }else{
            playerView.setTextSize(16);
            scoreView.setTextSize(16);
        }

        mContainerView_Players.addView(newView);
    }

    private void addGame(Game game){
        final ViewGroup newView = (ViewGroup) LayoutInflater.from(mActivity).inflate(
                R.layout.play_showexpansions_item, mContainerView_Expansions, false);

        TextView gameName = (TextView) newView.findViewById(R.id.gameName);
        gameName.setText(game.gameName);
        mContainerView_Expansions.addView(newView);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String string) {
        if (mListener != null) {
            mListener.onFragmentInteraction(string);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.view_play, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.edit_play) {
            ((MainActivity) mActivity).openAddPlay(this, GamesPerPlay.getBaseGame(Play.findById(Play.class, playID)).gameName, playID);
            return true;
        }else if (id == R.id.delete_play) {
            ((MainActivity) mActivity).deletePlay(playID, true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    Activity mActivity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
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
        mActivity = null;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        ((MainActivity)mActivity).unbindDrawables(viewPlayLayout);
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
