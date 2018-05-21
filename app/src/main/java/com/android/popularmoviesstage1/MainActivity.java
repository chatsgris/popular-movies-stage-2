package com.android.popularmoviesstage1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.android.popularmoviesstage1.data.FavoriteContract;
import com.android.popularmoviesstage1.data.FavoriteDbHelper;
import com.android.popularmoviesstage1.utils.JsonUtils;
import com.android.popularmoviesstage1.utils.NetworkUtils;
import org.json.JSONException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements
        RecyclerViewAdapter.RecyclerAdapterOnClickHandler, FavoritesAdapter.FavoritesAdapterOnClickHandler {

    RecyclerViewAdapter adapter;
    static FavoritesAdapter favoritesAdapter;
    public static String mApiKey = "6395d986e7ebd845e21161e14ab87ee7";
    String mMoviesData;
    RecyclerView mRecyclerView;
    public static SQLiteDatabase mDb;
    public static SharedPreferences mSharedPreferences;

    private void LoadFavoritesData() {
        if (isOnline()) {
            FavoriteDbHelper dbHelper = new FavoriteDbHelper(this);
            mDb = dbHelper.getWritableDatabase();
            Cursor cursor = getAllFavorites();

            favoritesAdapter = new FavoritesAdapter(this, cursor, this);
            mRecyclerView.setAdapter(favoritesAdapter);
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        } else {
            Context context = MainActivity.this;
            String message = "No internet connection detected.";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    public static Cursor getAllFavorites() {
        Cursor c = mDb.query(
                FavoriteContract.FavoriteEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                FavoriteContract.FavoriteEntry.COLUMN_TIMESTAMP + " DESC"
        );
        return c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.rvMovies);
        int numberOfColumns = 2;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        LoadMoviesPopularityData();
    }
    private void LoadMoviesPopularityData() {
        if (isOnline()) {
            try {
                mMoviesData = new MoviesDatabaseQueryTask().execute(
                        NetworkUtils.buildPopularityUrl(mApiKey)
                ).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            FavoriteDbHelper dbHelper = new FavoriteDbHelper(this);
            mDb = dbHelper.getWritableDatabase();

            adapter = new RecyclerViewAdapter(this, mMoviesData);
            adapter.setClickListener(this);
            mRecyclerView.setAdapter(adapter);

            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        } else {
            Context context = MainActivity.this;
            String message = "No internet connection detected.";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    private void LoadMoviesRatingData() {
        if (isOnline()) {
            try {
                mMoviesData = new MoviesDatabaseQueryTask().execute(
                        NetworkUtils.buildRatingUrl(mApiKey)
                ).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            adapter = new RecyclerViewAdapter(this, mMoviesData);
            adapter.setClickListener(this);
            mRecyclerView.setAdapter(adapter);

            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        } else {
            Context context = MainActivity.this;
            String message = "No internet connection detected.";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onFavoritesClick(String movieData, int viewId) {
        Intent myIntent = new Intent(MainActivity.this, DetailsActivity.class);
        myIntent.putExtra(Intent.EXTRA_TEXT, movieData);
        myIntent.putExtra("tag_id", viewId);
        MainActivity.this.startActivity(myIntent);
    }

    public class MoviesDatabaseQueryTask extends AsyncTask<URL, Void, String> {
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
    }

    @Override
    public void onRecyclerClick(View view, int position) {
        Intent myIntent = new Intent(MainActivity.this, DetailsActivity.class);
        String movieData = null;
        String movieId = null;
        try {
            movieData = JsonUtils.getMovieInfo(mMoviesData, position).toString();
            movieId = JsonUtils.getMovieId(JsonUtils.getMovieInfo(mMoviesData, position));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        myIntent.putExtra(Intent.EXTRA_TEXT, movieData);
        Boolean favorite_status = mSharedPreferences.getBoolean(movieId, false);
        myIntent.putExtra("favorite_status", favorite_status);
        MainActivity.this.startActivity(myIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sort_by, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int sortBy = item.getItemId();
        if (sortBy == R.id.sort_popularity) {
            LoadMoviesPopularityData();
            return true;
        } else if (sortBy == R.id.sort_rating) {
            LoadMoviesRatingData();
            return true;
        } else if (sortBy == R.id.sort_favorite) {
            LoadFavoritesData();
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
