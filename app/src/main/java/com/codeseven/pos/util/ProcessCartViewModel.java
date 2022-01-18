package com.codeseven.pos.util;

import androidx.databinding.BaseObservable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codeseven.pos.api.ProcessCartRepository;
import com.codeseven.pos.model.CatalogItem;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProcessCartViewModel extends ViewModel {

    @Inject
    ProcessCartObserver removeItemObserver;
    @Inject public ProcessCartViewModel(){
        removeItemObserver = new ProcessCartObserver();
    }

    public static  class ProcessCartObserver extends BaseObservable{

        private ProcessCartRepository processCartRepository;
        private MutableLiveData<String> removeItemResponse;
        private MutableLiveData<String> applyCouponResponse;

        @Inject
        public ProcessCartObserver() {
            this.processCartRepository = new ProcessCartRepository();
            this.removeItemResponse = processCartRepository.GetRequestResponse();
            this.applyCouponResponse = processCartRepository.getApplyCouponRequestResponse();
        }


        public void RemoveCartItem(String uid){
            processCartRepository.removeItemFromCart(uid);
        }
        public void  UpdateCartItem(CatalogItem item)
        {
            processCartRepository.UpdateItemInCart(item);
        }


        public MutableLiveData<String> getRequestResponse()
        {
            return removeItemResponse;
        }

        public MutableLiveData<String> getApplyCouponResponse(){ return applyCouponResponse;}

        public void ApplyCouponToCart(String coupon_code){
            processCartRepository.ApplyCouponOnCart(coupon_code);
        }



    }
}
