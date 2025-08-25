package com.s23010222.coconet;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DistributorOrderAdapter extends RecyclerView.Adapter<DistributorOrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orderList;

    public DistributorOrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_distributor_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.productName.setText(order.getProductName() + " - Order ID " + order.getOrderId());
        holder.farmerName.setText(order.getCustomerName());
        holder.location.setText(order.getLocation());
        holder.orderDateTime.setText(order.getOrderDateTime());
        holder.paymentAmount.setText(order.getPaymentAmount());
        holder.quantity.setText(order.getQuantity());

        String displayStatus = getDisplayStatus(order.getOrderStatus());
        holder.orderStatus.setText(displayStatus);
        holder.profileImage.setImageResource(order.getProfileImage());

        setOrderStatusColor(holder.orderStatus, displayStatus);
    }

    private String getDisplayStatus(String rawStatus) {
        if (rawStatus == null) return "Pending";
        String s = rawStatus.trim();
        if (s.equalsIgnoreCase("paid")) return "Pending"; // hide Paid on distributor UI
        if (s.equalsIgnoreCase("completed")) return "Delivered";
        return s;
    }

    private void setOrderStatusColor(TextView statusTextView, String status) {
        if (status == null) {
            statusTextView.setTextColor(Color.parseColor("#757575"));
            return;
        }
        switch (status.toLowerCase()) {
            case "pending":
                statusTextView.setTextColor(Color.parseColor("#FF9800"));
                break;
            case "delivered":
                statusTextView.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case "paid": // treat like pending visually
                statusTextView.setTextColor(Color.parseColor("#FF9800"));
                break;
            case "processing":
                statusTextView.setTextColor(Color.parseColor("#2196F3"));
                break;
            default:
                statusTextView.setTextColor(Color.parseColor("#757575"));
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView productName, farmerName, location, orderDateTime, paymentAmount, quantity, orderStatus;
        ImageView profileImage;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            farmerName = itemView.findViewById(R.id.farmerName);
            location = itemView.findViewById(R.id.location);
            orderDateTime = itemView.findViewById(R.id.orderDateTime);
            paymentAmount = itemView.findViewById(R.id.paymentAmount);
            quantity = itemView.findViewById(R.id.quantity);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            profileImage = itemView.findViewById(R.id.profileImage);
        }
    }
}
