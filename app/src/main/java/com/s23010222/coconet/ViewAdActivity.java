package com.s23010222.coconet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class ViewAdActivity extends AppCompatActivity {

    private ImageView btnBack, btnMenu;
    private ImageView ivProductImage;
    private TextView tvProductName, tvDescription, tvQuantity, tvPrice;
    private TextView tvAvailability, tvStockCondition;
    private TextView tvMobile, tvEmail, tvLocation;
    private Button btnDone;
    private Button btnLocation;
    private Button btnBuyNow;

    private FirebaseFirestore db;

    private Double farmerLatitude = null;
    private Double farmerLongitude = null;

    private String postLocation = null;

    // Add these fields to store loaded farmer info
    private String loadedFarmerId = null;
    private String loadedFarmerName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ad);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();
        loadAdData();

        //Logic for Controlling Buttons Bassed On User Types Like Famrer and Disributor
        String userType = getIntent().getStringExtra("user_type");
        if (userType != null && userType.equals("distributor")) {
            btnDone.setVisibility(View.GONE);
            btnLocation.setVisibility(View.VISIBLE);
            btnBuyNow.setVisibility(View.VISIBLE);
        } else {
            btnDone.setVisibility(View.VISIBLE);
            btnLocation.setVisibility(View.GONE);
            btnBuyNow.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnMenu = findViewById(R.id.btn_menu);

        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductName = findViewById(R.id.tv_product_name);
        tvDescription = findViewById(R.id.tv_description);
        tvQuantity = findViewById(R.id.tv_quantity);
        tvPrice = findViewById(R.id.tv_price);
        tvAvailability = findViewById(R.id.tv_availability);
        tvStockCondition = findViewById(R.id.tv_stock_condition);

        tvMobile = findViewById(R.id.tv_mobile);
        tvEmail = findViewById(R.id.tv_email);
        tvLocation = findViewById(R.id.tv_location);

        btnDone = findViewById(R.id.btn_done);
        btnLocation = findViewById(R.id.btn_location);
        btnBuyNow = findViewById(R.id.btn_buy_now);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnMenu.setOnClickListener(v -> showMenuOptions());

        btnDone.setOnClickListener(v -> handleDoneButton());

        tvMobile.setOnClickListener(v ->
                Toast.makeText(ViewAdActivity.this, "Mobile: " + tvMobile.getText(), Toast.LENGTH_SHORT).show()
        );

        tvEmail.setOnClickListener(v ->
                Toast.makeText(ViewAdActivity.this, "Email: " + tvEmail.getText(), Toast.LENGTH_SHORT).show()
        );

        tvLocation.setOnClickListener(v -> handleLocationButton());

        btnLocation.setOnClickListener(v -> handleLocationButton());

        btnBuyNow.setOnClickListener(v -> handleBuyNowButton());
    }

    private void loadAdData() {
        String adId = getIntent().getStringExtra("ad_id");
        if (adId == null || adId.isEmpty()) {
            Toast.makeText(this, "Error: No ad ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoadingState();

        db.collection("farmer_posts")
                .document(adId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String productName = documentSnapshot.getString("productName");
                        String description = documentSnapshot.getString("description");
                        String quantity = documentSnapshot.getString("quantity");
                        String price = documentSnapshot.getString("price");
                        String availability = documentSnapshot.getString("availability");
                        String stockCondition = documentSnapshot.getString("stockCondition");
                        String mobileNumber = documentSnapshot.getString("mobileNumber");
                        String farmerId = documentSnapshot.getString("farmerId");
                        String farmerName = documentSnapshot.getString("farmerName"); // If you have this field
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        if (documentSnapshot.contains("latitude")) {
                            farmerLatitude = documentSnapshot.getDouble("latitude");
                        }
                        if (documentSnapshot.contains("longitude")) {
                            farmerLongitude = documentSnapshot.getDouble("longitude");
                        }
                        postLocation = documentSnapshot.getString("location");
                        tvProductName.setText(productName != null ? productName : "N/A");
                        tvDescription.setText(description != null ? description : "No description available");
                        tvQuantity.setText(quantity != null ? quantity : "N/A");
                        tvPrice.setText(price != null ? "Rs" + price : "Price not specified");
                        tvAvailability.setText(availability != null ? availability : "N/A");
                        tvStockCondition.setText(stockCondition != null ? stockCondition : "N/A");
                        tvMobile.setText(mobileNumber != null ? mobileNumber : "Not provided");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.img)
                                    .centerCrop()
                                    .into(ivProductImage);
                        } else {
                            ivProductImage.setImageResource(R.drawable.img);
                        }
<<<<<<< HEAD
                        // Store loaded farmerId and farmerName for use in Buy Now
                        loadedFarmerId = farmerId;
                        loadedFarmerName = farmerName;

=======
>>>>>>> fef8d6bb83afa6b870f34fe1cd4dc7fded77f4da
                        if (farmerId != null && !farmerId.isEmpty()) {
                            loadFarmerDetails(farmerId);
                        } else {
                            tvEmail.setText("Email not available");
                            if (postLocation != null && !postLocation.isEmpty()) {
                                tvLocation.setText(postLocation);
                            } else {
                                tvLocation.setText("Location not specified");
                            }
                            hideLoadingState();
                        }
                    } else {
                        Toast.makeText(this, "Ad not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load ad: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    hideLoadingState();
                    finish();
                });
    }

    private void loadFarmerDetails(String farmerId) {
        db.collection("users")
                .document(farmerId)
                .get()
                .addOnSuccessListener(userSnapshot -> {
                    if (userSnapshot.exists()) {
                        String email = userSnapshot.getString("email");
                        String location = userSnapshot.getString("location");
<<<<<<< HEAD
                        String farmerName = userSnapshot.getString("name");
=======
>>>>>>> fef8d6bb83afa6b870f34fe1cd4dc7fded77f4da
                        tvEmail.setText(email != null ? email : "Email not available");
                        if (location != null && !location.isEmpty()) {
                            tvLocation.setText(location);
                        } else if (postLocation != null && !postLocation.isEmpty()) {
                            tvLocation.setText(postLocation);
                        } else {
                            tvLocation.setText("Location not specified");
                        }
                        // Optionally update loadedFarmerName if you want the latest
                        if (farmerName != null) loadedFarmerName = farmerName;
                    } else {
                        tvEmail.setText("Email not available");
                        if (postLocation != null && !postLocation.isEmpty()) {
                            tvLocation.setText(postLocation);
                        } else {
                            tvLocation.setText("Location not specified");
                        }
                    }
                    hideLoadingState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load farmer details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    tvEmail.setText("Email not available");
                    if (postLocation != null && !postLocation.isEmpty()) {
                        tvLocation.setText(postLocation);
                    } else {
                        tvLocation.setText("Location not specified");
                    }
                    hideLoadingState();
                });
    }

    private void showLoadingState() {
        tvProductName.setText("Loading...");
        tvDescription.setText("Loading...");
        tvQuantity.setText("Loading...");
        tvPrice.setText("Loading...");
        tvAvailability.setText("Loading...");
        tvStockCondition.setText("Loading...");
        tvMobile.setText("Loading...");
        tvEmail.setText("Loading...");
        tvLocation.setText("Loading...");
    }

    private void hideLoadingState() {
<<<<<<< HEAD
        // Optionally implement any UI state reset after loading
=======
>>>>>>> fef8d6bb83afa6b870f34fe1cd4dc7fded77f4da
    }

    private void showMenuOptions() {
        Toast.makeText(this, "Menu options", Toast.LENGTH_SHORT).show();
    }

    private void handleDoneButton() {
        Toast.makeText(this, "Done viewing ad details", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void handleLocationButton() {
        if (farmerLatitude != null && farmerLongitude != null) {
            String uri = "geo:" + farmerLatitude + "," + farmerLongitude + "?q=" + farmerLatitude + "," + farmerLongitude + "(Farmer Location)";
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Google Maps not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            String locationText = tvLocation.getText().toString();
            if (locationText != null && !locationText.equals("Location not specified")) {
                String uri = "geo:0,0?q=" + android.net.Uri.encode(locationText);
                Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Google Maps not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleBuyNowButton() {
        String productName = tvProductName.getText().toString();
        String price = tvPrice.getText().toString().replace("Rs", "").trim();
        String adId = getIntent().getStringExtra("ad_id");

        String farmerId = loadedFarmerId;
        String farmerName = loadedFarmerName != null ? loadedFarmerName : "Farmer";

        Intent intent = new Intent(ViewAdActivity.this, CreateOrderActivity.class);
        intent.putExtra("product_name", productName);
        intent.putExtra("price", price);
        intent.putExtra("farmer_id", farmerId);
        intent.putExtra("farmer_name", farmerName);
        intent.putExtra("product_id", adId);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
