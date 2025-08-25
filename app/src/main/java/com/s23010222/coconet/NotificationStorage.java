package com.s23010222.coconet;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class NotificationStorage {
    private static final String PREFS_NAME = "notifications_prefs";
    private static final String KEY_NOTIFICATIONS = "notifications";

    public static void addNotification(Context context, NotificationItem notification) {
        List<NotificationItem> notifications = getAllNotifications(context);
        notifications.add(0, notification); // newest first
        saveNotifications(context, notifications);
    }

    public static void addDistributorNotification(Context context, NotificationItem notification) {
        List<NotificationItem> notifications = getAllNotifications(context);
        notifications.add(0, notification); // newest first
        saveNotifications(context, notifications);
    }

    public static List<NotificationItem> getNotifications(Context context, String farmerId) {
        List<NotificationItem> all = getAllNotifications(context);
        List<NotificationItem> filtered = new ArrayList<>();
        for (NotificationItem n : all) {
            if (n.getFarmerId() != null && n.getFarmerId().equals(farmerId)) {
                filtered.add(n);
            }
        }
        return filtered;
    }

    public static List<NotificationItem> getDistributorNotifications(Context context, String distributorId) {
        List<NotificationItem> all = getAllNotifications(context);
        List<NotificationItem> filtered = new ArrayList<>();
        for (NotificationItem n : all) {
            if (n.getDistributorId() != null && n.getDistributorId().equals(distributorId)) {
                filtered.add(n);
            }
        }
        return filtered;
    }

    public static List<NotificationItem> getAllNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_NOTIFICATIONS, null);
        if (json == null) return new ArrayList<>();
        return new Gson().fromJson(json, new TypeToken<List<NotificationItem>>(){}.getType());
    }

    private static void saveNotifications(Context context, List<NotificationItem> notifications) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_NOTIFICATIONS, new Gson().toJson(notifications)).apply();
    }
}