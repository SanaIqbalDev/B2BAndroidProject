package com.codeseven.pos;

import static com.codeseven.pos.util.Constants.HEADER_CACHE_CONTROL;
import static com.codeseven.pos.util.Constants.HEADER_PRAGMA;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.request.RequestHeaders;
import com.codeseven.pos.helper.TimeoutInstrumentation;
import com.codeseven.pos.util.LoginPreference;
import com.codeseven.pos.util.Utilities;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import dagger.Provides;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApolloClientClass {

    public ApolloClient apolloClient;
    private OkHttpClient okHttpClient;
    private LoginPreference loginPreference;

    public ApolloClientClass() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(provideCache())
                .addInterceptor(provideOfflineCacheInterceptor())
                .addNetworkInterceptor(provideCacheInterceptor())
                .connectTimeout(30, TimeUnit.SECONDS) // connect timeout
                .writeTimeout(30, TimeUnit.SECONDS) // write timeout
                .readTimeout(30, TimeUnit.SECONDS); // read timeout

        okHttpClient = builder.build();


        apolloClient= ApolloClient.builder().
                serverUrl("https://mcstaging.24seven.pk/graphql").
                okHttpClient(okHttpClient).build();
        loginPreference = new LoginPreference();
    }
    Interceptor provideOfflineCacheInterceptor() {
        return chain -> {
            Request request = chain.request();

            if (!Utilities.isNetworkConnected()) {

                CacheControl cacheControl = new CacheControl.Builder()
                        .maxStale(7, TimeUnit.DAYS)
                        .build();

                request = request.newBuilder()
                        .removeHeader(HEADER_PRAGMA)
                        .removeHeader(HEADER_CACHE_CONTROL)
                        .cacheControl(cacheControl)
                        .build();
            }

            return chain.proceed(request);
        };
    }

    Interceptor provideCacheInterceptor() {
        return chain -> {
            Response response = chain.proceed(chain.request());
            CacheControl cacheControl;
            if (Utilities.isNetworkConnected()) {
                cacheControl = new CacheControl.Builder().maxAge(0, TimeUnit.SECONDS).build();
            } else {
                cacheControl = new CacheControl.Builder()
                        .maxStale(7, TimeUnit.DAYS)
                        .build();
            }

            return response.newBuilder()
                    .removeHeader(HEADER_PRAGMA)
                    .removeHeader(HEADER_CACHE_CONTROL)
                    .header(HEADER_CACHE_CONTROL, cacheControl.toString())
                    .build();

        };
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

    public Cache provideCache() {
        Cache cache = null;
        try {
            cache = new Cache(new File(MainApplication.getContext().getCacheDir(), "http-cache"), 20 * 1024 * 1024); // 10 MB
        } catch (Exception e) {
            Log.e("Cache", "Could not create Cache!");
        }

        return cache;
    }
}
