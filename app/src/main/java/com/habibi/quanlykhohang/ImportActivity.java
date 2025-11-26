package com.habibi.quanlykhohang;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageButton;
import java.io.IOException;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ImportActivity extends AppCompatActivity {
    private Retrofit retrofit;
    private ProductApiService apiService;

    private EditText etProductCode, etProductName, etQuantity, etLocation, etProductUnit, etProductDescription;
    private Button btnScan, btnSave;
    private TextView tvStatus, tvCreateDate, tvUpdateDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        retrofit = new Retrofit.Builder()
                .baseUrl("https://gelatinously-commutative-jerrie.ngrok-free.dev/api/") // Kết thúc bằng /
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ProductApiService.class);

        etProductCode = findViewById(R.id.etBarcode);
        etProductName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etLocation = findViewById(R.id.etLocation);
        etProductUnit = findViewById(R.id.etProductUnit);
        etProductDescription = findViewById(R.id.etProductDescription);
        btnScan = findViewById(R.id.btnScan);
        btnSave = findViewById(R.id.btnSave);
        tvStatus = findViewById(R.id.tvStatus);
        TextView tvCreateDate = findViewById(R.id.tvCreateDate);
        TextView tvUpdateDate = findViewById(R.id.tvUpdateDate);



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


        try {
            int productQuantity = Integer.parseInt(quantityText);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            String now = sdf.format(new Date());


            Product newProduct = new Product(
                    code, name, productQuantity, location, unit, desc, now, now
            );
            Log.d("DEBUG_NEWPRODUCT", new Gson().toJson(newProduct));
            addProductToApi(newProduct);

        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số lượng không hợp lệ");
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
                    String errorMsg = "Không thể thêm sản phẩm qua API (mã lỗi " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String detailedError = response.errorBody().string();
                            errorMsg += "\nChi tiết: " + detailedError;
                            Log.e("API_ERROR", detailedError);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    showAlert("Lỗi", errorMsg);
                }
            }
            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                showAlert("Lỗi mạng/API", t.getMessage());
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
}
