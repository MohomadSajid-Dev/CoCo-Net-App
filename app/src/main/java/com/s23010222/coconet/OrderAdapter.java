package com.s23010222.coconet;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Hide order if not visible to farmer
        if (order instanceof Map && ((Map)order).containsKey("visibleToFarmer") && !Boolean.TRUE.equals(((Map)order).get("visibleToFarmer"))) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        }

        // Hide delivered orders from current orders list (they should only appear in delivered orders)
        if ("Delivered".equals(order.getOrderStatus())) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        }

        holder.productName.setText(order.getProductName() + " - Order ID " + order.getOrderId());
        holder.customerName.setText(order.getCustomerName());
        holder.location.setText(order.getDistributorCity());
        holder.orderDateTime.setText(order.getOrderDateTime());
        holder.paymentAmount.setText(order.getPaymentAmount());
        holder.quantity.setText(order.getQuantity());
        holder.orderStatus.setText(order.getOrderStatus());
        holder.profileImage.setImageResource(order.getProfileImage());

        // Set different colors based on order status
        setOrderStatusColor(holder.orderStatus, order.getOrderStatus());

        // Handle delivered button visibility based on order status
        if ("Delivered".equals(order.getOrderStatus())) {
            holder.btnDelivered.setVisibility(View.GONE);
        } else {
            holder.btnDelivered.setVisibility(View.VISIBLE);
        }

        // Navigation button logic
        holder.btnNavigation.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            android.content.SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String farmerId = prefs.getString("farmer_id", "");
            String distributorId = order.getDistributorId();

            // Fetch farmer's coordinates
            db.collection("users").document(farmerId).get().addOnSuccessListener(farmerDoc -> {
                Double farmerLat = farmerDoc.getDouble("latitude");
                Double farmerLng = farmerDoc.getDouble("longitude");
                if (farmerLat == null || farmerLng == null) {
                    android.widget.Toast.makeText(context, "Farmer location not available", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                // Fetch distributor's coordinates
                db.collection("users").document(distributorId).get().addOnSuccessListener(distributorDoc -> {
                    Double distributorLat = distributorDoc.getDouble("latitude");
                    Double distributorLng = distributorDoc.getDouble("longitude");
                    if (distributorLat == null || distributorLng == null) {
                        android.widget.Toast.makeText(context, "Distributor location not available", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Build navigation intent with coordinates
                    String uri = "http://maps.google.com/maps?saddr=" + farmerLat + "," + farmerLng + "&daddr=" + distributorLat + "," + distributorLng;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps");
                    context.startActivity(intent);
                }).addOnFailureListener(e -> {
                    android.widget.Toast.makeText(context, "Failed to get distributor location", android.widget.Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                android.widget.Toast.makeText(context, "Failed to get farmer location", android.widget.Toast.LENGTH_SHORT).show();
            });
        });

        // Delivered button logic
        holder.btnDelivered.setOnClickListener(v -> {
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            android.content.SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String farmerId = prefs.getString("farmer_id", "");

            // Disable the button to prevent multiple clicks
            holder.btnDelivered.setEnabled(false);
            holder.btnDelivered.setText("Processing...");

            db.collection("orders")
                    .document(farmerId)
                    .collection("farmer_orders")
                    .document(order.getOrderId())
                    .update("orderStatus", "Delivered", "visibleToFarmer", false)
                    .addOnSuccessListener(aVoid -> {
                        // Update the order status in the local list
                        order.setOrderStatus("Delivered");

                        // Show success message
                        android.widget.Toast.makeText(context, "Order marked as delivered successfully!", android.widget.Toast.LENGTH_SHORT).show();

                        // Remove the order from the current list since it's now delivered
                        int orderPosition = orderList.indexOf(order);
                        if (orderPosition != -1) {
                            orderList.remove(orderPosition);
                            notifyItemRemoved(orderPosition);
                        }

                        // Notify adapter that data has changed
                        if (context instanceof android.app.Activity) {
                            ((android.app.Activity) context).runOnUiThread(() -> {
                                notifyDataSetChanged();
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Re-enable button on failure
                        holder.btnDelivered.setEnabled(true);
                        holder.btnDelivered.setText("Mark Delivered");
                        android.widget.Toast.makeText(context, "Failed to mark order as delivered: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void setOrderStatusColor(TextView statusTextView, String status) {
        switch (status.toLowerCase()) {
            case "paid":
                statusTextView.setTextColor(Color.parseColor("#4CAF50")); // Green
                break;
            case "pending":
                statusTextView.setTextColor(Color.parseColor("#FF9800")); // Orange
                break;
            case "cancelled":
                statusTextView.setTextColor(Color.parseColor("#F44336")); // Red
                break;
            case "processing":
                statusTextView.setTextColor(Color.parseColor("#2196F3")); // Blue
                break;
            case "delivered":
                statusTextView.setTextColor(Color.parseColor("#9C27B0")); // Purple
                break;
            default:
                statusTextView.setTextColor(Color.parseColor("#757575")); // Gray
                break;
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView productName, customerName, location, orderDateTime, paymentAmount, quantity, orderStatus;
        ImageView profileImage;
        Button btnNavigation, btnDelivered;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.productName);
            customerName = itemView.findViewById(R.id.customerName);
            location = itemView.findViewById(R.id.location);
            orderDateTime = itemView.findViewById(R.id.orderDateTime);
            paymentAmount = itemView.findViewById(R.id.paymentAmount);
            quantity = itemView.findViewById(R.id.quantity);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            profileImage = itemView.findViewById(R.id.profileImage);
            btnNavigation = itemView.findViewById(R.id.btnNavigation);
            btnDelivered = itemView.findViewById(R.id.btnDelivered);
        }
    }
}
