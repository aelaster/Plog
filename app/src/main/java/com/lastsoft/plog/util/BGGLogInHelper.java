package com.lastsoft.plog.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.lastsoft.plog.db.Player;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class BGGLogInHelper {
    private static final String TAG = "LogInTask";

    private Context mContext;
    private LogInListener mListener;
    private Cookies mCookies;
    private String mUsername;
    private String mPassword;

    public BGGLogInHelper(Context context, LogInListener listner) {
        mContext = context;
        mListener = listner;
        mCookies = new Cookies(mContext);
    }

    public CookieStore getCookieStore() {
        return mCookies.getCookieStore();
    }

    public void logOut(){
        if (mCookies != null){
            mCookies.clearCookies();
            if (mCookies.getCookieStore() != null) {
                mCookies.setCookieStore(null);
            }
        }
    }

    public void logIn() {
        if (checkCookies()) {
            if (mListener != null) {
                mListener.onLogInSuccess();
            }
        }

        if (!canLogIn()) {
            if (mListener != null) {
                mListener.onNeedCredentials();
            }
            return;
        }
        new LogInTask().execute();
    }

    public boolean checkCookies() {
        if (mCookies.getCookieStore() != null) {
            return true;
        }
        if (mCookies.loadCookies() != null) {
            return true;
        }
        return false;
    }

    public boolean canLogIn() {
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        long currentDefaultPlayer = app_preferences.getLong("defaultPlayer", -1);
        if (currentDefaultPlayer>=0) {
            Player defaultPlayer = Player.findById(Player.class, currentDefaultPlayer);
            mUsername = defaultPlayer.bggUsername;
            mPassword = defaultPlayer.bggPassword;
        }
        if (TextUtils.isEmpty(mUsername) || TextUtils.isEmpty(mPassword)) {
            return false;
        }
        return true;
    }

    class LogInTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (!canLogIn()) {
                //return mContext.getResources().getString(R.string.setUsernamePassword);
            }



            final DefaultHttpClient client = (DefaultHttpClient) HttpUtils.createHttpClient(mContext, false);
            final HttpPost post = new HttpPost("http://www.boardgamegeek.com/login");
            List<NameValuePair> pair = new ArrayList<NameValuePair>();
            pair.add(new BasicNameValuePair("username", mUsername));
            pair.add(new BasicNameValuePair("password", mPassword));

            UrlEncodedFormEntity entity;
            try {
                entity = new UrlEncodedFormEntity(pair, HTTP.UTF_8);
            } catch (UnsupportedEncodingException e) {
                return e.toString();
            }
            post.setEntity(entity);

            HttpResponse response;
            try {
                response = client.execute(post);
            } catch (ClientProtocolException e) {
                return e.toString();
            } catch (IOException e) {
                return e.toString();
            } finally {
                client.getConnectionManager().shutdown();
            }

            if (response == null) {
                return createErrorMessage(0);
            }


            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                return createErrorMessage(0);
            }

            List<Cookie> cookies = client.getCookieStore().getCookies();
            if (cookies == null || cookies.isEmpty()) {
                return createErrorMessage(0);
            }

            boolean foundPW = false;
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("bggpassword")) {
                    foundPW = true;
                    mCookies.setCookieStore(client.getCookieStore());
                    break;
                }
            }

            if (!foundPW){
                logOut();
            }

            if (mCookies.getCookieStore() == null) {
                return createErrorMessage(1);
            }

            mCookies.saveCookies();
            return null;
        }

        private String createErrorMessage(int type) {
            String message = "error";
            if (type == 0){
                //connection error
                message = "connection";
            }else{
                message = "credentials";
            }
            return message;
        }

        @Override
        protected void onPostExecute(String result) {
            if (mListener != null) {
                if (!TextUtils.isEmpty(result)) {
               //     Log.w(TAG, result);
                    mListener.onLogInError(result);
                } else {
                    mListener.onLogInSuccess();
                }
            }
        }
    }

    public interface LogInListener {
        public void onLogInSuccess();

        public void onLogInError(String errorMessage);

        public void onNeedCredentials();
    }
}