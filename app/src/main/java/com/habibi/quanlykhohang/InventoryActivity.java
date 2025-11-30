package com.habibi.quanlykhohang;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


// Retrofit

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryActivity extends AppCompatActivity {

    private ListView lvInventory;
    private final ArrayList<Product> productList = new ArrayList<>();
    private ProductApiService apiService;

    // Khai báo launcher để xử lý kết quả trả về sau khi xuất/xóa sản phẩm
    private ActivityResultLauncher<Intent> exportLauncher;

    private void sortByName() {
        productList.sort((p1, p2) ->
                p1.getProductName().compareToIgnoreCase(p2.getProductName()));
        setupAdapter();
    }

    private void sortByQuantityDes() {
        productList.sort((p1, p2) ->
                Integer.compare(p2.getProductQuantity(), p1.getProductQuantity()));
        setupAdapter();
    }


    private  void sortByLocation(){
        productList.sort((p1, p2) ->
                p1.getLocation().compareToIgnoreCase(p2.getLocation()));
        setupAdapter();
    }

    private void sortByUpdateDateDesc() {
        productList.sort((p1, p2) ->
                p2.getUpdateDate().compareToIgnoreCase(p1.getUpdateDate()));
        setupAdapter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        lvInventory = findViewById(R.id.lvInventory);

        apiService = RetrofitClient.getService(this);

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
        // Nút xuất hàng
        Button btnDel = findViewById(R.id.btnDel);
        btnDel.setOnClickListener(v -> {
            Intent intent = new Intent(InventoryActivity.this, ExportActivity.class);
            exportLauncher.launch(intent); // thay vì startActivity
        });

        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> showSortDialog());


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
            public void onResponse(@NonNull Call<List<Product>> call,
                                   @NonNull Response<List<Product>> response) {

                Log.e("CHECK_LOI", "Mã phản hồi từ Server: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    Log.d("DEBUG_PRODUCTS", new Gson().toJson(productList));
                    setupAdapter();
                } else {
                    Log.e("INV_API", "Error code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e("INV_API", "Body: " + response.errorBody().string());
                        } catch (Exception ignored) {}
                    }
                    setupDemoDataAndAdapter();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call,
                                  @NonNull Throwable t) {
                Log.e("INV_API", "Failure: " + t.getMessage(), t);
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

    private void showSortDialog() {
        String[] options = {
                "Tên (A-Z)",
                "Số lượng tăng dần",
                "Khu (A1, A2...)",
                "Ngày cập nhật mới nhất"
        };

        new AlertDialog.Builder(this)
                .setTitle("Sắp xếp theo")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: sortByName(); break;
                        case 1: sortByQuantityDes(); break;
                        case 2: sortByLocation(); break;
                        case 3: sortByUpdateDateDesc(); break;
                    }
                })
                .show();
    }


}
