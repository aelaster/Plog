package com.lastsoft.plog.util;

import android.content.Context;
import android.os.Environment;

import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.PlayersPerPlay;
import com.lastsoft.plog.db.PlaysPerGameGroup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Created by TheFlash on 8/22/2015.
 */
public class AppUtils {
    public static void deletePlay(Context context, long playId){
        Play deleteMe = Play.findById(Play.class, playId);

        //delete PlayersPerPlay
        List<PlayersPerPlay> players = PlayersPerPlay.getPlayers(deleteMe);
        for(PlayersPerPlay player:players){
            player.delete();
        }
        //delete GamesPerPay
        List<GamesPerPlay> games = GamesPerPlay.getGames(deleteMe);
        for(GamesPerPlay game:games){
            if (game.expansionFlag == true){
                if (game.bggPlayId != null && !game.bggPlayId.equals("")){
                    DeletePlayTask deletePlay = new DeletePlayTask(context);
                    try {
                        deletePlay.execute(game.bggPlayId);
                    } catch (Exception e) {

                    }
                }
            }
            game.delete();
        }

        //delete plays_per_game_group
        List<PlaysPerGameGroup> plays = PlaysPerGameGroup.getPlays(deleteMe);
        for(PlaysPerGameGroup play:plays){
            play.delete();
        }

        //delete play image
        if(deleteMe.playPhoto != null && !deleteMe.playPhoto.equals("")) {
            String deletePhoto =  Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES) + "/Plog/" + deleteMe.playPhoto;
            File deleteImage = new File(deletePhoto);
            if (deleteImage.exists()) {
                deleteImage.delete();
            }

            //delete play image thumb
            File deleteImage_thumb = new File(deletePhoto.substring(0, deletePhoto.length() - 4) + "_thumb6.jpg");
            if (deleteImage_thumb.exists()) {
                deleteImage_thumb.delete();
            }
        }

        //delete play from bgg
        if (deleteMe.bggPlayID != null && !deleteMe.bggPlayID.equals("")){
            DeletePlayTask deletePlay = new DeletePlayTask(context);
            try {
                deletePlay.execute(deleteMe.bggPlayID);
            } catch (Exception e) {

            }
        }

        //delete play
        deleteMe.delete();
    }
}
