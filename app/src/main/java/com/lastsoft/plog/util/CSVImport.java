package com.lastsoft.plog.util;

import android.app.Activity;
import android.util.Log;

import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerPlay;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by TheFlash on 5/29/2015.
 */
public class CSVImport {

    private Activity mActivity;
    public CSVImport(Activity activity) {
        mActivity = activity;
    }

    public void importCSV() {
        String next[] = {};
        try {
            CSVReader reader = new CSVReader(new InputStreamReader(mActivity.getAssets().open("GamesImport.csv")));
            while (true) {
                next = reader.readNext();
                if (next != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    Date date1 = dateFormat.parse(next[0]);

                    Play newPlay = new Play(date1, next[3].trim(), null);
                    newPlay.save();

                    int totalPlayers = 2;
                    if (!next[5].equals("")) {
                        totalPlayers++;
                    }
                    if (!next[6].equals("")) {
                        totalPlayers++;
                    }
                    if (!next[7].equals("")) {
                        totalPlayers++;
                    }
                    if (!next[8].equals("")) {
                        totalPlayers++;
                    }

                            /*
                                First, trim off asterisks from players 1-4 and hold in it's own variable, so the asterisk is still in the next array
                                If Player 1-4 isn't blank, check to see if that player's name exists
                                if not, add the player and get the id
                                otherwise, get the player id

                                Next, for each player 1-4 that has an asterisk, make their score 1 for the game
                             */

                    if (!next[5].equals("")) { //Player 1
                        addPlayer(next[5], newPlay, totalPlayers);
                    }
                    if (!next[6].equals("")) { //Player 2
                        addPlayer(next[6], newPlay, totalPlayers);
                    }
                    if (!next[7].equals("")) { //Player 3
                        addPlayer(next[7], newPlay, totalPlayers);
                    }
                    if (!next[8].equals("")) { //Player 4
                        addPlayer(next[8], newPlay, totalPlayers);
                    }

                           /*

                            Next, check "winner".

                            I AM 1, SHE IS 2 (player ids)

                            if "AEL", get My playerID and make my score 1 and make her score 0
                            if "SKG", get her playerID and make her score 1 and make my score 0
                            if "AEL and SKG" get our player ids and make our scores 1
                            if "Big Fat Losers" get out playerids and make our scores 0

                             */

                    int score;
                    if (next[4].trim().contains("AEL and SKG")) {
                        PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, (long) 1), newPlay, totalPlayers, totalPlayers);
                        newPlayer.save();
                        PlayersPerPlay newPlayer2 = new PlayersPerPlay(Player.findById(Player.class, (long) 2), newPlay, totalPlayers, totalPlayers);
                        newPlayer2.save();
                    } else if (next[4].trim().contains("AEL")) {
                        if (next[4].trim().endsWith("*")) {
                            PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, (long) 1), newPlay, totalPlayers - 1, totalPlayers);
                            newPlayer.save();
                        } else {
                            PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, (long) 1), newPlay, totalPlayers, totalPlayers);
                            newPlayer.save();
                        }
                        PlayersPerPlay newPlayer2 = new PlayersPerPlay(Player.findById(Player.class, (long) 2), newPlay, 0, totalPlayers);
                        newPlayer2.save();
                    } else if (next[4].trim().contains("SKG")) {
                        if (next[4].trim().endsWith("*")) {
                            PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, (long) 2), newPlay, totalPlayers - 1, totalPlayers);
                            newPlayer.save();
                        } else {
                            PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, (long) 2), newPlay, totalPlayers, totalPlayers);
                            newPlayer.save();
                        }
                        PlayersPerPlay newPlayer2 = new PlayersPerPlay(Player.findById(Player.class, (long) 1), newPlay, 0, totalPlayers);
                        newPlayer2.save();
                    } else {
                        PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, (long) 1), newPlay, 0, totalPlayers);
                        newPlayer.save();
                        PlayersPerPlay newPlayer2 = new PlayersPerPlay(Player.findById(Player.class, (long) 2), newPlay, 0, totalPlayers);
                        newPlayer2.save();
                    }

                    //last, add game to play
                    //if i don't have the game, add it

                    Game addedGame = Game.findGameByName_NoCase(URLDecoder.decode(next[1].trim(), "UTF-8"));
                    if (addedGame == null) {
                        Log.d("V1", "New Game = " + next[1].trim());
                        addedGame = new Game(next[1].trim());
                        addedGame.save();
                    }

                    GamesPerPlay newBaseGame = new GamesPerPlay(newPlay, addedGame, false);
                    newBaseGame.save();

                    //}
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void addPlayer(String playerName, Play newPlay, int totalPlayers){
        String playerHolder = playerName.trim();
        long playerID;
        boolean winnerFlag = false;
        int score;
        if (playerHolder.endsWith("*")){
            winnerFlag = true;
            playerHolder = playerHolder.substring(0, playerHolder.length()-1);
        }
        playerID = Player.playerExists_ID(playerHolder);
        if (playerID == -1) {
            //player doesn't exist.  Add them
            Player player = new Player(playerHolder);
            player.save();
            playerID = player.getId();
        }

        if (winnerFlag){
            score = totalPlayers;
        }else{
            score = 0;
        }

        PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, playerID), newPlay, score, totalPlayers);
        newPlayer.save();
    }
}
