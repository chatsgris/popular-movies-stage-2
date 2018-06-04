package com.android.popularmoviesstage1.utils;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.popularmoviesstage1.R;
import com.android.popularmoviesstage1.data.MoviesContract;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;

public class CursorAdapter extends RecyclerView.Adapter<CursorAdapter.ViewHolder>{

    private Cursor mCursor;
    private Context mContext;
    private ItemClickListener mClickListener;
    String mMovieId;

    public CursorAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.movie_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String movieString = mCursor.getString(mCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_MOVIE_DATA));
        final int id = mCursor.getInt(mCursor.getColumnIndex(MoviesContract.MovieEntry._ID));
        mMovieId = String.valueOf(mCursor.getInt(mCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_MOVIE_ID)));

        String posterPath = null;
        String title = null;
        try {
            JSONObject movieInfo = new JSONObject(movieString);
            posterPath = JsonUtils.getPosterPath(movieInfo);
            title = JsonUtils.getMovieTitle(movieInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String posterUrl = NetworkUtils.buildPosterUrl(posterPath);
        Picasso.with(holder.movieImageView.getContext())
                .load(posterUrl)
                .placeholder(R.drawable.placeholder)
                .into(holder.movieImageView);
        holder.movieTitleView.setText(title);
        holder.itemView.setTag(id);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView movieImageView;
        TextView movieTitleView;

        public ViewHolder(View itemView) {
            super(itemView);
            movieImageView = itemView.findViewById(R.id.movie_image);
            movieTitleView = itemView.findViewById(R.id.movie_title);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onFavoriteClick(view, mMovieId);
            }
        }
    }

    public Cursor swapCursor(Cursor c) {
        if (mCursor == c) {
            return null;
        }
        Cursor temp = mCursor;
        this.mCursor = c;
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

    public void setFavoriteClickListener(CursorAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onFavoriteClick(View view, String movieId);
    }
}
