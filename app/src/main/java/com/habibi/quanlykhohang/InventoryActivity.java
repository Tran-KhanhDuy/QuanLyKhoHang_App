package com.habibi.quanlykhohang;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        lvInventory = findViewById(R.id.lvInventory);

        // Khởi tạo Retrofit (nếu dùng API)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gelatinously-commutative-jerrie.ngrok-free.dev/api/") // link của bạn
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ProductApiService.class);

        // Gọi API lấy danh sách sản phẩm thật
        loadInventoryFromApi();

        lvInventory.setOnItemClickListener((parent, view, position, id) -> {
            Product selected = productList.get(position);
            Intent intent = new Intent(InventoryActivity.this, ProductInfoActivity.class);
            intent.putExtra("product", selected);
            startActivity(intent);
        });

        // Xử lý nút quay lại và thêm sản phẩm nếu có trong layout:
        ImageButton btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> finish());
        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(InventoryActivity.this, ImportActivity.class);
            startActivity(intent);
        });
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
                    // Fallback demo nếu API lỗi hoặc dữ liệu rỗng
                    setupDemoDataAndAdapter();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                // Fallback demo nếu kết nối API lỗi
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
            productList.add(new Product("123", "Sữa tươi", 25000.0, 10, "A1", "Hộp", "HSD 2026", "2025-11-01", "2025-11-14"));
            productList.add(new Product("456", "Bút bi", 5000.0, 100, "B2", "Chiếc", "Bút Thiên Long", "2025-09-10", "2025-11-15"));
        }
        setupAdapter();
    }
}
