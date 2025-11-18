package com.habibi.quanlykhohang;

import java.io.Serializable;

public class Product implements Serializable {
    private int id;
    private String productCode;              // Mã sản phẩm
    private String productName;              // Tên sản phẩm
    private double price;
    private int quantity;
    private String location;
    private String productUnit;              // Đơn vị tính
    private String productDescription;
    private String createDate;
    private String updateDate;  // Mô tả sản phẩm

    public Product() {}

    public Product(String productCode, String productName, double price, int quantity, String location, String productUnit, String productDescription,String createDate,
                   String updateDate) {
        this.productCode = productCode;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.location = location;
        this.productUnit = productUnit;
        this.productDescription = productDescription;
        this.createDate = createDate;
        this.updateDate = updateDate;
    }

    public int getId() { return id; }
    public String getProductCode() { return productCode; }
    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getLocation() { return location; }
    public String getProductUnit() { return productUnit; }
    public String getProductDescription() { return productDescription; }

    public void setId(int id) { this.id = id; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setLocation(String location) { this.location = location; }
    public void setProductUnit(String productUnit) { this.productUnit = productUnit; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }

    public String getCreateDate() { return createDate; }
    public void setCreateDate(String createDate) { this.createDate = createDate; }
    public String getUpdateDate() { return updateDate; }
    public void setUpdateDate(String updateDate) { this.updateDate = updateDate; }


}

