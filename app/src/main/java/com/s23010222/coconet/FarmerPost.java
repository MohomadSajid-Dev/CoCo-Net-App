package com.s23010222.coconet;

import android.os.Parcel;
import android.os.Parcelable;

public class FarmerPost implements Parcelable {
    private String id;
    private String productName;
    private String description;
    private String quantity;
    private String price;
    private String availability;
    private String stockCondition;
    private String mobileNumber;
    private String location;
    private Long timestamp;
    private String farmerId;
    private String imageUrl;
    private Double latitude;
    private Double longitude;

    public FarmerPost() {}

    public FarmerPost(String id, String productName, String description, String quantity,
                      String price, String availability, String stockCondition,
                      String mobileNumber, String location, Long timestamp,
                      String farmerId, String imageUrl, Double latitude, Double longitude) {
        this.id = id;
        this.productName = productName;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
        this.availability = availability;
        this.stockCondition = stockCondition;
        this.mobileNumber = mobileNumber;
        this.location = location;
        this.timestamp = timestamp;
        this.farmerId = farmerId;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected FarmerPost(Parcel in) {
        id = in.readString();
        productName = in.readString();
        description = in.readString();
        quantity = in.readString();
        price = in.readString();
        availability = in.readString();
        stockCondition = in.readString();
        mobileNumber = in.readString();
        location = in.readString();
        if (in.readByte() == 0) {
            timestamp = null;
        } else {
            timestamp = in.readLong();
        }
        farmerId = in.readString();
        imageUrl = in.readString();
        if (in.readByte() == 0) {
            latitude = null;
        } else {
            latitude = in.readDouble();
        }
        if (in.readByte() == 0) {
            longitude = null;
        } else {
            longitude = in.readDouble();
        }
    }

    public static final Creator<FarmerPost> CREATOR = new Creator<FarmerPost>() {
        @Override
        public FarmerPost createFromParcel(Parcel in) {
            return new FarmerPost(in);
        }

        @Override
        public FarmerPost[] newArray(int size) {
            return new FarmerPost[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(productName);
        dest.writeString(description);
        dest.writeString(quantity);
        dest.writeString(price);
        dest.writeString(availability);
        dest.writeString(stockCondition);
        dest.writeString(mobileNumber);
        dest.writeString(location);
        if (timestamp == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(timestamp);
        }
        dest.writeString(farmerId);
        dest.writeString(imageUrl);
        if (latitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(latitude);
        }
        if (longitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(longitude);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getStockCondition() { return stockCondition; }
    public void setStockCondition(String stockCondition) { this.stockCondition = stockCondition; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
