package com.android.popularmoviesstage1;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.popularmoviesstage1.utils.JsonUtils;
import com.android.popularmoviesstage1.utils.NetworkUtils;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private String mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    RecyclerViewAdapter(Context context, String data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.movie_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String posterPath = null;
        String title = null;
        try {
            JSONObject movieInfo = JsonUtils.getMovieInfo(mData, position);
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
    }

    @Override
    public int getItemCount() {
        JSONObject jsonObject;
        int count = 0;
        try {
            jsonObject = new JSONObject(mData);
            count = jsonObject.getJSONArray("results").length();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return count;
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
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}