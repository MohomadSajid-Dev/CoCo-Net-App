package com.s23010222.coconet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends BaseAdapter {
    private Context context;
    private List<NotificationItem> notifications;

    public NotificationAdapter(Context context, List<NotificationItem> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @Override
    public int getCount() { return notifications.size(); }

    @Override
    public Object getItem(int position) { return notifications.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_notification_card, parent, false);
        }
        NotificationItem notification = notifications.get(position);

        TextView title = convertView.findViewById(R.id.notification_card_title);
        TextView message = convertView.findViewById(R.id.notification_card_message);
        TextView time = convertView.findViewById(R.id.notification_card_time);

        title.setText(notification.getTitle());
        message.setText(notification.getMessage());
        time.setText(new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(notification.getTimestamp()));

        return convertView;
    }
}