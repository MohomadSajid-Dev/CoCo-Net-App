package com.s23010222.coconet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FarmerDashboardActivity extends AppCompatActivity {

    private EditText searchEditText;
    private CircleImageView profileImage;
    private LinearLayout addPostLayout;
    private TextView seeAllText;
    private TextView usernameText;
    private RecyclerView recyclerViewPosts;

    private LinearLayout homeTab, locationTab, ordersTab, notificationTab, menuTab;

    private FirebaseFirestore db;
    private List<FarmerPost> farmerPosts;
    private FarmerPostAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_dashboard);

        db = FirebaseFirestore.getInstance();
        farmerPosts = new ArrayList<>();

        initViews();
        setupRecyclerView();
        setupListeners();
        loadFarmerPosts();
        setupUserData();
    }

    private void initViews() {
        searchEditText = findViewById(R.id.searchEditText);
        profileImage = findViewById(R.id.profileImage);
        addPostLayout = findViewById(R.id.addPostLayout);
        seeAllText = findViewById(R.id.seeAllText);
<<<<<<< HEAD
        usernameText = findViewById(R.id.usernameText);
=======
>>>>>>> fef8d6bb83afa6b870f34fe1cd4dc7fded77f4da
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);

        homeTab = findViewById(R.id.homeTab);
        locationTab = findViewById(R.id.locationTab);
        ordersTab = findViewById(R.id.ordersTab);
        notificationTab = findViewById(R.id.notificationTab);
        menuTab = findViewById(R.id.menuTab);
    }

    private void setupRecyclerView() {
        adapter = new FarmerPostAdapter(farmerPosts, this::onPostClick);
        recyclerViewPosts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewPosts.setAdapter(adapter);
    }

    private void onPostClick(FarmerPost post) {
        Intent intent = new Intent(FarmerDashboardActivity.this, ViewAdActivity.class);
        intent.putExtra("ad_id", post.getId());
        intent.putExtra("user_type", "farmer");
        startActivity(intent);
    }

    private void setupListeners() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String searchText = s.toString().trim();
                if (!searchText.isEmpty()) {
                    performSearch(searchText);
                }
            }
        });

        profileImage.setOnClickListener(v -> {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
        });

        addPostLayout.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, PostAdActivity.class);
            startActivity(intent);
        });



        seeAllText.setOnClickListener(v -> {
            openAllProductsActivity("all", farmerPosts);
        });

        homeTab.setOnClickListener(v -> {
            Toast.makeText(this, "Home selected", Toast.LENGTH_SHORT).show();
        });

        locationTab.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, LocateDistributorsActivity.class);
            startActivity(intent);
        });

        ordersTab.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, NewOrdersActivity.class);
            startActivity(intent);
        });

        notificationTab.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        menuTab.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void loadFarmerPosts() {
        db.collection("farmer_posts")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    farmerPosts.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        FarmerPost post = new FarmerPost();
                        post.setId(document.getId());
                        post.setProductName(document.getString("productName"));
                        post.setDescription(document.getString("description"));
                        post.setQuantity(document.getString("quantity"));
                        post.setPrice(document.getString("price"));
                        post.setAvailability(document.getString("availability"));
                        post.setStockCondition(document.getString("stockCondition"));
                        post.setMobileNumber(document.getString("mobileNumber"));
                        post.setLocation(document.getString("location"));
                        post.setTimestamp(document.getLong("timestamp"));
                        post.setImageUrl(document.getString("imageUrl"));
                        post.setFarmerId(document.getString("farmerId"));

                        Double latitude = document.getDouble("latitude");
                        Double longitude = document.getDouble("longitude");
                        if (latitude != null && longitude != null) {
                            post.setLatitude(latitude);
                            post.setLongitude(longitude);
                        }

                        farmerPosts.add(post);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void performSearch(String searchText) {
        Toast.makeText(this, "Searching for: " + searchText, Toast.LENGTH_SHORT).show();
    }

    private void openAllProductsActivity(String category, List<FarmerPost> posts) {
        Intent intent = new Intent(this, AllProductsActivity.class);
        intent.putExtra("category", category);
        intent.putParcelableArrayListExtra("all_posts", new ArrayList<>(posts));
        startActivity(intent);
    }

    private void setupUserData() {
        // Set username from shared preferences
        String username = getUserName();
        if (usernameText != null) {
            usernameText.setText(username);
        }
    }

    private String getUserName() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("username", "User");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFarmerPosts();
    }
}
