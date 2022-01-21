package com.codeseven.pos.api;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.request.RequestHeaders;
import com.codeseven.pos.ApolloClientClass;
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


    public AddProductToCartRepository() {
        requestResponse = new MutableLiveData<>("");
        cartPreference = new CartPreference();
        loginPreference = new LoginPreference();
    }

    public void addProductToCart(String sku, String quantity)
    {
        String cart_id = cartPreference.GetCartId("cart_id");
        List<CartItemInput> cartItemInputList = new ArrayList<>();
        cartItemInputList.add(CartItemInput.builder().sku(sku).quantity(Double.parseDouble(quantity)).build());
        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));


        if(!cart_id.equals("")) {
            (new ApolloClientClass()).apolloClient.mutate(new AddProductsToCartMutation(cart_id, cartItemInputList)).toBuilder().requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<AddProductsToCartMutation.Data>() {
                @Override
                public void onResponse(@NonNull Response<AddProductsToCartMutation.Data> response) {
                    if (response.getErrors()!=null)
                        requestResponse.postValue(response.getErrors().get(0).getMessage());
                    else
                        requestResponse.postValue("Item added to cart");
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
