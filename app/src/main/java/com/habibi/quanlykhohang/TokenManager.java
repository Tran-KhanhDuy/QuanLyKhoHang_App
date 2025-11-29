package com.habibi.quanlykhohang;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveUserInfo(String token, String username, String role) {
        editor.putString("ACCESS_TOKEN", token);
        editor.putString("USERNAME", username);
        editor.putString("ROLE", role);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString("ACCESS_TOKEN", null);
    }

    public String getUsername() {
        return prefs.getString("USERNAME", "Người dùng");
    }

    public void clearInfo() {
        editor.clear();
        editor.apply();
    }
}