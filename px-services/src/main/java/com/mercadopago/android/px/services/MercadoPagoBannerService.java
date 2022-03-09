package com.mercadopago.android.px.services;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mercadopago.android.px.internal.services.BannerAPIService;
import com.mercadopago.android.px.internal.util.RetrofitUtil;
import com.mercadopago.android.px.model.exceptions.ApiException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MercadoPagoBannerService {
    final Context context;
    private final Retrofit retrofitClient;

    public MercadoPagoBannerService(Context context) {
        this.context = context;
        this.retrofitClient = RetrofitUtil.getRetrofitClient(context);
    }

    public void sendClick(@NonNull String url){
        final BannerAPIService preferenceService = retrofitClient.create(BannerAPIService.class);
        preferenceService.sendClicksAPI(url).enqueue(new retrofit2.Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                System.out.println(response);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                System.out.println(t.getMessage());
            }
        });
    }

    public void sendPrint(@NonNull String url){
        final BannerAPIService preferenceService = retrofitClient.create(BannerAPIService.class);
        preferenceService.sendPrintsAPI(url).enqueue(new retrofit2.Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(!response.isSuccessful()){
                    System.out.println("error msj");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                System.out.println(t.getMessage());
            }
        });
    }

}
