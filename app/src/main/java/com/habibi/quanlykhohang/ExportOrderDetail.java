package com.habibi.quanlykhohang;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ExportOrderDetail implements Serializable {
    @SerializedName("productId")
    private int productId;

    @SerializedName("product")
    private Product product; // Kèm thông tin sản phẩm (để lấy tên, vị trí)

    @SerializedName("quantity")
    private int quantity;

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
}