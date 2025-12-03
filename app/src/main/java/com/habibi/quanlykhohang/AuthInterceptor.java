package com.habibi.quanlykhohang;

import android.content.Context;
import android.content.Intent;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private String token;
    private Context context; // 1. Thêm biến Context

    // 2. Cập nhật Constructor để nhận Context
    public AuthInterceptor(String token, Context context) {
        this.token = token;
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .method(original.method(), original.body());

        Response response = chain.proceed(builder.build());

        // 3. KIỂM TRA LỖI 401 (HẾT HẠN)
        if (response.code() == 401) {
            // Token hết hạn hoặc không hợp lệ
            TokenManager tokenManager = new TokenManager(context);
            tokenManager.clearInfo(); // Xóa token cũ đi

            // Chuyển ngay về màn hình Login
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);

            // Đóng response để tránh leak
            response.close();
        }

        return response;
    }
}