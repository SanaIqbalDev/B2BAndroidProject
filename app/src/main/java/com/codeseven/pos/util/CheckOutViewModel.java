package com.codeseven.pos.util;

import androidx.databinding.BaseObservable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codeseven.pos.api.CheckOutRepository;

import javax.inject.Inject;

import apollo.pos.GetCustomerDetailsQuery;
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
        private MutableLiveData<GetCustomerDetailsQuery.Customer> CustomerShippingData;


        @Inject
        public CheckoutObserver(){
            this.checkOutRepository = new CheckOutRepository();
            this.CustomerInfoResponse = checkOutRepository.getCustomerInfoResponse();
            this.CustomerShippingData = checkOutRepository.getResponseData();
        }

        public void GetCustomerAddressDetails(){
            checkOutRepository.getCustomerInfo();
        }
        public MutableLiveData<String> getCustomerInfoResponse(){
            return CustomerInfoResponse;
        }

        public MutableLiveData<GetCustomerDetailsQuery.Customer> getCustomerShippingData(){
            return CustomerShippingData;
        }
    }
}
