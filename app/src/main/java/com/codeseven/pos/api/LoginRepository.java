package com.codeseven.pos.api;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.codeseven.pos.ApolloClientClass;
import com.codeseven.pos.util.LoginPreference;

import apollo.pos.GenerateCustomerTokenByPhoneMutation;

public class LoginRepository {

    private MutableLiveData<String> loginResponse;
    private LoginPreference loginPreference;

    public LoginRepository() {
        loginResponse = new MutableLiveData<>();
        loginPreference = new LoginPreference();
    }

    public void generateCustomerTokenByPhone(String phoneNumber, String password)
    {
        (new ApolloClientClass()).apolloClient.mutate(new GenerateCustomerTokenByPhoneMutation("waleed.umar@codeninja.pk",password, false)).enqueue(new ApolloCall.Callback<GenerateCustomerTokenByPhoneMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<GenerateCustomerTokenByPhoneMutation.Data> response) {
               if(response.getData().generateCustomerTokenByPhone()!= null)
               {
                   loginResponse.postValue("Generated Token:" + response.getData().generateCustomerTokenByPhone().token());
                   loginPreference.AddLoginPreferences("token",response.getData().generateCustomerTokenByPhone().token());
               }
               else
                   loginResponse.postValue(response.getErrors().get(0).getMessage());

            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                loginResponse.postValue(e.getMessage());
            }


        });
    }

    public MutableLiveData<String> getLoginResponse()
    {
        return loginResponse;
    }
}
