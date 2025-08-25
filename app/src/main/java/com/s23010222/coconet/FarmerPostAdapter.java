package com.s23010222.coconet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class FarmerPostAdapter extends RecyclerView.Adapter<FarmerPostAdapter.PostViewHolder> {

    private List<FarmerPost> posts;
    private OnPostClickListener listener;

    public interface OnPostClickListener {
        void onPostClick(FarmerPost post);
    }

    public FarmerPostAdapter(List<FarmerPost> posts, OnPostClickListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_farmer_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        FarmerPost post = posts.get(position);
        holder.bind(post, listener);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvProductName;
        private TextView tvPrice;
        private TextView tvQuantity;
        private TextView tvLocation;
        private ImageView ivProductImage;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvLocation = itemView.findViewById(R.id.tv_location);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
        }

        public void bind(FarmerPost post, OnPostClickListener listener) {
            tvProductName.setText(post.getProductName());
            tvPrice.setText("Rs " + post.getPrice());
            tvQuantity.setText("QTY: " + post.getQuantity());
            tvQuantity.setTextColor(0xFF4CAF50);
            tvQuantity.setTypeface(tvQuantity.getTypeface(), android.graphics.Typeface.BOLD);
            tvLocation.setText(post.getLocation());
            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(post.getImageUrl())
                        .placeholder(R.drawable.img)
                        .centerCrop()
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.img);
            }
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostClick(post);
                }
            });
        }
    }
}
