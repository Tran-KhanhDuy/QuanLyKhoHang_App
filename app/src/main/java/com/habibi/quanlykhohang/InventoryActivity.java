package com.habibi.quanlykhohang;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

// Retrofit
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryActivity extends AppCompatActivity {

    private ListView lvInventory;
    private final ArrayList<Product> productList = new ArrayList<>();
    private ProductApiService apiService;

    // Khai báo launcher để xử lý kết quả trả về sau khi xuất/xóa sản phẩm
    private ActivityResultLauncher<Intent> exportLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        lvInventory = findViewById(R.id.lvInventory);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gelatinously-commutative-jerrie.ngrok-free.dev/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ProductApiService.class);

        // Đăng ký launcher để xử lý khi ExportActivity trả về kết quả
        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadInventoryFromApi(); // Sync list chỉ khi có thay đổi
                    }
                }
        );

        loadInventoryFromApi();

        // Mở ProductInfoActivity khi click sản phẩm
        lvInventory.setOnItemClickListener((parent, view, position, id) -> {
            Product selected = productList.get(position);
            Intent intent = new Intent(InventoryActivity.this, ProductInfoActivity.class);
            intent.putExtra("product", selected);
            startActivity(intent);
        });

        // Nút quay lại
        ImageButton btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> finish());

        // Nút thêm sản phẩm
        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(InventoryActivity.this, ImportActivity.class);
            startActivity(intent);
        });

        // Ví dụ: nếu có nút xuất kho ở giao diện này,
        // hoặc nếu muốn mở ExportActivity từ đây:
        // Button btnExport = findViewById(R.id.btnExport);
        // btnExport.setOnClickListener(v -> {
        //     Intent intent = new Intent(this, ExportActivity.class);
        //     exportLauncher.launch(intent);
        // });
        // Hoặc có thể truyền product cụ thể nếu cần xuất theo sản phẩm đã chọn.
    }

    private void loadInventoryFromApi() {
        apiService.getAllProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    setupAdapter();
                } else {
                    setupDemoDataAndAdapter();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                setupDemoDataAndAdapter();
            }
        });
    }

    private void setupAdapter() {
        ArrayAdapter<Product> adapter = new ArrayAdapter<Product>(this, 0, productList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View listItemView = convertView;
                if (listItemView == null) {
                    listItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_product_name, parent, false);
                }
                Product product = getItem(position);
                if (product != null) {
                    TextView nameTextView = listItemView.findViewById(R.id.tvProductName);
                    if (nameTextView != null) {
                        nameTextView.setText(product.getProductName());
                    }
                }
                return listItemView;
            }
        };
        lvInventory.setAdapter(adapter);
    }

    private void setupDemoDataAndAdapter() {
        if (productList.isEmpty()) {
//            productList.add(new Product("123", "Sữa tươi", 25000.0, 10, "A1", "Hộp", "HSD 2026", "2025-11-01", "2025-11-14"));
//            productList.add(new Product("456", "Bút bi", 5000.0, 100, "B2", "Chiếc", "Bút Thiên Long", "2025-09-10", "2025-11-15"));
        }
        setupAdapter();
    }
}
