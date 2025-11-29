package com.habibi.quanlykhohang;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private Button btnImport, btnExport, btnInventory;
    public static ProductApiService api;

    //alo
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TokenManager tokenManager = new TokenManager(this);

        // Kiểm tra: Nếu không có token -> Về trang Login
        if (tokenManager.getToken() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        // Khởi tạo Retrofit và API Service
        api = RetrofitClient.getService(this);


        LinearLayout btnImport = findViewById(R.id.btnImportLayout);
        btnImport.setOnClickListener(v ->{
            Toast.makeText(this, "Đã bấm vào Nhập Hàng", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, ImportActivity.class);
            startActivity(intent);
        });

        LinearLayout btnExport = findViewById(R.id.btnExportLayout);
        btnExport.setOnClickListener(v -> {
            Toast.makeText(this, "Đã bấm vào Xuất Hàng", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, ExportActivity.class);
            startActivity(intent);
        });

        LinearLayout btnInventory = findViewById(R.id.btnInventoryLayout);
        btnInventory.setOnClickListener(v -> {
            Toast.makeText(this, "Đã bấm vào Hàng tồn kho", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
            startActivity(intent);
        });
    }
}