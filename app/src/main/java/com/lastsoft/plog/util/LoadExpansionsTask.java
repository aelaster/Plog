package com.lastsoft.plog.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.lastsoft.plog.db.Game;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TheFlash on 5/25/2015.
 */
public class LoadExpansionsTask extends AsyncTask<String, Void, List<Game>> {

    public Context theContext;
    public LoadExpansionsTask(Context context){
        this.theContext = context;
    }

    private String getGameDetails(String thingID){
        try {
            URL url = null;
            URLConnection ucon = null;
            url = new URL("https://www.boardgamegeek.com/xmlapi2/thing?id=" + thingID);
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
    protected List<Game> doInBackground(final String... args) {
        String myString = "";
        String bggProcess = "false";
        List<Game> theGames = null;
        try {
            // first we get the info about the game we're looking for
            URL url;
            myString = getGameDetails(args[0]);

            if (myString != null) {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser parser = factory.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(myString));
                //parser.nextTag();
                // parser.require(XmlPullParser.START_TAG, null, "items");

                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String name = parser.getName();
                    //Log.d("V1", "name = " + name);
                    // Starts by looking for the entry tag
                    if (name.equals("items")) {
                    } else if (name.equals("item")) {
                        theGames = readEntry(parser);
                    } else {
                        skip(parser);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return theGames;
    }

    @Override
    protected void onPostExecute(final List<Game> result) {
        /*Log.d("V1", "result = " + result);
        if (result.equals("true")) {
            Toast.makeText(theContext, theContext.getString(R.string.bgg_process_notice), Toast.LENGTH_LONG).show();
        }*/
    }

    private List<Game> readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Game> expansions = new ArrayList<Game>();

        parser.require(XmlPullParser.START_TAG, null, "item");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();


            if (name.equals("link")) {
                if(parser.getAttributeValue(null, "type").equals("boardgameexpansion")){
                    String expansionBGGID = parser.getAttributeValue(null, "id");
                    Game expansion = Game.findGameByBGGID(expansionBGGID);
                    if (expansion!= null){
                        expansions.add(expansion);
                        skip(parser);
                    }else {
                        skip(parser);
                    }
                }else {
                    skip(parser);
                }
            }else {
                skip(parser);
            }
        }

        return expansions;

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
                //Log.d("V1", gameName);
            }  else if (name.equals("status")) {
                //gameOwn = readOwn(parser);
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
                updateMe.expansionFlag = true;
                updateMe.save();
            }
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

    private String readTotal(XmlPullParser parser) throws IOException, XmlPullParserException {
        String total = "";
        String tag = parser.getName();
        if (tag.equals("items")) {
            total = parser.getAttributeValue(null, "totalitems");
        }
        return total;
    }

    private String readOwn(XmlPullParser parser) throws IOException, XmlPullParserException {
        String own = "";
        String tag = parser.getName();
        if (tag.equals("status")) {
            own = parser.getAttributeValue(null, "own");
        }
        Log.d("V1", "own = " + own);
        return own;
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