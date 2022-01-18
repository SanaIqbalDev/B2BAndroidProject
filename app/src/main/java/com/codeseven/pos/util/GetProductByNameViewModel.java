package com.codeseven.pos.util;

import androidx.databinding.BaseObservable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codeseven.pos.api.GetProductWithNameRepository;

import java.util.List;

import javax.inject.Inject;

import apollo.pos.GetAutocompleteResultsQuery;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class GetProductByNameViewModel extends ViewModel {

    @Inject GetProductsByNameObserver getProductsByNameObserver;

    @Inject public GetProductByNameViewModel(){
        this.getProductsByNameObserver = getObserver();
    }

    private GetProductsByNameObserver getObserver(){
        return new GetProductsByNameObserver();
    }


    public static class GetProductsByNameObserver extends BaseObservable{

        private GetProductWithNameRepository repository;
        private MutableLiveData<List<GetAutocompleteResultsQuery.Item>> itemsList = new MutableLiveData<>();

        @Inject
        public GetProductsByNameObserver() {
            this.repository = new GetProductWithNameRepository();
            this.itemsList = repository.GetItems();
        }
        public MutableLiveData<List<GetAutocompleteResultsQuery.Item>> getItemsList() {
            return itemsList;
        }

        public void getProductsByName(String itemName){
            repository.getProducts(itemName);
        }
    }
}
