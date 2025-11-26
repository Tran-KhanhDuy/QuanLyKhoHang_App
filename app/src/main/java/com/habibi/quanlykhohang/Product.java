package com.habibi.quanlykhohang;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Product implements Serializable {
    private int id;
    private String productCode;
    private String productName;

    @SerializedName("productQuantity")
    private int productQuantity;

    private String location;
    private String productUnit;
    private String productDescription;
    private String createDate;
    private String updateDate;

    public Product() {}

    public Product(String productCode, String productName, int productQuantity, String location, String productUnit,
                   String productDescription, String createDate, String updateDate) {
        this.productCode = productCode;
        this.productName = productName;
        this.productQuantity = productQuantity;
        this.location = location;
        this.productUnit = productUnit;
        this.productDescription = productDescription;
        this.createDate = createDate;
        this.updateDate = updateDate;
    }

    public int getId() { return id; }
    public String getProductCode() { return productCode; }
    public String getProductName() { return productName; }
    public int getProductQuantity() { return productQuantity; }
    public String getLocation() { return location; }
    public String getProductUnit() { return productUnit; }
    public String getProductDescription() { return productDescription; }
    public String getCreateDate() { return createDate; }
    public String getUpdateDate() { return updateDate; }

    public void setId(int id) { this.id = id; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setProductQuantity(int productQuantity) { this.productQuantity = productQuantity; }
    public void setLocation(String location) { this.location = location; }
    public void setProductUnit(String productUnit) { this.productUnit = productUnit; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
    public void setCreateDate(String createDate) { this.createDate = createDate; }
    public void setUpdateDate(String updateDate) { this.updateDate = updateDate; }
}
