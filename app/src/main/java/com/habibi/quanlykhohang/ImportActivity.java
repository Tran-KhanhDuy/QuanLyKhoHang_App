package com.habibi.quanlykhohang;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView; // Nhớ import cái này
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ImportActivity extends AppCompatActivity {
    private ProductApiService apiService;

    // Sửa EditText thành AutoCompleteTextView
    private AutoCompleteTextView etProductName, etProductUnit;
    private EditText etProductCode, etQuantity, etLocation, etProductDescription;
    private Button btnScan, btnSave;
    private TextView tvStatus;

    // Voice & Spinner
    private ImageButton btnVoiceName;
    private Spinner spSupplier;
    private ArrayAdapter<Supplier> supplierAdapter;
    private List<Supplier> listSupplier = new ArrayList<>();

    // Biến cho chức năng tìm kiếm (Lấy từ Export qua)
    private ArrayAdapter<String> nameSearchAdapter;
    private List<Product> suggestedProducts = new ArrayList<>();
    private Call<List<Product>> searchCall;
    private Product selectedProduct; // Sản phẩm đã chọn từ gợi ý

    private final ActivityResultLauncher<Intent> voiceLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> resultVoice = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (resultVoice != null && !resultVoice.isEmpty()) {
                        String text = resultVoice.get(0);
                        etProductName.setText(text);
                        // Sau khi voice nhập xong, có thể trigger tìm kiếm luôn nếu muốn
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        apiService = RetrofitClient.getService(this);

        // Ánh xạ
        etProductCode = findViewById(R.id.etBarcode);
        etProductName = findViewById(R.id.etName); // Đây là AutoCompleteTextView
        etQuantity = findViewById(R.id.etQuantity);
        etLocation = findViewById(R.id.etLocation);
        etProductUnit = findViewById(R.id.etProductUnit);
        String[] units = {"-- Chọn đơn vị tính --", "Cái", "Thùng", "Bịch", "Kg", "Tấn", "Mét", "Lít"};

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, units);

        etProductUnit.setAdapter(unitAdapter);
        etProductUnit.setThreshold(0); // gõ hoặc bấm vào là hiện danh sách

        etProductUnit.setOnClickListener(v -> etProductUnit.showDropDown());

        etProductDescription = findViewById(R.id.etProductDescription);
        btnScan = findViewById(R.id.btnScan);
        btnSave = findViewById(R.id.btnSave);
        tvStatus = findViewById(R.id.tvStatus);

        // --- CẤU HÌNH TÌM KIẾM TÊN SẢN PHẨM (GIỐNG EXPORT) ---
        nameSearchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        etProductName.setAdapter(nameSearchAdapter);
        etProductName.setThreshold(1); // Gõ 1 chữ là tìm

        etProductName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nếu sửa tên khác với sản phẩm đã chọn -> Reset thông tin
                if (selectedProduct != null && !s.toString().equals(selectedProduct.getProductName())) {
                    selectedProduct = null;
                    clearDependentFields();
                }

                if (searchCall != null) searchCall.cancel();

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
                                    nameSearchAdapter.clear();
                                    nameSearchAdapter.addAll(names);
                                    nameSearchAdapter.notifyDataSetChanged();
                                });
                            }
                        }
                        @Override
                        public void onFailure(Call<List<Product>> call, Throwable t) {}
                    });
                } else {
                    nameSearchAdapter.clear();
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Khi người dùng chọn 1 dòng gợi ý
        etProductName.setOnItemClickListener((parent, view, position, id) -> {
            Product selected = suggestedProducts.get(position);
            fillProductInfo(selected);
        });
        // -----------------------------------------------------

        // Cấu hình Voice
        btnVoiceName = findViewById(R.id.btnVoiceName);
        btnVoiceName.setOnClickListener(v -> startVoiceInput());

        // Cấu hình Spinner
        spSupplier = findViewById(R.id.spSupplier);
        supplierAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listSupplier);
        supplierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSupplier.setAdapter(supplierAdapter);
        loadSuppliers();

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

    // Hàm điền thông tin khi chọn gợi ý (hoặc quét mã ra hàng cũ)
    private void fillProductInfo(Product product) {
        selectedProduct = product;
        etProductName.setText(product.getProductName());
        etProductName.dismissDropDown(); // Ẩn gợi ý sau khi chọn

        etProductCode.setText(product.getProductCode());
        etLocation.setText(product.getLocation());
        etProductUnit.setText(product.getProductUnit());
        etProductDescription.setText(product.getProductDescription());

        // Riêng số lượng nhập thì để trống hoặc 0 để người dùng tự nhập thêm
        etQuantity.setText("");
        etQuantity.requestFocus(); // Nhảy trỏ chuột vào ô số lượng

        tvStatus.setText("✅ Đã tìm thấy: " + product.getProductName() + " (Tồn: " + product.getProductQuantity() + ")");
    }

    // Hàm xóa thông tin phụ khi đổi tên
    private void clearDependentFields() {
        etProductCode.setText("");
        etLocation.setText("");
        etProductUnit.setText("");
        etProductDescription.setText("");
        etQuantity.setText("");
        tvStatus.setText("");
    }

    private void loadSuppliers() {
        apiService.getSuppliers().enqueue(new Callback<List<Supplier>>() {
            @Override
            public void onResponse(Call<List<Supplier>> call, Response<List<Supplier>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listSupplier.clear();
                    listSupplier.addAll(response.body());
                    supplierAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<Supplier>> call, Throwable t) {}
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
            tvStatus.setText("🔍 Đang tìm thông tin...");

            // Gọi API tìm theo mã vạch để điền thông tin nếu có
            fetchProductByBarcode(productCode);
        }
    }

    // Hàm tìm sản phẩm theo mã vạch (khi quét)
    private void fetchProductByBarcode(String barcode) {
        apiService.getProductByBarcode(barcode).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fillProductInfo(response.body());
                } else {
                    tvStatus.setText("⚠️ Sản phẩm mới (Chưa có trong kho)");
                    etProductName.requestFocus();
                }
            }
            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                tvStatus.setText("❌ Lỗi kết nối");
            }
        });
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy nói tên sản phẩm...");
        try {
            voiceLauncher.launch(intent);
        } catch (Exception e) {
            showAlert("Lỗi", "Thiết bị không hỗ trợ nhập giọng nói");
        }
    }

    private void saveImport() {
        String code = etProductCode.getText().toString().trim();
        String name = etProductName.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String unit = etProductUnit.getText().toString().trim();
        String desc = etProductDescription.getText().toString().trim();

        if (code.isEmpty() || name.isEmpty() || quantityText.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đủ thông tin bắt buộc");
            return;
        }

        Supplier selectedSupplier = (Supplier) spSupplier.getSelectedItem();
        Integer supplierId = null;
        if (selectedSupplier != null) {
            supplierId = selectedSupplier.getId();
        }

        try {
            int productQuantity = Integer.parseInt(quantityText);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            String now = sdf.format(new Date());

            // Nếu sản phẩm đã có (selectedProduct != null), ta lấy ID cũ để cập nhật số lượng
            // Tuy nhiên API addProduct của bạn có thể đã xử lý việc cộng dồn, hoặc tạo mới
            // Ở đây ta cứ gửi Object lên, Server sẽ tự xử lý (thường là nhập mới thì tạo mới)

            Product newProduct = new Product(
                    code, name, productQuantity, location, unit, desc, now, now
            );

            // Nếu là hàng cũ, có thể bạn muốn cập nhật ngày tạo lấy từ hàng cũ,
            // nhưng nhập kho bản chất là thêm số lượng nên coi như tạo giao dịch mới.

            addProductToApi(newProduct, supplierId);

        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số lượng không hợp lệ");
        }
    }

    private void addProductToApi(Product newProduct, Integer supplierId) {
        apiService.addProduct(newProduct).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product savedProduct = response.body();

                    WarehouseTransaction transaction = new WarehouseTransaction(
                            savedProduct.getId(),
                            savedProduct.getProductQuantity(),
                            "Import",
                            "Nhập hàng từ App Mobile"
                    );
                    transaction.setSupplierId(supplierId);

                    apiService.addTransaction(transaction).enqueue(new Callback<Object>() {
                        @Override
                        public void onResponse(Call<Object> call, Response<Object> response) {
                            Log.d("HISTORY_LOG", "Đã lưu lịch sử nhập kho");
                        }
                        @Override
                        public void onFailure(Call<Object> call, Throwable t) {}
                    });

                    showAlert("Thành công", "Nhập kho thành công!", () -> {
                        clearForm();
                    });

                } else {
                    showAlert("Lỗi", "Không thêm được (Mã: " + response.code() + ")");
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

        // Reset biến chọn
        selectedProduct = null;
        if (spSupplier.getAdapter() != null && spSupplier.getAdapter().getCount() > 0) {
            spSupplier.setSelection(0);
        }
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
}