package com.habibi.quanlykhohang;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

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
    // private DatabaseHelper dbHelper;  // KHÔNG dùng SQLite nữa

    private Retrofit retrofit;
    private ProductApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        lvInventory = findViewById(R.id.lvInventory);

        // Khởi tạo Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl("https://gelatinously-commutative-jerrie.ngrok-free.dev/api/") // Thay bằng link ngrok của bạn!
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ProductApiService.class);

        loadInventoryFromApi();

        ImageButton btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(InventoryActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            finish();
        });
        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(InventoryActivity.this, ImportActivity.class);
            startActivity(intent);
        });

    }

    private void loadInventoryFromApi() {
        Call<List<Product>> call = apiService.getAllProducts(); // hàm GET lên API

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                List<String> items = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    if (products.isEmpty()) {
                        items.add("Không có sản phẩm nào");
                    } else {
                        for (Product product : products) {
                            String item = String.format(
                                    "Mã: %s | Tên: %s | SL: %d | Vị trí: %s | Giá: %.0f",
                                    product.getBarcode(),
                                    product.getName(),
                                    product.getQuantity(),
                                    product.getLocation(),
                                    product.getPrice()
                            );
                            items.add(item);
                        }
                    }
                } else {
                    items.add("Lỗi tải dữ liệu từ API!");
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(InventoryActivity.this, android.R.layout.simple_list_item_1, items);
                lvInventory.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                List<String> items = new ArrayList<>();
                items.add("Lỗi kết nối: " + t.getMessage());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(InventoryActivity.this, android.R.layout.simple_list_item_1, items);
                lvInventory.setAdapter(adapter);
            }
        });
    }
}
