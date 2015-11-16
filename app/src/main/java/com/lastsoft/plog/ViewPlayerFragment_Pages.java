package com.lastsoft.plog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lastsoft.plog.db.Player;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;


public class ViewPlayerFragment_Pages extends Fragment {
    private long playerID;
    String imageTransID;
    String nameTransID;
    String dateTransID;
    private ViewGroup mContainerView_Players;
    private ViewGroup mContainerView_Expansions;
    ImageView playerImage;
    public static final String ARG_PAGE = "page";
    private int mPageNumber;
    LinearLayout progressContainer;
    DisplayImageOptions options;

    public static ViewPlayerFragment_Pages newInstance(long playerID, String transID, String transID2, String transID3) {
        ViewPlayerFragment_Pages fragment = new ViewPlayerFragment_Pages();
        Bundle args = new Bundle();
        args.putLong("playerID", playerID);
        args.putString("imageTransID", transID);
        args.putString("nameTransID", transID2);
        args.putString("dateTransID", transID3);
        fragment.setArguments(args);
        return fragment;
    }

    public ViewPlayerFragment_Pages() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playerID = getArguments().getLong("playerID");
            imageTransID = getArguments().getString("imageTransID");
            nameTransID = getArguments().getString("nameTransID");
            dateTransID = getArguments().getString("dateTransID");
        }
        options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(false)
                .considerExifParams(true)
                .build();
        setHasOptionsMenu(true);
    }



    View viewPlayLayout;
    TextView gameName;
    TextView playDate;
    ImageView closeButton;
    ImageView menuButton;
    Player thisPlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewPlayLayout = inflater.inflate(R.layout.fragment_view_play, container, false);
        viewPlayLayout.setBackgroundColor(getResources().getColor(R.color.cardview_initial_background));
        progressContainer = (LinearLayout) viewPlayLayout.findViewById(R.id.progressContainer);
        LinearLayout linLayout = (LinearLayout) viewPlayLayout.findViewById(R.id.linearLayout);
        //Log.d("V1", "playID = " + playID);

        options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(false)
                .considerExifParams(true)
                .build();

        drawLayout();

        viewPlayLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                viewPlayLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                getActivity().startPostponedEnterTransition();
                return true;
            }
        });
        return viewPlayLayout;
    }



    public void redrawLayout(){
        mContainerView_Players.removeAllViews();
        mContainerView_Expansions.removeAllViews();
        drawLayout();
    }

    public void drawLayout(){
        thisPlayer = Player.findById(Player.class, playerID);
        //Log.d("V1", "imageTransID = " + imageTransID);

        options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(false)
                .considerExifParams(true)
                .build();

        mContainerView_Players = (ViewGroup) viewPlayLayout.findViewById(R.id.container_players);
        mContainerView_Expansions = (ViewGroup) viewPlayLayout.findViewById(R.id.container_expansions);

        playerImage = (ImageView) viewPlayLayout.findViewById(R.id.imageView1);
        playerImage.setTransitionName(imageTransID);
        if (thisPlayer.playerPhoto != null && !thisPlayer.playerPhoto.equals("") && new File(thisPlayer.playerPhoto.substring(7, thisPlayer.playerPhoto.length())).exists()){
            ImageLoader.getInstance().displayImage(thisPlayer.playerPhoto, playerImage, options);
            //Picasso.with(mActivity).load(thisPlay.playPhoto).fit().into(playImage);

            playerImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (progressContainer.getVisibility() != View.VISIBLE) {
                        progressContainer.setVisibility(View.VISIBLE);
                        String[] photoParts = thisPlayer.playerPhoto.split("/");
                        File newFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/Plog/",photoParts[photoParts.length-1]);
                        Uri contentUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), "com.lastsoft.plog.fileprovider", newFile);
                        //Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath("Pictures/" + photoParts[photoParts.length - 1]).build();
                        //Log.d("V1", uri.toString());
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(contentUri, "image/*");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);
                    }
                }
            });
        }else{
            String gamePieceThumb = thisPlayer.defaultColor;
            if (gamePieceThumb != null && !gamePieceThumb.equals("")) {
                ImageLoader.getInstance().displayImage("file://" + gamePieceThumb, playerImage, options);
                //Picasso.with(mActivity).load("http:" + GamesPerPlay.getBaseGame(thisPlay).gameThumb).fit().into(playImage);
            }else{
                playerImage.setImageDrawable(null);
            }
            playerImage.setOnClickListener(null);

        }



    }




    @Override
    public void onResume() {
        super.onResume();
        options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(false)
                .considerExifParams(true)
                .build();
        progressContainer.setVisibility(View.GONE);
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

    int myContainer;
    public int getContainer(){
        myContainer = R.id.container;
        return myContainer;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //((MainActivity)mActivity).unbindDrawables(viewPlayLayout);
    }

    @Nullable
    public View getSharedImageElement() {
        View view = getView().findViewById(R.id.imageView1);
        //if (isViewInBounds(getView().findViewById(R.id.scroll_view), view)) {
            return view;
        //}
        //return null;
    }

    @Nullable
    public View getSharedNameElement() {
        View view = getView().findViewById(R.id.gameName);
        //if (isViewInBounds(getView().findViewById(R.id.scroll_view), view)) {
        return view;
        //}
        //return null;
    }

    @Nullable
    public View getSharedDateElement() {
        View view = getView().findViewById(R.id.gameDate);
        //if (isViewInBounds(getView().findViewById(R.id.scroll_view), view)) {
        return view;
        //}
        //return null;
    }
}
