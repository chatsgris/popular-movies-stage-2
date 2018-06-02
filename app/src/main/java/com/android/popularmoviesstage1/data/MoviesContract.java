package com.android.popularmoviesstage1.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class MoviesContract {

    public static final String AUTHORITY = "com.android.popularmoviesstage1";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_MOVIES = "movies";

    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String TABLE_NAME = "tableFavorites";
        public static final String COLUMN_MOVIE_DATA = "movieData";
        public static final String COLUMN_TITLE = "movieTitle";
        public static final String COLUMN_MOVIE_ID ="movieId";
    }
}
