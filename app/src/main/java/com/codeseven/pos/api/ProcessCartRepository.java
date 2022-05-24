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

import apollo.pos.ApplyCouponToCartMutation;
import apollo.pos.RemoveItemFromCartMutation;
import apollo.pos.UpdateCartItemMutation;
import apollo.pos.type.ApplyCouponToCartInput;
import apollo.pos.type.CartItemInput;
import apollo.pos.type.CartItemUpdateInput;
import apollo.pos.type.RemoveItemFromCartInput;
import apollo.pos.type.UpdateCartItemsInput;

public class ProcessCartRepository {

    private final MutableLiveData<String> requestResponse;
    private final MutableLiveData<String> applyCouponRequestResponse;
    private final LoginPreference loginPreference;
    private final CartPreference cartPreference;
    private final ApolloClientClass apolloClientClass;

    public ProcessCartRepository() {
        requestResponse = new MutableLiveData<>();
        loginPreference = new LoginPreference();
        cartPreference = new CartPreference();
        applyCouponRequestResponse = new MutableLiveData<>();
        apolloClientClass = new ApolloClientClass();
    }

    public void removeItemFromCart(String item_uid){


        RemoveItemFromCartInput removeItemFromCartInput = RemoveItemFromCartInput.builder().cart_id(apolloClientClass.getCartId()).cart_item_uid(item_uid).build();
        apolloClientClass.apolloClient.mutate((new RemoveItemFromCartMutation(removeItemFromCartInput)))
                .toBuilder().requestHeaders(apolloClientClass.getRequestHeader()).build().enqueue(new ApolloCall.Callback<RemoveItemFromCartMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<RemoveItemFromCartMutation.Data> response) {
                if(response.hasErrors()) {
                    if (response.getErrors().size() > 0)
                        requestResponse.postValue(response.getErrors().get(0).getMessage());
                }
                else
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

        String cart_id = apolloClientClass.getCartId();

        List<CartItemUpdateInput> cartItemUpdateInputs = Collections.singletonList((CartItemUpdateInput.builder().cart_item_uid(item.getItemUid()).quantity(Double.valueOf(item.getItemQuantity())).build()));
        UpdateCartItemsInput itemsInput= UpdateCartItemsInput.builder().cart_id(cart_id).cart_items(cartItemUpdateInputs).build();

        apolloClientClass.apolloClient.mutate(new UpdateCartItemMutation(itemsInput))
                .toBuilder().requestHeaders(apolloClientClass.getRequestHeader()).build().enqueue(new ApolloCall.Callback<UpdateCartItemMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<UpdateCartItemMutation.Data> response) {
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
            }
        });
    }

    public void ApplyCouponOnCart(String coupon_code){
        String cart_id = apolloClientClass.getCartId();

       apolloClientClass.apolloClient.mutate(new ApplyCouponToCartMutation(ApplyCouponToCartInput.builder().cart_id(cart_id).coupon_code(coupon_code).build()))
                .toBuilder().requestHeaders(apolloClientClass.getRequestHeader()).build().enqueue(new ApolloCall.Callback<ApplyCouponToCartMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<ApplyCouponToCartMutation.Data> response) {
                if(response.hasErrors()) {
                    if (response.getErrors().size() > 0)
                        applyCouponRequestResponse.postValue(response.getErrors().get(0).getMessage());
                }
                else
                    applyCouponRequestResponse.postValue("Cart Updated.");
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                applyCouponRequestResponse.postValue(e.getLocalizedMessage());
            }
        });

    }

    public MutableLiveData<String> GetRequestResponse(){
        return  requestResponse;

    }

    public MutableLiveData<String>  getApplyCouponRequestResponse(){
        return applyCouponRequestResponse;
    }


}
