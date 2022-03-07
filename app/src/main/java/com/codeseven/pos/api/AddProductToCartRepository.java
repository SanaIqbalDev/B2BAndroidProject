package com.codeseven.pos.api;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.codeseven.pos.ApolloClientClass;
import com.codeseven.pos.MainApplication;
import com.codeseven.pos.R;
import com.codeseven.pos.util.CartPreference;
import com.codeseven.pos.util.LoginPreference;

import java.util.ArrayList;
import java.util.List;

import apollo.pos.AddProductsToCartMutation;
import apollo.pos.type.CartItemInput;

public class AddProductToCartRepository {


    private MutableLiveData<String> requestResponse;
    private CartPreference cartPreference;
    private LoginPreference loginPreference;
    private ApolloClientClass apolloClientClass;


    public AddProductToCartRepository() {
        requestResponse = new MutableLiveData<>("");
        cartPreference = new CartPreference();
        loginPreference = new LoginPreference();
        apolloClientClass = new ApolloClientClass();
    }

    public void addProductToCart(String sku, String quantity)
    {
        String cart_id = apolloClientClass.getCartId();
        List<CartItemInput> cartItemInputList = new ArrayList<>();
        cartItemInputList.add(CartItemInput.builder().sku(sku).quantity(Double.parseDouble(quantity)).build());


        if(!cart_id.equals("")) {
            apolloClientClass.apolloClient.mutate(new AddProductsToCartMutation(cart_id, cartItemInputList)).toBuilder().requestHeaders(apolloClientClass.getRequestHeader()).build().enqueue(new ApolloCall.Callback<AddProductsToCartMutation.Data>() {
                @Override
                public void onResponse(@NonNull Response<AddProductsToCartMutation.Data> response) {
                    if (response.hasErrors())
                        requestResponse.postValue(response.getErrors().get(0).getMessage());
                    else{
                        if(response.getData().addProductsToCart().user_errors() != null) {
                            if(response.getData().addProductsToCart().user_errors().size()>0) {
                                    requestResponse.postValue(response.getData().addProductsToCart().user_errors().get(0).message());
                            }
                            else {
                                requestResponse.postValue(MainApplication.getContext().getResources().getString(R.string.item_added_to_cart));
                            }
                        }
                        else {
                            requestResponse.postValue(MainApplication.getContext().getResources().getString(R.string.item_added_to_cart));
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull ApolloException e) {
                    requestResponse.postValue("Error");
                }
            });

        }
    }

    public MutableLiveData<String> getRequestResponse()
    {
        return requestResponse;
    }


}
