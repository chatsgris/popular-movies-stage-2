package com.android.popularmoviesstage1.utils;

import android.net.Uri;
import android.os.AsyncTask;

import com.android.popularmoviesstage1.DetailsActivity;
import com.android.popularmoviesstage1.MainActivity;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class NetworkUtils {
    final static String POSTER_BASE_URL =
            "http://image.tmdb.org/t/p/w185";
    final static String POPULARITY_URL =
            "http://api.themoviedb.org/3/movie/popular";
    final static String RATING_URL =
            "http://api.themoviedb.org/3/movie/top_rated";
    final static String TRAILER_URL =
            "https://www.youtube.com/watch";
    final static String GET_VIDEO_URL =
            "https://api.themoviedb.org/3/movie";

    public static Uri buildTrailerUri(String apiKey, int movieId) {
        String videoInfo;
        String videoKey = null;

        URL trailerUrl = NetworkUtils.buildGetVideoUrl(apiKey, movieId);
        try {
            videoInfo = new VideoInfoQueryTask().execute(trailerUrl).get();
            videoKey = JsonUtils.getVideoKey(videoInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Uri uri = Uri.parse(TRAILER_URL).buildUpon()
                .appendQueryParameter("v", videoKey)
                .build();
        return uri;
    }

    public static class VideoInfoQueryTask extends AsyncTask<URL, Void, String> {
        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            String videoInfo = null;
            try {
                videoInfo = NetworkUtils.getResponseFromHttpUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return videoInfo;
        }
    }

    public static URL buildReviewUrl (String apiKey, int movieId) {
        Uri uri = Uri.parse(GET_VIDEO_URL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath("reviews")
                .appendQueryParameter("api_key", apiKey)
                .build();
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static URL buildGetVideoUrl (String apiKey, int movieId) {
        Uri builtUri = Uri.parse(GET_VIDEO_URL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath("videos")
                .appendQueryParameter("api_key", apiKey)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static URL buildPopularityUrl (String apiKey) {
        Uri builtUri = Uri.parse(POPULARITY_URL).buildUpon()
                .appendQueryParameter("api_key", apiKey)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static URL buildRatingUrl (String apiKey) {
        Uri builtUri = Uri.parse(RATING_URL).buildUpon()
                .appendQueryParameter("api_key", apiKey)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String buildPosterUrl(String posterPath) {
        Uri builtUri = Uri.parse(POSTER_BASE_URL).buildUpon()
                .appendPath(posterPath)
                .build();
        return builtUri.toString();
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}