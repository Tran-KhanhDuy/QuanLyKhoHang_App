package com.habibi.quanlykhohang;

import com.google.gson.annotations.SerializedName;

public class WarehouseTransaction {
    // Tên trong ngoặc "" phải trùng KHỚP với tên biến bên C# (WarehouseTransactionModel.cs)

    @SerializedName("productId")
    private int productId;

    @SerializedName("productName")
    private String productName; // Thêm cái này cho chắc, dù server có thể tự tìm

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("transactionType")
    private String transactionType; // "Import" hoặc "Export"

    @SerializedName("notes")
    private String notes;
    @SerializedName("supplierId")
    private Integer supplierId;
    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }
    public WarehouseTransaction(int productId, int quantity, String transactionType, String notes) {
        this.productId = productId;
        this.quantity = quantity;
        this.transactionType = transactionType;
        this.notes = notes;
        this.productName = ""; // Tạm để rỗng
    }
}