package com.android.popularmoviesstage1.asynctasks;

import android.os.AsyncTask;

import com.android.popularmoviesstage1.utils.NetworkUtils;

import java.io.IOException;
import java.net.URL;

public class MoviesDatabaseQueryTask extends AsyncTask<URL, Void, String> {
    public interface MoviesDatabaseAsyncResponse {
        void processFinish(String output);
    }

    public MoviesDatabaseAsyncResponse delegate = null;
    public MoviesDatabaseQueryTask(MoviesDatabaseAsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(URL... urls) {
        URL url = urls[0];
        String moviesDatabaseResults = null;
        try {
            moviesDatabaseResults = NetworkUtils.getResponseFromHttpUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return moviesDatabaseResults;
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }
}
