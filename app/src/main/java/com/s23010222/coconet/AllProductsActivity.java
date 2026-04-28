package com.s23010222.coconet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AllProductsActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView titleText;
    private EditText searchEditText;
    private RecyclerView recyclerViewAllPosts;
    private TextView noPostsText;

    private List<FarmerPost> allPosts;
    private List<FarmerPost> filteredPosts;
    private FarmerPostAdapter farmerPostAdapter;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_products);

        initializeViews();
        getIntentData();
        setupRecyclerView();
        setupClickListeners();
        displayPosts();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.titleText);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerViewAllPosts = findViewById(R.id.recyclerViewAllPosts);
        noPostsText = findViewById(R.id.noPostsText);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        category = intent.getStringExtra("category");
        allPosts = intent.getParcelableArrayListExtra("all_posts");

        if (allPosts == null) {
            allPosts = new ArrayList<>();
        }

        filteredPosts = new ArrayList<>(allPosts);

        if (category != null && category.equals("coconut")) {
            titleText.setText("All Coconut Deals");
        } else {
            titleText.setText("All Other Deals");
        }
    }

    private void setupRecyclerView() {
        farmerPostAdapter = new FarmerPostAdapter(filteredPosts, this::onFarmerPostClick);
        recyclerViewAllPosts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewAllPosts.setAdapter(farmerPostAdapter);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPosts(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private void onFarmerPostClick(FarmerPost post) {
        Intent intent = new Intent(AllProductsActivity.this, ViewAdActivity.class);
        intent.putExtra("ad_id", post.getId());
        startActivity(intent);
    }

    private void displayPosts() {
        if (filteredPosts.isEmpty()) {
            recyclerViewAllPosts.setVisibility(View.GONE);
            noPostsText.setVisibility(View.VISIBLE);
            noPostsText.setText("No posts available");
        } else {
            recyclerViewAllPosts.setVisibility(View.VISIBLE);
            noPostsText.setVisibility(View.GONE);
            farmerPostAdapter.notifyDataSetChanged();
        }
    }

    private void filterPosts(String query) {
        filteredPosts.clear();

        if (query.isEmpty()) {
            filteredPosts.addAll(allPosts);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (FarmerPost post : allPosts) {
                if (matchesSearchQuery(post, lowerCaseQuery)) {
                    filteredPosts.add(post);
                }
            }
        }

        displayPosts();
    }

    private boolean matchesSearchQuery(FarmerPost post, String query) {
        return (post.getProductName() != null && post.getProductName().toLowerCase().contains(query)) ||
                (post.getDescription() != null && post.getDescription().toLowerCase().contains(query)) ||
                (post.getLocation() != null && post.getLocation().toLowerCase().contains(query)) ||
                (post.getPrice() != null && post.getPrice().toLowerCase().contains(query));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
