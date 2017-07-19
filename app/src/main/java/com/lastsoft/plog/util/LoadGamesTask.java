package com.lastsoft.plog.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.lastsoft.plog.R;
import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.Player;
import com.orm.StringUtil;

import org.apache.http.util.ByteArrayBuffer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by TheFlash on 5/25/2015.
 */
public class LoadGamesTask extends AsyncTask<String, Void, String> {

    public Context theContext;
    public LoadGamesTask(Context context){
        this.theContext = context;
    }

    private String getGamesCollection(String bggUsername){
        try {
            URL url = null;
            URLConnection ucon = null;
            url = new URL("https://www.boardgamegeek.com/xmlapi2/collection?username=" + bggUsername);
            ucon = url.openConnection();
            ucon.setConnectTimeout(3000);
            ucon.setReadTimeout(3000);
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "will be processed";
        }catch (Exception e) {
            e.printStackTrace();
            return "will be processed";
        }
    }

    private String getExpansionsCollection(String bggUsername){
        try {
            URL url = null;
            URLConnection ucon = null;
            url = new URL("https://www.boardgamegeek.com/xmlapi2/collection?username=" + bggUsername + "&subtype=boardgameexpansion");
            ucon = url.openConnection();
            ucon.setConnectTimeout(3000);
            ucon.setReadTimeout(3000);
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "will be processed";
        }catch (Exception e) {
            e.printStackTrace();
            return "will be processed";
        }
    }

    // automatically done on worker thread (separate from UI thread)
    @Override
    protected String doInBackground(final String... args) {

        String myString = "";
        String myString2 = "";
        String bggProcess = "false";

        try {

            // first we go through and add every game in the collection
            URL url;

            SharedPreferences app_preferences;
            app_preferences = PreferenceManager.getDefaultSharedPreferences(theContext);
            long currentDefaultPlayer = app_preferences.getLong("defaultPlayer", -1);
            if (currentDefaultPlayer >=0 ) {
                Player defaultPlayer = Player.findById(Player.class, currentDefaultPlayer);
                if (defaultPlayer != null) {
                    myString = getGamesCollection(defaultPlayer.bggUsername);
                    if (myString != null && myString.contains("will be processed")){
                        //do it again
                        bggProcess = "true";
                    }

                    if (myString != null && !bggProcess.equals("true")) {
                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        factory.setNamespaceAware(true);
                        XmlPullParser parser = factory.newPullParser();
                        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                        parser.setInput(new StringReader(myString));
                        while (parser.next() != XmlPullParser.END_DOCUMENT) {
                            if (parser.getEventType() != XmlPullParser.START_TAG) {
                                continue;
                            }
                            String name = parser.getName();
                            //Log.d("V1", "name = " + name);
                            // Starts by looking for the entry tag
                            if (name.equals("items")) {
                                Integer.parseInt(readTotal(parser));
                            } else if (name.equals("item")) {
                                readEntry(parser, readBGGID(parser), readBGGCollectionID(parser));
                            } else {
                                skip(parser);
                            }
                        }


                        // then we go through and flag every expansion in my collection
                        myString2 = getExpansionsCollection(defaultPlayer.bggUsername);
                        if (myString2 != null && myString2.contains("will be processed")){
                            //do it again
                            bggProcess = "true";
                        }

                        if (myString2 != null && !bggProcess.equals("true")) {

                            factory = XmlPullParserFactory.newInstance();
                            factory.setNamespaceAware(true);
                            parser = factory.newPullParser();
                            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                            parser.setInput(new StringReader(myString2));

                            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                                if (parser.getEventType() != XmlPullParser.START_TAG) {
                                    continue;
                                }
                                String name = parser.getName();
                                // Starts by looking for the entry tag
                                if (name.equals("items")) {
                                    Integer.parseInt(readTotal(parser));
                                } else if (name.equals("item")) {
                                    readEntry_Expansion(parser);
                                } else {
                                    skip(parser);
                                }
                            }
                        }
                    }
                }
            }else{
                bggProcess = "derp";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bggProcess;
    }

    @Override
    protected void onPostExecute(final String result) {
        Log.d("V1", result);
        if (result.equals("true")) {
            Toast.makeText(theContext, theContext.getString(R.string.bgg_process_notice), Toast.LENGTH_LONG).show();
        }else if (result.equals("derp")){
            Toast.makeText(theContext, theContext.getString(R.string.no_default_player), Toast.LENGTH_LONG).show();
        }
    }

    private Game readEntry(XmlPullParser parser, String gameBGGID, String gameBGGCollectionID) throws XmlPullParserException, IOException {
        String gameName = "", gameThumb = "", gameOwn = "", gameImage = "";
        parser.require(XmlPullParser.START_TAG, null, "item");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("name")) {
                gameName = readName(parser);
                List<Game> finders = null;
                finders = Game.find(Game.class, StringUtil.toSQLName("gameName") + " = ?", gameName);
                if (finders.size() != 0){
                    for(Game game:finders){
                        if (!game.collectionFlag) {
                            game.collectionFlag = true;
                            game.save();
                        }
                        if (game.gameBGGCollectionID == null || game.gameBGGCollectionID.equals("")) {
                            game.gameBGGCollectionID = gameBGGCollectionID;
                            game.save();
                        }
                    }
                    return null;
                }
            } else if (name.equals("status")) {
                gameOwn = parser.getAttributeValue(null, "own");
                skip(parser);
            } else if (name.equals("thumbnail")) {
                gameThumb = readThumbnail(parser);
            }else if (name.equals("image")) {
                gameImage = readImage(parser);
            }else {
                skip(parser);
            }
        }

        parser.require(XmlPullParser.END_TAG, null, "item");
        if (gameOwn.equals("1")) {
            Game game = new Game(gameName, gameBGGID, gameBGGCollectionID, gameImage, gameThumb, false, true);
            game.save();
            return game;
        }else{
            //check to see if it's in the collection.  if so, remove it
            List<Game> finders = null;
            finders = Game.find(Game.class, StringUtil.toSQLName("gameName") + " = ?", gameName);
            if (finders.size() != 0){
                for(Game game:finders){
                    game.delete();
                }
            }
            return null;
        }

    }

    private void readEntry_Expansion(XmlPullParser parser) throws XmlPullParserException, IOException {
        String gameName = "", gameOwn = "";

        parser.require(XmlPullParser.START_TAG, null, "item");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();


            if (name.equals("name")) {
                gameName = readName(parser);
            }  else if (name.equals("status")) {
                gameOwn = parser.getAttributeValue(null, "own");
                skip(parser);
            } else {
                skip(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, null, "item");

        if (gameOwn.equals("1")) {
            Game updateMe = Game.findGameByName(gameName);
            if (updateMe != null && !updateMe.expansionFlag) {
                updateMe.collectionFlag = true;
                updateMe.expansionFlag = true;
                updateMe.save();
            }else if (updateMe != null && !updateMe.collectionFlag){
                updateMe.collectionFlag = true;
                updateMe.save();            }
        }
    }

    private String readBGGID(XmlPullParser parser) throws IOException, XmlPullParserException {
        String bggid = "";
        String tag = parser.getName();
        if (tag.equals("item")) {
            bggid = parser.getAttributeValue(null, "objectid");
        }
        return bggid;
    }

    private String readBGGCollectionID(XmlPullParser parser) throws IOException, XmlPullParserException {
        String collectionid = "";
        String tag = parser.getName();
        if (tag.equals("item")) {
            collectionid = parser.getAttributeValue(null, "collid");
        }
        return collectionid;
    }

    private String readTotal(XmlPullParser parser) throws IOException, XmlPullParserException {
        String total = "";
        String tag = parser.getName();
        if (tag.equals("items")) {
            total = parser.getAttributeValue(null, "totalitems");
        }
        return total;
    }

    private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "name");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "name");
        return title;
    }

    private String readThumbnail(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "thumbnail");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "thumbnail");
        return title;
    }

    private String readImage(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "image");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "image");
        return title;
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