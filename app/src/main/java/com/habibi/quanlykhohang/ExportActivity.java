package com.habibi.quanlykhohang;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageButton;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExportActivity extends AppCompatActivity {
    private Retrofit retrofit;
    private ProductApiService apiService;

    private EditText etBarcode, etQuantity;
    private Button btnScan, btnExport;
    private TextView tvProductName, tvCurrentStock;
    private DatabaseHelper dbHelper;
    private Product selectedProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        retrofit = new Retrofit.Builder()
                .baseUrl("https://gelatinously-commutative-jerrie.ngrok-free.dev/api/productsapi/") // Thay bằng link ngrok của bạn!
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ProductApiService.class);

        etBarcode = findViewById(R.id.etBarcode);
        etQuantity = findViewById(R.id.etQuantity);
        btnScan = findViewById(R.id.btnScan);
        btnExport = findViewById(R.id.btnExport);
        tvProductName = findViewById(R.id.tvProductName);
        tvCurrentStock = findViewById(R.id.tvCurrentStock);

        btnScan.setOnClickListener(v -> scanBarcode());
        btnExport.setOnClickListener(v -> exportProduct());

        ImageButton btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(ExportActivity.this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void scanBarcode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Quét mã vạch sản phẩm xuất");
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
                loadProduct(barcode);
            }
        }
    }

    private void loadProduct(String barcode) {
        Product product = dbHelper.getProductByBarcode(barcode);
        if (product != null) {
            selectedProduct = product;
            tvProductName.setText("Tên: " + product.getName());
            tvCurrentStock.setText("Tồn kho: " + product.getQuantity());
        } else {
            showAlert("Lỗi", "Không tìm thấy sản phẩm");
            clearInfo();
        }
    }

    private void exportProduct() {
        if (selectedProduct == null) {
            showAlert("Lỗi", "Vui lòng quét mã vạch trước");
            return;
        }

        String quantityText = etQuantity.getText().toString().trim();
        if (quantityText.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập số lượng");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showAlert("Lỗi", "Số lượng phải lớn hơn 0");
                return;
            }

            if (quantity > selectedProduct.getQuantity()) {
                showAlert("Lỗi", "Số lượng xuất vượt quá tồn kho");
                return;
            }

            // Tạo bản ghi mới, update quantity và gọi lên API
            int newStock = selectedProduct.getQuantity() - quantity;
            Product updatedProduct = new Product(
                    selectedProduct.getBarcode(),
                    selectedProduct.getName(),
                    selectedProduct.getPrice(),
                    newStock,
                    selectedProduct.getLocation()
            );
            updatedProduct.setId(selectedProduct.getId());

            // Gọi xuất kho qua API PUT
            exportProductToApi(updatedProduct, selectedProduct.getId());

        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số lượng không hợp lệ");
        }
    }

    private void exportProductToApi(Product updatedProduct, int id) {
        Call<Product> call = apiService.updateProduct(id, updatedProduct);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful()) {
                    showAlert("Thành công", "Xuất hàng thành công!\nTồn kho mới: " +
                            updatedProduct.getQuantity(), () -> {
                        clearInfo();
                        etQuantity.setText("");
                    });
                } else {
                    showAlert("Lỗi", "Không thể cập nhật số lượng qua API (error " + response.code() + ")");
                }
            }
            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                showAlert("Lỗi mạng/API", t.getMessage());
            }
        });
    }


    private void clearInfo() {
        selectedProduct = null;
        etBarcode.setText("");
        tvProductName.setText("Tên: --");
        tvCurrentStock.setText("Tồn kho: --");
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
