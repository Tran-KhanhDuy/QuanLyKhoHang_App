package com.habibi.quanlykhohang;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import java.util.List;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public interface ProductApiService {
    @POST("productsapi")
    Call<Product> addProduct(@Body Product product);

    @GET("productsapi")
    Call<List<Product>> getAllProducts();

    @PUT("productsapi/{id}")
    Call<Product> updateProduct(@Path("id") int id, @Body Product product);

    @DELETE("productsapi/{id}")
    Call<Void> deleteProduct(@Path("id") int id);
}
