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
    @POST("productsapi")
    Call<Product> addProduct(@Body Product product);

    @GET("productsapi")
    Call<List<Product>> getAllProducts();

    @GET("productsapi/{barcode}")
    Call<Product> getProductByBarcode(@Path("barcode") String barcode);

    @PUT("productsapi/{id}")
    Call<Product> updateProduct(@Body Product product);

    @DELETE("productsapi/{id}")
    Call<Void> deleteProduct(@Path("id") int id);

    // THÊM API SEARCH THEO TÊN:
    @GET("productsapi/search")
    Call<List<Product>> searchProductsByName(@Query("name") String name);
}

