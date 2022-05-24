package com.codeseven.pos;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory;
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy;
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory;
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory;
import com.apollographql.apollo.internal.batch.BatchConfig;
import com.apollographql.apollo.request.RequestHeaders;
import com.codeseven.pos.util.CartPreference;
import com.codeseven.pos.util.LoginPreference;
import com.codeseven.pos.util.Utilities;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApolloClientClass {

    public ApolloClient apolloClient;
    private final OkHttpClient okHttpClient;
    private final LoginPreference loginPreference;
    private CartPreference cartPref;

    public ApolloClientClass() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS) // connect timeout
                .writeTimeout(30, TimeUnit.SECONDS) // write timeout
                .readTimeout(30, TimeUnit.SECONDS);
        okHttpClient = builder.build();
        apolloClient= ApolloClient.builder().
                serverUrl("https://mcstaging.24seven.pk/graphql").
                okHttpClient(okHttpClient).
                build();

        loginPreference = new LoginPreference();
        cartPref = new CartPreference();
    }
    public ApolloClientClass(boolean isCache) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // connect timeout
                .writeTimeout(30, TimeUnit.SECONDS) // write timeout
                .readTimeout(30, TimeUnit.SECONDS);
        okHttpClient = builder.build();

        NormalizedCacheFactory sqlCacheFactory = new SqlNormalizedCacheFactory(MainApplication.getContext(), "24_seven");

        BatchConfig batchConfig = new BatchConfig();
        batchConfig.copy(true,50,10);

        apolloClient= ApolloClient.builder().
                serverUrl("https://mcstaging.24seven.pk/graphql").
                okHttpClient(okHttpClient).
                normalizedCache(sqlCacheFactory).
                batchingConfiguration(batchConfig).
                build();

        loginPreference = new LoginPreference();
        cartPref = new CartPreference();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public  ApolloClient getNewClient(String token){
       return ApolloClient.builder()
                .serverUrl("https://mcstaging.24seven.pk/graphql")
                .okHttpClient(new OkHttpClient.Builder().readTimeout(Duration.ofSeconds(new Long(20000)))
                        .addInterceptor(chain -> chain.proceed(chain.request().newBuilder().addHeader("authorization", "bearer "+token).build()))
                        .build())
                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public  ApolloClient getNewClientAB(){
        return ApolloClient.builder()
                .serverUrl("https://mcstaging.24seven.pk/graphql")
                .okHttpClient(new OkHttpClient.Builder().readTimeout(Duration.ofSeconds(new Long(20000))).build())
                .build();
    }



    public RequestHeaders getRequestHeader()
    {
        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));

        return requestHeader.build();
    }

    public String getCartId(){
        return (cartPref.GetCartId("cart_id"));
    }
}
