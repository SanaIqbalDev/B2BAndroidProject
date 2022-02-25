package com.codeseven.pos;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory;
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy;
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory;
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory;
import com.apollographql.apollo.request.RequestHeaders;
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
    private OkHttpClient okHttpClient;
    private LoginPreference loginPreference;

    public ApolloClientClass() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // connect timeout
                .writeTimeout(30, TimeUnit.SECONDS) // write timeout
                .readTimeout(30, TimeUnit.SECONDS);



//                .addInterceptor(provideOfflineCacheInterceptor())
//                .addNetworkInterceptor(provideCacheInterceptor()); // read timeout


        okHttpClient = builder.build();
//        okHttpClient.interceptors().add(provideOfflineCacheInterceptor());
//        okHttpClient.networkInterceptors().add(provideCacheInterceptor());

        NormalizedCacheFactory cacheFactory = new LruNormalizedCacheFactory(EvictionPolicy.builder().maxSizeBytes(10 * 1024 * 1024).build());


//        SqlNormalizedCacheFactory sqlNormalizedCacheFactory = new SqlNormalizedCacheFactory(MainApplication.getContext(), "apollo.db");

        NormalizedCacheFactory sqlCacheFactory = new SqlNormalizedCacheFactory(MainApplication.getContext(), "24_seven");
//        NormalizedCacheFactory memoryFirstThenSqlCacheFactory = new LruNormalizedCacheFactory(
//                EvictionPolicy.builder().maxSizeBytes(10 * 1024 * 1024).build()
//        ).chain(sqlCacheFactory);

        apolloClient= ApolloClient.builder().
                serverUrl("https://mcstaging.24seven.pk/graphql").
                okHttpClient(okHttpClient).
                normalizedCache(sqlCacheFactory).
                build();


//        NormalizedCache.configureApolloClientBuilder(
//                apolloClient,
//                new MemoryCacheFactory(10 * 1024 * 1024, -1),
//                TypePolicyCacheKeyGenerator.INSTANCE,
//                FieldPolicyCacheResolver.INSTANCE,
//                false
//        );

        loginPreference = new LoginPreference();
    }
    Interceptor provideOfflineCacheInterceptor() {
        return chain -> {
                Request originalRequest = chain.request();
                String cacheHeaderValue = Utilities.isNetworkConnected()
                        ? "public, max-age=2419200"
                        : "public, only-if-cached, max-stale=2419200" ;
                Request request = originalRequest.newBuilder().build();
                Response response = chain.proceed(request);
                return response.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", cacheHeaderValue)
                        .build();
        };
    }
//
    Interceptor provideCacheInterceptor() {
        return chain -> {
            Request originalRequest = chain.request();
            String cacheHeaderValue = Utilities.isNetworkConnected()
                    ? "public, max-age=2419200"
                    : "public, only-if-cached, max-stale=2419200" ;
            Request request = originalRequest.newBuilder().build();
            Response response = chain.proceed(request);
            return response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", cacheHeaderValue)
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

//    public Cache provideCache() {
//        Cache cache = null;
//        try {
//            cache = new Cache(new File(MainApplication.getContext().getCacheDir(), "http-cache"), 20 * 1024 * 1024); // 10 MB
//        } catch (Exception e) {
//            Log.e("Cache", "Could not create Cache!");
//        }
//
//        return cache;
//    }
}
