package com.habibi.quanlykhohang;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import java.util.List;
import retrofit2.http.Query;

public interface ProductApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    @POST("api/productsapi")
    Call<Product> addProduct(@Body Product product);

    @GET("api/productsapi")
    Call<List<Product>> getAllProducts();

    // Ví dụ 1: /api/productsapi/GetByBarcode?barcode=xxx
    @GET("api/ProductsApi/barcode/{barcode}")
    Call<Product> getProductByBarcode(@Path("barcode") String barcode);

    @PUT("api/productsapi/{id}")
    Call<Product> updateProduct(@Path("id") int id, @Body Product product);

    @DELETE("api/productsapi/{id}")
    Call<Void> deleteProduct(@Path("id") int id);

    // API SEARCH THEO TÊN (SỬA LẠI ĐÚNG ENDPOINT)
    @GET("api/ProductsApi/search")
    Call<List<Product>> searchProductsByName(@Query("name") String name);

    @POST("api/WarehouseTransactionApi")
    Call<Object> addTransaction(@Body WarehouseTransaction transaction);
    @GET("api/SupplierApi")
    Call<List<Supplier>> getSuppliers();
    @GET("api/OrderPicking/pending")
    Call<List<ExportOrder>> getPendingOrders();

    // Lấy chi tiết đơn
    @GET("api/OrderPicking/detail/{id}")
    Call<ExportOrder> getOrderDetail(@Path("id") int id);

    // Xác nhận xong
    @POST("api/OrderPicking/complete/{id}")
    Call<Void> completeOrder(@Path("id") int id);
    @POST("api/OrderPicking/start/{id}")
    Call<Object> startPicking(@Path("id") int id);
    @POST("api/OrderPicking/cancel/{id}")
    Call<Object> cancelPicking(@retrofit2.http.Path("id") int id);
}

