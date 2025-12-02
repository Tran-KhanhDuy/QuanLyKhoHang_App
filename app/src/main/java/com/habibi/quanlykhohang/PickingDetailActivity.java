package com.habibi.quanlykhohang;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
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
    private boolean isOrderCompleted = false;
    // Biến lưu vị trí món hàng đang được chọn để kiểm tra
    private int currentCheckingPosition = -1;

    // Launcher cho việc quét mã
    private final ActivityResultLauncher<Intent> barcodeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                IntentResult intentResult = IntentIntegrator.parseActivityResult(result.getResultCode(), result.getData());
                if (intentResult != null && intentResult.getContents() != null) {
                    String scannedCode = intentResult.getContents();
                    // Kiểm tra mã vừa quét
                    checkBarcode(scannedCode);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picking_detail);

        apiService = RetrofitClient.getService(this);

        Intent intent = getIntent();
        orderId = intent.getIntExtra("orderId", 0);
        String orderCode = intent.getStringExtra("orderCode");

        tvOrderCode = findViewById(R.id.tvOrderCode);
        lvDetails = findViewById(R.id.lvDetails);
        btnComplete = findViewById(R.id.btnComplete);
        ImageButton btnReturn = findViewById(R.id.btnReturn);

        tvOrderCode.setText("Đơn: " + orderCode);

        setupAdapter();
        loadOrderDetails();

        btnReturn.setOnClickListener(v -> finish());

        // Sự kiện bấm vào một dòng sản phẩm -> Hiện bảng kiểm tra
        lvDetails.setOnItemClickListener((parent, view, position, id) -> {
            ExportOrderDetail item = detailList.get(position);
            if (!item.isPicked()) {
                currentCheckingPosition = position; // Lưu lại vị trí đang chọn
                showVerifyDialog(item);
            } else {
                Toast.makeText(this, "Món này đã lấy xong rồi!", Toast.LENGTH_SHORT).show();
            }
        });

        btnComplete.setOnClickListener(v -> confirmCompletion());
    }

    // Hàm hiển thị hộp thoại chọn cách kiểm tra (Scan hoặc Nhập tay)
    private void showVerifyDialog(ExportOrderDetail item) {
        String productName = item.getProduct().getProductName();
        String targetCode = item.getProduct().getProductCode();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận lấy hàng: " + productName);
        builder.setMessage("Yêu cầu mã: " + targetCode + "\n\nBạn muốn làm gì?");

        // Nút Quét Mã
        builder.setPositiveButton("📷 Quét Mã Vạch", (dialog, which) -> {
            scanBarcode();
        });

        // Nút Nhập Tay (Xử lý trường hợp mã bị mờ/hỏng)
        builder.setNeutralButton("⌨️ Nhập Tay", (dialog, which) -> {
            showManualInputDialog();
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    // Hộp thoại nhập mã bằng tay
    private void showManualInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập mã sản phẩm thủ công");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Nhập mã vạch...");
        builder.setView(input);

        builder.setPositiveButton("Kiểm tra", (dialog, which) -> {
            String manualCode = input.getText().toString().trim();
            checkBarcode(manualCode);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    // Hàm xử lý logic kiểm tra mã (Quan trọng nhất)
    private void checkBarcode(String inputCode) {
        if (currentCheckingPosition == -1) return;

        ExportOrderDetail currentItem = detailList.get(currentCheckingPosition);
        String realCode = currentItem.getProduct().getProductCode();

        // So sánh mã nhập vào với mã thật của sản phẩm
        if (inputCode.equalsIgnoreCase(realCode)) {
            // ĐÚNG HÀNG -> Đánh dấu đã lấy
            currentItem.setPicked(true);
            adapter.notifyDataSetChanged(); // Cập nhật giao diện (đổi màu xanh)
            Toast.makeText(this, "✅ Chính xác! Đã lấy xong món này.", Toast.LENGTH_SHORT).show();
        } else {
            // SAI HÀNG -> Báo lỗi
            showErrorAlert("Sai hàng!", "Mã bạn vừa nhập là: " + inputCode + "\nNhưng sản phẩm cần lấy là: " + realCode);
        }
    }

    private void scanBarcode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Quét mã sản phẩm để xác nhận");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        barcodeLauncher.launch(integrator.createScanIntent());
    }

    // Cập nhật Adapter để hiển thị màu xanh khi đã lấy xong
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

                    // Đổi màu nền nếu đã lấy xong
                    View container = convertView.findViewById(R.id.layoutContainer); // Cần đặt ID cho LinearLayout trong XML
                    if (item.isPicked()) {
                        container.setBackgroundColor(Color.parseColor("#E8F5E9")); // Màu xanh nhạt
                        tvName.setText("✅ " + item.getProduct().getProductName()); // Thêm dấu tích
                        tvName.setTextColor(Color.parseColor("#2E7D32"));
                    } else {
                        container.setBackgroundColor(Color.WHITE);
                        tvName.setText(item.getProduct().getProductName());
                        tvName.setTextColor(Color.BLACK);
                    }

                    tvLoc.setText("Vị trí: " + item.getProduct().getLocation());
                    tvQty.setText("SL: " + item.getQuantity());
                }
                return convertView;
            }
        };
        lvDetails.setAdapter(adapter);
    }

    private void confirmCompletion() {
        // Kiểm tra xem đã lấy hết chưa
        boolean allPicked = true;
        for (ExportOrderDetail item : detailList) {
            if (!item.isPicked()) {
                allPicked = false;
                break;
            }
        }

        if (!allPicked) {
            showErrorAlert("Chưa xong!", "Vẫn còn sản phẩm chưa được lấy (chưa có dấu tích xanh). Vui lòng kiểm tra lại.");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn đã lấy đủ và kiểm tra kỹ tất cả sản phẩm?")
                .setPositiveButton("Hoàn tất đơn hàng", (dialog, which) -> completeOrderApi())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showErrorAlert(String title, String msg) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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

    private void completeOrderApi() {
        apiService.completeOrder(orderId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isOrderCompleted = true;
                    Toast.makeText(PickingDetailActivity.this, "Đã hoàn thành đơn hàng!", Toast.LENGTH_LONG).show();
                    finish();
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
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Nếu thoát màn hình mà CHƯA hoàn thành đơn -> Gọi API nhả đơn
        if (!isOrderCompleted) {
            releaseOrder();
        }
    }

    // Hàm gọi API nhả đơn
    private void releaseOrder() {
        // Lưu ý: Gọi API trong onDestroy nên dùng enqueue (bất đồng bộ) để không chặn UI
        // Nhưng vì Activity đang đóng, ta không cần cập nhật UI hay Toast gì cả.
        // Chỉ cần gửi lệnh đi là được.
        apiService.cancelPicking(orderId).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                // Gửi thành công, Server tự reset trạng thái
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                // Lỗi mạng thì chịu, chấp nhận rủi ro treo đơn 1 lúc
            }
        });
    }
}