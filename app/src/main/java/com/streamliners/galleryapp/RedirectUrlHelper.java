package com.streamliners.galleryapp;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Represent class for redirected URL(s)
 */

public class RedirectUrlHelper extends AsyncTask<String,Void,String> {

    private OnFetchedUrlListener listener;

    public RedirectUrlHelper fetchRedirectedURL(OnFetchedUrlListener listener){
        this.listener = listener;
        return this;
    }

    @Override
    protected String doInBackground(String... url) {

        String redURL = "";
        try {
            URL urlTmp = null;
            HttpURLConnection connection = null;

            try {
                urlTmp = new URL(url[0]);
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }

            try {
                connection = (HttpURLConnection) urlTmp.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                connection.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
            }

            redURL = connection.getURL().toString();
            connection.disconnect();

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return redURL;
    }

    protected void onPostExecute(String redURL) {
        listener.onFetchedUrl(redURL);
    }

    /**
     * For the callback
     */
    interface OnFetchedUrlListener{
        void onFetchedUrl(String url);
    }
}
