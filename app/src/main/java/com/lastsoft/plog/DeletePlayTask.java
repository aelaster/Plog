package com.lastsoft.plog;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.util.BGGLogInHelper;
import com.lastsoft.plog.util.HttpUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TheFlash on 5/25/2015.
 */
public class DeletePlayTask extends AsyncTask<Play, Void, String> {

    Context theContext;
    public DeletePlayTask(Context context){
        this.theContext = context;
    }

    @Override
    protected String doInBackground(final Play... playToLog) {

        String myString = "";
        String bggProcess = "false";

        BGGLogInHelper helper = new BGGLogInHelper(theContext, null);
        if (helper.checkCookies()) {
            UrlEncodedFormEntity entity;
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("ajax", "1"));
            nvps.add(new BasicNameValuePair("action", "delete"));
            nvps.add(new BasicNameValuePair("playid", playToLog[0].bggPlayID));



            try {
                entity = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
                HttpPost post = new HttpPost("http://www.boardgamegeek.com/geekplay.php");
                post.setEntity(entity);
                HttpClient mClient = HttpUtils.createHttpClient(theContext, helper.getCookieStore());
                HttpResponse response = mClient.execute(post);
                if (response == null) {

                } else if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {

                } else {
                    String theResponse = HttpUtils.parseResponse(response);

                }


                //Log.d("V1", response.getStatusLine().);
            } catch (UnsupportedEncodingException e) {
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


        return bggProcess;
    }

    @Override
    protected void onPostExecute(final String result) {
        if (result.equals("true")) {
            Toast.makeText(theContext, theContext.getString(R.string.bgg_process_notice), Toast.LENGTH_LONG).show();
        }
    }



}