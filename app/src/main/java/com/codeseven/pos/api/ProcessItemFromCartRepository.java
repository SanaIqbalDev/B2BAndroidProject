package com.codeseven.pos.api;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.request.RequestHeaders;
import com.codeseven.pos.ApolloClientClass;
import com.codeseven.pos.model.CatalogItem;
import com.codeseven.pos.util.CartPreference;
import com.codeseven.pos.util.LoginPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import apollo.pos.RemoveItemFromCartMutation;
import apollo.pos.UpdateCartItemMutation;
import apollo.pos.type.CartItemInput;
import apollo.pos.type.CartItemUpdateInput;
import apollo.pos.type.RemoveItemFromCartInput;
import apollo.pos.type.UpdateCartItemsInput;

public class ProcessItemFromCartRepository {

    private MutableLiveData<String> requestResponse;
    private LoginPreference loginPreference;
    private CartPreference cartPreference;
    public ProcessItemFromCartRepository() {
        requestResponse = new MutableLiveData<>();
        loginPreference = new LoginPreference();
        cartPreference = new CartPreference();
    }

    public void removeItemFromCart(String item_uid){

        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));
        cartPreference.GetCartId("cart_id");


        RemoveItemFromCartInput removeItemFromCartInput = RemoveItemFromCartInput.builder().cart_id( cartPreference.GetCartId("cart_id")).cart_item_uid(item_uid).build();
        (new ApolloClientClass()).apolloClient.mutate((new RemoveItemFromCartMutation(removeItemFromCartInput))).toBuilder().requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<RemoveItemFromCartMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<RemoveItemFromCartMutation.Data> response) {
                requestResponse.postValue(response.getData().toString());
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                requestResponse.postValue(e.toString());

            }
        });
    }
    public void UpdateItemInCart(CatalogItem item)
    {

        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));
        String cart_id = cartPreference.GetCartId("cart_id");

        List<CartItemUpdateInput> cartItemUpdateInputs = Collections.singletonList((CartItemUpdateInput.builder().cart_item_uid(item.getItemUid()).quantity(Double.valueOf(item.getItemQuantity())).build()));
        UpdateCartItemsInput itemsInput= UpdateCartItemsInput.builder().cart_id(cart_id).cart_items(cartItemUpdateInputs).build();

        (new ApolloClientClass()).apolloClient.mutate(new UpdateCartItemMutation(itemsInput)).toBuilder().requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<UpdateCartItemMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<UpdateCartItemMutation.Data> response) {
                String ab = "";
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                String ab = "";
            }
        });
    }


    public MutableLiveData<String> GetRequestResponse(){
        return  requestResponse;

    }


}
