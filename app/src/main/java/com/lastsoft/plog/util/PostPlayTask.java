package com.lastsoft.plog.util;

import android.content.Context;
import android.os.AsyncTask;

import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;

import java.util.List;

/**
 * Created by TheFlash on 5/25/2015.
 */
public class PostPlayTask extends AsyncTask<Play, Void, String> {

    Context theContext;
    String bggUsername;
    BGGLogInHelper helper;
    public PostPlayTask(Context context, String bggUsername){
        this.bggUsername = bggUsername;
        this.theContext = context;
    }

    @Override
    protected String doInBackground(final Play... playToLog) {

        String bggProcess = "false";

        //Log.d("V1", "trying to post a play to bgg");

        helper = new BGGLogInHelper(theContext, null);
        if (helper.canLogIn() && helper.checkCookies()) {
            //first do base game
            BGGUtils.postPlay(theContext, helper, bggUsername, playToLog[0], null);

            //then do expansions
            List<GamesPerPlay> expansions = GamesPerPlay.getExpansions(playToLog[0]);
            for(GamesPerPlay expansion:expansions){
                BGGUtils.postPlay(theContext, helper, bggUsername, playToLog[0], expansion);
            }
        }


        return bggProcess;
    }
}