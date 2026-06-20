package com.s23010222.coconet.ui.distributor;

import com.s23010222.coconet.model.NotificationItem;
import com.s23010222.coconet.R;
import com.s23010222.coconet.util.NotificationStorage;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DistributorNotificationActivity extends AppCompatActivity {

    private LinearLayout notificationContainer;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distributor_notifications);

        notificationContainer = findViewById(R.id.notification_container);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String distributorId = prefs.getString("distributor_id", "");

        if (distributorId.isEmpty()) {
            showEmptyState("Please login to view notifications");
            return;
        }

        // Load distributor notifications
        List<NotificationItem> notifications = NotificationStorage.getDistributorNotifications(this, distributorId);

        if (notifications.isEmpty()) {
            showEmptyState("No notifications yet");
        } else {
            displayNotifications(notifications);
        }
    }

    private void displayNotifications(List<NotificationItem> notifications) {
        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

        for (NotificationItem notification : notifications) {
            CardView cardView = (CardView) inflater.inflate(R.layout.item_distributor_notification, notificationContainer, false);
            TextView title = cardView.findViewById(R.id.notificationTitle);
            TextView message = cardView.findViewById(R.id.notificationMessage);
            TextView time = cardView.findViewById(R.id.notificationTimestamp);

            // Set values
            title.setText(notification.getTitle());
            message.setText(notification.getMessage());
            time.setText(sdf.format(notification.getTimestamp()));

            notificationContainer.addView(cardView);
        }
    }

    private void showEmptyState(String message) {
        TextView emptyStateText = new TextView(this);
        emptyStateText.setText(message);
        emptyStateText.setTextSize(16);
        emptyStateText.setTextColor(android.graphics.Color.GRAY);
        emptyStateText.setGravity(android.view.Gravity.CENTER);
        emptyStateText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Add some top margin to center it
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) emptyStateText.getLayoutParams();
        params.topMargin = 200;
        emptyStateText.setLayoutParams(params);

        notificationContainer.addView(emptyStateText);
    }
}
