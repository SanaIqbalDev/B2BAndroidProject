package com.codeseven.pos;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.request.RequestHeaders;
import com.codeseven.pos.helper.TimeoutInstrumentation;
import com.codeseven.pos.util.LoginPreference;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import okhttp3.OkHttpClient;

public class ApolloClientClass {

    public ApolloClient apolloClient;
    private OkHttpClient okHttpClient;
    private LoginPreference loginPreference;

    public ApolloClientClass() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS) // connect timeout
                .writeTimeout(30, TimeUnit.SECONDS) // write timeout
                .readTimeout(30, TimeUnit.SECONDS); // read timeout

        okHttpClient = builder.build();


        apolloClient= ApolloClient.builder().
                serverUrl("https://mcstaging.24seven.pk/graphql").
                okHttpClient(okHttpClient).build();
        loginPreference = new LoginPreference();
    }


//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public ApolloClientClass() {
//        okHttpClient = new OkHttpClient().newBuilder().callTimeout(Duration.ofSeconds(20)).build();
//        apolloClient= ApolloClient.builder().
//                serverUrl("https://mcstaging.24seven.pk/graphql").
//                okHttpClient(okHttpClient).build();
//        loginPreference = new LoginPreference();
//    }

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
}
