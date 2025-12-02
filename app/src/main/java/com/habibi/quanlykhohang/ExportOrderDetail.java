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
    private boolean isPicked = false;

    // --- THÊM 2 HÀM NÀY VÀO ---
    public boolean isPicked() {
        return isPicked;
    }

    public void setPicked(boolean picked) {
        isPicked = picked;
    }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
}