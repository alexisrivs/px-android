package com.mercadopago.android.px.internal.services;

import com.mercadopago.android.px.internal.callbacks.MPCall;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface BannerAPIService {

    @Headers({"user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36", "Content-Type: application/json"})
    @GET("https://click1.mercadolibre.com.ar/display/clicks/MLA/count")
    Call<Void> sendClicksAPI(@Query("a") String data);

    @Headers({"user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36", "Content-Type: application/json"})
    @GET("https://print1.mercadoclics.com/display/prints/MLA/count")
    Call<Void> sendPrintsAPI(@Query("d") String data);

}
