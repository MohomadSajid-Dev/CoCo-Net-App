package com.s23010222.coconet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.s23010222.coconet.DistributorNotificationHelper;

public class CreateOrderActivity extends AppCompatActivity {

    private EditText quantityEditText;
    private TextView productNameText, priceText, farmerNameText;
    private Button createOrderButton;
    private ImageView backButton;
    private FirebaseFirestore db;

    // Data passed from previous activity
    private String productName, price, farmerId, farmerName, productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        db = FirebaseFirestore.getInstance();

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            productName = intent.getStringExtra("product_name");
            price = intent.getStringExtra("price");
            farmerId = intent.getStringExtra("farmer_id");
            farmerName = intent.getStringExtra("farmer_name");
            productId = intent.getStringExtra("product_id");
        }

        initViews();
        setupData();
        setupListeners();
    }

    private void initViews() {
        quantityEditText = findViewById(R.id.quantityEditText);
        productNameText = findViewById(R.id.productNameText);
        priceText = findViewById(R.id.priceText);
        farmerNameText = findViewById(R.id.farmerNameText);
        createOrderButton = findViewById(R.id.createOrderButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupData() {
        productNameText.setText(productName);
        priceText.setText("Price: " + price);
        farmerNameText.setText("Farmer: " + farmerName);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        createOrderButton.setOnClickListener(v -> {
            String quantity = quantityEditText.getText().toString().trim();

            if (quantity.isEmpty()) {
                Toast.makeText(this, "Please enter quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int qty = Integer.parseInt(quantity);
                if (qty <= 0) {
                    Toast.makeText(this, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                createOrder(quantity);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createOrder(String quantity) {
        // 1. Fetch the current number of orders for this farmer to generate sequential orderId
        db.collection("orders")
                .document(farmerId)
                .collection("farmer_orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int orderCount = queryDocumentSnapshots.size();
                    String orderId = String.format("#CNO-%03d", orderCount + 1);

                    // 2. Format date/time as '20 May 2025 | 9:35 PM'
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy | h:mm a");
                    String currentDateTime = java.time.LocalDateTime.now().format(formatter);

                    // 3. Calculate total amount
                    String priceValue = price.replaceAll("[^0-9]", "");
                    int priceInt = Integer.parseInt(priceValue);
                    int quantityInt = Integer.parseInt(quantity);
                    int totalAmount = priceInt * quantityInt;

                    // 4. Fetch distributor location from SharedPreferences (or DB if needed)
                    String distributorLocation = getCurrentLocation();
                    String distributorName = getCurrentDistributorName();

                    // 5. Create order data
                    Map<String, Object> orderData = new HashMap<>();
                    orderData.put("orderId", orderId);
                    orderData.put("productName", productName);
                    orderData.put("productId", productId);
                    orderData.put("customerName", distributorName);
                    orderData.put("distributorId", getCurrentDistributorId());
                    orderData.put("distributorName", distributorName);
                    orderData.put("distributorLocation", distributorLocation);
                    orderData.put("farmerId", farmerId);
                    orderData.put("farmerName", farmerName);
                    orderData.put("location", distributorLocation);
                    orderData.put("quantity", quantity);
                    orderData.put("pricePerUnit", price);
                    orderData.put("totalAmount", "Rs " + totalAmount);
                    orderData.put("orderStatus", "Pending");
                    orderData.put("paymentStatus", "Pending");
                    orderData.put("orderDateTime", currentDateTime);
                    orderData.put("visibleToFarmer", true);

                    // 6. Save order to Firebase
                    db.collection("orders")
                            .document(farmerId)
                            .collection("farmer_orders")
                            .document(orderId)
                            .set(orderData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Order created successfully!", Toast.LENGTH_SHORT).show();

                                // Add notification for farmer
                                NotificationStorage.addNotification(
                                        CreateOrderActivity.this,
                                        new NotificationItem(
                                                "New Order Received",
                                                "You have received a new order for " + quantity + " " + productName + (Integer.parseInt(quantity) > 1 ? "s" : "") + " with payment: Rs " + totalAmount + ".",
                                                System.currentTimeMillis(),
                                                farmerId
                                        )
                                );

                                // Add notification for distributor
                                String distributorId = getCurrentDistributorId();
                                NotificationStorage.addDistributorNotification(
                                        CreateOrderActivity.this,
                                        new NotificationItem(
                                                "Order Placed Successfully!",
                                                "Your order for " + quantity + " " + productName + " from " + farmerName + " has been placed. Total: Rs " + totalAmount + ".",
                                                System.currentTimeMillis(),
                                                distributorId,
                                                "distributor"
                                        )
                                );

                                // Show push notification for distributor
                                DistributorNotificationHelper notificationHelper = new DistributorNotificationHelper(this);
                                notificationHelper.showOrderPlacedNotification(orderId, productName, quantity, "Rs " + totalAmount, farmerName);

                                // Navigate to payment activity
                                Intent paymentIntent = new Intent(CreateOrderActivity.this, PaymentActivity.class);
                                paymentIntent.putExtra("order_id", orderId);
                                paymentIntent.putExtra("product_name", productName);
                                paymentIntent.putExtra("customer_name", distributorName);
                                paymentIntent.putExtra("location", distributorLocation);
                                paymentIntent.putExtra("quantity", quantity);
                                paymentIntent.putExtra("amount", "Rs " + totalAmount);
                                paymentIntent.putExtra("farmer_id", farmerId);
                                startActivity(paymentIntent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to create order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to generate order ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getCurrentDistributorId() {
        // Get current distributor ID from SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentDistributorId = prefs.getString("distributor_id", "");
        String role = prefs.getString("role", "");
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        // Check if user is logged in and is a distributor
        if (currentDistributorId.isEmpty() || !isLoggedIn || !"Distributor".equalsIgnoreCase(role)) {
            // If not logged in or not a distributor, show error and return placeholder
            Toast.makeText(this, "Error: Distributor not logged in. Please login again.", Toast.LENGTH_LONG).show();
            return "distributor_001"; // Fallback for testing
        }

        return currentDistributorId;
    }

    private String getCurrentDistributorName() {
        // Get current distributor name from SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentDistributorName = prefs.getString("username", ""); // Use username as distributor name
        String role = prefs.getString("role", "");
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        // Check if user is logged in and is a distributor
        if (currentDistributorName.isEmpty() || !isLoggedIn || !"Distributor".equalsIgnoreCase(role)) {
            return "Mohomad Sajid"; // Fallback for testing
        }

        return currentDistributorName;
    }

    private String getCurrentLocation() {
        // TODO: Implement to get current location
        return "New Town | Anuradhapura";
    }
}