package com.habibi.quanlykhohang;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("username") // Phải trùng tên với JSON server trả về
    private String username;

    @SerializedName("role")     // Phải trùng tên với JSON server trả về
    private String role;

    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}