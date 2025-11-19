package com.habibi.quanlykhohang;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ExportLogRealm extends RealmObject {
    @PrimaryKey
    private long id; // Dùng timestamp hoặc tự tăng đơn giản
    private String productCode;
    private String productName;
    private double price;
    private int quantity;
    private String location;
    private String productUnit;
    private String productDescription;
    private String createDate;
    private String updateDate;
    private String action;     // "EXPORT" hoặc "DELETE"
    private String timestamp;  // Thời điểm xuất/xóa

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getProductCode() {
        return productCode;
    }
    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }
    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getProductUnit() {
        return productUnit;
    }
    public void setProductUnit(String productUnit) {
        this.productUnit = productUnit;
    }
    public String getProductDescription() {
        return productDescription;
    }
    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }
    public String getCreateDate() {
        return createDate;
    }
    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
    public String getUpdateDate() {
        return updateDate;
    }
    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

