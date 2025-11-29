package com.habibi.quanlykhohang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText edtUser, edtPass;
    Button btnLogin;
    ProductApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUser = findViewById(R.id.edtUsername);
        edtPass = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Lấy service (Lưu ý: Login không cần Token nên gọi service nào cũng được,
        // nhưng tốt nhất dùng service chưa gắn token hoặc service chung)
        apiService = RetrofitClient.getService(this);

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String user = edtUser.getText().toString().trim();
        String pass = edtPass.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi API
        Call<LoginResponse> call = apiService.login(new LoginRequest(user, pass));
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse res = response.body();

                    // 1. Lưu thông tin vào máy
                    TokenManager tokenManager = new TokenManager(LoginActivity.this);
                    tokenManager.saveUserInfo(res.getToken(), res.getUsername(), res.getRole());

                    Toast.makeText(LoginActivity.this, "Xin chào " + res.getUsername(), Toast.LENGTH_SHORT).show();

                    // 2. Chuyển sang màn hình chính
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Đóng Login để không quay lại được
                } else {
                    Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối Server!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}