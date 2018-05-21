package com.android.popularmoviesstage1;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.popularmoviesstage1.utils.JsonUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private String mData;
    private LayoutInflater mInflater;

    ReviewAdapter(Context context, String data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = mInflater.inflate(R.layout.review_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String author = null;
        String review = null;
        try {
            JSONObject reviewInfo = JsonUtils.getReviewInfo(mData, position);
            author = JsonUtils.getAuthor(reviewInfo);
            review = JsonUtils.getReview(reviewInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        holder.reviewTv.setText(review);
        holder.authorTv.setText(author);
    }

    @Override
    public int getItemCount() {
        JSONObject jsonObject;
        int count = 0;
        try {
            jsonObject = new JSONObject(mData);
            count = JsonUtils.getReviewCount(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return count;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView reviewTv;
        TextView authorTv;

        ViewHolder(View itemView) {
            super(itemView);
            reviewTv = itemView.findViewById(R.id.review_content);
            authorTv = itemView.findViewById(R.id.review_author);
        }
    }
}
