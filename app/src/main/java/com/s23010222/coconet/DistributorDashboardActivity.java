package com.s23010222.coconet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.Toast;
import android.os.Handler;
import android.content.SharedPreferences;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.airbnb.lottie.LottieAnimationView;
import android.view.Gravity;
import android.widget.FrameLayout.LayoutParams;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class DistributorDashboardActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView usernameText;

    private EditText searchEditText;

    private TextView seeAllText, seeAllOtherText;

    private LinearLayout homeTab, locationTab, ordersTab, notificationTab, menuTab;

    private RecyclerView recyclerViewFarmerPosts;
    private List<FarmerPost> farmerPosts;
    private List<FarmerPost> allFarmerPosts;
    private FarmerPostAdapter farmerPostAdapter;
    private FirebaseFirestore db;

    private static final int DASHBOARD_POST_LIMIT = 4;

    private View dealsBannerCard;

    private SensorManager sensorManager;
    private float lastX, lastY, lastZ;
    private long lastShakeTime = 0;
    private static final int SHAKE_THRESHOLD = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distributor_dashboard);

        db = FirebaseFirestore.getInstance();
        farmerPosts = new ArrayList<>();
        allFarmerPosts = new ArrayList<>();

        initializeViews();
        setupFarmerPostsRecyclerView();
        loadFarmerPosts();
        setupClickListeners();
        setupUserData();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profileImage);
        usernameText = findViewById(R.id.usernameText);

        searchEditText = findViewById(R.id.searchEditText);

        seeAllText = findViewById(R.id.seeAllText);
        seeAllOtherText = findViewById(R.id.seeAllOtherText);

        dealsBannerCard = findViewById(R.id.dealsBannerCard);

        homeTab = findViewById(R.id.homeTab);
        locationTab = findViewById(R.id.locationTab);
        ordersTab = findViewById(R.id.ordersTab);
        notificationTab = findViewById(R.id.notificationTab);
        menuTab = findViewById(R.id.menuTab);
    }

    private void setupFarmerPostsRecyclerView() {
        recyclerViewFarmerPosts = findViewById(R.id.recyclerViewFarmerPosts);
        farmerPostAdapter = new FarmerPostAdapter(farmerPosts, this::onFarmerPostClick);
        recyclerViewFarmerPosts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewFarmerPosts.setAdapter(farmerPostAdapter);
    }

    private void loadFarmerPosts() {
        db.collection("farmer_posts")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allFarmerPosts.clear();
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

                        allFarmerPosts.add(post);
                    }

                    displayLimitedPosts();
                })
                .addOnFailureListener(e -> {
                });
    }

    private void displayLimitedPosts() {
        farmerPosts.clear();

        int postsToShow = Math.min(DASHBOARD_POST_LIMIT, allFarmerPosts.size());
        for (int i = 0; i < postsToShow; i++) {
            farmerPosts.add(allFarmerPosts.get(i));
        }

        farmerPostAdapter.notifyDataSetChanged();
    }

    private void onFarmerPostClick(FarmerPost post) {
        Intent intent = new Intent(DistributorDashboardActivity.this, ViewAdActivity.class);
        intent.putExtra("ad_id", post.getId());
        intent.putExtra("user_type", "distributor");
        startActivity(intent);
    }

    private void setupClickListeners() {
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfileActivity();
            }
        });

        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    openSearchActivity();
                }
            }
        });

<<<<<<< HEAD
        // Add text change listener for search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String searchText = s.toString().trim();
                if (!searchText.isEmpty()) {
                    performSearch(searchText);
                } else {
                    // Reset to show all posts if search is empty
                    displayLimitedPosts();
                }
            }
        });

=======
>>>>>>> fef8d6bb83afa6b870f34fe1cd4dc7fded77f4da
        seeAllText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAllProductsActivity("coconut", allFarmerPosts);
            }
        });

        seeAllOtherText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAllProductsActivity("other", allFarmerPosts);
            }
        });

        dealsBannerCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seeAllText.performClick();
            }
        });

        homeTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshDashboard();
            }
        });

        locationTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocationActivity();
            }
        });

        ordersTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOrdersActivity();
            }
        });

        notificationTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotificationActivity();
            }
        });

        menuTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenuActivity();
            }
        });
    }

    private void setupUserData() {
<<<<<<< HEAD
        // Set username from shared preferences or default
        String username = getUserName();
        if (usernameText != null) {
            usernameText.setText(username);
        }

        // Load profile image if available
        String profileImageUrl = getUserProfileImageUrl();
        if (profileImage != null && profileImageUrl != null && !profileImageUrl.isEmpty()) {
            // You can use Glide or other image loading library here
            // Glide.with(this).load(profileImageUrl).into(profileImage);
        }
    }

    private void openProfileActivity() {
        Intent intent = new Intent(this, DistributorProfileActivity.class);
        startActivity(intent);
    }

    private void openSearchActivity() {
        // For distributors, this could open a search activity to find specific products
        Toast.makeText(this, "Search functionality coming soon", Toast.LENGTH_SHORT).show();
=======
    }

    private void openProfileActivity() {
    }

    private void openSearchActivity() {
>>>>>>> fef8d6bb83afa6b870f34fe1cd4dc7fded77f4da
    }

    private void openAllProductsActivity(String category, List<FarmerPost> allPosts) {
        Intent intent = new Intent(this, AllProductsActivity.class);
        intent.putExtra("category", category);
        intent.putParcelableArrayListExtra("all_posts", new ArrayList<>(allPosts));
        startActivity(intent);
    }

    private void openLocationActivity() {
        Intent intent = new Intent(this, LocateFarmersActivity.class);
        startActivity(intent);
    }

    private void openOrdersActivity() {
<<<<<<< HEAD
        Intent intent = new Intent(this, DistributorOrdersActivity.class);
        startActivity(intent);
    }

    private void openNotificationActivity() {
        Intent intent = new Intent(this, DistributorNotificationActivity.class);
        startActivity(intent);
    }

    private void openMenuActivity() {
        Intent intent = new Intent(this, DistributorProfileActivity.class);
        startActivity(intent);
=======
    }

    private void openNotificationActivity() {
    }

    private void openMenuActivity() {
>>>>>>> fef8d6bb83afa6b870f34fe1cd4dc7fded77f4da
    }

    private void refreshDashboard() {
        loadFarmerPosts();
    }

<<<<<<< HEAD
    private void performSearch(String searchText) {
        // Filter posts based on search text
        List<FarmerPost> filteredPosts = new ArrayList<>();
        for (FarmerPost post : allFarmerPosts) {
            if (post.getProductName().toLowerCase().contains(searchText.toLowerCase()) ||
                    post.getDescription().toLowerCase().contains(searchText.toLowerCase()) ||
                    post.getLocation().toLowerCase().contains(searchText.toLowerCase())) {
                filteredPosts.add(post);
            }
        }

        // Update the displayed posts
        farmerPosts.clear();
        int postsToShow = Math.min(DASHBOARD_POST_LIMIT, filteredPosts.size());
        for (int i = 0; i < postsToShow; i++) {
            farmerPosts.add(filteredPosts.get(i));
        }

        farmerPostAdapter.notifyDataSetChanged();
    }

    private String getUserName() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("username", "User");
=======
    private String getUserName() {
        return "Username";
>>>>>>> fef8d6bb83afa6b870f34fe1cd4dc7fded77f4da
    }

    private String getUserProfileImageUrl() {
        return "";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastShakeTime) > 200) {
                long diffTime = currentTime - lastShakeTime;
                lastShakeTime = currentTime;

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    refreshDashboard();
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
}
