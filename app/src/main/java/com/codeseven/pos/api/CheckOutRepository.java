package com.codeseven.pos.api;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.request.RequestHeaders;
import com.codeseven.pos.ApolloClientClass;
import com.codeseven.pos.util.LoginPreference;

import apollo.pos.GetCustomerDetailsQuery;

public class CheckOutRepository {

    private LoginPreference loginPreference;
    private MutableLiveData<String> CustomerInfoResponse;
    private MutableLiveData<GetCustomerDetailsQuery.Customer> responseData;

    public CheckOutRepository() {
        this.loginPreference = new LoginPreference();
        CustomerInfoResponse = new MutableLiveData<>("");
        responseData = new MutableLiveData<>();
    }

    public void  getCustomerInfo()
    {
        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));

        (new ApolloClientClass()).apolloClient.query(new GetCustomerDetailsQuery()).toBuilder().requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<GetCustomerDetailsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetCustomerDetailsQuery.Data> response) {
                responseData.postValue(response.getData().customer());
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                CustomerInfoResponse.postValue(e.getLocalizedMessage());
            }
        });
    }

    public MutableLiveData<String> getCustomerInfoResponse(){
        return CustomerInfoResponse;
    }
    public MutableLiveData<GetCustomerDetailsQuery.Customer> getResponseData(){return responseData; }
}
