package com.codeseven.pos;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.request.RequestHeaders;
import com.codeseven.pos.util.LoginPreference;

import okhttp3.OkHttpClient;

public class ApolloClientClass {

    public ApolloClient apolloClient;
    private OkHttpClient okHttpClient;
    private LoginPreference loginPreference;




    public ApolloClientClass() {
        okHttpClient = new OkHttpClient().newBuilder().build();
        apolloClient= ApolloClient.builder().
                serverUrl("https://mcstaging.24seven.pk/graphql").
                okHttpClient(okHttpClient).build();

        loginPreference = new LoginPreference();
    }

    public  ApolloClient getNewClient(String token){
       return ApolloClient.builder()
                .serverUrl("https://mcstaging.24seven.pk/graphql")
                .okHttpClient(new OkHttpClient.Builder()
                        .addInterceptor(chain -> chain.proceed(chain.request().newBuilder().addHeader("authorization", "bearer "+token).build()))
                        .build())
                .build();
    }
    public RequestHeaders getRequestHeader()
    {
        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));

        return requestHeader.build();
    }
}
