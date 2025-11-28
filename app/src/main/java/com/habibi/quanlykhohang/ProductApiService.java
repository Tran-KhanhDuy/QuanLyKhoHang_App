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
    @POST("api/productsapi")
    Call<Product> addProduct(@Body Product product);

    @GET("api/productsapi")
    Call<List<Product>> getAllProducts();

    @GET("api/productsapi/{barcode}")
    Call<Product> getProductByBarcode(@Path("barcode") String barcode);

    @PUT("api/productsapi/{id}")
    Call<Product> updateProduct(@Path("id") int id, @Body Product product);

    @DELETE("api/productsapi/{id}")
    Call<Void> deleteProduct(@Path("id") int id);

    // API SEARCH THEO TÊN (SỬA LẠI ĐÚNG ENDPOINT)
    @GET("api/productsapi")
    Call<List<Product>> searchProductsByName(@Query("name") String name);
}

