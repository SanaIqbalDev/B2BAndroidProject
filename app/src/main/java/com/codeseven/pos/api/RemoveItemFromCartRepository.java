package com.codeseven.pos.api;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.codeseven.pos.ApolloClientClass;
import com.codeseven.pos.util.CartPreference;
import com.codeseven.pos.util.LoginPreference;

import apollo.pos.RemoveItemFromCartMutation;
import apollo.pos.type.RemoveItemFromCartInput;

public class RemoveItemFromCartRepository {

    private MutableLiveData<String> requestResponse;
    private LoginPreference loginPreference;
    private CartPreference cartPreference;
    public RemoveItemFromCartRepository() {
        requestResponse = new MutableLiveData<>();
        loginPreference = new LoginPreference();
        cartPreference = new CartPreference();
    }

    public void removeItemFromCart(String item_uid){
        cartPreference.GetCartId("cart_id");
        RemoveItemFromCartInput removeItemFromCartInput = RemoveItemFromCartInput.builder().cart_id( cartPreference.GetCartId("cart_id")).cart_item_uid(item_uid).build();
        (new ApolloClientClass()).apolloClient.mutate((new RemoveItemFromCartMutation(removeItemFromCartInput))).enqueue(new ApolloCall.Callback<RemoveItemFromCartMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<RemoveItemFromCartMutation.Data> response) {

            }

            @Override
            public void onFailure(@NonNull ApolloException e) {

            }
        });
    }
}
