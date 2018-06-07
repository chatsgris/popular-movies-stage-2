package com.android.popularmoviesstage1.asynctasks;

import android.os.AsyncTask;
import com.android.popularmoviesstage1.utils.NetworkUtils;
import java.io.IOException;
import java.net.URL;

public class ReviewInfoQueryTask extends AsyncTask<URL, Void, String> {
    public interface ReviewInfoAsyncResponse {
        void processFinish(String output);
    }

    public ReviewInfoQueryTask.ReviewInfoAsyncResponse delegate = null;
    public ReviewInfoQueryTask(ReviewInfoQueryTask.ReviewInfoAsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(URL... urls) {
        URL url = urls[0];
        String reviewInfo = null;
        try {
            reviewInfo = NetworkUtils.getResponseFromHttpUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reviewInfo;
    }
}
