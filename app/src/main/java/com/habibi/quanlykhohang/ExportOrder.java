package com.habibi.quanlykhohang;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class ExportOrder implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("orderCode")
    private String orderCode;

    @SerializedName("status")
    private String status;

    @SerializedName("createdDate")
    private String createdDate;

    @SerializedName("details")
    private List<ExportOrderDetail> details;

    public int getId() { return id; }
    public String getOrderCode() { return orderCode; }
    public String getCreatedDate() { return createdDate; }
    public List<ExportOrderDetail> getDetails() { return details; }

    // Hàm này giúp hiển thị nhanh lên ListView (Mã đơn + Ngày)
    @Override
    public String toString() {
        // Cắt chuỗi ngày cho gọn (Lấy 10 ký tự đầu hoặc hiển thị cả)
        String dateShort = createdDate != null && createdDate.length() > 10
                ? createdDate.substring(0, 10) : createdDate;
        return orderCode + " (" + dateShort + ") - " + (details != null ? details.size() : 0) + " món";
    }
}