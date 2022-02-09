package com.codeseven.pos.util;

import androidx.databinding.BaseObservable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apollographql.apollo.api.Input;
import com.codeseven.pos.api.CheckOutRepository;
import com.codeseven.pos.model.AddressItem;

import java.util.List;

import javax.inject.Inject;

import apollo.pos.GetAvailablePaymentMethodsQuery;
import apollo.pos.GetCustomerAddressesQuery;
import apollo.pos.GetCustomerWalletQuery;
import apollo.pos.fragment.AvailableShippingMethodsCheckoutFragment;
import apollo.pos.fragment.ShippingInformationFragment;
import apollo.pos.type.CartAddressInput;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CheckOutViewModel extends ViewModel {

    @Inject CheckoutObserver checkoutObserver;

    @Inject public CheckOutViewModel(){
        this.checkoutObserver = getCheckoutObserver();
    }

    private CheckoutObserver getCheckoutObserver()
    {
        return new CheckoutObserver();
    }

    public static class CheckoutObserver extends BaseObservable{

        private CheckOutRepository checkOutRepository;
        private MutableLiveData<String> CustomerInfoResponse;
        private MutableLiveData<ShippingInformationFragment.Shipping_address> CustomerShippingData;
        private MutableLiveData<AvailableShippingMethodsCheckoutFragment.Available_shipping_method> AvailableShippingMethod;

        private MutableLiveData< GetCustomerWalletQuery.Wallet> customerWalletData;
        private MutableLiveData<String> walletQueryResponse;

        private MutableLiveData<String> applyWalletQueryResponse;

        private MutableLiveData<List<GetAvailablePaymentMethodsQuery.Available_payment_method>> GetAvailablePaymentMethods;
        private MutableLiveData<String> GetAvailablePaymentMethodsResponse;

        private MutableLiveData<List<String>> listOfAddresses;
        private MutableLiveData<String> addressListResponse;

        private MutableLiveData<String> default_shipping_id;
        private MutableLiveData<GetCustomerAddressesQuery.Address> default_shipping_Address;

        private MutableLiveData<String> getApplyDeliveryCartResponse;


        @Inject
        public CheckoutObserver(){
            this.checkOutRepository = new CheckOutRepository();
            this.CustomerInfoResponse = checkOutRepository.getCustomerInfoResponse();
            this.CustomerShippingData = checkOutRepository.CustomerShippingAddress();
            this.AvailableShippingMethod = checkOutRepository.AvailableShippingMethod();
            this.customerWalletData = checkOutRepository.getCustomer_wallet();
            this.walletQueryResponse = checkOutRepository.getWalletQueryResponse();
            this.applyWalletQueryResponse = checkOutRepository.getApplyWalletQueryResponse();
            GetAvailablePaymentMethods = checkOutRepository.getListAvailablePaymentMethods();
            GetAvailablePaymentMethodsResponse = checkOutRepository.getGetPaymentMethodResponse();
            this.listOfAddresses = checkOutRepository.getListOfAddresses();
            this.addressListResponse = checkOutRepository.getAddressResponse();
            default_shipping_id = checkOutRepository.getDefault_shipping_id();
            default_shipping_Address = checkOutRepository.getDefault_shipping_address();
            getApplyDeliveryCartResponse = checkOutRepository.getGetApplyDeliveryCartResponse();
        }

        public void GetCustomerAddressDetails(){
            checkOutRepository.getShippingAddressAndMethod();
        }
        public MutableLiveData<String> getCustomerInfoResponse(){
            return CustomerInfoResponse;
        }

        public MutableLiveData<ShippingInformationFragment.Shipping_address> getCustomerShippingData(){
            return CustomerShippingData;
        }

        public MutableLiveData<AvailableShippingMethodsCheckoutFragment.Available_shipping_method> GetShippingMethod(){
            return AvailableShippingMethod;
        }


        public MutableLiveData<GetCustomerWalletQuery.Wallet> getCustomerWalletData() {
            return customerWalletData;
        }

        public MutableLiveData<String> getWalletQueryResponse() {
            return walletQueryResponse;
        }


        public void getCustomerWallet(){
            checkOutRepository.getCustomerWallet();
        }

        public MutableLiveData<String> getApplyWalletQueryResponse() {
            return applyWalletQueryResponse;
        }

        public void ApplyWallet(boolean applyWallet){
            checkOutRepository.ApplyWalletToCart(applyWallet);
        }


        public void GetListOfAvailablePaymentMethods()
        {
            checkOutRepository.getAvailablePaymentMethods();
        }

        public void SetCustomerShippingAddress(Input<CartAddressInput> cartAddressInput, int customer_address_id,String customer_notes,
                                               String pickup_location_code ){
            checkOutRepository.SetCustomerShippingAddress(cartAddressInput,customer_address_id,customer_notes,pickup_location_code);
        }
        public MutableLiveData<String> getPaymentMethodsResponse(){
            return GetAvailablePaymentMethodsResponse;
        }


        public MutableLiveData<List<GetAvailablePaymentMethodsQuery.Available_payment_method>> getGetAvailablePaymentMethods() {
            return GetAvailablePaymentMethods;
        }


        public void GetCustomerAvailableAddresses(){
            checkOutRepository.GetCustomerAddresses();
        }

        public MutableLiveData<List<String>> getListOfAddresses() {
            return listOfAddresses;
        }
        public MutableLiveData<String> getAddressListResponse() {
            return addressListResponse;
        }

        public MutableLiveData<List<AddressItem>> getAddressObjects()
        {
            return checkOutRepository.getAddressObjects();
        }

        public MutableLiveData<String> getDefault_shipping_id() {
            return default_shipping_id;
        }

        public MutableLiveData<GetCustomerAddressesQuery.Address> getDefault_shipping_Address() {
            return default_shipping_Address;
        }

        public void applyDeliveryCart(String comments, String date, String timeslot)
        {
            checkOutRepository.ApplyDeliveryCart(comments,date,timeslot);
        }


    }
}
