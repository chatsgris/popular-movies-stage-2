package com.android.popularmoviesstage1;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
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

import com.android.popularmoviesstage1.asynctasks.FavoriteAsyncHandler;
import com.android.popularmoviesstage1.asynctasks.ReviewInfoQueryTask;
import com.android.popularmoviesstage1.data.MoviesContract;
import com.android.popularmoviesstage1.utils.JsonUtils;
import com.android.popularmoviesstage1.utils.NetworkUtils;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.android.popularmoviesstage1.utils.NetworkUtils.isOnline;

public class DetailsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        ReviewInfoQueryTask.ReviewInfoAsyncResponse{

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
    int mPosition;
    String mMovieTitle;

    int MOVIE_SOURCE = 0;

    private static final String TAG = DetailsActivity.class.getSimpleName();
    private static final int LOADER_ID = 0;

    public void addFavorite(int movieId, String movieData, String movieTitle) {
        ContentValues cv = new ContentValues();
        cv.put(MoviesContract.MovieEntry.COLUMN_MOVIE_DATA, movieData);
        cv.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
        cv.put(MoviesContract.MovieEntry.COLUMN_TITLE, movieTitle);

        FavoriteAsyncHandler asyncHandler = new FavoriteAsyncHandler(getContentResolver()) {
            @Override
            protected void onInsertComplete(int token, Object cookie, Uri uri) {
                if(uri != null) {
                    Toast.makeText(DetailsActivity.this, "Movie added to Favorites", Toast.LENGTH_LONG).show();
                }
            }
        };
        asyncHandler.startInsert(1, null, MoviesContract.MovieEntry.CONTENT_URI, cv);
    }

    public void deleteFavorite(int movieId) {
        Uri uri = MoviesContract.MovieEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(String.valueOf(movieId)).build();

        FavoriteAsyncHandler asyncHandler = new FavoriteAsyncHandler(getContentResolver()) {
            @Override
            protected void onDeleteComplete(int token, Object cookie, int result) {
                if (result > 0) {
                    Toast.makeText(DetailsActivity.this, "Movie deleted from Favorites", Toast.LENGTH_LONG).show();
                } else {Toast.makeText(DetailsActivity.this, "Failed to delete", Toast.LENGTH_LONG).show();}
            }
        };
        asyncHandler.startDelete(1, null, uri, null, null);
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

            getSupportLoaderManager().initLoader(LOADER_ID, null, this);

            Context context = DetailsActivity.this;
            if (isOnline(context)) {
                setClicktoTrailer(mTrailerButton, mMovieId);
                setReviewView(mMovieId);
            } else {
                String message = "No internet. Trailer and Reviews are unavailable.";
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }

            handleFavoriteButton(mFavoriteButton, mMovieId, mMovieData, mMovieTitle);

        } else if (intentThatStartedThisActivity.hasExtra("POSITION")) {
            MOVIE_SOURCE = 2;
            mPosition = intentThatStartedThisActivity.getIntExtra("POSITION", 0);
            FavoriteAsyncHandler asyncHandler = new FavoriteAsyncHandler(getContentResolver()) {
                @Override
                protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                    JSONObject jsonObj;
                    String poster = null;
                    String release = null;
                    String vote = null;
                    String plot = null;
                    String movieTitle = null;
                    int movieId = 0;

                    if (cursor != null && cursor.moveToPosition(mPosition)) {
                        String movieData = cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_MOVIE_DATA));

                        try {
                            jsonObj = new JSONObject(movieData);
                            movieTitle = JsonUtils.getMovieTitle(jsonObj);
                            poster = NetworkUtils.buildPosterUrl(JsonUtils.getPosterPath(jsonObj));
                            release = JsonUtils.getMovieRelease(jsonObj);
                            vote = JsonUtils.getMovieVoting(jsonObj);
                            plot = JsonUtils.getMoviePlot(jsonObj);
                            movieId = JsonUtils.getMovieId(jsonObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        mTitle.setText(movieTitle);
                        Picasso.with(mPoster.getContext())
                                .load(poster)
                                .placeholder(R.drawable.placeholder)
                                .into(mPoster);
                        mRelease.setText(release);
                        mVoting.setText(vote);
                        mPlot.setText(plot);

                        if (isOnline(DetailsActivity.this)) {
                            setClicktoTrailer(mTrailerButton, movieId);
                            setReviewView(movieId);
                        } else {
                            String message = "No internet. Trailer and Reviews are unavailable.";
                            Toast.makeText(DetailsActivity.this, message, Toast.LENGTH_LONG).show();
                        }

                        handleFavoriteButton(mFavoriteButton, movieId, movieData, movieTitle);
                    }
                }
            };
            asyncHandler.startQuery(1,
                    null,
                    MoviesContract.MovieEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    MoviesContract.MovieEntry.COLUMN_MOVIE_ID);
        }
    }

    public void handleFavoriteButton(final ToggleButton toggleButton, final int movieId, final String movieData, final String movieTitle) {
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked && buttonView.isPressed()) {
                    toggleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.baseline_favorite_border_black_48dp));
                    deleteFavorite(movieId);
                } else if (isChecked && buttonView.isPressed()){
                    toggleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.baseline_favorite_black_48dp));
                    addFavorite(movieId, movieData, movieTitle);
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
        if (MOVIE_SOURCE == 1) {
            if (data != null && data.moveToNext()) {
                mFavoriteButton.setChecked(true);
                mFavoriteButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.baseline_favorite_black_48dp));
            } else {
                mFavoriteButton.setChecked(false);
                mFavoriteButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.baseline_favorite_border_black_48dp));
            }
        } else {
            mFavoriteButton.setChecked(true);
            mFavoriteButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.baseline_favorite_black_48dp));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void setClicktoTrailer(Button trailerButton, final int movieId) {
        trailerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri trailerUri = NetworkUtils.buildTrailerUri(MainActivity.mApiKey, movieId);
                Intent intent = new  Intent(Intent.ACTION_VIEW);
                intent.setData(trailerUri);

                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                boolean isIntentSafe = activities.size() > 0;
                if (isIntentSafe) {startActivity(intent);}
            }
        });
    }

    public void setReviewView(int movieId) {
        try {
            mReviewData = new ReviewInfoQueryTask(this).execute(NetworkUtils.buildReviewUrl(MainActivity.mApiKey, movieId)).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        recyclerView = findViewById(R.id.rv_reviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (mReviewData != null) {
            adapter = new ReviewAdapter(this, mReviewData);
            recyclerView.setAdapter(adapter);
        } else {
            String message = "No reviews available";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void processFinish(String output) {}
}
