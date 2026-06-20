package com.s23010222.coconet.util;

import com.s23010222.coconet.model.Order;
import com.s23010222.coconet.R;
import com.s23010222.coconet.ui.distributor.DistributorDashboardActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class DistributorNotificationHelper {
    private static final String CHANNEL_ID = "distributor_notifications_channel";
    private static final String CHANNEL_NAME = "Distributor Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for distributors about orders and payments";

    private Context context;
    private NotificationManager notificationManager;

    public DistributorNotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showOrderPlacedNotification(String orderId, String productName, String quantity, String totalAmount, String farmerName) {
        String title = "Order Placed Successfully!";
        String message = String.format("Your order for %s %s from %s has been placed. Total: %s",
                quantity, productName, farmerName, totalAmount);

        Intent intent = new Intent(context, DistributorDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(orderId.hashCode(), builder.build());
    }

    public void showPaymentConfirmationNotification(String orderId, String productName, String amount, String farmerName) {
        String title = "Payment Confirmed!";
        String message = String.format("Payment of %s for order %s (%s) from %s has been confirmed",
                amount, orderId, productName, farmerName);

        Intent intent = new Intent(context, DistributorDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((orderId + "_payment").hashCode(), builder.build());
    }

    public void showOrderStatusUpdateNotification(String orderId, String productName, String status, String farmerName) {
        String title = "Order Status Updated";
        String message = String.format("Order %s (%s) is now %s by %s",
                orderId, productName, status, farmerName);

        Intent intent = new Intent(context, DistributorDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((orderId + "_status").hashCode(), builder.build());
    }

    public void showDeliveryUpdateNotification(String orderId, String productName, String deliveryStatus, String farmerName) {
        String title = "Delivery Update";
        String message = String.format("Order %s (%s) delivery status: %s by %s",
                orderId, productName, deliveryStatus, farmerName);

        // Create intent to open distributor dashboard
        Intent intent = new Intent(context, DistributorDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((orderId + "_delivery").hashCode(), builder.build());
    }
}
