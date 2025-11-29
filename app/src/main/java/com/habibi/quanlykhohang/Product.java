package com.habibi.quanlykhohang;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Product implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("productCode") // Server: ProductCode
    private String productCode;

    @SerializedName("productName") // Server: ProductName
    private String productName;

    @SerializedName("productQuantity") // Server: ProductQuantity
    private int productQuantity;

    @SerializedName("location") // Server: Location
    private String location;

    @SerializedName("productUnit") // Server: ProductUnit
    private String productUnit;

    @SerializedName("productDescription") // Server: ProductDescription
    private String productDescription;

    @SerializedName("createDate") // Server: CreateDate
    private String createDate;

    @SerializedName("updateDate") // Server: UpdateDate
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

    // Getter & Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getProductQuantity() { return productQuantity; }
    public void setProductQuantity(int productQuantity) { this.productQuantity = productQuantity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getProductUnit() { return productUnit; }
    public void setProductUnit(String productUnit) { this.productUnit = productUnit; }

    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }

    public String getCreateDate() { return createDate; }
    public void setCreateDate(String createDate) { this.createDate = createDate; }

    public String getUpdateDate() { return updateDate; }
    public void setUpdateDate(String updateDate) { this.updateDate = updateDate; }
}