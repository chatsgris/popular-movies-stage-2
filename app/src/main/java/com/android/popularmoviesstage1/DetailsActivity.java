package com.android.popularmoviesstage1;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.android.popularmoviesstage1.data.MoviesContract;
import com.android.popularmoviesstage1.utils.JsonUtils;
import com.android.popularmoviesstage1.utils.NetworkUtils;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class DetailsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

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

    String mReviewData;
    String mMovieData;
    int mMovieId;
    String mMovieTitle;

    int MOVIE_SOURCE = 0;

    private static final String TAG = DetailsActivity.class.getSimpleName();
    private static final int LOADER_ID = 0;

    public void addFavorite() {
        ContentValues cv = new ContentValues();
        cv.put(MoviesContract.MovieEntry.COLUMN_MOVIE_DATA, mMovieData);
        cv.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, mMovieId);
        cv.put(MoviesContract.MovieEntry.COLUMN_TITLE, mMovieTitle);

        Uri uri = getContentResolver().insert(MoviesContract.MovieEntry.CONTENT_URI, cv);
        if(uri != null) {
            Toast.makeText(this, "Movie added to Favorites", Toast.LENGTH_LONG).show();
        }
    }

    public void deleteFavorite() {
        Uri uri = MoviesContract.MovieEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(String.valueOf(mMovieId)).build();
        int i = getContentResolver().delete(uri, null, null);
        if (i > 0) {
            Toast.makeText(this, "Movie deleted from Favorites", Toast.LENGTH_LONG).show();
        } else {Toast.makeText(this, "Failed to delete", Toast.LENGTH_LONG).show();}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mTitle = findViewById(R.id.detail_title);
        mPoster = findViewById(R.id.detail_poster);
        mRelease = findViewById(R.id.detail_release_date);
        mVoting = findViewById(R.id.detail_vote_score);
        mPlot = findViewById(R.id.detail_plot_data);
        mFavoriteButton = findViewById(R.id.detail_favorite_icon);
        mFavoriteText = findViewById(R.id.detail_favorite_text);
        mTrailerButton = findViewById(R.id.detail_watch_trailer);

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
            MOVIE_SOURCE = 1;
            mMovieData = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT);

            JSONObject jsonObj;
            String poster = null;
            String release = null;
            String vote = null;
            String plot = null;

            try {
                jsonObj = new JSONObject(mMovieData);
                mMovieTitle = JsonUtils.getMovieTitle(jsonObj);
                poster = NetworkUtils.buildPosterUrl(JsonUtils.getPosterPath(jsonObj));
                release = JsonUtils.getMovieRelease(jsonObj);
                vote = JsonUtils.getMovieVoting(jsonObj);
                plot = JsonUtils.getMoviePlot(jsonObj);
                mMovieId = JsonUtils.getMovieId(jsonObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mTitle.setText(mMovieTitle);
            Picasso.with(mPoster.getContext())
                    .load(poster)
                    .placeholder(R.drawable.placeholder)
                    .into(mPoster);
            mRelease.setText(release);
            mVoting.setText(vote);
            mPlot.setText(plot);
        } else if (intentThatStartedThisActivity.hasExtra("MOVIE_ID")) {
            MOVIE_SOURCE = 2;
            mMovieId = Integer.parseInt(intentThatStartedThisActivity.getStringExtra("MOVIE_ID"));
        }

        handleFavoriteButton(mFavoriteButton);
        setClicktoTrailer(mTrailerButton);
        setReviewView();
    }

    public void handleFavoriteButton(final ToggleButton toggleButton) {
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked && buttonView.isPressed()) {
                    toggleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.baseline_favorite_border_black_48dp));
                    deleteFavorite();
                } else if (isChecked && buttonView.isPressed()){
                    toggleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.baseline_favorite_black_48dp));
                    addFavorite();
                }
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {
        return new AsyncTaskLoader<Cursor>(this) {
            Cursor mMoviesData = null;

            @Override
            protected void onStartLoading() {
                if (mMoviesData != null) {
                    deliverResult(mMoviesData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    return getContentResolver().query(MoviesContract.MovieEntry.CONTENT_URI,
                            null,
                            MoviesContract.MovieEntry.COLUMN_MOVIE_ID + "=?",
                            new String[] {String.valueOf(mMovieId)},
                            null);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(Cursor data) {
                mMoviesData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            if (MOVIE_SOURCE == 2) {
                mMovieData = data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_MOVIE_DATA));

                JSONObject jsonObj;
                String poster = null;
                String release = null;
                String vote = null;
                String plot = null;

                try {
                    jsonObj = new JSONObject(mMovieData);
                    mMovieTitle = JsonUtils.getMovieTitle(jsonObj);
                    poster = NetworkUtils.buildPosterUrl(JsonUtils.getPosterPath(jsonObj));
                    release = JsonUtils.getMovieRelease(jsonObj);
                    vote = JsonUtils.getMovieVoting(jsonObj);
                    plot = JsonUtils.getMoviePlot(jsonObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mTitle.setText(mMovieTitle);
                Picasso.with(mPoster.getContext())
                        .load(poster)
                        .placeholder(R.drawable.placeholder)
                        .into(mPoster);
                mRelease.setText(release);
                mVoting.setText(vote);
                mPlot.setText(plot);
            }
            mFavoriteButton.setChecked(true);
            mFavoriteButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.baseline_favorite_black_48dp));
        } else {
            mFavoriteButton.setChecked(false);
            mFavoriteButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.baseline_favorite_border_black_48dp));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
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

    public void setClicktoTrailer(Button trailerButton) {
        trailerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri trailerUri = NetworkUtils.buildTrailerUri(MainActivity.mApiKey, mMovieId);
                Intent intent = new  Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.google.android.youtube");
                intent.setData(trailerUri);
                startActivity(intent);
            }
        });
    }

    public void setReviewView() {
        try {
            mReviewData = new ReviewInfoQueryTask().execute(NetworkUtils.buildReviewUrl(MainActivity.mApiKey, mMovieId)).get();
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
