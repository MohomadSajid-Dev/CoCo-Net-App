package com.s23010222.coconet.ui.distributor;

import com.s23010222.coconet.adapter.DistributorOrderAdapter;
import com.s23010222.coconet.adapter.OrderAdapter;
import com.s23010222.coconet.model.Order;
import com.s23010222.coconet.R;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistributorOrdersActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private DistributorOrderAdapter orderAdapter;
    private List<Order> orderList;
    private ImageView backButton;
    private FirebaseFirestore db;
    private String currentDistributorId;
    private ListenerRegistration orderListener;
    private Map<String, String> previousOrderStatuses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distributor_orders);

        db = FirebaseFirestore.getInstance();
        currentDistributorId = getCurrentDistributorId();
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
        orderAdapter = new DistributorOrderAdapter(this, orderList);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(orderAdapter);
    }

    private void setupRealTimeListener() {
        if (currentDistributorId == null || currentDistributorId.isEmpty()) {
            Toast.makeText(this, "Error: Unable to load orders. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }

        orderListener = db.collectionGroup("farmer_orders")
                .whereEqualTo("distributorId", currentDistributorId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (error instanceof FirebaseFirestoreException &&
                                ((FirebaseFirestoreException) error).getCode() == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                            android.util.Log.e("DistributorOrders", "Index required: " + error.getMessage());
                        } else {
                            android.util.Log.e("DistributorOrders", "Failed to load orders", error);
                        }
                        return;
                    }
                    if (value != null) {
                        List<Order> newOrderList = new ArrayList<>();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : value) {
                            String orderId = document.getString("orderId");
                            String status = document.getString("orderStatus");

                            String amount = document.getString("amount");
                            if (amount == null || amount.isEmpty()) {
                                amount = document.getString("totalAmount");
                            }

                            Order order = new Order(
                                    orderId,
                                    document.getString("productName"),
                                    document.getString("farmerName"), // show farmer name to distributor
                                    document.getString("distributorLocation"),
                                    document.getString("orderDateTime"),
                                    amount,
                                    document.getString("quantity"),
                                    status,
                                    R.drawable.profile_placeholder,
                                    document.getString("distributorId"),
                                    document.getString("distributorLocation") != null ? document.getString("distributorLocation") : "Location not specified"
                            );
                            newOrderList.add(order);

                            previousOrderStatuses.put(orderId, status);
                        }

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
                                return d2.compareTo(d1);
                            } catch (java.text.ParseException ex) {
                                return dateStr2.compareTo(dateStr1);
                            }
                        });

                        orderList.clear();
                        orderList.addAll(newOrderList);
                        orderAdapter.notifyDataSetChanged();
                    }
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

    private String getCurrentDistributorId() {
        android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentId = prefs.getString("distributor_id", "");
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        String role = prefs.getString("role", "");
        if (currentId.isEmpty() || !isLoggedIn || !"Distributor".equalsIgnoreCase(role)) {
            Toast.makeText(this, "Error: Distributor not logged in. Please login again.", Toast.LENGTH_LONG).show();
            return "";
        }
        return currentId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) {
            orderListener.remove();
        }
    }
}
