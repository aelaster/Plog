package com.lastsoft.plog.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.lastsoft.plog.db.Player;

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

/**
 * Created by TheFlash on 5/25/2015.
 */
public class LoadPlaysTask extends AsyncTask<String, Void, String> {

    public Context theContext;
    public LoadPlaysTask(Context context){
        this.theContext = context;
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

    private String getPlays(String bggUsername){
        try {
            URL url = null;
            URLConnection ucon = null;
            url = new URL("https://www.boardgamegeek.com/xmlapi2/plays?username=" + bggUsername);
            ucon = url.openConnection();
            ucon.setConnectTimeout(3000);
            ucon.setReadTimeout(30000);
                     /* Define InputStreams to read
                        * from the URLConnection. */
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is, 1024);

                       /* Read bytes to the Buffer until
                        * there is nothing more to read(-1). */
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

    // automatically done on worker thread (separate from UI thread)
    @Override
    protected String doInBackground(final String... args) {

        String myString = "";

        try {

            // first we go through and add every game in the collection
            URL url;

            SharedPreferences app_preferences;
            app_preferences = PreferenceManager.getDefaultSharedPreferences(theContext);
            long currentDefaultPlayer = app_preferences.getLong("defaultPlayer", -1);
            if (currentDefaultPlayer >=0 ) {
                Player defaultPlayer = Player.findById(Player.class, currentDefaultPlayer);
                if (defaultPlayer != null) {
                    //Log.d("V1", "https://www.boardgamegeek.com/xmlapi2/collection?username=" + defaultPlayer.bggUsername);
                    myString = getPlays(defaultPlayer.bggUsername);
                    //Log.d("V1", myString);

                    if (myString != null) {
                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        factory.setNamespaceAware(true);
                        XmlPullParser parser = factory.newPullParser();
                        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                        parser.setInput(new StringReader(myString));
                        //parser.nextTag();
                        // parser.require(XmlPullParser.START_TAG, null, "items");

                        long plogPlayID;

                        while (parser.next() != XmlPullParser.END_DOCUMENT) {
                            if (parser.getEventType() != XmlPullParser.START_TAG) {
                                continue;
                            }
                            String name = parser.getName();
                            Log.d("V1", "name = " + name);
                            // Starts by looking for the entry tag
                            if (name.equals("plays")) {
                                //entries.add(readEntry(parser));
                                int total = 0;
                                total = Integer.parseInt(readTotal(parser));

                                //mDataset = new String[total];
                                //mDataset_Thumb = new String[total];
                            } else if (name.equals("play")) {
                                //Log.d("V1", "name = " + mDataset[i]);
                                AddPlay playToAdd = readEntry(parser, readPlayID(parser), readPlayDate(parser));
                                //TEST THIS BIA
                                /*Log.d("V1", "*****START PLAY*****");
                                Log.d("V1", "playID = " + playToAdd.playID);
                                Log.d("V1", "playDate = " + playToAdd.playDate);
                                Log.d("V1", "playComment = " + playToAdd.playComment);
                                Log.d("V1", "theGame.gameId = " + playToAdd.theGame.gameId);
                                Log.d("V1", "theGame.gameName = " + playToAdd.theGame.gameName);
                                Log.d("V1", "theGame.expansionFlag = " + playToAdd.theGame.expansionFlag);
                                for (AddPlayer thePlayer: playToAdd.thePlayers){
                                    Log.d("V1", "thePlayer.playerName = " + thePlayer.playerName);
                                    Log.d("V1", "thePlayer.userName = " + thePlayer.userName);
                                    Log.d("V1", "thePlayer.color = " + thePlayer.color);
                                    Log.d("V1", "thePlayer.score = " + thePlayer.score);
                                }
                                Log.d("V1", "*****END PLAY*****");*/
                            } else {
                                skip(parser);
                            }
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
    protected void onPostExecute(final String result) {
        Log.d("V1", result);

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