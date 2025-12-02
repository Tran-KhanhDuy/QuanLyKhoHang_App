package com.habibi.quanlykhohang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PickingListActivity extends AppCompatActivity {

    private ListView lvOrders;
    private ProductApiService apiService;
    private List<ExportOrder> orderList = new ArrayList<>();
    private ArrayAdapter<ExportOrder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picking_list);

        // Sử dụng RetrofitClient để có Token
        apiService = RetrofitClient.getService(this);

        lvOrders = findViewById(R.id.lvOrders);
        ImageButton btnReturn = findViewById(R.id.btnReturn);
        ImageButton btnRefresh = findViewById(R.id.btnRefresh);

        // Cấu hình ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, orderList);
        lvOrders.setAdapter(adapter);

        // Tải dữ liệu ban đầu
        loadPendingOrders();

        // --- SỬA ĐOẠN NÀY: XỬ LÝ KHI BẤM VÀO ĐƠN ---
        lvOrders.setOnItemClickListener((parent, view, position, id) -> {
            ExportOrder selected = orderList.get(position);
            // Gọi hàm xác nhận nhận đơn trước khi chuyển màn hình
            confirmStartPicking(selected);
        });
        // -------------------------------------------

        btnReturn.setOnClickListener(v -> finish());
        btnRefresh.setOnClickListener(v -> loadPendingOrders());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingOrders(); // Tải lại khi quay lại màn hình này
    }

    // Hàm gọi API nhận đơn (Start Picking)
    private void confirmStartPicking(ExportOrder order) {
        apiService.startPicking(order.getId()).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    // 1. Thành công (Server đã khóa đơn cho bạn)
                    // -> Chuyển sang màn hình chi tiết
                    Intent intent = new Intent(PickingListActivity.this, PickingDetailActivity.class);
                    intent.putExtra("orderId", order.getId());
                    intent.putExtra("orderCode", order.getOrderCode());
                    startActivity(intent);
                } else {
                    // 2. Thất bại (Có thể người khác đã nhận mất rồi)
                    try {
                        String msg = "Lỗi";
                        if (response.errorBody() != null) {
                            msg = response.errorBody().string();
                        }
                        Toast.makeText(PickingListActivity.this, "Không thể nhận đơn: " + msg, Toast.LENGTH_LONG).show();

                        // Tải lại danh sách để cập nhật tình trạng mới nhất
                        loadPendingOrders();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(PickingListActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPendingOrders() {
        apiService.getPendingOrders().enqueue(new Callback<List<ExportOrder>>() {
            @Override
            public void onResponse(Call<List<ExportOrder>> call, Response<List<ExportOrder>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    orderList.clear();
                    orderList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if(orderList.isEmpty()){
                        Toast.makeText(PickingListActivity.this, "Không có đơn hàng nào cần soạn", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PickingListActivity.this, "Lỗi tải danh sách: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ExportOrder>> call, Throwable t) {
                Toast.makeText(PickingListActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}