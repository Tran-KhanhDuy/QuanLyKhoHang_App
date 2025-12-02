package com.habibi.quanlykhohang;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ImportActivity extends AppCompatActivity {
    private ProductApiService apiService;
    private AutoCompleteTextView etProductUnit;
    private EditText etProductCode, etProductName, etQuantity, etLocation, etProductDescription;
    private Button btnScan, btnSave;
    private TextView tvStatus;

    // --- [1] THÊM BIẾN CHO VOICE & SPINNER ---
    private ImageButton btnVoiceName;
    private Spinner spSupplier;
    private ArrayAdapter<Supplier> supplierAdapter;
    private List<Supplier> listSupplier = new ArrayList<>();

    // --- [2] LAUNCHER XỬ LÝ GIỌNG NÓI ---
    private final ActivityResultLauncher<Intent> voiceLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> resultVoice = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (resultVoice != null && !resultVoice.isEmpty()) {
                        String text = resultVoice.get(0);
                        etProductName.setText(text); // Điền chữ vào ô Tên
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        apiService = RetrofitClient.getService(this);

        // Ánh xạ View
        etProductCode = findViewById(R.id.etBarcode);
        etProductName = findViewById(R.id.etName);
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

        // --- [3] CẤU HÌNH NÚT VOICE ---
        btnVoiceName = findViewById(R.id.btnVoiceName);
        btnVoiceName.setOnClickListener(v -> startVoiceInput());

        // --- [4] CẤU HÌNH SPINNER ---
        spSupplier = findViewById(R.id.spSupplier);
        supplierAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listSupplier);
        supplierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSupplier.setAdapter(supplierAdapter);

        // Tải danh sách nhà cung cấp từ API
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

    // Hàm gọi API lấy danh sách Nhà cung cấp
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
            tvStatus.setText("📝 Đã quét mã: " + productCode);
            etProductName.requestFocus();
        }
    }

    // Hàm gọi trình nhập giọng nói Google
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

        // --- [5] LẤY ID NHÀ CUNG CẤP TỪ SPINNER ---
        Supplier selectedSupplier = (Supplier) spSupplier.getSelectedItem();
        Integer supplierId = null;
        if (selectedSupplier != null) {
            supplierId = selectedSupplier.getId();
        }
        // -------------------------------------------

        try {
            int productQuantity = Integer.parseInt(quantityText);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            String now = sdf.format(new Date());

            Product newProduct = new Product(
                    code, name, productQuantity, location, unit, desc, now, now
            );

            // Truyền supplierId vào hàm xử lý
            addProductToApi(newProduct, supplierId);

        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số lượng không hợp lệ");
        }
    }

    // Đã thêm tham số supplierId
    private void addProductToApi(Product newProduct, Integer supplierId) {
        apiService.addProduct(newProduct).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product savedProduct = response.body();

                    // --- [6] GHI LỊCH SỬ KÈM NHÀ CUNG CẤP ---
                    WarehouseTransaction transaction = new WarehouseTransaction(
                            savedProduct.getId(),
                            savedProduct.getProductQuantity(),
                            "Import",
                            "Nhập hàng từ App Mobile"
                    );

                    // Gán ID nhà cung cấp vào phiếu lịch sử
                    transaction.setSupplierId(supplierId);

                    apiService.addTransaction(transaction).enqueue(new Callback<Object>() {
                        @Override
                        public void onResponse(Call<Object> call, Response<Object> response) {
                            Log.d("HISTORY_LOG", "Đã lưu lịch sử nhập kho");
                        }
                        @Override
                        public void onFailure(Call<Object> call, Throwable t) {
                            Log.e("HISTORY_LOG", "Lỗi lưu lịch sử: " + t.getMessage());
                        }
                    });
                    // ------------------------------------

                    showAlert("Thành công", "Thêm sản phẩm và lưu lịch sử thành công!", () -> {
                        clearForm();
                    });

                } else {
                    String errorMsg = "Lỗi thêm sản phẩm (Mã: " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += "\nChi tiết: " + response.errorBody().string();
                        } catch (IOException e) {}
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

    private void clearForm() {
        etProductCode.setText("");
        etProductName.setText("");
        etQuantity.setText("");
        etLocation.setText("");
        etProductUnit.setText("");
        etProductDescription.setText("");
        tvStatus.setText("");

        // Reset Spinner về vị trí đầu tiên
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