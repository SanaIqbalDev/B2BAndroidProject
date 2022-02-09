package com.codeseven.pos.api;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.ScalarType;
import com.apollographql.apollo.api.ScalarTypeAdapters;
import com.apollographql.apollo.cache.CacheHeaders;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.request.RequestHeaders;
import com.codeseven.pos.ApolloClientClass;
import com.codeseven.pos.ui.CatalogFragment;
import com.codeseven.pos.util.CartPreference;
import com.codeseven.pos.util.LoginPreference;

import java.util.ArrayList;
import java.util.List;

import apollo.pos.CreateCartMutation;
import apollo.pos.GetCartByIdQuery;
import apollo.pos.GetCustomerCartQuery;
import apollo.pos.RemoveItemFromCartMutation;
import okhttp3.OkHttpClient;
import okhttp3.internal.http2.Header;

public class ViewCartRepository {

    private CartPreference cartPreference;
    private LoginPreference loginPreference;
    private MutableLiveData<String> cartRequestResponse;
    private String cartId ;
    private MutableLiveData<List<GetCartByIdQuery.Item>> cartItems;

    public ViewCartRepository() {
        cartPreference = new CartPreference();
        loginPreference = new LoginPreference();
        this.cartRequestResponse = new MutableLiveData<>();
        cartId = "";
        cartItems = new MutableLiveData<>(new ArrayList<>());
    }

    public void getCustomerExistingCart(){

        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));

        (new ApolloClientClass()).apolloClient.query(new GetCustomerCartQuery()).toBuilder().requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<GetCustomerCartQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetCustomerCartQuery.Data> response) {
                if(response.getErrors()!=null) {
                    if (response.getErrors().size() > 0)
                        cartRequestResponse.postValue(response.getErrors().get(0).getMessage());
                }
                else {
                    cartRequestResponse.postValue(response.getData().toString());
                    cartId = response.getData().customerCart().id();
                    cartPreference.AddCartId("cart_id", cartId);
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {

                cartRequestResponse.postValue("No cart found");

                getCustomerNewCart();
            }
        });
    }

    public void getCustomerNewCart()
    {
        (new ApolloClientClass()).apolloClient.mutate(new CreateCartMutation()).enqueue(new ApolloCall.Callback<CreateCartMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<CreateCartMutation.Data> response) {
                cartRequestResponse.postValue("New cart Created");
                cartId = response.getData().cartId();

                cartPreference.AddCartId("cart_id",cartId);

            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                cartRequestResponse.postValue(e.toString());

            }
        });

    }
    public void GetCartById(String cartId){


        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));


        (new ApolloClientClass()).apolloClient.query(new GetCartByIdQuery(cartId)).toBuilder().requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<GetCartByIdQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetCartByIdQuery.Data> response) {
                if(response.getErrors()!=null){
                    if(response.getErrors().size()>0)
                        cartRequestResponse.postValue(response.getErrors().get(0).getMessage());
                }
                else {
                    if (response.getData().cart() != null)
                        cartItems.postValue(response.getData().cart().items());
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                cartRequestResponse.postValue(e.toString());
            }
        });
    }
    public MutableLiveData<String> getCartRequestResponse(){
        return cartRequestResponse;
    }
    public String getCartId()
    {
        return cartId;
    }
    public MutableLiveData<List<GetCartByIdQuery.Item>> getCartItemsList(){
        return cartItems;
    }

}

