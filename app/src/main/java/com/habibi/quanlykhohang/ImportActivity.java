package com.habibi.quanlykhohang;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageButton;
import java.io.IOException;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Retrofit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ImportActivity extends AppCompatActivity {
    private Retrofit retrofit;
    private ProductApiService apiService;
    private AutoCompleteTextView etProductName;
    private ArrayAdapter<String> nameAdapter;
    private List<Product> suggestedProducts = new ArrayList<>();
    private Call<List<Product>> searchCall;
    private Product selectedProduct;
    private EditText etProductCode, etQuantity, etLocation, etProductUnit, etProductDescription;
    private Button btnScan, btnSave;
    private TextView tvStatus, tvCreateDate, tvUpdateDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        apiService = RetrofitClient.getService(this);

        etProductCode = findViewById(R.id.etBarcode);
        etProductName = findViewById(R.id.etName); // AutoCompleteTextView trong XML
        etQuantity = findViewById(R.id.etQuantity);
        etLocation = findViewById(R.id.etLocation);
        etProductUnit = findViewById(R.id.etProductUnit);
        etProductDescription = findViewById(R.id.etProductDescription);
        btnScan = findViewById(R.id.btnScan);
        btnSave = findViewById(R.id.btnSave);
        tvStatus = findViewById(R.id.tvStatus);
        tvCreateDate = findViewById(R.id.tvCreateDate);
        tvUpdateDate = findViewById(R.id.tvUpdateDate);

// 1. Adapter cho AutoComplete tên sản phẩm
        nameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        etProductName.setAdapter(nameAdapter);
        etProductName.setThreshold(1);

        etProductName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchCall != null) searchCall.cancel();
                if (s.length() > 0) {
                    searchCall = apiService.searchProductsByName(s.toString());
                    searchCall.enqueue(new Callback<List<Product>>() {
                        @Override
                        public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                            Log.d("ImportSearchByName", "code=" + response.code());
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
                                    if (!names.isEmpty()) etProductName.showDropDown();
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Product>> call, Throwable t) {
                            if (!call.isCanceled()) {
                                Log.e("ImportSearchByName", "API error: " + t.getMessage());
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

        etProductName.setOnItemClickListener((parent, view, position, id) -> {
            Product p = suggestedProducts.get(position);
            fillProductInfo(p);
        });

// 2. Scan barcode
        btnScan.setOnClickListener(v -> scanBarcode());
        btnSave.setOnClickListener(v -> saveImport());

        ImageButton btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(ImportActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
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

        if (result != null && result.getContents() != null) {
            String productCode = result.getContents();
            etProductCode.setText(productCode);
            fetchProductByBarcode(productCode);
            tvStatus.setText("📝 Đã quét mã: " + productCode + "\nNhập thông tin để lưu!");
            etProductName.requestFocus();
        }
    }

    private void saveImport() {
        String code = etProductCode.getText().toString().trim();
        String name = etProductName.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String unit = etProductUnit.getText().toString().trim();
        String desc = etProductDescription.getText().toString().trim();

        if (code.isEmpty() || name.isEmpty() || quantityText.isEmpty()
                || location.isEmpty() || unit.isEmpty() || desc.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin");
            return;
        }

        int quantityToImport;
        try {
            quantityToImport = Integer.parseInt(quantityText);
            if (quantityToImport <= 0) {
                showAlert("Lỗi", "Số lượng nhập phải > 0");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số lượng không hợp lệ");
            return;
        }

        String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(new Date());

        // 1. Nếu đã chọn sản phẩm có sẵn (scan hoặc search ra)
        if (selectedProduct != null && selectedProduct.getId() != 0) {
            int newStock = selectedProduct.getProductQuantity() + quantityToImport;

            Product updated = new Product(
                    code,
                    name,
                    newStock,
                    location,
                    unit,
                    desc,
                    selectedProduct.getCreateDate(), // giữ nguyên
                    now                              // cập nhật ngày sửa
            );
            updated.setId(selectedProduct.getId());

            updateProductToApi(updated, quantityToImport);
        } else {
            // 2. Sản phẩm mới hoàn toàn
            Product newProduct = new Product(
                    code, name, quantityToImport, location, unit, desc, now, now
            );
            addProductToApi(newProduct);
        }
    }


    private void addProductToApi(Product newProduct) {
        // 1. Gọi API thêm sản phẩm
        apiService.addProduct(newProduct).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                // Kiểm tra xem server có trả về dữ liệu sản phẩm (kèm ID) không
                if (response.isSuccessful() && response.body() != null) {
                    Product savedProduct = response.body(); // Sản phẩm đã được Server lưu và trả về (có ID)

                    // --- BẮT ĐẦU ĐOẠN CODE GHI LỊCH SỬ ---

                    // Tạo đối tượng lịch sử để gửi đi
                    // (Lưu ý: Username và Date server sẽ tự điền dựa vào Token, ta chỉ cần gửi thông tin cơ bản)
                    WarehouseTransaction transaction = new WarehouseTransaction(
                            savedProduct.getId(),               // Lấy ID thật từ Server
                            savedProduct.getProductQuantity(),  // Số lượng vừa nhập
                            "Import",                           // Loại giao dịch
                            "Nhập hàng từ App Mobile"           // Ghi chú
                    );

                    // Gọi API ghi lịch sử (Chạy ngầm, không cần chờ kết quả để hiển thị thông báo)
                    apiService.addTransaction(transaction).enqueue(new Callback<Object>() {
                        @Override
                        public void onResponse(Call<Object> call, Response<Object> response) {
                            Log.d("HISTORY_LOG", "Đã lưu lịch sử nhập kho. Code: " + response.code());
                        }

                        @Override
                        public void onFailure(Call<Object> call, Throwable t) {
                            Log.e("HISTORY_LOG", "Lỗi lưu lịch sử: " + t.getMessage());
                        }
                    });
                    // --- KẾT THÚC ĐOẠN GHI LỊCH SỬ ---

                    // Hiển thị thông báo thành công cho người dùng
                    showAlert("Thành công", "Thêm sản phẩm mới và ghi lịch sử thành công!", () -> {
                        clearForm();
                    });

                } else {
                    // Xử lý lỗi nếu server từ chối
                    String errorMsg = "Lỗi thêm sản phẩm (Mã: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += "\nChi tiết: " + response.errorBody().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    showAlert("Lỗi", errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                showAlert("Lỗi kết nối", t.getMessage());
            }
        });
    }
    private void updateProductToApi(Product updatedProduct, int quantityImported) {
        apiService.updateProduct(updatedProduct.getId(), updatedProduct)
                .enqueue(new Callback<Product>() {
                    @Override
                    public void onResponse(Call<Product> call, Response<Product> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Product saved = response.body();

                            // ghi lịch sử import
                            WarehouseTransaction transaction = new WarehouseTransaction(
                                    saved.getId(),
                                    quantityImported,
                                    "Import",
                                    "Nhập thêm hàng từ App Mobile"
                            );
                            apiService.addTransaction(transaction).enqueue(new Callback<Object>() {
                                @Override
                                public void onResponse(Call<Object> call, Response<Object> res) {
                                    Log.d("HISTORY_LOG", "Đã lưu lịch sử nhập thêm");
                                }

                                @Override
                                public void onFailure(Call<Object> call, Throwable t) {
                                    Log.e("HISTORY_LOG", "Lỗi lưu lịch sử: " + t.getMessage());
                                }
                            });

                            showAlert("Thành công", "Đã cập nhật số lượng sản phẩm!", () -> {
                                clearForm();
                            });
                        } else {
                            showAlert("Lỗi", "Không cập nhật được sản phẩm (mã " + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<Product> call, Throwable t) {
                        showAlert("Lỗi kết nối", t.getMessage());
                    }
                });
    }


    private void clearForm() {
        etProductCode.setText("");
        etProductName.setText("");
        etQuantity.setText("");
        etLocation.setText("");
        etProductUnit.setText("");
        etProductDescription.setText("");
        tvStatus.setText("");
        etProductCode.requestFocus();
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

    private void fetchProductByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) return;

        apiService.getProductByBarcode(barcode.trim())
                .enqueue(new Callback<Product>() {
                    @Override
                    public void onResponse(Call<Product> call, Response<Product> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            fillProductInfo(response.body());
                        } else {
                            // không có -> import như sản phẩm mới
                            tvStatus.setText("Mã mới, nhập thông tin để tạo sản phẩm");
                        }
                    }

                    @Override
                    public void onFailure(Call<Product> call, Throwable t) {
                        tvStatus.setText("Lỗi mạng, không kiểm tra được mã");
                    }
                });
    }
    private void fillProductInfo(Product p) {
        selectedProduct = p;
        etProductCode.setText(p.getProductCode());
        etProductName.setText(p.getProductName());
        etLocation.setText(p.getLocation());
        etProductUnit.setText(p.getProductUnit());
        etProductDescription.setText(p.getProductDescription());
        // nếu layout có:
        tvCreateDate.setText("Ngày tạo: " + p.getCreateDate());
        tvUpdateDate.setText("Ngày cập nhật: " + p.getUpdateDate());
        tvStatus.setText("Đã tìm thấy sản phẩm, nhập SỐ LƯỢNG cần nhập thêm");
    }


}
