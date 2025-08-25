package com.s23010222.coconet;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import com.s23010222.coconet.DistributorNotificationHelper;

public class PaymentActivity extends AppCompatActivity {

    private EditText etCardHolder, etCardNumber, etExpiryDate, etCVC;
    private MaterialButton paymentButton;
    private ImageView btnBack;
    private FirebaseFirestore db;

    // Order data passed from previous activity
    private String orderId, productName, customerName, location, quantity, amount, farmerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        db = FirebaseFirestore.getInstance();

        // Get order data from intent
        Intent intent = getIntent();
        if (intent != null) {
            orderId = intent.getStringExtra("order_id");
            productName = intent.getStringExtra("product_name");
            customerName = intent.getStringExtra("customer_name");
            location = intent.getStringExtra("location");
            quantity = intent.getStringExtra("quantity");
            amount = intent.getStringExtra("amount");
            farmerId = intent.getStringExtra("farmer_id");
        }

        // Initialize views
        etCardHolder = findViewById(R.id.et_card_holder);
        etCardNumber = findViewById(R.id.et_card_number);
        etExpiryDate = findViewById(R.id.et_expiry_date);
        etCVC = findViewById(R.id.et_cvc);
        paymentButton = findViewById(R.id.paymentAmount);
        btnBack = findViewById(R.id.btn_back);

        // Formatters
        setupExpiryDateFormatter();
        setupCardNumberFormatter();

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Pay button
        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePayment();
            }
        });
    }

    private void setupExpiryDateFormatter() {
        etExpiryDate.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d]", "");
                    int length = clean.length();

                    if (length >= 3) {
                        clean = clean.substring(0, 2) + "/" + clean.substring(2);
                    }

                    if (clean.length() > 5)
                        clean = clean.substring(0, 5);

                    current = clean;
                    etExpiryDate.removeTextChangedListener(this);
                    etExpiryDate.setText(clean);
                    etExpiryDate.setSelection(clean.length());
                    etExpiryDate.addTextChangedListener(this);
                }
            }
        });
    }

    private void setupCardNumberFormatter() {
        etCardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isDeleting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                isDeleting = count > after;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String original = s.toString();
                String clean = original.replaceAll("\\D", "");

                if (clean.length() > 16) {
                    clean = clean.substring(0, 16);
                }

                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < clean.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(clean.charAt(i));
                }

                etCardNumber.removeTextChangedListener(this);
                etCardNumber.setText(formatted.toString());
                etCardNumber.setSelection(formatted.length());
                etCardNumber.addTextChangedListener(this);
            }
        });
    }

    private void handlePayment() {
        String holder = etCardHolder.getText().toString().trim();
        String number = etCardNumber.getText().toString().replaceAll("\\s", "").trim();
        String expiry = etExpiryDate.getText().toString().trim();
        String cvc = etCVC.getText().toString().trim();

        if (TextUtils.isEmpty(holder) || TextUtils.isEmpty(number)
                || TextUtils.isEmpty(expiry) || TextUtils.isEmpty(cvc)) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (number.length() != 16) {
            Toast.makeText(this, "Invalid card number (must be 16 digits)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            Toast.makeText(this, "Invalid expiry date (MM/YY)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cvc.length() < 3 || cvc.length() > 4) {
            Toast.makeText(this, "Invalid CVC", Toast.LENGTH_SHORT).show();
            return;
        }

        // Process payment and update order status
        processPaymentAndUpdateOrder();
    }

    private void processPaymentAndUpdateOrder() {
        // Show loading state
        paymentButton.setEnabled(false);
        paymentButton.setText("Processing...");

        // Format date/time as '20 May 2025 | 9:35 PM'
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy | h:mm a");
        String currentDateTime = java.time.LocalDateTime.now().format(formatter);

        // Create order data
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId); // Use existing orderId
        orderData.put("productName", productName);
        orderData.put("customerName", customerName);
        orderData.put("location", location);
        orderData.put("quantity", quantity);
        orderData.put("amount", amount);
        orderData.put("farmerId", farmerId);
        orderData.put("orderStatus", "Paid");
        orderData.put("paymentStatus", "Completed");
        orderData.put("orderDateTime", currentDateTime);
        orderData.put("paymentDateTime", currentDateTime);
        orderData.put("distributorId", getCurrentUserId());
        orderData.put("visibleToFarmer", true);
        // Optionally add distributorName and distributorLocation if available
        android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String distributorName = prefs.getString("username", "");
        String distributorLocation = location; // Already passed in intent
        orderData.put("distributorName", distributorName);
        orderData.put("distributorLocation", distributorLocation);

        // Save order to Firebase
        db.collection("orders")
                .document(farmerId)
                .collection("farmer_orders")
                .document(orderId)
                .set(orderData)
                .addOnSuccessListener(aVoid -> {
                    // Payment successful
                    Toast.makeText(this,
                            "Payment Success!\nOrder status updated to Paid",
                            Toast.LENGTH_LONG).show();

                    // Add notification for distributor about payment confirmation
                    String distributorId = getCurrentUserId();
                    NotificationStorage.addDistributorNotification(
                            this,
                            new NotificationItem(
                                    "Payment Confirmed!",
                                    "Payment of " + amount + " for order " + orderId + " (" + productName + ") from " + farmerId + " has been confirmed",
                                    System.currentTimeMillis(),
                                    distributorId,
                                    "distributor"
                            )
                    );

                    // Show push notification for distributor
                    DistributorNotificationHelper notificationHelper = new DistributorNotificationHelper(this);
                    notificationHelper.showPaymentConfirmationNotification(orderId, productName, amount, farmerId);

                    // Clear form
                    etCardHolder.setText("");
                    etCardNumber.setText("");
                    etExpiryDate.setText("");
                    etCVC.setText("");

                    // Reset button
                    paymentButton.setEnabled(true);
                    paymentButton.setText("Pay Now");

                    // Navigate back or to success screen
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Payment failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    paymentButton.setEnabled(true);
                    paymentButton.setText("Pay Now");
                });
    }

    private String getCurrentUserId() {
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
}
