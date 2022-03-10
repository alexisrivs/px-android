package com.mercadopago.android.px.internal.services;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Url;

public interface BannerAPIService {

    @GET
    Call<Void> sendClicksAPI(@Url String url);

    @GET
    Call<Void> sendPrintsAPI(@Url String url);
}
