package com.codeseven.pos.util;

import androidx.databinding.BaseObservable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codeseven.pos.api.ViewCartRepository;

import java.util.List;

import javax.inject.Inject;

import apollo.pos.GetCartByIdQuery;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CartViewModel  extends ViewModel {

    @Inject
    public CartObserver cartObserver;


    @Inject public CartViewModel() {
        this.cartObserver = getCartObserver();
    }

    public CartObserver getCartObserver(){
        return new CartObserver();
    }



    public static class CartObserver extends BaseObservable {
        private ViewCartRepository cartRepository;
        private MutableLiveData<String> cartRequestResponse;
        private MutableLiveData<List<GetCartByIdQuery.Item>> cartItemsList;



        @Inject
        public CartObserver() {
            this.cartRepository = new ViewCartRepository();
            this.cartRequestResponse = cartRepository.getCartRequestResponse();
            cartItemsList = cartRepository.getCartItemsList();
        }


        public void getCartId(){
            cartRepository.getCustomerExistingCart();
        }
        public void getCartItems(String id)
        {
            cartRepository.GetCartById(id);
        }
        public MutableLiveData<List<GetCartByIdQuery.Item>> getCartItemsList(){
            return cartItemsList;
        }

        public MutableLiveData<String> getCartRequestResponse(){
            return cartRequestResponse;
        }

        public void GetCustomerCart()
        {
            cartRepository.getCustomerExistingCart();
        }
    }
}
