package com.habibi.quanlykhohang;

import com.google.gson.annotations.SerializedName;

public class WarehouseTransaction {
    @SerializedName("productId")
    private int productId; // ID của sản phẩm (quan trọng)

    @SerializedName("productQuantity")
    private int productQuantity;  // Số lượng

    @SerializedName("transactionType")
    private String transactionType; // "Import" hoặc "Export"

    @SerializedName("notes")
    private String notes; // Ghi chú (nếu có)

    // Constructor
    public WarehouseTransaction(int productId, int productQuantity, String transactionType, String notes) {
        this.productId = productId;
        this.productQuantity = productQuantity;
        this.transactionType = transactionType;
        this.notes = notes;
    }
}