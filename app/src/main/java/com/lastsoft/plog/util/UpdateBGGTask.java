package com.lastsoft.plog.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.lastsoft.plog.R;
import com.lastsoft.plog.db.Game;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TheFlash on 5/25/2015.
 */
public class UpdateBGGTask extends AsyncTask<String, Void, String> {
    private final ProgressDialog mydialog;
    Context theContext;
    boolean addMe;
    public UpdateBGGTask(Context context, boolean addToCollection){
        this.theContext = context;
        this.addMe = addToCollection;
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
        String bggID = args[0];
        int i = 0;
        int totalCount = 0;

        try {

            // first we search for the game by its name
            /*URL url;
            Log.d("V1", "https://www.boardgamegeek.com/xmlapi2/search?exact=1&query=" + URLEncoder.encode(args[0], "UTF-8"));
            url = new URL("https://www.boardgamegeek.com/xmlapi2/search?exact=1&query=" + URLEncoder.encode(args[0], "UTF-8"));
            URLConnection ucon = url.openConnection();
            ucon.setConnectTimeout(3000);
            ucon.setReadTimeout(30000);

            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is, 1024);

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
                    bggID = readBGGID(parser);
                    break;
                } else {
                    skip(parser);
                }
            }*/
            //if (!bggID.equals("")) {
                // then we go through and flag every expansion in my collection

            Log.d("V1", "https://www.boardgamegeek.com/xmlapi2/thing?id=" + bggID);
            URL url;
            url = new URL("https://www.boardgamegeek.com/xmlapi2/thing?id=" + bggID);
            URLConnection ucon = url.openConnection();
            ucon.setConnectTimeout(3000);
            ucon.setReadTimeout(30000);

            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is, 1024);

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
                // Starts by looking for the entry tag
                if (name.equals("items")) {
                } else if (name.equals("item")) {
                    readEntry(parser, args[1], bggID);
                    break;
                } else {
                    skip(parser);
                }
            }
           // }


            if (addMe){
                //try adding this bia
                ///geekcollection.php?fieldname=status&collid=&objecttype=thing&objectid=157327&own=1&B1=Cancel&wishlistpriority=1&ajax=1&action=savedata
                BGGLogInHelper helper = new BGGLogInHelper(theContext, null);
                if (helper.canLogIn() && helper.checkCookies()) {
                    UrlEncodedFormEntity entity;
                    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                    nvps.add(new BasicNameValuePair("fieldname", "status"));
                    nvps.add(new BasicNameValuePair("objecttype", "thing"));
                    nvps.add(new BasicNameValuePair("objectid", bggID));
                    nvps.add(new BasicNameValuePair("own", "1"));
                    nvps.add(new BasicNameValuePair("B1", "Cancel"));
                    nvps.add(new BasicNameValuePair("wishlistpriority", "1"));
                    nvps.add(new BasicNameValuePair("ajax", "1"));
                    nvps.add(new BasicNameValuePair("action", "savedata"));
                    try {
                        entity = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
                        HttpPost post = new HttpPost("http://www.boardgamegeek.com/geekcollection.php");
                        post.setEntity(entity);
                        HttpClient mClient = HttpUtils.createHttpClient(theContext, helper.getCookieStore());
                        HttpResponse response = mClient.execute(post);
                        if (response == null) {

                        } else if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {

                        } else {

                        }
                    } catch (UnsupportedEncodingException e) {
                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
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
        if (game != null) {
            game.gameBGGID = gameBGGID;
            game.gameImage = gameImage;
            game.gameThumb = gameThumb;
            game.save();
        }
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