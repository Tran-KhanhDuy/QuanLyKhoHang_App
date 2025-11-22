package com.habibi.quanlykhohang;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
public class ProductInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_info);

        Product product = (Product) getIntent().getSerializableExtra("product");
        if(product != null) {
            ((TextView) findViewById(R.id.tvProductId)).setText("ID: " + product.getId());
            ((TextView) findViewById(R.id.tvProductName)).setText(product.getProductName());
            ((TextView) findViewById(R.id.tvBarcode)).setText("Mã: " + product.getProductCode());
            ((TextView) findViewById(R.id.tvQuantity)).setText("Số lượng: " + product.getQuantity());
            ((TextView) findViewById(R.id.tvLocation)).setText("Vị trí: " + product.getLocation());
            ((TextView) findViewById(R.id.tvUnit)).setText("Đơn vị: " + product.getProductUnit());
            ((TextView) findViewById(R.id.tvDescription)).setText("Mô tả: " + product.getProductDescription());
            ((TextView) findViewById(R.id.tvCreateDate)).setText("Ngày tạo: " + product.getCreateDate());
            ((TextView) findViewById(R.id.tvUpdateDate)).setText("Ngày cập nhật: " + product.getUpdateDate());

        }
        ImageButton btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> finish());
    }
}

