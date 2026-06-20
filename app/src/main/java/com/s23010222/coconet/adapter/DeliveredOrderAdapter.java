package com.s23010222.coconet.adapter;

import com.s23010222.coconet.model.Order;
import com.s23010222.coconet.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeliveredOrderAdapter extends RecyclerView.Adapter<DeliveredOrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orderList;

    public DeliveredOrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_delivered_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.productName.setText(order.getProductName() + " - Order ID " + order.getOrderId());
        holder.customerName.setText(order.getCustomerName());
        holder.location.setText(order.getDistributorCity());
        holder.orderDateTime.setText(order.getOrderDateTime());
        holder.paymentAmount.setText(order.getPaymentAmount());
        holder.quantity.setText(order.getQuantity());
        holder.orderStatus.setText(order.getOrderStatus());
        holder.profileImage.setImageResource(order.getProfileImage());

        setOrderStatusColor(holder.orderStatus, order.getOrderStatus());

        holder.btnNavigation.setOnClickListener(v -> {
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            android.content.SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String farmerId = prefs.getString("farmer_id", "");

            db.collection("users").document(farmerId).get().addOnSuccessListener(farmerDoc -> {
                Double farmerLat = farmerDoc.getDouble("latitude");
                Double farmerLng = farmerDoc.getDouble("longitude");
                if (farmerLat == null || farmerLng == null) {
                    android.widget.Toast.makeText(context, "Farmer location not available", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                db.collection("users").document(order.getDistributorId()).get().addOnSuccessListener(distributorDoc -> {
                    Double distributorLat = distributorDoc.getDouble("latitude");
                    Double distributorLng = distributorDoc.getDouble("longitude");
                    if (distributorLat == null || distributorLng == null) {
                        android.widget.Toast.makeText(context, "Distributor location not available", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String uri = "http://maps.google.com/maps?saddr=" + farmerLat + "," + farmerLng + "&daddr=" + distributorLat + "," + distributorLng;
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps");
                    context.startActivity(intent);
                }).addOnFailureListener(e -> {
                    android.widget.Toast.makeText(context, "Failed to get distributor location", android.widget.Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                android.widget.Toast.makeText(context, "Failed to get farmer location", android.widget.Toast.LENGTH_SHORT).show();
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
        Button btnNavigation;

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
        }
    }
}
