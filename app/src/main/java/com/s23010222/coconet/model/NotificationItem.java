package com.s23010222.coconet.model;



public class NotificationItem {
    private String title;
    private String message;
    private long timestamp;
    private String farmerId;
    private String distributorId;
    private String type;

    public NotificationItem(String title, String message, long timestamp, String farmerId) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.farmerId = farmerId;
        this.type = "farmer";
    }

    public NotificationItem(String title, String message, long timestamp, String distributorId, String type) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.distributorId = distributorId;
        this.type = type;
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public String getFarmerId() { return farmerId; }
    public String getDistributorId() { return distributorId; }
    public String getType() { return type; }
}