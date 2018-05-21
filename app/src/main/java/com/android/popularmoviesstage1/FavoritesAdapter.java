package com.android.popularmoviesstage1;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.popularmoviesstage1.data.FavoriteContract;
import com.android.popularmoviesstage1.utils.JsonUtils;
import com.android.popularmoviesstage1.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
    private Cursor mCursor;
    private Context mContext;
    final private FavoritesAdapterOnClickHandler mClickHandler;

    public interface FavoritesAdapterOnClickHandler {
        void onFavoritesClick(String movieData, int viewid);
    }

    public FavoritesAdapter(Context context, Cursor cursor, FavoritesAdapterOnClickHandler clickHandler) {
        this.mContext = context;
        this.mCursor = cursor;
        this.mClickHandler = clickHandler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.movie_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position))
            return;

        String movieString = mCursor.getString(mCursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_DATA));
        long id = mCursor.getLong(mCursor.getColumnIndex(FavoriteContract.FavoriteEntry._ID));
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
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) mCursor.close();
        mCursor = newCursor;
        if (newCursor != null) {
            this.notifyDataSetChanged();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView movieImageView;
        TextView movieTitleView;

        ViewHolder(View itemView) {
            super(itemView);
            movieImageView = itemView.findViewById(R.id.movie_image);
            movieTitleView = itemView.findViewById(R.id.movie_title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            String movieData = mCursor.getString(mCursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_MOVIE_DATA));
            int viewId = view.getId();
            mClickHandler.onFavoritesClick(movieData, viewId);
        }
    }
}
