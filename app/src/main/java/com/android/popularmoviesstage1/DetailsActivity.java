package com.android.popularmoviesstage1;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.popularmoviesstage1.data.FavoriteContract;
import com.android.popularmoviesstage1.utils.JsonUtils;
import com.android.popularmoviesstage1.utils.NetworkUtils;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static com.android.popularmoviesstage1.MainActivity.mDb;
import static com.android.popularmoviesstage1.MainActivity.mSharedPreferences;

public class DetailsActivity extends AppCompatActivity {

    ReviewAdapter adapter;
    TextView mTitle;
    ImageView mPoster;
    TextView mRelease;
    TextView mVoting;
    TextView mPlot;
    ToggleButton mFavoriteButton;
    TextView mFavoriteText;
    Button mTrailerButton;
    RecyclerView recyclerView;

    JSONObject jsonObj;
    String title;
    String poster;
    String release;
    String vote;
    String plot;
    String movieId;
    String movieData;
    String mReviewData;
    Boolean favoriteStatus;
    Long viewId;

    public void addFavorite(String movieData) {
        ContentValues cv = new ContentValues();
        cv.put(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_DATA, movieData);
        mDb.insert(FavoriteContract.FavoriteEntry.TABLE_NAME, null, cv);
        MainActivity.favoritesAdapter.swapCursor(MainActivity.getAllFavorites());
    }

    public void removeFavorite(long id) {
        if (mDb.delete(FavoriteContract.FavoriteEntry.TABLE_NAME, FavoriteContract.FavoriteEntry._ID + "=" + id, null) > 0) {
            MainActivity.favoritesAdapter.swapCursor(MainActivity.getAllFavorites());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        findViews();
        Intent intentThatStartedThisActivity = getIntent();
        getDataFromIntent(intentThatStartedThisActivity);
        setDataToViews();
        setClicktoTrailer(mTrailerButton);
        setReviewView();
        handleFavoriteButton(mFavoriteButton);
    }

    public void handleFavoriteButton(final ToggleButton toggleButton) {
        if (favoriteStatus) {
            toggleButton.setChecked(true);
            toggleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.baseline_favorite_black_48dp));
        } else {
            toggleButton.setChecked(false);
            toggleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.baseline_favorite_border_black_48dp));
        }
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toggleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.baseline_favorite_black_48dp));
                    addFavorite(movieData);
                    mSharedPreferences.edit().putBoolean(movieId, true).apply();
                }
                else {
                    toggleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.baseline_favorite_border_black_48dp));
                    mSharedPreferences.edit().putBoolean(movieId, false).apply();
                    removeFavorite(viewId);
                }
            }
        });
    }

    public static class ReviewInfoQueryTask extends AsyncTask<URL, Void, String> {
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

    public void setDataToViews() {
        mTitle.setText(title);
        Picasso.with(mPoster.getContext())
                .load(poster)
                .placeholder(R.drawable.placeholder)
                .into(mPoster);
        mRelease.setText(release);
        mVoting.setText(vote);
        mPlot.setText(plot);
    }

    public void findViews() {
        mTitle = findViewById(R.id.detail_title);
        mPoster = findViewById(R.id.detail_poster);
        mRelease = findViewById(R.id.detail_release_date);
        mVoting = findViewById(R.id.detail_vote_score);
        mPlot = findViewById(R.id.detail_plot_data);
        mFavoriteButton = findViewById(R.id.detail_favorite_icon);
        mFavoriteText = findViewById(R.id.detail_favorite_text);
        mTrailerButton = findViewById(R.id.detail_watch_trailer);
    }

    public void setClicktoTrailer(Button trailerButton) {
        trailerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri trailerUri = NetworkUtils.buildTrailerUri(MainActivity.mApiKey, movieId);
                Intent intent = new  Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.google.android.youtube");
                intent.setData(trailerUri);
                startActivity(intent);
            }
        });
    }

    public void getDataFromIntent(Intent intent) {
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            movieData = intent.getStringExtra(Intent.EXTRA_TEXT);
            try {
                jsonObj = new JSONObject(movieData);
                title = JsonUtils.getMovieTitle(jsonObj);
                poster = NetworkUtils.buildPosterUrl(JsonUtils.getPosterPath(jsonObj));
                release = JsonUtils.getMovieRelease(jsonObj);
                vote = JsonUtils.getMovieVoting(jsonObj);
                plot = JsonUtils.getMoviePlot(jsonObj);
                movieId = JsonUtils.getMovieId(jsonObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (intent.hasExtra("favorite_status")) {
            favoriteStatus = intent.getBooleanExtra("favorite_status", false);
        }
    }

    public void setReviewView() {
        try {
            mReviewData = new ReviewInfoQueryTask().execute(NetworkUtils.buildReviewUrl(MainActivity.mApiKey, movieId)).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        recyclerView = findViewById(R.id.rv_reviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewAdapter(this, mReviewData);
        recyclerView.setAdapter(adapter);
    }
}
