package com.s23010222.coconet.ui.farmer;

import com.s23010222.coconet.adapter.OrderAdapter;
import com.s23010222.coconet.model.Order;
import com.s23010222.coconet.R;
import com.s23010222.coconet.util.NotificationHelper;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewOrdersActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private ImageView backButton;
    private FirebaseFirestore db;
    private String currentFarmerId; // Current farmer's ID
    private ListenerRegistration orderListener; // For real-time updates
    private NotificationHelper notificationHelper;
    private Map<String, String> previousOrderStatuses; // Track previous statuses

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_orders);

        db = FirebaseFirestore.getInstance();
        currentFarmerId = getCurrentFarmerId(); // Get current farmer ID
        notificationHelper = new NotificationHelper(this);
        previousOrderStatuses = new HashMap<>();

        initViews();
        setupRecyclerView();
        setupRealTimeListener();
        setupClickListeners();
    }

    private void initViews() {
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        backButton = findViewById(R.id.backButton);
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        ordersRecyclerView.setLayoutManager(layoutManager);
        ordersRecyclerView.setAdapter(orderAdapter);
    }

    private void setupRealTimeListener() {
        if (currentFarmerId == null || currentFarmerId.isEmpty()) {
            Toast.makeText(this, "Error: Unable to load orders. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }

        orderListener = db.collection("orders")
                .document(currentFarmerId)
                .collection("farmer_orders")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        List<Order> newOrderList = new ArrayList<>();
                        List<com.google.firebase.firestore.QueryDocumentSnapshot> documents = new ArrayList<>();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : value) {
                            documents.add(document);
                        }

                        if (documents.isEmpty()) {
                            orderList.clear();
                            orderAdapter.notifyDataSetChanged();
                            Toast.makeText(this, "No orders found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        final int[] completed = {0};
                        final int[] totalToProcess = {0};

                        // First, count how many orders we need to process (excluding delivered ones)
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : documents) {
                            String status = document.getString("orderStatus");
                            if (!"Delivered".equals(status)) {
                                totalToProcess[0]++;
                            }
                        }

                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : documents) {
                            String orderId = document.getString("orderId");
                            String status = document.getString("orderStatus");
                            String distributorId = document.getString("distributorId");

                            if ("Delivered".equals(status)) {
                                continue;
                            }

                            db.collection("users").document(distributorId).get()
                                    .addOnSuccessListener(userDoc -> {
                                        String distributorCity = userDoc.getString("city");
                                        Order order = new Order(
                                                orderId,
                                                document.getString("productName"),
                                                document.getString("customerName"),
                                                document.getString("location"),
                                                document.getString("orderDateTime"),
                                                document.getString("amount"),
                                                document.getString("quantity"),
                                                status,
                                                R.drawable.profile_placeholder,
                                                distributorId,
                                                distributorCity != null ? distributorCity : "Location not specified"
                                        );
                                        newOrderList.add(order);
                                        checkStatusChangeAndNotify(orderId, status, order);

                                        completed[0]++;
                                        if (completed[0] == totalToProcess[0]) {
                                            newOrderList.sort((o1, o2) -> {
                                                String dateStr1 = o1.getOrderDateTime();
                                                String dateStr2 = o2.getOrderDateTime();
                                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy | h:mm a", java.util.Locale.getDefault());
                                                try {
                                                    java.util.Date d1 = dateStr1 != null ? sdf.parse(dateStr1) : null;
                                                    java.util.Date d2 = dateStr2 != null ? sdf.parse(dateStr2) : null;
                                                    if (d1 == null && d2 == null) return 0;
                                                    if (d1 == null) return 1;
                                                    if (d2 == null) return -1;
                                                    return d2.compareTo(d1); // Descending order
                                                } catch (java.text.ParseException ex) {
                                                    return dateStr2.compareTo(dateStr1);
                                                }
                                            });
                                            orderList.clear();
                                            orderList.addAll(newOrderList);
                                            orderAdapter.notifyDataSetChanged();
                                            if (orderList.isEmpty()) {
                                                Toast.makeText(this, "No orders found", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        completed[0]++;
                                        if (completed[0] == totalToProcess[0]) {
                                            newOrderList.sort((o1, o2) -> {
                                                String dateStr1 = o1.getOrderDateTime();
                                                String dateStr2 = o2.getOrderDateTime();
                                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy | h:mm a", java.util.Locale.getDefault());
                                                try {
                                                    java.util.Date d1 = dateStr1 != null ? sdf.parse(dateStr1) : null;
                                                    java.util.Date d2 = dateStr2 != null ? sdf.parse(dateStr2) : null;
                                                    if (d1 == null && d2 == null) return 0;
                                                    if (d1 == null) return 1;
                                                    if (d2 == null) return -1;
                                                    return d2.compareTo(d1); // Descending order
                                                } catch (java.text.ParseException ex) {
                                                    return dateStr2.compareTo(dateStr1);
                                                }
                                            });
                                            orderList.clear();
                                            orderList.addAll(newOrderList);
                                            orderAdapter.notifyDataSetChanged();
                                            if (orderList.isEmpty()) {
                                                Toast.makeText(this, "No orders found", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void checkStatusChangeAndNotify(String orderId, String newStatus, Order order) {
        String previousStatus = previousOrderStatuses.get(orderId);

        if (previousStatus != null && !previousStatus.equals(newStatus)) {
            // Status changed, show notification
            if ("Paid".equals(newStatus)) {
                notificationHelper.showPaymentReceivedNotification(
                        orderId,
                        order.getProductName(),
                        order.getPaymentAmount(),
                        order.getCustomerName()
                );
            } else {
                notificationHelper.showOrderStatusNotification(
                        orderId,
                        order.getProductName(),
                        newStatus,
                        order.getCustomerName()
                );
            }
        }

        // Update the previous status
        previousOrderStatuses.put(orderId, newStatus);
    }

    private void loadOrdersFromFirebase() {
        // Check if we have a valid farmer ID
        if (currentFarmerId == null || currentFarmerId.isEmpty()) {
            Toast.makeText(this, "Error: Unable to load orders. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }

        // Load orders for the current farmer from Firebase
        db.collection("orders")
                .document(currentFarmerId)
                .collection("farmer_orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String orderId = document.getString("orderId");
                        String status = document.getString("orderStatus");
                        String distributorId = document.getString("distributorId");

                        db.collection("users").document(distributorId).get()
                                .addOnSuccessListener(userDoc -> {
                                    String distributorCity = userDoc.getString("city");
                                    Order order = new Order(
                                            orderId,
                                            document.getString("productName"),
                                            document.getString("customerName"),
                                            document.getString("location"),
                                            document.getString("orderDateTime"),
                                            document.getString("amount"),
                                            document.getString("quantity"),
                                            status,
                                            R.drawable.profile_placeholder,
                                            distributorId,
                                            distributorCity != null ? distributorCity : "Location not specified"
                                    );
                                    orderList.add(order);
                                    previousOrderStatuses.put(orderId, status);
                                    orderAdapter.notifyDataSetChanged();
                                });
                    }

                    // Sort the orders by orderDateTime in descending order (newest first)
                    orderList.sort((o1, o2) -> {
                        String dateStr1 = o1.getOrderDateTime();
                        String dateStr2 = o2.getOrderDateTime();
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy | h:mm a", java.util.Locale.getDefault());
                        try {
                            java.util.Date d1 = dateStr1 != null ? sdf.parse(dateStr1) : null;
                            java.util.Date d2 = dateStr2 != null ? sdf.parse(dateStr2) : null;
                            if (d1 == null && d2 == null) return 0;
                            if (d1 == null) return 1;
                            if (d2 == null) return -1;
                            return d2.compareTo(d1); // Descending order
                        } catch (java.text.ParseException ex) {
                            // Fallback to string comparison if parsing fails
                            return dateStr2.compareTo(dateStr1);
                        }
                    });

                    orderAdapter.notifyDataSetChanged();

                    if (orderList.isEmpty()) {
                        Toast.makeText(this, "No orders found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Do not load any dummy/sample orders
                });
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private String getCurrentFarmerId() {
        // Get current farmer ID from SharedPreferences (same as PostAdActivity)
        android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentFarmerId = prefs.getString("farmer_id", "");
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (currentFarmerId.isEmpty() || !isLoggedIn) {
            // If not logged in, show error and return empty
            Toast.makeText(this, "Error: User not logged in. Please login again.", Toast.LENGTH_LONG).show();
            return "";
        }

        return currentFarmerId;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Real-time listener will automatically update the UI
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the real-time listener when activity is destroyed
        if (orderListener != null) {
            orderListener.remove();
        }
    }
}
