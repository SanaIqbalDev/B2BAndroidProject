package com.codeseven.pos.util;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codeseven.pos.BR;
import com.codeseven.pos.api.AddProductToCartRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddToCartViewModel extends ViewModel {

    @Inject
    public AddToCartObsrever productObserver;

    @Inject
    public AddToCartViewModel(AddToCartObsrever productObserver) {
        getProductObserver();
    }

    private AddToCartObsrever getProductObserver() {
        return new AddToCartObsrever();
    }


    public static class AddToCartObsrever extends BaseObservable{


        private String productSku;
        private String productName;
        private String productPrice;
        private String productDescription;
        private String productQuantity= "1";

        private AddProductToCartRepository productToCartRepository;
        private MutableLiveData<String> repositoryResponse;

        @Inject
        public AddToCartObsrever() {
            productSku = "";
            productName = "";
            productPrice = "";
            productQuantity = "1";
            productDescription = "";

            productToCartRepository = new AddProductToCartRepository();
            repositoryResponse = productToCartRepository.getRequestResponse();
        }

        public String getProductSku() {
            return productSku;
        }

        public void setProductSku(String productSku) {
            this.productSku = productSku;
        }

        public String getProductDescription() {
            return productDescription;
        }

        public void setProductDescription(String productDescription) {
            this.productDescription = productDescription;
        }

        @Bindable
        public String getProductName(){
            return productName;
        }


        @Bindable
        public String getProductPrice(){
            return productPrice;
        }

        @Bindable
        public String getProductQuantity(){
            return productQuantity;
        }


        public void setProductName(String productName_)
        {
            productName = productName_;
            notifyPropertyChanged(BR.productName);

        }

        public void setProductPrice(String productPrice_)
        {
            productPrice = productPrice_;
            notifyPropertyChanged(BR.productPrice);
        }

        public void setProductQuantity(String productQuantity_)
        {
            productQuantity = productQuantity_;
            notifyPropertyChanged(BR.productQuantity);
        }


        public void increaseQuantity()
        {
            String currentQuantity = getProductQuantity();

            Integer temp = Integer.parseInt(currentQuantity) + 1;
            setProductQuantity(String.valueOf(temp));
        }

        public void decreaseQuantity()
        {

            int temp = Integer.parseInt(getProductQuantity());
            if(temp>1) {

                setProductQuantity(String.valueOf(temp-1));
            }
        }

        public void placeOrder()
        {
            productToCartRepository.addProductToCart(getProductSku(), getProductQuantity());

        }
        public void placeOrder(String sku, String quantity)
        {
            productToCartRepository.addProductToCart(sku, quantity);

        }

        public MutableLiveData<String> getRepositoryResponse()
        {
            return repositoryResponse;
        }


    }

}
