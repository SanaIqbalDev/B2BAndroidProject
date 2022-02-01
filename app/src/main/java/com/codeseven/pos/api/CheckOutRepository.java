package com.codeseven.pos.api;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.request.RequestHeaders;
import com.codeseven.pos.ApolloClientClass;
import com.codeseven.pos.model.AddressItem;
import com.codeseven.pos.util.CartPreference;
import com.codeseven.pos.util.LoginPreference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import apollo.pos.ApplyWalletToCartMutation;
import apollo.pos.GetAvailablePaymentMethodsQuery;
import apollo.pos.GetCustomerAddressesQuery;
import apollo.pos.GetCustomerDetailsQuery;
import apollo.pos.GetCustomerWalletQuery;
import apollo.pos.GetSelectedAndAvailableShippingMethodsQuery;
import apollo.pos.fragment.AvailableShippingMethodsCheckoutFragment;
import apollo.pos.fragment.ShippingInformationFragment;
import apollo.pos.type.ApplyWalletCartInput;

public class CheckOutRepository {

    private LoginPreference loginPreference;
    private MutableLiveData<String> CustomerInfoResponse;

    private MutableLiveData<ShippingInformationFragment.Shipping_address> customerShippingDetails;
    private MutableLiveData<AvailableShippingMethodsCheckoutFragment.Available_shipping_method> availableShippingMethod;

    private MutableLiveData< GetCustomerWalletQuery.Wallet> customer_wallet;
    private MutableLiveData<String> walletQueryResponse;

    private MutableLiveData<String> applyWalletQueryResponse;

    private MutableLiveData<List<GetAvailablePaymentMethodsQuery.Available_payment_method>> listAvailablePaymentMethods;
    private MutableLiveData<String> getPaymentMethodResponse;

    private MutableLiveData<List<String>> listOfAddresses;
    private MutableLiveData<String> getAddressResponse;

    private MutableLiveData<List<AddressItem>> addressObjects;

    private CartPreference cartPreference;

    public CheckOutRepository() {
        this.loginPreference = new LoginPreference();

        CustomerInfoResponse = new MutableLiveData<>("");

        customerShippingDetails = new MutableLiveData<>();
        availableShippingMethod = new MutableLiveData<>();

        customer_wallet = new MutableLiveData<>();
        walletQueryResponse = new MutableLiveData<>();

        applyWalletQueryResponse = new MutableLiveData<>();
        listAvailablePaymentMethods = new MutableLiveData<>();

        getPaymentMethodResponse = new MutableLiveData<>();

        listOfAddresses = new MutableLiveData<>(new ArrayList<>());
        getAddressResponse = new MutableLiveData<>();

        addressObjects = new MutableLiveData<>(new ArrayList<>());

        cartPreference = new CartPreference();


    }

    public void  getCustomerInfo()
    {
        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));

        (new ApolloClientClass()).apolloClient.query(new GetCustomerDetailsQuery()).toBuilder().requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<GetCustomerDetailsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetCustomerDetailsQuery.Data> response) {
                if(response.getErrors()!=null) {
                    if (response.getErrors().size() > 0)
                        CustomerInfoResponse.postValue(response.getErrors().get(0).getMessage());
                }
                else {
//                    responseData.postValue(response.getData().customer());
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                CustomerInfoResponse.postValue(e.getLocalizedMessage());
            }
        });
    }
    public void getShippingAddressAndMethod(){

        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));
        String cart_id =cartPreference.GetCartId("cart_id");

        (new ApolloClientClass()).apolloClient.query(new GetSelectedAndAvailableShippingMethodsQuery(cart_id)).toBuilder().requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<GetSelectedAndAvailableShippingMethodsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetSelectedAndAvailableShippingMethodsQuery.Data> response) {
                if(response.getErrors()!=null) {
                    if (response.getErrors().size() > 0)
                        CustomerInfoResponse.postValue(response.getErrors().get(0).getMessage());
                }
                else {
                    customerShippingDetails.postValue(response.getData().cart().fragments().shippingInformationFragment().shipping_addresses().get(0));
                    availableShippingMethod.postValue(Objects.requireNonNull(Objects.requireNonNull(response.getData().cart()).fragments().availableShippingMethodsCheckoutFragment().shipping_addresses().get(0).available_shipping_methods()).get(0));

                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                CustomerInfoResponse.postValue(e.getLocalizedMessage());
            }
        });


    }
    public void getCustomerWallet()
    {
        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));


        (new ApolloClientClass()).apolloClient.query(new GetCustomerWalletQuery()).toBuilder().requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<GetCustomerWalletQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetCustomerWalletQuery.Data> response) {

                if(response.getErrors()!= null) {
                    if (response.getErrors().size() > 0) {
                        walletQueryResponse.postValue(response.getErrors().get(0).getMessage());
                    }
                }
                else {
                    cartPreference.AddRemainingWalletAmount(response.getData().customer().wallet().wallet_amount());
                    customer_wallet.postValue(response.getData().customer().wallet());
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                walletQueryResponse.postValue(e.getLocalizedMessage());

            }
        });

    }
    public void ApplyWalletToCart(boolean applyWallet){

        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));
        String cart_id =cartPreference.GetCartId("cart_id");

        (new ApolloClientClass()).apolloClient.mutate(new ApplyWalletToCartMutation(ApplyWalletCartInput.builder()
                .apply_wallet(applyWallet).cart_id(cart_id).build())).toBuilder().requestHeaders(requestHeader.build()).build()
                .enqueue(new ApolloCall.Callback<ApplyWalletToCartMutation.Data>() {
                    @Override
                    public void onResponse(@NonNull Response<ApplyWalletToCartMutation.Data> response) {
                        if(response.getErrors()!=null)
                        {
                            if(response.getErrors().size()>0)
                                applyWalletQueryResponse.postValue(response.getErrors().get(0).getMessage());
                        }
                        else {
                            cartPreference.AddRemainingWalletAmount(response.getData().applyWalletCart().cart().remaining_wallet_amount().toString());
                            cartPreference.AddNeedToPay(String.valueOf(response.getData().applyWalletCart().cart().need_to_pay().doubleValue()));
                            cartPreference.AddOrderTotal(String.valueOf(response.getData().applyWalletCart().cart().order_total().doubleValue()));
                            applyWalletQueryResponse.postValue("Success");
                        }

                    }

                    @Override
                    public void onFailure(@NonNull ApolloException e) {
                        applyWalletQueryResponse.postValue(e.getLocalizedMessage());

                    }
                });

    }


    public void getAvailablePaymentMethods(){
        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));
        String cart_id =cartPreference.GetCartId("cart_id");


        (new ApolloClientClass()).apolloClient.query(new GetAvailablePaymentMethodsQuery(cart_id)).toBuilder().
                requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<GetAvailablePaymentMethodsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetAvailablePaymentMethodsQuery.Data> response) {

                if(response.getErrors()!=null){
                    if(response.getErrors().size()>0)
                    {
                        getPaymentMethodResponse.postValue(response.getErrors().get(0).getMessage());
                    }
                }
                else {
                    listAvailablePaymentMethods.postValue(response.getData().cart().available_payment_methods());
                    cartPreference.SetCartWalletAmount(response.getData().cart().wallet_payment());
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {

                getPaymentMethodResponse.postValue(e.getLocalizedMessage());
            }
        });


    }

    public void GetCustomerAddresses(){

        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));

        (new ApolloClientClass()).apolloClient.query(new GetCustomerAddressesQuery()).toBuilder().requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<GetCustomerAddressesQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetCustomerAddressesQuery.Data> response) {
                if(response.getErrors()!=null){
                    if(response.getErrors().size()>0)
                        getAddressResponse.postValue(response.getErrors().get(0).getMessage());
                }
                else
                {
                    List<String> address= new ArrayList<>();
                    List<AddressItem> addressObjects_= new ArrayList<>();

                    for(int i=0;i<response.getData().customer().addresses().size();i++){
                        GetCustomerAddressesQuery.Address a = response.getData().customer().addresses().get(i);
                        address.add(a.firstname()+" "+a.lastname()+"\n"+a.street().get(0)+"\n"+a.city()+" "+a.region().region_code()+
                                " "+a.country_code()+"\n\n"+ a.telephone());

                        addressObjects_.add(new AddressItem(a.id(), a.firstname(),a.lastname(),a.city(),a.country_code().rawValue(),a.street().get(0),"",a.telephone()));
                    }

                    listOfAddresses.postValue(address);
                    addressObjects.postValue(addressObjects_);
                }

            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                getAddressResponse.postValue(e.getLocalizedMessage());
            }
        });
    }

    public MutableLiveData<String> getCustomerInfoResponse(){
        return CustomerInfoResponse;
    }
    public MutableLiveData<ShippingInformationFragment.Shipping_address> CustomerShippingAddress()
    {
        return customerShippingDetails;
    }

    public MutableLiveData<AvailableShippingMethodsCheckoutFragment.Available_shipping_method> AvailableShippingMethod()
    {
        return availableShippingMethod;
    }


    public MutableLiveData<GetCustomerWalletQuery.Wallet> getCustomer_wallet() {
        return customer_wallet;
    }
    public MutableLiveData<String> getWalletQueryResponse() {
        return walletQueryResponse;
    }

    public MutableLiveData<String> getApplyWalletQueryResponse() {
        return applyWalletQueryResponse;
    }

    public MutableLiveData<List<GetAvailablePaymentMethodsQuery.Available_payment_method>> getListAvailablePaymentMethods() {
        return listAvailablePaymentMethods;
    }

    public MutableLiveData<String> getGetPaymentMethodResponse() {
        return getPaymentMethodResponse;
    }

    public MutableLiveData<List<String>> getListOfAddresses() {
        return listOfAddresses;
    }

    public MutableLiveData<String> getAddressResponse() {
        return getAddressResponse;
    }

    public MutableLiveData<List<AddressItem>> getAddressObjects() {
        return addressObjects;
    }
}
