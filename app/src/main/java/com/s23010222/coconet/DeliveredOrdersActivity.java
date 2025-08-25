package com.s23010222.coconet;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DeliveredOrdersActivity extends AppCompatActivity {

    private RecyclerView deliveredOrdersRecyclerView;
    private DeliveredOrderAdapter orderAdapter;
    private List<Order> deliveredOrderList;
    private ImageView backButton;
    private FirebaseFirestore db;
    private String currentFarmerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivered_orders);

        db = FirebaseFirestore.getInstance();
        currentFarmerId = getCurrentFarmerId();

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadDeliveredOrders();
    }

    private void initViews() {
        deliveredOrdersRecyclerView = findViewById(R.id.deliveredOrdersRecyclerView);
        backButton = findViewById(R.id.backButton);
    }

    private void setupRecyclerView() {
        deliveredOrderList = new ArrayList<>();
        orderAdapter = new DeliveredOrderAdapter(this, deliveredOrderList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        deliveredOrdersRecyclerView.setLayoutManager(layoutManager);
        deliveredOrdersRecyclerView.setAdapter(orderAdapter);
    }

    private void loadDeliveredOrders() {
        if (currentFarmerId == null || currentFarmerId.isEmpty()) {
            Toast.makeText(this, "Error: Unable to load orders. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }

        db.collection("orders")
                .document(currentFarmerId)
                .collection("farmer_orders")
                .whereEqualTo("orderStatus", "Delivered")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    deliveredOrderList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        orderAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "No delivered orders found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final int[] completed = {0};
                    final int totalDocuments = queryDocumentSnapshots.size();

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
                                    deliveredOrderList.add(order);

                                    completed[0]++;
                                    if (completed[0] == totalDocuments) {
                                        // All async calls done, update UI
                                        deliveredOrderList.sort((o1, o2) -> {
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
                                        orderAdapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    completed[0]++;
                                    if (completed[0] == totalDocuments) {
                                        orderAdapter.notifyDataSetChanged();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load delivered orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentFarmerId = prefs.getString("farmer_id", "");
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (currentFarmerId.isEmpty() || !isLoggedIn) {
            Toast.makeText(this, "Error: User not logged in. Please login again.", Toast.LENGTH_LONG).show();
            return "";
        }

        return currentFarmerId;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDeliveredOrders();
    }
}
