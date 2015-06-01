package com.lastsoft.plog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.Player;
import com.orm.StringUtil;

import org.apache.http.util.ByteArrayBuffer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by TheFlash on 5/25/2015.
 */
public class UpdateBGGTask extends AsyncTask<String, Void, String> {
    private final ProgressDialog mydialog;
    Context theContext;
    public UpdateBGGTask(Context context){
        this.theContext = context;
        mydialog = new ProgressDialog(theContext);
    }

    // can use UI thread here
    @Override
    protected void onPreExecute() {

        mydialog.setMessage(theContext.getString(R.string.contacting_the_geek));
        mydialog.setCancelable(false);
        try{
            mydialog.show();
        }catch (Exception e){}
    }
    // automatically done on worker thread (separate from UI thread)
    @Override
    protected String doInBackground(final String... args) {

        String myString = "";
        String bggID = "";
        int i = 0;
        int totalCount = 0;

        try {

            // first we search for the game by its name
            URL url;
            Log.d("V1", "https://www.boardgamegeek.com/xmlapi2/search?exact=1&query=" + URLEncoder.encode(args[0], "UTF-8"));
            url = new URL("https://www.boardgamegeek.com/xmlapi2/search?exact=1&query=" + URLEncoder.encode(args[0], "UTF-8"));
            URLConnection ucon = url.openConnection();
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

            myString = new String(baf.toByteArray());
            //Log.d("V1", myString);

            bis.close();
            is.close();

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
                    //entries.add(readEntry(parser));
                    int total = 0;
                    total = Integer.parseInt(readTotal(parser));
                    if (total == 0){
                        break;
                    }
                } else if (name.equals("item")) {
                    //Log.d("V1", "name = " + mDataset[i]);
                    bggID = readBGGID(parser);
                    break;
                } else {
                    skip(parser);
                }
            }
            if (!bggID.equals("")) {
                // then we go through and flag every expansion in my collection
                url = new URL("https://www.boardgamegeek.com/xmlapi2/thing?id=" + bggID);
                ucon = url.openConnection();
                ucon.setConnectTimeout(3000);
                ucon.setReadTimeout(30000);
                     /* Define InputStreams to read
                        * from the URLConnection. */
                is = ucon.getInputStream();
                bis = new BufferedInputStream(is, 1024);

                       /* Read bytes to the Buffer until
                        * there is nothing more to read(-1). */
                baf = new ByteArrayBuffer(1024);
                current = 0;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }

                myString = new String(baf.toByteArray());



                bis.close();
                is.close();

                factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                parser = factory.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(new StringReader(myString));
                //parser.nextTag();
                // parser.require(XmlPullParser.START_TAG, null, "items");

                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String name = parser.getName();
                    // Starts by looking for the entry tag
                    if (name.equals("items")) {
                    } else if (name.equals("item")) {
                        readEntry(parser, args[0], bggID);
                        break;
                    } else {
                        skip(parser);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(final String result) {
        mydialog.dismiss();
    }

    private Game readEntry(XmlPullParser parser, String gameName, String gameBGGID) throws XmlPullParserException, IOException {
        String gameThumb = "", gameImage = "";

        parser.require(XmlPullParser.START_TAG, null, "item");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("thumbnail")) {
                gameThumb = readThumbnail(parser);
            }else if (name.equals("image")) {
                gameImage = readImage(parser);
            }else {
                skip(parser);
            }
        }

        parser.require(XmlPullParser.END_TAG, null, "item");
        Game game = Game.findGameByName(gameName);
        game.gameBGGID = gameBGGID;
        game.gameImage = gameImage;
        game.gameThumb = gameThumb;
        game.save();
        return game;
    }



    private String readBGGID(XmlPullParser parser) throws IOException, XmlPullParserException {
        String bggid = "";
        String tag = parser.getName();
        if (tag.equals("item")) {
            bggid = parser.getAttributeValue(null, "id");
        }
        return bggid;
    }

    private String readTotal(XmlPullParser parser) throws IOException, XmlPullParserException {
        String total = "";
        String tag = parser.getName();
        if (tag.equals("items")) {
            total = parser.getAttributeValue(null, "total");
        }
        return total;
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