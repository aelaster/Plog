package com.lastsoft.plog.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.lastsoft.plog.R;
import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GameGroup;
import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerPlay;
import com.lastsoft.plog.db.PlaysPerGameGroup;

import org.apache.http.util.ByteArrayBuffer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by TheFlash on 5/25/2015.
 */
public class SyncPlaysTask extends AsyncTask<String, String, String> {
    private ProgressDialog mydialog;
    public Context theContext;
    int syncCounter = 0;
    int totalCount = 0;
    int pageCounter = 1;
    SharedPreferences app_preferences;
    public SyncPlaysTask(Context context){
        this.theContext = context;
        app_preferences = PreferenceManager.getDefaultSharedPreferences(theContext);
    }

    private class AddPlay {
        String playID;
        String playComment;
        Date playDate;
        AddGame theGame;
        AddPlayer[] thePlayers;

        public AddPlay(String playID, String playComment, Date playDate, AddGame theGame, AddPlayer[] thePlayers) {
            this.playID = playID;
            this.playComment = playComment;
            this.playDate = playDate;
            this.theGame = theGame;
            this.thePlayers = thePlayers;
        }
    }

    private class AddPlayer {
        public String playerName;
        public String userName;
        public String color;
        public float score;

        public AddPlayer(String playerName, String userName, String color, float score) {
            this.playerName = playerName;
            this.userName = userName;
            this.color = color;
            this.score = score;
        }
    }

    private class AddGame {
        public String gameName;
        public String gameId;
        public boolean expansionFlag;

        public AddGame(String gameName, String gameId, boolean expansionFlag) {
            this.gameName = gameName;
            this.gameId = gameId;
            this.expansionFlag = expansionFlag;
        }
    }

    private String getPlays(String bggUsername, int pageNumber){
        try {
            URL url = null;
            URLConnection ucon = null;
            url = new URL("https://www.boardgamegeek.com/xmlapi2/plays?username=" + bggUsername + "&page=" + pageNumber);
            ucon = url.openConnection();
            ucon.setConnectTimeout(3000);
            ucon.setReadTimeout(30000);
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is, 1024);

            ByteArrayBuffer baf = new ByteArrayBuffer(1024);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }

            String myString = new String(baf.toByteArray());
            bis.close();
            is.close();

            return myString;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // can use UI thread here
    @Override
    protected void onPreExecute() {
        mydialog = new ProgressDialog(theContext);
        mydialog.setMessage(theContext.getString(R.string.syncing_plays));
        mydialog.setCancelable(false);
        try {
            mydialog.show();
        } catch (Exception ignored) {
        }
    }

    // automatically done on worker thread (separate from UI thread)
    @Override
    protected String doInBackground(final String... args) {
        try {
            // first we go through and add every game in the collection
            URL url;
            long currentDefaultPlayer = app_preferences.getLong("defaultPlayer", -1);
            if (currentDefaultPlayer >=0 ) {
                Player defaultPlayer = Player.findById(Player.class, currentDefaultPlayer);
                if (defaultPlayer != null) {
                    //Log.d("V1", "https://www.boardgamegeek.com/xmlapi2/collection?username=" + defaultPlayer.bggUsername);
                    boolean stopLooping = false;
                    while(!stopLooping) {
                        parsePlaysXML(pageCounter, defaultPlayer.bggUsername);
                        if (totalCount > syncCounter){
                            //more to go!
                            totalCount = totalCount - 100;
                            pageCounter++;
                        }else{
                            stopLooping = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "done";
    }

    @Override
    protected void onProgressUpdate(String... args) {
        if (args.length == 2){
            mydialog.setMessage(theContext.getString(R.string.syncing_plays) + " (" + args[0] + "/" + args[1] + ")");
        }else {
            updateGameViaBGG(args[2], args[3]);
        }
    }

    @Override
    protected void onPostExecute(final String result) {
        //Log.d("V1", result);
        mydialog.dismiss();
    }

    private void parsePlaysXML(int pageNumber, String bggUsername){
        try {
            String myString = getPlays(bggUsername, pageNumber);
            if (myString != null) {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser parser = factory.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(myString));

                Play newPlay = null;

                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String name = parser.getName();
                    //Log.d("V1", "name = " + name);
                    // Starts by looking for the entry tag
                    if (name.equals("plays")) {
                        //entries.add(readEntry(parser));
                        if (totalCount == 0) {
                            totalCount = Integer.parseInt(readTotal(parser));
                        }else{
                            Integer.parseInt(readTotal(parser));
                        }

                        //mDataset = new String[total];
                        //mDataset_Thumb = new String[total];
                    } else if (name.equals("play")) {
                        //Log.d("V1", "name = " + mDataset[i]);
                        //build play to add, read from BGG
                        ArrayList<Long> addedUsers = new ArrayList<Long>();
                        AddPlay playToAdd = readEntry(parser, readPlayID(parser), readPlayDate(parser));

                        publishProgress("" + syncCounter, "" + totalCount);

                        //make sure this play doesn't already exist in Db
                        if (Play.findPlayByBGGID(playToAdd.playID) == null) {

                            if (playToAdd.theGame.expansionFlag == false) {
                                //Log.d("V1", "Adding Base Game");
                                //this is the base game
                                //we create a new play

                                //add the play
                                newPlay = new Play(playToAdd.playDate, playToAdd.playComment, "", playToAdd.playID);
                                newPlay.save();

                                //determine high score
                                float highScore = -99999;
                                for (AddPlayer thePlayer : playToAdd.thePlayers) {
                                    if (thePlayer.score > highScore) {
                                        highScore = thePlayer.score;
                                    }
                                }

                                //add players to the play
                                for (AddPlayer thePlayer : playToAdd.thePlayers) {
                                    Player thisPlayer = Player.findPlayerByName(thePlayer.playerName);
                                    if (thisPlayer == null) {
                                        //player doesn't exist.
                                        Player player = new Player(thePlayer.playerName, thePlayer.userName, "", "");
                                        player.save();
                                        thisPlayer = player;
                                    }
                                    addedUsers.add(thisPlayer.getId());
                                    PlayersPerPlay newPlayer = new PlayersPerPlay(thisPlayer, newPlay, thePlayer.score, thePlayer.color, highScore);
                                    newPlayer.save();
                                }

                                //add base game to new play
                                Game theGame = Game.findGameByName(playToAdd.theGame.gameName);
                                if (theGame == null) {
                                    //game does not exist
                                    Game game = new Game(playToAdd.theGame.gameName, playToAdd.theGame.gameId, "", false);
                                    game.save();
                                    theGame = game;
                                    //updateGameViaBGG(playToAdd.theGame.gameId, playToAdd.theGame.gameName);
                                    publishProgress("", "", playToAdd.theGame.gameId, playToAdd.theGame.gameName);
                                }
                                GamesPerPlay newBaseGame = new GamesPerPlay(newPlay, theGame, false);
                                newBaseGame.save();

                                //check for a group
                                List<GameGroup> gameGroups = GameGroup.listAll(GameGroup.class);
                                for (GameGroup thisGroup : gameGroups) {
                                    List<Player> players = GameGroup.getGroupPlayers(thisGroup);
                                    boolean included = true;
                                    for (Player playa : players) {
                                        if (!addedUsers.contains(playa.getId())) {
                                            included = false;
                                            break;
                                        }
                                    }
                                    if (included) {
                                        //add this to PlaysPerGameGroup
                                        PlaysPerGameGroup newGroupPlay = new PlaysPerGameGroup(newPlay, thisGroup);
                                        newGroupPlay.save();
                                    }
                                }

                                //remove from bucket list if it's there
                                //only do this if there are more than one players...or the remove solo plays setting is enabled
                                if (playToAdd.thePlayers.length > 1 || app_preferences.getBoolean("solo_remove_bucket_list", true) == true) {
                                    if (theGame != null && theGame.taggedToPlay > 0) {
                                        theGame.taggedToPlay = 0;
                                        theGame.save();
                                    }
                                }
                                //Log.d("V1", "Added " + playToAdd.theGame.gameName + " to play " + newPlay.getId());
                            } else {
                                //Log.d("V1", "Adding Expansion");
                                //this is an expansion, so we add it to the previous play
                                if (newPlay != null) {
                                    Game theGame = Game.findGameByName(playToAdd.theGame.gameName);
                                    if (theGame == null) {
                                        //game does not exist
                                        Game game = new Game(playToAdd.theGame.gameName, playToAdd.theGame.gameId, "", true);
                                        game.save();
                                        theGame = game;
                                        //updateGameViaBGG(playToAdd.theGame.gameId, playToAdd.theGame.gameName);
                                        publishProgress("", "", playToAdd.theGame.gameId, playToAdd.theGame.gameName);
                                    }
                                    GamesPerPlay newBaseGame = new GamesPerPlay(newPlay, theGame, true);
                                    newBaseGame.save();
                                    //Log.d("V1", "Added " + playToAdd.theGame.gameName + " to play " + newPlay.getId());
                                }
                            }

                        }
                        syncCounter++;
                    } else {
                        skip(parser);
                    }
                }
            }
        }catch (Exception ignored){
            ignored.printStackTrace();
        }
    }

    private void updateGameViaBGG(String bggID, String gameName){
        UpdateBGGTask gameUpdate = new UpdateBGGTask(theContext, false, true);
        try {
            gameUpdate.execute(bggID, gameName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AddPlay readEntry(XmlPullParser parser, String playID, String playDate) throws XmlPullParserException, IOException {
        AddGame gameToAdd = null;
        AddPlayer[] playersToAdd = null;
        String gameName = "", gameBGGID = "", playComments = "";

        parser.require(XmlPullParser.START_TAG, null, "play");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("item")) {
                gameName = readName(parser);
                gameBGGID = readBGGID(parser);
                gameToAdd = readGame(parser, gameName, gameBGGID);
            }else if (name.equals("players")) {
                playersToAdd = readPlayers(parser);
            }else if (name.equals("comments")) {
                playComments = readComments(parser);
            }else {
                skip(parser);
            }
        }

        Date date1 = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            date1 = dateFormat.parse(playDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        parser.require(XmlPullParser.END_TAG, null, "play");
        return new AddPlay(playID, playComments, date1, gameToAdd, playersToAdd);

    }

    private String readComments(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "comments");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "comments");
        return title;
    }


    private AddGame readGame(XmlPullParser parser, String gameName, String gameBGGID) throws XmlPullParserException, IOException {
        boolean expansionFlag = false;
        parser.require(XmlPullParser.START_TAG, null, "item");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("subtypes")) {
                expansionFlag = readSubtypes(parser);
            }else {
                skip(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, null, "item");
        return new AddGame(gameName, gameBGGID, expansionFlag);
    }

    private boolean readSubtypes(XmlPullParser parser) throws XmlPullParserException, IOException {
        boolean expansionFlag = false;
        parser.require(XmlPullParser.START_TAG, null, "subtypes");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("subtype")) {
                if (readSubtype(parser).equals("boardgameexpansion")){
                    //this is a part of the current play
                    expansionFlag = true;
                }
                skip(parser);
            }
        }

        parser.require(XmlPullParser.END_TAG, null, "subtypes");
        return expansionFlag;
    }

    private AddPlayer[] readPlayers(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<AddPlayer> allPlayers = new ArrayList<AddPlayer>();
        parser.require(XmlPullParser.START_TAG, null, "players");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("player")) {
                allPlayers.add(readPlayer(parser));
            }else {
                skip(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, null, "players");
        return allPlayers.toArray(new AddPlayer[allPlayers.size()]);
    }

    private AddPlayer readPlayer(XmlPullParser parser) throws IOException, XmlPullParserException {
        String playerName = "";
        String userName = "";
        String color = "";
        float score = 0;
        String tag = parser.getName();
        if (tag.equals("player")) {
            playerName = parser.getAttributeValue(null, "name");
            userName = parser.getAttributeValue(null, "username");
            color = parser.getAttributeValue(null, "color");
            score = Float.parseFloat(parser.getAttributeValue(null, "score"));
            skip(parser);
            return new AddPlayer(playerName, userName, color, score);
        }
        return null;

    }


    private String readPlayID(XmlPullParser parser) throws IOException, XmlPullParserException {
        String bggid = "";
        String tag = parser.getName();
        if (tag.equals("play")) {
            bggid = parser.getAttributeValue(null, "id");
        }
        return bggid;
    }

    private String readPlayDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        String bggid = "";
        String tag = parser.getName();
        if (tag.equals("play")) {
            bggid = parser.getAttributeValue(null, "date");
        }
        return bggid;
    }

    private String readBGGID(XmlPullParser parser) throws IOException, XmlPullParserException {
        String bggid = "";
        String tag = parser.getName();
        if (tag.equals("item")) {
            bggid = parser.getAttributeValue(null, "objectid");
        }
        return bggid;
    }

    private String readTotal(XmlPullParser parser) throws IOException, XmlPullParserException {
        String total = "";
        String tag = parser.getName();
        if (tag.equals("plays")) {
            total = parser.getAttributeValue(null, "total");
        }
        return total;
    }

    private String readSubtype(XmlPullParser parser) throws IOException, XmlPullParserException {
        String total = "";
        String tag = parser.getName();
        if (tag.equals("subtype")) {
            total = parser.getAttributeValue(null, "value");
        }
        return total;
    }

    private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        String name = "";
        String tag = parser.getName();
        if (tag.equals("item")) {
            name = parser.getAttributeValue(null, "name");
        }
        return name;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}