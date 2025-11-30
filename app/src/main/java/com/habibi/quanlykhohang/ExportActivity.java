package com.habibi.quanlykhohang;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import io.realm.Realm;

public class ExportActivity extends AppCompatActivity {
    private ProductApiService apiService;
    private AutoCompleteTextView etName;
    private EditText etBarcode, etQuantity, etLocation, etProductUnit, etProductDescription;
    private TextView tvCurrentStock, tvCreateDate, tvUpdateDate;
    private ArrayAdapter<String> nameAdapter;
    private List<Product> suggestedProducts = new ArrayList<>();
    private Product selectedProduct;
    private Call<List<Product>> searchCall;

    private final ActivityResultLauncher<Intent> barcodeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                IntentResult intentResult = IntentIntegrator.parseActivityResult(result.getResultCode(), result.getData());
                if (intentResult != null && intentResult.getContents() != null) {
                    String barcode = intentResult.getContents();
                    etBarcode.setText(barcode);
                    fetchProductByBarcode(barcode);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        // --- SỬA 1: Dùng Client chung để có Token ---
        apiService = RetrofitClient.getService(this);
        // --------------------------------------------

        etBarcode = findViewById(R.id.etBarcode);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etLocation = findViewById(R.id.etLocation);
        etProductUnit = findViewById(R.id.etProductUnit);
        etProductDescription = findViewById(R.id.etProductDescription);
        tvCurrentStock = findViewById(R.id.tvCurrentStock);
        tvCreateDate = findViewById(R.id.tvCreateDate);
        tvUpdateDate = findViewById(R.id.tvUpdateDate);
        Button btnScan = findViewById(R.id.btnScan);
        Button btnExport = findViewById(R.id.btnExport);
        Button btnDecrease = findViewById(R.id.btnDecrease);
        Button btnIncrease = findViewById(R.id.btnIncrease);
        ImageButton btnReturn = findViewById(R.id.btnReturn);

        nameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        etName.setAdapter(nameAdapter);
        etName.setThreshold(1);

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchCall != null) {
                    searchCall.cancel();
                }

                if (s.length() > 0) {
                    searchCall = apiService.searchProductsByName(s.toString());
                    searchCall.enqueue(new Callback<List<Product>>() {
                        @Override
                        public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                suggestedProducts = response.body();
                                List<String> names = new ArrayList<>();
                                for (Product p : suggestedProducts) {
                                    names.add(p.getProductName());
                                }
                                runOnUiThread(() -> {
                                    nameAdapter.clear();
                                    nameAdapter.addAll(names);
                                    nameAdapter.notifyDataSetChanged();
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Product>> call, Throwable t) {
                            if (!call.isCanceled()) {
                                Log.e("SearchByName", "API call failed: ", t);
                            }
                        }
                    });
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etName.setOnItemClickListener((parent, view, position, id) -> {
            Product selected = suggestedProducts.get(position);
            fillProductInfo(selected);
        });

        etBarcode.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String barcode = etBarcode.getText().toString().trim();
                if (!barcode.isEmpty()) {
                    fetchProductByBarcode(barcode);
                }
            }
        });

        btnScan.setOnClickListener(v -> scanBarcode());
        btnExport.setOnClickListener(v -> exportProduct());
        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(ExportActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnDecrease.setOnClickListener(v -> {
            String val = etQuantity.getText().toString().trim();
            int value = val.isEmpty() ? 0 : Integer.parseInt(val);
            if (value > 1) value--;
            etQuantity.setText(String.valueOf(value));
        });

        btnIncrease.setOnClickListener(v -> {
            String val = etQuantity.getText().toString().trim();
            int value = val.isEmpty() ? 0 : Integer.parseInt(val);
            value++;
            etQuantity.setText(String.valueOf(value));
        });
    }

    private void fillProductInfo(Product product) {
        selectedProduct = product;
        etName.setText(product.getProductName(), false);
        etBarcode.setText(product.getProductCode());
        etQuantity.setText("");
        etLocation.setText(product.getLocation());
        etProductUnit.setText(product.getProductUnit());
        etProductDescription.setText(product.getProductDescription());
        tvCurrentStock.setText(getString(R.string.current_stock_label, product.getProductQuantity()));
        tvCreateDate.setText("Ngày tạo: " + product.getCreateDate());
        tvUpdateDate.setText("Ngày cập nhật: " + product.getUpdateDate());
    }

    private void fetchProductByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            clearInfo();
            etName.setText(getString(R.string.no_product_data));
            return;
        }

        Call<Product> call = apiService.getProductByBarcode(barcode.trim());
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(@NonNull Call<Product> call,
                                   @NonNull Response<Product> response) {

                if (response.isSuccessful() && response.body() != null) {
                    fillProductInfo(response.body());
                } else {
                    clearInfo();
                    etName.setText(getString(R.string.no_product_data));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Product> call,
                                  @NonNull Throwable t) {
                clearInfo();
                etName.setText(getString(R.string.network_error));
            }
        });
    }


    private void scanBarcode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt(getString(R.string.scan_prompt));
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        barcodeLauncher.launch(integrator.createScanIntent());
    }

    private void exportProduct() {
        String barcode = etBarcode.getText().toString().trim();
        String name = (selectedProduct != null) ? selectedProduct.getProductName() : "";
        String quantityText = etQuantity.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String unit = etProductUnit.getText().toString().trim();
        String desc = etProductDescription.getText().toString().trim();

        if (barcode.isEmpty() || name.isEmpty() || quantityText.isEmpty() || location.isEmpty() || unit.isEmpty() || desc.isEmpty()) {
            showAlert("Lỗi", "Vui lòng điền đầy đủ tất cả thông tin!");
            return;
        }

        if (selectedProduct == null) {
            showAlert("Lỗi", "Không có thông tin sản phẩm!");
            return;
        }

        try {
            int quantityToExport = Integer.parseInt(quantityText);

            if (quantityToExport <= 0) {
                showAlert("Lỗi", "Số lượng xuất phải > 0");
                return;
            }

            if (quantityToExport > selectedProduct.getProductQuantity()) {
                showAlert("Lỗi", "Số lượng xuất không được lớn hơn tồn kho!");
                return;
            }

            // Trường hợp 1: Xuất một phần (Update)
            if (quantityToExport < selectedProduct.getProductQuantity()) {
                int newStockQuantity = selectedProduct.getProductQuantity() - quantityToExport;
                String updateDate = getCurrentDateTimeString();

                Product updatedProduct = new Product(
                        selectedProduct.getProductCode(),
                        selectedProduct.getProductName(),
                        newStockQuantity,
                        selectedProduct.getLocation(),
                        selectedProduct.getProductUnit(),
                        selectedProduct.getProductDescription(),
                        selectedProduct.getCreateDate(),
                        updateDate
                );
                updatedProduct.setId(selectedProduct.getId());

                // Truyền thêm quantityToExport để ghi lịch sử
                exportProductToApi(selectedProduct.getId(), updatedProduct, quantityToExport);
                return;
            }

            // Trường hợp 2: Xuất hết (Delete)
            if (quantityToExport == selectedProduct.getProductQuantity()) {
                Call<Void> call = apiService.deleteProduct(selectedProduct.getId());

                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // --- SỬA 2: GHI LỊCH SỬ KHI XÓA ---
                            recordTransaction(selectedProduct.getId(), quantityToExport);
                            // ----------------------------------

                            showAlert("Thành công", "Đã xuất hết sản phẩm!\nSản phẩm đã bị xóa khỏi kho.", () -> {
                                Intent resultIntent = new Intent();
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            });
                        } else {
                            String errorMsg = "Không thể xóa sản phẩm qua API (error " + response.code() + ")";
                            if (response.errorBody() != null) {
                                try {
                                    errorMsg += "\nLý do: " + response.errorBody().string();
                                } catch (IOException e) {
                                }
                            }
                            showAlert("Lỗi", errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        showAlert("Lỗi mạng/API", t.getMessage());
                    }
                });
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số lượng không hợp lệ!");
        }
    }

    // Đã thêm tham số quantityExported để ghi log
    private void exportProductToApi(int id, Product updatedProduct, int quantityExported) {
        Call<Product> call = apiService.updateProduct(id, updatedProduct);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful()) {

                    // --- SỬA 3: GHI LỊCH SỬ KHI UPDATE ---
                    recordTransaction(id, quantityExported);
                    // -------------------------------------

                    showAlert("Thành công", "Xuất kho thành công!", () -> {
                        Intent resultIntent = new Intent();
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    });
                } else {
                    String errorMsg = "Không thể cập nhật sản phẩm (mã lỗi: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String serverError = response.errorBody().string();
                            errorMsg += "\nLý do: " + serverError;
                        } catch (IOException e) {
                        }
                    }
                    showAlert("Lỗi Cập Nhật", errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                showAlert("Lỗi Mạng/API", t.getMessage());
            }
        });
    }

    // Hàm phụ để gọi API ghi lịch sử (Code gọn hơn)
    private void recordTransaction(int productId, int quantity) {
        WarehouseTransaction transaction = new WarehouseTransaction(
                productId,
                quantity,
                "Export",
                "Xuất hàng từ App Mobile"
        );

        apiService.addTransaction(transaction).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                Log.d("HISTORY", "Đã lưu lịch sử xuất kho");
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.e("HISTORY", "Lỗi lưu lịch sử: " + t.getMessage());
                Log.e("HISTORY", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private String getCurrentDateTimeString() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
    }

    private void clearInfo() {
        selectedProduct = null;
        etBarcode.setText("");
        etName.setText("");
        etQuantity.setText("");
        etLocation.setText("");
        etProductUnit.setText("");
        etProductDescription.setText("");
        tvCurrentStock.setText("Tồn kho: -");
        tvCreateDate.setText("Ngày tạo: ");
        tvUpdateDate.setText("Ngày cập nhật: ");
    }

    private void showAlert(String title, String message, Runnable onOK) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> onOK.run())
                .show();
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}