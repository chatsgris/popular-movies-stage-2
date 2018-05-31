package com.android.popularmoviesstage1.data;

import android.provider.BaseColumns;

public class MoviesContract {
    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "tableFavorites";
        public static final String COLUMN_MOVIE_DATA = "movieData";
        public static final String COLUMN_TIMESTAMP = "timeStamp";
    }
}
