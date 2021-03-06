package com.codeseven.pos.util;

import androidx.databinding.BaseObservable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codeseven.pos.api.CatalogRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import apollo.pos.GetMegaMenuQuery;
import apollo.pos.fragment.ProductsFragment;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CatalogViewModel extends ViewModel {


    @Inject
    CatalogObserver catalogObserver;

    @Inject
    public CatalogViewModel() {
        catalogObserver = getProductListObserver();
    }

    private CatalogObserver getProductListObserver()
    {
        return new CatalogObserver();
    }

    public static class CatalogObserver extends BaseObservable{

        private CatalogRepository catalogRepository;
        private MutableLiveData<List<ProductsFragment.Item>> productsList ;
        private MutableLiveData<String> catalogRequestResponse;
        private String responseMessage;
        private MutableLiveData<Integer> pageCount;
        private MutableLiveData<List<GetMegaMenuQuery.CategoryList>> categoryLists;

        @Inject
        public CatalogObserver() {

            catalogRepository = new CatalogRepository();
            catalogRequestResponse = catalogRepository.getCatalogResponse();
            productsList = catalogRepository.getProductsFragment();
            pageCount = catalogRepository.getPageCount();
            responseMessage = catalogRepository.getMessage();
            categoryLists = catalogRepository.getCategoryList();
        }

        public void getUpdatedcatalog(int currentPage, int pageSize, String category)
        {
            catalogRepository.getCatalog(currentPage,pageSize, category);
        }
        public void getCatalog(int currentPage, int pageSize, String category)
        {
            catalogRepository.getCatalog(currentPage,pageSize, category);
        }

        public void GetPageSizeOfCategory(String category){
            catalogRepository.GetPageSizeofCategory(category);

        }
        public MutableLiveData<List<ProductsFragment.Item>> getProductFragments()
        {
            return productsList;
        }

        public MutableLiveData<String> getCatalogRequestResponse(){
            return catalogRequestResponse;
        }

        public MutableLiveData<Integer> getPageCount(){
            return pageCount;
        }

        public String getResponseMessage(){
            return responseMessage;
        }

        public void GetCategoryList()
        {
            catalogRepository.getCaterogiesList();
        }

        public MutableLiveData<List<GetMegaMenuQuery.CategoryList>> getCategoryLists() {
            return categoryLists;
        }

    }
}
