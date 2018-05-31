package com.android.popularmoviesstage1;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.android.popularmoviesstage1.utils.JsonUtils;
import com.android.popularmoviesstage1.utils.NetworkUtils;
import org.json.JSONException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener {
    RecyclerViewAdapter adapter;
    static String mApiKey = "6395d986e7ebd845e21161e14ab87ee7";
    String mMoviesData;
    RecyclerView mRecyclerView;
    String LOG_SORT = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.rvMovies);
        int numberOfColumns = 2;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        if (savedInstanceState != null) {
            LOG_SORT = savedInstanceState.getString("LOG_SORT");
            switch (LOG_SORT) {
                case "popularity": LoadMoviesPopularityData();
                break;
                case "rating": LoadMoviesRatingData();
                break;
                case "": LoadMoviesPopularityData();
                break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("LOG_SORT", LOG_SORT);
        super.onSaveInstanceState(outState);
        mRecyclerView = findViewById(R.id.rvMovies);
        int numberOfColumns = 2;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
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
            adapter = new RecyclerViewAdapter(this, mMoviesData);
            adapter.setClickListener(this);
            mRecyclerView.setAdapter(adapter);
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
        } else {
            Context context = MainActivity.this;
            String message = "No internet connection detected.";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
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
    public void onItemClick(View view, int position) {
        Intent myIntent = new Intent(MainActivity.this, DetailsActivity.class);
        String movieData = null;
        try {
            movieData = JsonUtils.getMovieInfo(mMoviesData, position).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        myIntent.putExtra(Intent.EXTRA_TEXT, movieData);
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
            LOG_SORT = "popularity";
            LoadMoviesPopularityData();
            return true;
        } else if (sortBy == R.id.sort_rating) {
            LOG_SORT = "rating";
            LoadMoviesRatingData();
            return true;
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