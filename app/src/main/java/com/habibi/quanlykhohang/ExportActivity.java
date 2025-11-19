package com.habibi.quanlykhohang;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExportActivity extends AppCompatActivity {
    private ProductApiService apiService;

    // Đúng biến: etName là AutoCompleteTextView cho tên sản phẩm
    private AutoCompleteTextView etName;
    private EditText etBarcode, etQuantity, etPrice, etLocation, etProductUnit, etProductDescription;
    private TextView tvCurrentStock, tvCreateDate, tvUpdateDate;
    private ArrayAdapter<String> nameAdapter;
    private List<Product> suggestedProducts = new ArrayList<>();
    private Product selectedProduct;

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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gelatinously-commutative-jerrie.ngrok-free.dev/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ProductApiService.class);

        // Gán đúng id và kiểu biến
        etBarcode = findViewById(R.id.etBarcode);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etPrice = findViewById(R.id.etPrice);
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

        // Thiết lập adapter và logic search cho AutoCompleteTextView
        nameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        etName.setAdapter(nameAdapter);
        etName.setThreshold(1);

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Khi người dùng gõ vào trường tên, gọi API search
                if (s.length() > 0) {
                    apiService.searchProductsByName(s.toString())
                            .enqueue(new Callback<List<Product>>() {
                                @Override
                                public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        suggestedProducts = response.body();
                                        List<String> names = new ArrayList<>();
                                        for (Product p : suggestedProducts) {
                                            names.add(p.getProductName());
                                        }
                                        nameAdapter.clear();
                                        nameAdapter.addAll(names);
                                        nameAdapter.notifyDataSetChanged();
                                    }
                                }
                                @Override
                                public void onFailure(Call<List<Product>> call, Throwable t) {}
                            });
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        etName.setOnItemClickListener((parent, view, position, id) -> {
            // Khi chọn tên sản phẩm, tự động điền info
            Product selected = suggestedProducts.get(position);
            fillProductInfo(selected);
        });

        // Khi rời khỏi EditText barcode thì lấy info sản phẩm theo barcode nếu có nhập/thay đổi
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

    // Dùng chung khi điền info sản phẩm vào form
    private void fillProductInfo(Product product) {
        selectedProduct = product;
        etBarcode.setText(product.getProductCode());
        etName.setText(product.getProductName(), false); // Không gợi ý lại nữa
        etPrice.setText(String.valueOf(product.getPrice()));
        etQuantity.setText("");
        etLocation.setText(product.getLocation());
        etProductUnit.setText(product.getProductUnit());
        etProductDescription.setText(product.getProductDescription());
        tvCurrentStock.setText(getString(R.string.current_stock_label, product.getQuantity()));
        tvCreateDate.setText("Ngày tạo: " + product.getCreateDate());
        tvUpdateDate.setText("Ngày cập nhật: " + product.getUpdateDate());
    }

    // Gọi API lấy sản phẩm theo mã barcode
    private void fetchProductByBarcode(String barcode) {
        Call<Product> call = apiService.getProductByBarcode(barcode);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(@NonNull Call<Product> call, @NonNull Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fillProductInfo(response.body());
                } else {
                    etName.setText(getString(R.string.no_product_data));
                    clearInfo();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
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

            if (quantityToExport < selectedProduct.getQuantity()) {
                int newStockQuantity = selectedProduct.getQuantity() - quantityToExport;
                String updateDate = getCurrentDateTimeString();

                Product updatedProduct = new Product(
                        selectedProduct.getProductCode(),
                        selectedProduct.getProductName(),
                        selectedProduct.getPrice(),
                        newStockQuantity,
                        selectedProduct.getLocation(),
                        selectedProduct.getProductUnit(),
                        selectedProduct.getProductDescription(),
                        selectedProduct.getCreateDate(),
                        updateDate
                );
                exportProductToApi(updatedProduct); // đây là nơi gọi cập nhật qua API
                return; // Xử lý xong trường hợp này thì trả về không làm tiếp!
            }

            // Trường hợp xuất đủ số lượng --> bước tiếp theo mới hướng dẫn
            if (quantityToExport == selectedProduct.getQuantity()) {
                // Gọi API xóa sản phẩm khỏi kho
                Call<Void> call = apiService.deleteProduct(selectedProduct.getId());// hoặc dùng barcode nếu API là theo mã

                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if(response.isSuccessful()){
                            showAlert("Thành công", "Đã xuất hết sản phẩm!\nSản phẩm đã bị xóa khỏi kho.", () -> {
                                Intent resultIntent = new Intent();
                                setResult(RESULT_OK, resultIntent);
                                finish(); // Xóa thông tin khỏi giao diện, làm mới form
                            });
                        }else {
                            showAlert("Lỗi", "Không thể xóa sản phẩm qua API (error " + response.code() + ")");
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
            showAlert(getString(R.string.error_title), getString(R.string.invalid_price_or_quantity_error));
        }
    }

    private void exportProductToApi(Product updatedProduct) {
        Call<Product> call = apiService.updateProduct(updatedProduct);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful()) {
                    showAlert("Thành công", "Xuất kho thành công!", () -> {
                        Intent resultIntent = new Intent();
                        setResult(RESULT_OK, resultIntent);
                        finish();
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
        etName.setText("");
        etPrice.setText("");
        etLocation.setText("");
        etProductUnit.setText("");
        etProductDescription.setText("");
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
