package com.habibi.quanlykhohang;

import com.google.gson.annotations.SerializedName;

public class Supplier {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    // Getter
    public int getId() { return id; }
    public String getName() { return name; }

    // --- QUAN TRỌNG NHẤT ---
    // Hàm này giúp cái Spinner biết nó phải hiện chữ gì ra màn hình.
    // Nếu không có hàm này, Spinner sẽ hiện mã loằng ngoằng (địa chỉ bộ nhớ)
    @Override
    public String toString() {
        return name;
    }
}