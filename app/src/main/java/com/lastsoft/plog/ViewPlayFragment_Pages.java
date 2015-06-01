package com.lastsoft.plog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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



public class ViewPlayFragment_Pages extends Fragment {
    // TODO: Rename and change types of parameters
    private long playID;
    String imageTransID;
    String nameTransID;
    String dateTransID;
    private ViewGroup mContainerView_Players;
    private ViewGroup mContainerView_Expansions;
    ImageView playImage;
    public static final String ARG_PAGE = "page";
    private int mPageNumber;

    public static ViewPlayFragment_Pages newInstance(long playID, String transID, String transID2, String transID3) {
        ViewPlayFragment_Pages fragment = new ViewPlayFragment_Pages();
        Bundle args = new Bundle();
        args.putLong("playID", playID);
        args.putString("imageTransID", transID);
        args.putString("nameTransID", transID2);
        args.putString("dateTransID", transID3);
        fragment.setArguments(args);
        return fragment;
    }

    public ViewPlayFragment_Pages() {
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
        //Log.d("V1", "playID = " + playID);
        final Play thisPlay = Play.findById(Play.class, playID);

    //    Log.d("V1", "nameTransID = " + nameTransID);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .considerExifParams(true)
                .build();

        mContainerView_Players = (ViewGroup) viewPlayLayout.findViewById(R.id.container_players);
        mContainerView_Expansions = (ViewGroup) viewPlayLayout.findViewById(R.id.container_expansions);

        playImage = (ImageView) viewPlayLayout.findViewById(R.id.imageView1);
        if (thisPlay.playPhoto != null && !thisPlay.playPhoto.equals("")){
            ImageLoader.getInstance().displayImage(thisPlay.playPhoto, playImage, options);
            playImage.setTransitionName(imageTransID);
            playImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(thisPlay.playPhoto), "image/*");
                    startActivity(intent);
                }
            });
        }else{
            if (GamesPerPlay.getBaseGame(thisPlay).gameThumb != null) {
                ImageLoader.getInstance().displayImage("http:" + GamesPerPlay.getBaseGame(thisPlay).gameThumb, playImage, options);
                playImage.setTransitionName(imageTransID);
            }else{
                playImage.setImageDrawable(null);
                playImage.setTransitionName(imageTransID);
            }
        }

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

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        ((MainActivity)mActivity).unbindDrawables(viewPlayLayout);
    }


}
