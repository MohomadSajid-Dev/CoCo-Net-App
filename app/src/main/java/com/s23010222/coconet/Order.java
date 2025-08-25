package com.s23010222.coconet;

public class Order {
    private String orderId;
    private String productName;
    private String customerName;
    private String location;
    private String orderDateTime;
    private String paymentAmount;
    private String quantity;
    private String orderStatus;
    private int profileImage;
    private String distributorId;
    private String distributorCity;

    public Order(String orderId, String productName, String customerName, String location,
                 String orderDateTime, String paymentAmount, String quantity, String orderStatus, int profileImage,
                 String distributorId, String distributorCity) {
        this.orderId = orderId;
        this.productName = productName;
        this.customerName = customerName;
        this.location = location;
        this.orderDateTime = orderDateTime;
        this.paymentAmount = paymentAmount;
        this.quantity = quantity;
        this.orderStatus = orderStatus;
        this.profileImage = profileImage;
        this.distributorId = distributorId;
        this.distributorCity = distributorCity;
    }

    public String getOrderId() { return orderId; }
    public String getProductName() { return productName; }
    public String getCustomerName() { return customerName; }
    public String getLocation() { return location; }
    public String getOrderDateTime() { return orderDateTime; }
    public String getPaymentAmount() { return paymentAmount; }
    public String getQuantity() { return quantity; }
    public String getOrderStatus() { return orderStatus; }
    public int getProfileImage() { return profileImage; }
    public String getDistributorId() { return distributorId; }
    public void setDistributorId(String distributorId) { this.distributorId = distributorId; }
    public String getDistributorCity() { return distributorCity; }
    public void setDistributorCity(String distributorCity) { this.distributorCity = distributorCity; }

    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setLocation(String location) { this.location = location; }
    public void setOrderDateTime(String orderDateTime) { this.orderDateTime = orderDateTime; }
    public void setPaymentAmount(String paymentAmount) { this.paymentAmount = paymentAmount; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public void setProfileImage(int profileImage) { this.profileImage = profileImage; }
}