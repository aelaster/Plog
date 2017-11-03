package com.lastsoft.plog.util;

import android.content.Context;
import android.text.TextUtils;

import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Location;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.PlayersPerPlay;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class BGGUtils {
    public static void postPlay(Context theContext, BGGLogInHelper helper, String bggUsername, Play playToLog, GamesPerPlay expansionGame){
        DateFormat outputFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String output = outputFormatter.format(playToLog.playDate); // Output : 01/20/2010
        List<NameValuePair> nvps;
        UrlEncodedFormEntity entity;
        if (expansionGame == null) {
            //this is the base game
            Game theGame = GamesPerPlay.getBaseGame(playToLog);
            nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("ajax", "1"));
            nvps.add(new BasicNameValuePair("action", "save"));
            nvps.add(new BasicNameValuePair("version", "2"));
            nvps.add(new BasicNameValuePair("objecttype", "thing"));
            nvps.add(new BasicNameValuePair("objectid", theGame.gameBGGID));
            nvps.add(new BasicNameValuePair("playdate", output));
            nvps.add(new BasicNameValuePair("dateinput", output));
            if (playToLog.bggPlayID != null && !playToLog.bggPlayID.equals("")) {
                nvps.add(new BasicNameValuePair("playid", playToLog.bggPlayID));
            }
            if (playToLog.playLocation == null) {
                nvps.add(new BasicNameValuePair("location", "Plog"));
            }else{
                Location useMe = Location.findById(Location.class, playToLog.playLocation.getId());
                if(useMe != null){
                    nvps.add(new BasicNameValuePair("location", useMe.locationName));
                }else {
                    nvps.add(new BasicNameValuePair("location", "Plog"));
                }
            }
            nvps.add(new BasicNameValuePair("quantity", String.valueOf(1)));
            nvps.add(new BasicNameValuePair("incomplete", "0"));
            nvps.add(new BasicNameValuePair("nowinstats", "0"));
            nvps.add(new BasicNameValuePair("comments", playToLog.playNotes));

            List<PlayersPerPlay> playaz = PlayersPerPlay.getPlayers(playToLog);
            for(PlayersPerPlay player:playaz){
                addPair(nvps, player.getId().intValue(), "playerid", "player_" + player.player.getId());
                addPair(nvps, player.getId().intValue(), "name", player.player.playerName);
                addPair(nvps, player.getId().intValue(), "username", player.player.bggUsername);
                addPair(nvps, player.getId().intValue(), "color", player.color);
                NumberFormat nf = new DecimalFormat("###.#");
                addPair(nvps, player.getId().intValue(), "score", nf.format(player.score)+"");
                addPair(nvps, player.getId().intValue(), "new", GamesPerPlay.hasGameBeenPlayed(theGame, player.player) ? "0" : "1");
                addPair(nvps, player.getId().intValue(), "win", PlayersPerPlay.isWinner(playToLog, player.player) ? "1" : "0");
            }
        }else{
            //this is an expansion
            nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("ajax", "1"));
            nvps.add(new BasicNameValuePair("action", "save"));
            nvps.add(new BasicNameValuePair("version", "2"));
            nvps.add(new BasicNameValuePair("objecttype", "thing"));
            nvps.add(new BasicNameValuePair("objectid", expansionGame.game.gameBGGID));
            nvps.add(new BasicNameValuePair("playdate", output));
            nvps.add(new BasicNameValuePair("dateinput", output));
            if (expansionGame.bggPlayId != null && !expansionGame.bggPlayId.equals("")) {
                nvps.add(new BasicNameValuePair("playid", expansionGame.bggPlayId));
            }
            if (playToLog.playLocation == null) {
                nvps.add(new BasicNameValuePair("location", "Plog"));
            }else{
                Location useMe = Location.findById(Location.class, playToLog.playLocation.getId());
                if(useMe != null){
                    nvps.add(new BasicNameValuePair("location", useMe.locationName));
                }else {
                    nvps.add(new BasicNameValuePair("location", "Plog"));
                }
            }
            nvps.add(new BasicNameValuePair("quantity", String.valueOf(1)));
            nvps.add(new BasicNameValuePair("incomplete", "0"));
            nvps.add(new BasicNameValuePair("nowinstats", "0"));
            nvps.add(new BasicNameValuePair("comments", playToLog.playNotes));

            List<PlayersPerPlay> playaz = PlayersPerPlay.getPlayers(playToLog);
            for(PlayersPerPlay player:playaz){
                addPair(nvps, player.getId().intValue(), "playerid", "player_" + player.player.getId());
                addPair(nvps, player.getId().intValue(), "name", player.player.playerName);
                addPair(nvps, player.getId().intValue(), "username", player.player.bggUsername);
                addPair(nvps, player.getId().intValue(), "color", player.color);
                NumberFormat nf = new DecimalFormat("###.#");
                addPair(nvps, player.getId().intValue(), "score", nf.format(player.score)+"");
                addPair(nvps, player.getId().intValue(), "new", GamesPerPlay.hasGameBeenPlayed(expansionGame.game, player.player) ? "0" : "1");
                addPair(nvps, player.getId().intValue(), "win", PlayersPerPlay.isWinner(playToLog, player.player) ? "1" : "0");
            }
        }



        try {
            entity = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
            HttpPost post = new HttpPost("https://www.boardgamegeek.com/geekplay.php");
            post.setEntity(entity);
            HttpClient mClient = HttpUtils.createHttpClient(theContext, helper.getCookieStore());
            HttpResponse response = mClient.execute(post);
            if (response == null) {

            } else if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {

            } else {
                String theResponse = HttpUtils.parseResponse(response);

                if (isValidResponse(theResponse)) {
                    if (theResponse.contains("playid")) {
                        //in here, play is logged.  therefore, poll and get the id for the most recent play of the game and save the id
                        String[] values = theResponse.split("\\,");
                        String[] playIDarray = values[0].split("\\:");
                        if (expansionGame == null) {
                            playToLog.bggPlayID = playIDarray[1].replace("\"", "");
                            playToLog.save();
                        }else{
                            expansionGame.bggPlayId = playIDarray[1].replace("\"", "");
                            expansionGame.save();
                        }
                    }else {

                        String getPlayIDurl = HttpUtils.constructPlayUrlSpecific(bggUsername, GamesPerPlay.getBaseGame(playToLog).gameBGGID, output);

                        post = new HttpPost(getPlayIDurl);
                        mClient = HttpUtils.createHttpClient(theContext, helper.getCookieStore());
                        response = mClient.execute(post);
                        String theResponse2 = HttpUtils.parseResponse(response);

                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        factory.setNamespaceAware(true);
                        XmlPullParser parser = factory.newPullParser();
                        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                        parser.setInput(new StringReader(theResponse2));

                        while (parser.next() != XmlPullParser.END_DOCUMENT) {
                            if (parser.getEventType() != XmlPullParser.START_TAG) {
                                continue;
                            }
                            String name = parser.getName();
                            // Starts by looking for the entry tag
                            if (name.equals("play")) {
                                String readPlayID = "";
                                readPlayID = readPlayID(parser);
                                if (expansionGame == null) {
                                    playToLog.bggPlayID = readPlayID;
                                    playToLog.save();
                                }else{
                                    expansionGame.bggPlayId = readPlayID;
                                    expansionGame.save();
                                }
                                break;
                            } else if (name.equals("plays")) {

                            } else {
                                skip(parser);
                            }
                        }
                    }


                }
            }

        } catch (UnsupportedEncodingException e) {
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private static void addPair(List<NameValuePair> nvps, int index, String key, String value) {
        nvps.add(new BasicNameValuePair("players[" + index + "][" + key + "]", value));
    }

    public static boolean isValidResponse(String mResponse) {
        if (TextUtils.isEmpty(mResponse)) {
            return false;
        }
        return mResponse.startsWith("Plays: <a") || mResponse.startsWith("{\"html\":\"Plays:")|| mResponse.startsWith("{\"playid\":");
    }

    private static String readPlayID(XmlPullParser parser) throws IOException, XmlPullParserException {
        String bggid = "";
        String tag = parser.getName();
        if (tag.equals("play")) {
            bggid = parser.getAttributeValue(null, "id");
        }
        return bggid;
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
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