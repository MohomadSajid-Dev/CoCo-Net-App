package com.s23010222.coconet;

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

public class NotificationActivity extends AppCompatActivity {
    private LinearLayout notificationContainer;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notificationContainer = findViewById(R.id.notification_container);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        // Get current farmerId from SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String farmerId = prefs.getString("farmer_id", "");
        List<NotificationItem> notifications = NotificationStorage.getNotifications(this, farmerId);
        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

        for (NotificationItem notification : notifications) {
            CardView cardView = (CardView) inflater.inflate(R.layout.item_notification_card, notificationContainer, false);
            TextView title = cardView.findViewById(R.id.notification_card_title);
            TextView message = cardView.findViewById(R.id.notification_card_message);
            TextView time = cardView.findViewById(R.id.notification_card_time);
            // Set values
            title.setText(notification.getTitle());
            message.setText(notification.getMessage());
            time.setText(sdf.format(notification.getTimestamp()));
            notificationContainer.addView(cardView);
        }
    }
}