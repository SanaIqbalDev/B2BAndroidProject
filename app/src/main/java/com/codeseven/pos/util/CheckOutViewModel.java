package com.codeseven.pos.util;

import androidx.databinding.BaseObservable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codeseven.pos.api.CheckOutRepository;
import com.codeseven.pos.model.AddressItem;

import java.util.List;

import javax.inject.Inject;

import apollo.pos.GetAvailablePaymentMethodsQuery;
import apollo.pos.GetCustomerAddressesQuery;
import apollo.pos.GetCustomerWalletQuery;
import apollo.pos.fragment.AvailableShippingMethodsCheckoutFragment;
import apollo.pos.fragment.ShippingInformationFragment;
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
    }
}
