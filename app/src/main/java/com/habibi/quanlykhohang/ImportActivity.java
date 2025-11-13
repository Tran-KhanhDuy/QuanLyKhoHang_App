package com.habibi.quanlykhohang;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ImportActivity extends AppCompatActivity {
    private Retrofit retrofit;
    private ProductApiService apiService;

    private EditText etBarcode, etName, etPrice, etQuantity, etLocation;
    private Button btnScan, btnSave;
    private TextView tvStatus;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        retrofit = new Retrofit.Builder()
                .baseUrl("https://gelatinously-commutative-jerrie.ngrok-free.dev/api/productsapi/") // Thay bằng link ngrok của bạn!
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ProductApiService.class);


        etBarcode = findViewById(R.id.etBarcode);
        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        etLocation = findViewById(R.id.etLocation);
        btnScan = findViewById(R.id.btnScan);
        btnSave = findViewById(R.id.btnSave);
        tvStatus = findViewById(R.id.tvStatus);

        btnScan.setOnClickListener(v -> scanBarcode());
        btnSave.setOnClickListener(v -> saveImport());

        ImageButton btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(ImportActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            finish();
        });
    }

    private void scanBarcode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Quét mã vạch sản phẩm");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() != null) {
                String barcode = result.getContents();
                etBarcode.setText(barcode);

                Product existingProduct = dbHelper.getProductByBarcode(barcode);
                if (existingProduct != null) {
                    etName.setText(existingProduct.getName());
                    etPrice.setText(String.valueOf(existingProduct.getPrice()));
                    etLocation.setText(existingProduct.getLocation());
                    tvStatus.setText("✓ Sản phẩm đã tồn tại. Tồn kho: " + existingProduct.getQuantity());
                    etQuantity.requestFocus();
                } else {
                    tvStatus.setText("📝 Sản phẩm mới, điền thêm thông tin");
                    etName.requestFocus();
                }
            }
        }
    }

    private void saveImport() {
        String barcode = etBarcode.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String priceText = etPrice.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (barcode.isEmpty() || name.isEmpty() || priceText.isEmpty() ||
                quantityText.isEmpty() || location.isEmpty()) {
            showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int quantity = Integer.parseInt(quantityText);

            if (quantity <= 0) {
                showAlert("Lỗi", "Số lượng phải lớn hơn 0");
                return;
            }

            Product existingProduct = dbHelper.getProductByBarcode(barcode);

            if (existingProduct != null) {
                // Sản phẩm đã tồn tại, vẫn cập nhật tồn kho LOCAL (nếu muốn giữ tính năng nhập nhanh offline)
                if (dbHelper.updateInventory(barcode, quantity)) {
                    showAlert("Thành công", "Nhập hàng thành công!\nTồn kho mới: " +
                            (existingProduct.getQuantity() + quantity), () -> {
                        clearForm();
                    });
                } else {
                    showAlert("Lỗi", "Không thể cập nhật hàng");
                }
            } else {
                // Sản phẩm mới, gọi lên API để thêm vào database chung
                Product newProduct = new Product(barcode, name, price, quantity, location);
                addProductToApi(newProduct); // HÀM NÀY gửi dữ liệu lên API qua Retrofit
            }
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Giá hoặc số lượng không hợp lệ");
        }
    }

    private void addProductToApi(Product newProduct) {
        Call<Product> call = apiService.addProduct(newProduct);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful()) {
                    showAlert("Thành công", "Thêm sản phẩm mới thành công!", () -> {
                        clearForm();
                    });
                } else {
                    showAlert("Lỗi", "Không thể thêm sản phẩm qua API (error " + response.code() + ")");
                }
            }
            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                showAlert("Lỗi mạng/API", t.getMessage());
            }
        });
    }


    private void clearForm() {
        etBarcode.setText("");
        etName.setText("");
        etPrice.setText("");
        etQuantity.setText("");
        etLocation.setText("");
        tvStatus.setText("");
        etBarcode.requestFocus();
    }

    private void showAlert(String title, String message, Runnable onOK) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> onOK.run())
                .show();
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
