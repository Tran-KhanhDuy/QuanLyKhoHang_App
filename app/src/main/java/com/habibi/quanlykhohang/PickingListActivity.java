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

        apiService = RetrofitClient.getService(this);

        lvOrders = findViewById(R.id.lvOrders);
        ImageButton btnReturn = findViewById(R.id.btnReturn);
        ImageButton btnRefresh = findViewById(R.id.btnRefresh);

        // Cấu hình ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, orderList);
        lvOrders.setAdapter(adapter);

        // Tải dữ liệu
        loadPendingOrders();

        // Sự kiện bấm vào một đơn -> Sang màn hình chi tiết (sẽ làm sau)
        lvOrders.setOnItemClickListener((parent, view, position, id) -> {
            ExportOrder selected = orderList.get(position);

            // Chuyển sang màn hình chi tiết (PickingDetailActivity - Tạo ở bước sau)
            Intent intent = new Intent(PickingListActivity.this, PickingDetailActivity.class);
            intent.putExtra("orderId", selected.getId());
            intent.putExtra("orderCode", selected.getOrderCode());
            startActivity(intent);
        });

        btnReturn.setOnClickListener(v -> finish());
        btnRefresh.setOnClickListener(v -> loadPendingOrders());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingOrders(); // Tải lại khi quay lại màn hình này
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