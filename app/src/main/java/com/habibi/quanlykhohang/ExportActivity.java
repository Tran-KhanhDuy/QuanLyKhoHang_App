package com.habibi.quanlykhohang;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageButton;
import java.text.SimpleDateFormat;
import java.util.Date;


import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.time.LocalDateTime;

public class ExportActivity extends AppCompatActivity {
    private ProductApiService apiService;

    private EditText etBarcode, etQuantity, etPrice, etLocation, etProductUnit, etProductDescription;
    private TextView tvProductName, tvCurrentStock, tvCreateDate, tvUpdateDate;
    private Product selectedProduct;

    private final ActivityResultLauncher<Intent> barcodeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                IntentResult intentResult = IntentIntegrator.parseActivityResult(result.getResultCode(), result.getData());
                if (intentResult != null && intentResult.getContents() != null) {
                    String barcode = intentResult.getContents();
                    etBarcode.setText(barcode);

                    // Gọi API lấy sản phẩm theo barcode
                    Call<Product> call = apiService.getProductByBarcode(barcode);
                    call.enqueue(new Callback<Product>() {
                        @Override
                        public void onResponse(@NonNull Call<Product> call, @NonNull Response<Product> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                selectedProduct = response.body();

                                etPrice.setText(String.valueOf(selectedProduct.getPrice()));
                                etQuantity.setText(""); // Để người dùng nhập số lượng xuất
                                etLocation.setText(selectedProduct.getLocation());
                                etProductUnit.setText(selectedProduct.getProductUnit());
                                etProductDescription.setText(selectedProduct.getProductDescription());
                                tvProductName.setText(getString(R.string.product_name_label, selectedProduct.getProductName()));
                                tvCurrentStock.setText(getString(R.string.current_stock_label, selectedProduct.getQuantity()));
                                tvCreateDate.setText("Ngày tạo: " + selectedProduct.getCreateDate());
                                tvUpdateDate.setText("Ngày cập nhật: " + selectedProduct.getUpdateDate());
                            } else {
                                tvProductName.setText(getString(R.string.no_product_data));
                                clearInfo();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
                            tvProductName.setText(getString(R.string.network_error));
                        }
                    });
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gelatinously-commutative-jerrie.ngrok-free.dev/api/") // Kết thúc bằng /
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ProductApiService.class);

        etBarcode = findViewById(R.id.etBarcode);
        etQuantity = findViewById(R.id.etQuantity);
        Button btnScan = findViewById(R.id.btnScan);
        Button btnExport = findViewById(R.id.btnExport);
        tvProductName = findViewById(R.id.etName);
        tvCurrentStock = findViewById(R.id.tvCurrentStock);
        etPrice = findViewById(R.id.etPrice);
        etLocation = findViewById(R.id.etLocation);
        etProductUnit = findViewById(R.id.etProductUnit);
        etProductDescription = findViewById(R.id.etProductDescription);
        tvCreateDate = findViewById(R.id.tvCreateDate);
        tvUpdateDate = findViewById(R.id.tvUpdateDate);
        Button btnDecrease = findViewById(R.id.btnDecrease);
        Button btnIncrease = findViewById(R.id.btnIncrease);
        EditText etQuantity = findViewById(R.id.etQuantity);

        btnScan.setOnClickListener(v -> scanBarcode());
        btnExport.setOnClickListener(v -> exportProduct());

        ImageButton btnReturn = findViewById(R.id.btnReturn);
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
        String priceText = etPrice.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String unit = etProductUnit.getText().toString().trim();
        String desc = etProductDescription.getText().toString().trim();

        if (barcode.isEmpty() || name.isEmpty() || priceText.isEmpty()
                || quantityText.isEmpty() || location.isEmpty()
                || unit.isEmpty() || desc.isEmpty()) {
            showAlert(getString(R.string.error_title), getString(R.string.fill_all_fields_error));
            return;
        }

        if (selectedProduct == null) {
            showAlert(getString(R.string.error_title), getString(R.string.no_product_data));
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int quantityToExport = Integer.parseInt(quantityText);

            if (quantityToExport <= 0) {
                showAlert(getString(R.string.error_title), getString(R.string.quantity_must_be_positive_error));
                return;
            }

            if (quantityToExport > selectedProduct.getQuantity()) {
                showAlert(getString(R.string.error_title), "Số lượng xuất không được lớn hơn tồn kho!");
                return;
            }

            int newStockQuantity = selectedProduct.getQuantity() - quantityToExport;

            // Ghi nhận thời gian xuất kho mới:
            String updateDate = getCurrentDateTimeString();

            // Tạo đối tượng Product với số lượng mới và ngày cập nhật
            Product updatedProduct = new Product(
                    barcode,
                    name,
                    price,
                    newStockQuantity,
                    location,
                    unit,
                    desc,
                    selectedProduct.getCreateDate(), // Giữ nguyên ngày tạo
                    updateDate                        // Ngày cập nhật mới nhất
            );

            exportProductToApi(updatedProduct);

        } catch (NumberFormatException e) {
            showAlert(getString(R.string.error_title), getString(R.string.invalid_price_or_quantity_error));
        }
    }

    // Hàm PUT lên API để xuất kho
    private void exportProductToApi(Product updatedProduct) {
        Call<Product> call = apiService.updateProduct(updatedProduct);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful()) {
                    showAlert("Thành công", "Xuất kho thành công!", () -> {
                        clearInfo();
                    });
                } else {
                    showAlert("Lỗi", "Không thể cập nhật sản phẩm qua API (error " + response.code() + ")");
                }
            }
            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                showAlert("Lỗi mạng/API", t.getMessage());
            }
        });
    }

    private String getCurrentDateTimeString() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
    }

    private void clearInfo() {
        selectedProduct = null;
        etBarcode.setText("");
        etPrice.setText("");
        etLocation.setText("");
        etProductUnit.setText("");
        etProductDescription.setText("");
        tvProductName.setText(getString(R.string.product_name_default));
        tvCurrentStock.setText(getString(R.string.current_stock_default));
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
