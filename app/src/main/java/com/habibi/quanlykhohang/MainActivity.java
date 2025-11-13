package com.habibi.quanlykhohang;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button btnImport, btnExport, btnInventory;
//alo
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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