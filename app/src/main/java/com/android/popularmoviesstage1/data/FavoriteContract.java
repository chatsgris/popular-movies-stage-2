package com.android.popularmoviesstage1.data;

import android.provider.BaseColumns;

public class FavoriteContract {
    public static final class FavoriteEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_MOVIE_DATA = "movieData";
        public static final String COLUMN_TIMESTAMP = "timeStamp";
    }
}
