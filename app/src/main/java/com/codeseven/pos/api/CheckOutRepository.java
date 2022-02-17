package com.codeseven.pos.api;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Input;
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

import apollo.pos.ApplyDeliveryCartMutation;
import apollo.pos.ApplyWalletToCartMutation;
import apollo.pos.GetAvailablePaymentMethodsQuery;
import apollo.pos.GetCartDetailsQuery;
import apollo.pos.GetCustomerAddressesQuery;
import apollo.pos.GetCustomerDetailsQuery;
import apollo.pos.GetCustomerWalletQuery;
import apollo.pos.GetSelectedAndAvailableShippingMethodsQuery;
import apollo.pos.PlaceOrderMutation;
import apollo.pos.SetBillingAddresMutation;
import apollo.pos.SetPaymentMethodOnCartMutation;
import apollo.pos.SetShippingMutation;
import apollo.pos.SetShippingmethodOnCartMutation;
import apollo.pos.fragment.AvailableShippingMethodsCheckoutFragment;
import apollo.pos.fragment.ShippingInformationFragment;
import apollo.pos.type.ApplyDeliveryCartInput;
import apollo.pos.type.ApplyWalletCartInput;
import apollo.pos.type.BillingAddressInput;
import apollo.pos.type.CartAddressInput;
import apollo.pos.type.SetShippingAddressesOnCartInput;
import apollo.pos.type.ShippingAddressInput;
import apollo.pos.type.ShippingMethodInput;
//import apollo.pos.type.ShippingMethodInput;

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
    private MutableLiveData<String> default_shipping_id;
    private MutableLiveData<GetCustomerAddressesQuery.Address> default_shipping_address;
    private MutableLiveData<List<AddressItem>> addressObjects;
    private MutableLiveData<String> getApplyDeliveryCartResponse;

    private CartPreference cartPreference;

    private MutableLiveData<String> placeOrderResponseMessage;

    private  MutableLiveData<Boolean> istrue;

    //    private MutableLiveData<List<Boolean>> shouldPlaceOrder;
    private List<Boolean> counter;


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

        default_shipping_id = new MutableLiveData<>();
        default_shipping_address = new MutableLiveData<>();

        getApplyDeliveryCartResponse = new MutableLiveData<>();

        placeOrderResponseMessage =  new MutableLiveData<>();

        istrue = new MutableLiveData<>();
        istrue.postValue(true);
//        shouldPlaceOrder = new MutableLiveData<>(new ArrayList<>());

        counter = new ArrayList<>();

        cartPreference = new CartPreference();


    }

    public void  getCustomerInfo(){
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
                    try {
                        if(response.getData().cart().fragments().shippingInformationFragment().shipping_addresses()!=null) {
                            if (response.getData().cart().fragments().shippingInformationFragment().shipping_addresses().size() > 0)
                            {
                                customerShippingDetails.postValue(response.getData().cart().fragments().shippingInformationFragment().shipping_addresses().get(0));
                            }
                            if(response.getData().cart().fragments().availableShippingMethodsCheckoutFragment().shipping_addresses() != null) {
                                if(response.getData().cart().fragments().availableShippingMethodsCheckoutFragment().shipping_addresses().size()>0)
                                {
                                    if(response.getData().cart().fragments().availableShippingMethodsCheckoutFragment().shipping_addresses().get(0).available_shipping_methods() != null) {
                                        if(response.getData().cart().fragments().availableShippingMethodsCheckoutFragment().shipping_addresses().get(0).available_shipping_methods().size() >0)
                                            availableShippingMethod.postValue(Objects.requireNonNull(Objects.requireNonNull(response.getData().cart()).fragments().availableShippingMethodsCheckoutFragment().shipping_addresses().get(0).available_shipping_methods()).get(0));
                                    }
                                }
                            }
                        }
                    }
                    catch (NullPointerException e){
                        Log.e("error",e.getLocalizedMessage());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                CustomerInfoResponse.postValue(e.getLocalizedMessage());
            }
        });


    }
    public void getCustomerWallet(){
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
//                    cartPreference.AddRemainingWalletAmount(response.getData().customer().wallet().wallet_amount());
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
                    default_shipping_id.postValue(response.getData().customer().default_shipping());

                    for(int i=0;i<response.getData().customer().addresses().size();i++){
                        GetCustomerAddressesQuery.Address a = response.getData().customer().addresses().get(i);
                        address.add(a.firstname()+" "+a.lastname()+"\n"+a.street().get(0)+"\n"+a.city()+" "+a.region().region_code()+
                                " "+a.country_code()+"\n\n"+ a.telephone());

                        addressObjects_.add(new AddressItem(a.id(), a.firstname(),a.lastname(),a.city(),a.country_code().rawValue(),a.street().get(0),"",a.telephone()));


                        if(a.default_shipping()){
                            default_shipping_address.postValue(a);
                        }

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

    public void SetCustomerShippingAddress(int customer_address_id,String customer_notes,
                                           String pickup_location_code){

        ShippingAddressInput input = ShippingAddressInput.builder().customer_address_id(customer_address_id).build();
        List<ShippingAddressInput> list = new ArrayList<>();
        list.add(input);
        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));

        SetShippingAddressesOnCartInput ab = SetShippingAddressesOnCartInput.builder().shipping_addresses(list).cart_id(cartPreference.GetCartId("cart_id")).build();

        (new ApolloClientClass()).apolloClient.mutate(new SetShippingMutation(ab)).toBuilder().requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<SetShippingMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<SetShippingMutation.Data> response) {
                if(!response.hasErrors())
                {
                    counter.add(0,true);
                    placeOrderResponseMessage.postValue("Shipping Address success");

                }
                else
                {
                    counter.add(0, false);
                    placeOrderResponseMessage.postValue("Shipping Address failure");

                }

            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                counter.add(0, false);
                placeOrderResponseMessage.postValue("Shipping Address failure");

            }
        });


    }
    public void SetShippingMethodOnCart(){

        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));

        List<ShippingMethodInput> list = new ArrayList<>();
        list.add(ShippingMethodInput.builder().method_code("flatrate").carrier_code("flatrate").build());


        (new ApolloClientClass()).apolloClient.mutate(new SetShippingmethodOnCartMutation(cartPreference.GetCartId("cart_id"),list)).toBuilder()
                .requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<SetShippingmethodOnCartMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<SetShippingmethodOnCartMutation.Data> response) {

                if(!response.hasErrors()) {
                    String ab = "";
                    placeOrderResponseMessage.postValue("Shipping Method success");
                    counter.add(1, true);
                    if(counter.size()==5){
                        checkCount();
                    }
                }
                else {
                    counter.add(1, false);
                    placeOrderResponseMessage.postValue("Shipping Method failure");

                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                String ab = "";
                counter.add(1, false);
                placeOrderResponseMessage.postValue("Shipping Method failure");

                if(e.getCause().getLocalizedMessage().equals("timeout"))
                {
                    placeOrderResponseMessage.postValue(e.getCause().getMessage());
                    SetShippingMethodOnCart();
                }

            }
        });


    }

    public void SetPaymentMethodOnCart(String payment_method_code){

        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));


        (new ApolloClientClass()).apolloClient.mutate(new SetPaymentMethodOnCartMutation(cartPreference.GetCartId("cart_id"), payment_method_code))
                .toBuilder().requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<SetPaymentMethodOnCartMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<SetPaymentMethodOnCartMutation.Data> response) {
                if(!response.hasErrors())
                {
                    counter.add(4,true);
                    placeOrderResponseMessage.postValue("Payment Method success");
                }
                else{
                    counter.add(4,false);
                    placeOrderResponseMessage.postValue("Payment Method failure");
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                counter.add(4,false);
                placeOrderResponseMessage.postValue("Payment Method failure");
                String ab = "";
            }
        });
    }

    public void SetBillingAddress(CartAddressInput addressInput, int address_id, boolean same_as_shipping)
    {
        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));


        (new ApolloClientClass()).apolloClient.mutate(new SetBillingAddresMutation(BillingAddressInput.builder()
                .customer_address_id(address_id).build() , cartPreference.GetCartId("cart_id") )).toBuilder()
                .requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<SetBillingAddresMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<SetBillingAddresMutation.Data> response) {
                if(!response.hasErrors()){
                    counter.add(2,true);
                    placeOrderResponseMessage.postValue("Billing Address Success");

                }
                else
                {
                    counter.add(2,false);
                    placeOrderResponseMessage.postValue("Billing Address failure");
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                counter.add(2, false);
                placeOrderResponseMessage.postValue("Billing Address failure");
            }
        });
//        (new ApolloClientClass()).apolloClient.mutate(new (addressInput,address_id,same_as_shipping,cartPreference.GetCartId("cart_id")))
//                .toBuilder().requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<SetBillingAddressOnCartMutation.Data>() {
//            @Override
//            public void onResponse(@NonNull Response<SetBillingAddressOnCartMutation.Data> response) {
//                if(!response.hasErrors()){
//                    counter.add(3,true);
//                    placeOrderResponseMessage.postValue("Billing Address Success");
//
//                }
//                else
//                {
//                    counter.add(3,false);
//                    placeOrderResponseMessage.postValue("Billing Address failure");
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull ApolloException e) {
//                counter.add(3, false);
//                placeOrderResponseMessage.postValue("Billing Address failure");
//            }
//        });
    }
    public void ApplyDeliveryCart(String comments, String date, String timeslot)
    {
        ApplyDeliveryCartInput applyDeliveryCartInput = ApplyDeliveryCartInput.builder().cart_id(cartPreference.GetCartId("cart_id")).shipping_arrival_comments(comments)
                .shipping_arrival_date(date).shipping_arrival_timeslot(timeslot).build();

        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));


        (new ApolloClientClass()).apolloClient.mutate(new ApplyDeliveryCartMutation(applyDeliveryCartInput)).toBuilder()
                .requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<ApplyDeliveryCartMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<ApplyDeliveryCartMutation.Data> response) {
                if(response.hasErrors())
                {
                    counter.add(3, false);
                    getApplyDeliveryCartResponse.postValue("Error applying delivery cart.");
                    placeOrderResponseMessage.postValue("Delivery Cart failure");

                }
                else
                {
                    counter.add(3, true);
                    getApplyDeliveryCartResponse.postValue("Delivery Cart Applied.");
                    placeOrderResponseMessage.postValue("Delivery Cart success");

                }

//                checkCount();

            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                counter.add(3, false);
                getApplyDeliveryCartResponse.postValue("Error applying delivery cart.");
                placeOrderResponseMessage.postValue("Delivery Cart failure");


            }
        });
    }


    public void checkCount(){
        if(counter.size()== 5){
            istrue.postValue(true);
            for(int i=0 ; i<counter.size();i++){
                if(!counter.get(i))
                    istrue.postValue(false);
            }
            if(istrue.getValue())
            {
                PlaceOrder();
            }
        }
    }
    public void GetCartDetail(){

        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));

        (new ApolloClientClass()).apolloClient.query(new GetCartDetailsQuery(cartPreference.GetCartId("cart_id"))).
                toBuilder().requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<GetCartDetailsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetCartDetailsQuery.Data> response) {
                String ab = "";
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                String ab = "";
            }
        });
    }
    public void PlaceOrder()
    {
        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));

        (new ApolloClientClass()).apolloClient.mutate(new PlaceOrderMutation(cartPreference.GetCartId("cart_id"))).toBuilder().requestHeaders(requestHeader.build())
                .build().enqueue(new ApolloCall.Callback<PlaceOrderMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<PlaceOrderMutation.Data> response) {
                String ab = "";

                if(response.hasErrors())
                {
//                    response.getErrors().get(0).getMessage();
                    placeOrderResponseMessage.postValue(response.getErrors().get(0).getMessage());
                }
                else
                {
                    placeOrderResponseMessage.postValue("Place order success");
                }

            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                String ab = "";
                PlaceOrder();

                if(e.getCause().getLocalizedMessage().equals("timeout"))
                {
                    placeOrderResponseMessage.postValue(e.getCause().getMessage());
//                    PlaceOrder();
                }
                else {
                    placeOrderResponseMessage.postValue("Place order failure");
                }
            }
        });
    }


    public MutableLiveData<String> getCustomerInfoResponse(){
        return CustomerInfoResponse;
    }
    public MutableLiveData<ShippingInformationFragment.Shipping_address> CustomerShippingAddress(){
        return customerShippingDetails;
    }
    public MutableLiveData<AvailableShippingMethodsCheckoutFragment.Available_shipping_method> AvailableShippingMethod() {
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

    public MutableLiveData<String> getDefault_shipping_id() {
        return default_shipping_id;
    }

    public MutableLiveData<GetCustomerAddressesQuery.Address> getDefault_shipping_address() {
        return default_shipping_address;
    }

    public MutableLiveData<String> getGetApplyDeliveryCartResponse() {
        return getApplyDeliveryCartResponse;
    }

    public MutableLiveData<String> getPlaceOrderResponseMessage() {
        return placeOrderResponseMessage;
    }
    public MutableLiveData<Boolean> getIstruePlaceOrder()
    {
        return  istrue;
    }
}
