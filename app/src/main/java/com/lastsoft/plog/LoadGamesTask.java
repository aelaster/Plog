package com.lastsoft.plog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by TheFlash on 5/25/2015.
 */
public class LoadGamesTask extends AsyncTask<String, Void, String> {

    Context theContext;
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
        String bggProcess = "false";
        /*
        BGGLogInHelper helper = new BGGLogInHelper(theContext, null);
        if (helper.checkCookies()) {
            UrlEncodedFormEntity entity;
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("ajax", "1"));
            nvps.add(new BasicNameValuePair("action", "save"));
            nvps.add(new BasicNameValuePair("version", "2"));
            nvps.add(new BasicNameValuePair("objecttype", "thing"));
            nvps.add(new BasicNameValuePair("objectid", String.valueOf(145659)));
            nvps.add(new BasicNameValuePair("playdate", "2015-06-15"));
            // TODO: ask Aldie what this is
            nvps.add(new BasicNameValuePair("dateinput", "2015-06-15"));
            nvps.add(new BasicNameValuePair("length", String.valueOf(60)));
            nvps.add(new BasicNameValuePair("location", "Mi Casa"));
            nvps.add(new BasicNameValuePair("quantity", String.valueOf(1)));
            nvps.add(new BasicNameValuePair("incomplete", "0"));
            nvps.add(new BasicNameValuePair("nowinstats", "0"));
            nvps.add(new BasicNameValuePair("comments", "TESTIE!"));

            try {
                entity = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
                HttpPost post = new HttpPost("http://www.boardgamegeek.com/geekplay.php");
                post.setEntity(entity);
                HttpClient mClient = HttpUtils.createHttpClient(theContext, helper.getCookieStore());
                HttpResponse response = mClient.execute(post);
            } catch (UnsupportedEncodingException e) {
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }*/


        try {

            // first we go through and add every game in the collection
            URL url;

            SharedPreferences app_preferences;
            SharedPreferences.Editor editor;
            app_preferences = PreferenceManager.getDefaultSharedPreferences(theContext);
            editor = app_preferences.edit();
            long currentDefaultPlayer = app_preferences.getLong("defaultPlayer", -1);
            if (currentDefaultPlayer >=0 ) {
                Player defaultPlayer = Player.findById(Player.class, currentDefaultPlayer);
                if (defaultPlayer != null) {
                    //Log.d("V1", "https://www.boardgamegeek.com/xmlapi2/collection?username=" + defaultPlayer.bggUsername);
                    myString = getGamesCollection(defaultPlayer.bggUsername);
                   // Log.d("V1", myString);
                    if (myString != null && myString.contains("will be processed")){
                        //do it again
                        bggProcess = "true";
                    }

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
                                //entries.add(readEntry(parser));
                                int total = 0;
                                total = Integer.parseInt(readTotal(parser));

                                //mDataset = new String[total];
                                //mDataset_Thumb = new String[total];
                            } else if (name.equals("item")) {
                                //Log.d("V1", "name = " + mDataset[i]);
                                readEntry(parser, readBGGID(parser));
                            } else {
                                skip(parser);
                            }
                        }


                        // then we go through and flag every expansion in my collection
                        url = new URL("https://www.boardgamegeek.com/xmlapi2/collection?username=" + defaultPlayer.bggUsername + "&subtype=boardgameexpansion");
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

                        bis.close();
                        is.close();

                        if (myString != null) {

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
                                //Log.d("V1", "name = " + name);
                                // Starts by looking for the entry tag
                                if (name.equals("items")) {
                                    //entries.add(readEntry(parser));
                                    int total = 0;
                                    total = Integer.parseInt(readTotal(parser));

                                    //mDataset = new String[total];
                                    //mDataset_Thumb = new String[total];
                                } else if (name.equals("item")) {
                                    //Log.d("V1", "name = " + mDataset[i]);
                                    readEntry_Expansion(parser);
                                } else {
                                    skip(parser);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bggProcess;
    }

    @Override
    protected void onPostExecute(final String result) {
        Log.d("V1", "result = " + result);
        if (result.equals("true")) {
            Toast.makeText(theContext, theContext.getString(R.string.bgg_process_notice), Toast.LENGTH_LONG).show();
        }
    }

    private Game readEntry(XmlPullParser parser, String gameBGGID) throws XmlPullParserException, IOException {
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
                    return null;
                }
            } else if (name.equals("status")) {
                //gameOwn = readOwn(parser);
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
            Game game = new Game(gameName, gameBGGID, gameImage, gameThumb, false);
            game.save();
            return game;
        }else{
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