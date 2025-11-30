package com.habibi.quanlykhohang;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PickingDetailActivity extends AppCompatActivity {

    private TextView tvOrderCode;
    private ListView lvDetails;
    private Button btnComplete;
    private ProductApiService apiService;

    private int orderId;
    private List<ExportOrderDetail> detailList = new ArrayList<>();
    private ArrayAdapter<ExportOrderDetail> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picking_detail);

        apiService = RetrofitClient.getService(this);

        // Nhận dữ liệu từ màn hình trước
        Intent intent = getIntent();
        orderId = intent.getIntExtra("orderId", 0);
        String orderCode = intent.getStringExtra("orderCode");

        // Ánh xạ
        tvOrderCode = findViewById(R.id.tvOrderCode);
        lvDetails = findViewById(R.id.lvDetails);
        btnComplete = findViewById(R.id.btnComplete);
        ImageButton btnReturn = findViewById(R.id.btnReturn);

        tvOrderCode.setText("Đơn: " + orderCode);

        // Cấu hình Adapter tùy chỉnh để hiển thị đẹp
        setupAdapter();

        // Tải dữ liệu
        loadOrderDetails();

        // Sự kiện nút
        btnReturn.setOnClickListener(v -> finish());

        btnComplete.setOnClickListener(v -> confirmCompletion());
    }

    private void setupAdapter() {
        adapter = new ArrayAdapter<ExportOrderDetail>(this, 0, detailList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_picking_detail, parent, false);
                }

                ExportOrderDetail item = getItem(position);
                if (item != null && item.getProduct() != null) {
                    TextView tvName = convertView.findViewById(R.id.tvProductName);
                    TextView tvLoc = convertView.findViewById(R.id.tvLocation);
                    TextView tvQty = convertView.findViewById(R.id.tvQuantity);

                    tvName.setText(item.getProduct().getProductName());
                    tvLoc.setText("Vị trí: " + item.getProduct().getLocation());
                    tvQty.setText("x" + item.getQuantity());
                }
                return convertView;
            }
        };
        lvDetails.setAdapter(adapter);
    }

    private void loadOrderDetails() {
        apiService.getOrderDetail(orderId).enqueue(new Callback<ExportOrder>() {
            @Override
            public void onResponse(Call<ExportOrder> call, Response<ExportOrder> response) {
                if (response.isSuccessful() && response.body() != null) {
                    detailList.clear();
                    if (response.body().getDetails() != null) {
                        detailList.addAll(response.body().getDetails());
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(PickingDetailActivity.this, "Lỗi tải chi tiết", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ExportOrder> call, Throwable t) {
                Toast.makeText(PickingDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmCompletion() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn đã lấy đủ hàng cho đơn này chưa?")
                .setPositiveButton("Đã xong", (dialog, which) -> {
                    // Gọi API hoàn thành
                    completeOrderApi();
                })
                .setNegativeButton("Chưa", null)
                .show();
    }

    private void completeOrderApi() {
        // Sửa <Object> thành <Void>
        apiService.completeOrder(orderId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PickingDetailActivity.this, "Đã hoàn thành đơn hàng!", Toast.LENGTH_LONG).show();
                    finish(); // Quay về danh sách
                } else {
                    Toast.makeText(PickingDetailActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(PickingDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}