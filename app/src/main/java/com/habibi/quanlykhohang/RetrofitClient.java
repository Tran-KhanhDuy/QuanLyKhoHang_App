package com.habibi.quanlykhohang;

import android.content.Context;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Đây là địa chỉ Server thật của bạn (đã publish)
    private static final String BASE_URL = "http://dtuan244-001-site1.ntempurl.com/";

    private static Retrofit retrofit = null;

    public static ProductApiService getService(Context context) {
        // 1. Lấy Token đang lưu trong máy ra
        TokenManager tokenManager = new TokenManager(context);
        String token = tokenManager.getToken();

        // 2. Cấu hình OkHttpClient (Bộ phận gửi tin)
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Đợi kết nối tối đa 30s
                .readTimeout(30, TimeUnit.SECONDS)    // Đợi đọc dữ liệu tối đa 30s
                .writeTimeout(30, TimeUnit.SECONDS);

        // 3. QUAN TRỌNG: Nếu có Token thì gắn Interceptor vào để kẹp token theo
        if (token != null && !token.isEmpty()) {
            clientBuilder.addInterceptor(new AuthInterceptor(token, context));
        }

        OkHttpClient client = clientBuilder.build();

        // 4. Khởi tạo Retrofit
        // Lưu ý: Ta tạo mới Retrofit mỗi lần gọi để đảm bảo luôn dùng Token mới nhất
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client) // Gắn bộ phận gửi tin vào
                .addConverterFactory(GsonConverterFactory.create()) // Để hiểu được JSON
                .build();

        return retrofit.create(ProductApiService.class);
    }
}