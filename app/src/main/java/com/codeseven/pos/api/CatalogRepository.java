package com.codeseven.pos.api;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.fetcher.ApolloResponseFetchers;
import com.apollographql.apollo.request.RequestHeaders;
import com.codeseven.pos.ApolloClientClass;
import com.codeseven.pos.MainApplication;
import com.codeseven.pos.util.LoginPreference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.StatsSnapshot;

import java.util.ArrayList;
import java.util.List;

import apollo.pos.GetAllItemsQuery;
import apollo.pos.GetMegaMenuQuery;
import apollo.pos.GetPageSizeQuery;
import apollo.pos.GetProductsQuery;
import apollo.pos.fragment.ProductsFragment;
import apollo.pos.type.FilterEqualTypeInput;
import apollo.pos.type.ProductAttributeFilterInput;
import apollo.pos.type.ProductAttributeSortInput;
import apollo.pos.type.SortEnum;


public class CatalogRepository {

    private MutableLiveData<String> CatalogRequestResponse;
    private String message;
    private MutableLiveData<List<ProductsFragment.Item>> productsFragment;
    private MutableLiveData<Integer> pageCount;
    private MutableLiveData<List<GetMegaMenuQuery.CategoryList>> categoryLists;
    private ApolloClientClass apolloClientClass;

    int currentUnit = 0;
    private ArrayList<String> allCategories = new ArrayList<>();
    private int allCategoriesIndexCurrent = 0;

    private LoginPreference loginPreference;

    public CatalogRepository() {
        CatalogRequestResponse = new MutableLiveData<>("");
        productsFragment = new MutableLiveData<>();
        pageCount = new MutableLiveData<>(0);
        categoryLists = new MutableLiveData<>();
        apolloClientClass = new ApolloClientClass(true);
        loginPreference = new LoginPreference();
    }


    public void GetPageSizeofCategory(String category){

        GetPageSizeQuery query = new GetPageSizeQuery(12,
                ProductAttributeFilterInput.builder().category_id(FilterEqualTypeInput.builder().eq(category).build()).build());

        apolloClientClass.apolloClient.query(query).toBuilder().responseFetcher(ApolloResponseFetchers.CACHE_FIRST)
                .build().watcher().enqueueAndWatch(new ApolloCall.Callback<GetPageSizeQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetPageSizeQuery.Data> response) {

                if(response.hasErrors())
                {
                    Log.d("cache","Get page size error");

                }
                else {
                    int pageCount =  response.getData().products().fragments().pageFragment().page_info().total_pages().intValue();
                    String isLastCategory = loginPreference.GetCategoryLastItem();

                    if(category.equals(isLastCategory)){
                        loginPreference.setLastPageItemsCount(pageCount);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                Log.e("Cache Categories", e.getLocalizedMessage());
            }
        });
    }




    public void getCatalog(int currentPage, int pageSize, String category){

        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
//        requestHeader.addHeader("store","ur");
        requestHeader.addHeader("store","b2b");


        ArrayList<String> category_id = new ArrayList<>();
        category_id.add(category);
        Input<List<String>> in = new Input<List<String>>(category_id, true);
        Input<FilterEqualTypeInput> categoryIdInput = new Input<>(FilterEqualTypeInput.builder().inInput(in).build(),true);

        SortEnum sortOrder = SortEnum.safeValueOf("ASC");
        Input<SortEnum> sortOrderInput = new Input<>(sortOrder,true);
        Input<ProductAttributeSortInput> sortOption = new Input<>(ProductAttributeSortInput.builder().positionInput(sortOrderInput).build(),true);

        GetProductsQuery a = new GetProductsQuery(pageSize,currentPage,
                ProductAttributeFilterInput.builder().category_id(FilterEqualTypeInput.builder().eq(category).build()).build()
                , sortOption);

        apolloClientClass.apolloClient.query(a).toBuilder().requestHeaders(requestHeader.build()).responseFetcher(ApolloResponseFetchers.NETWORK_FIRST).build().watcher().enqueueAndWatch(new ApolloCall.Callback<GetProductsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetProductsQuery.Data> response) {
                if(response.hasErrors()) {
                    if (response.getErrors().size() > 0)
                        CatalogRequestResponse.postValue(response.getErrors().get(0).getMessage());
                }
                else
                {
                        try {
                            if (response.getData().products().fragments().productsFragment().page_info().total_pages() != null) {
                                pageCount.postValue(response.getData().products().fragments().productsFragment().page_info().total_pages());
                            }
                            productsFragment.postValue(response.getData().products().fragments().productsFragment().items());
                        }
                        catch (NullPointerException nullPointerException)
                        {
                        }
                }


            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                message = e.getMessage();
                CatalogRequestResponse.postValue(e.getLocalizedMessage());

            }
        });
    }
    public void getCaterogiesList(){

        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
        requestHeader.addHeader("store","b2b");

        apolloClientClass.apolloClient.query(new GetMegaMenuQuery()).toBuilder().requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<GetMegaMenuQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetMegaMenuQuery.Data> response) {
                if(response.hasErrors())
                {
                    Log.d("cache","Category List error");
                    if(response.getErrors().get(0)!=null)
                        CatalogRequestResponse.postValue(response.getErrors().get(0).getMessage());
                }
                else
                {
                    Log.d("cache","Category List recieved");

                    categoryLists.postValue(response.getData().categoryList());
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                Log.d("cache","Category List error" + e.getMessage());
                CatalogRequestResponse.postValue(e.getMessage());
            }
        });
    }


    public MutableLiveData<List<ProductsFragment.Item>> getProductsFragment(){ return productsFragment; }
    public MutableLiveData<String> getCatalogResponse()
    {
        return CatalogRequestResponse;
    }
    public MutableLiveData<Integer> getPageCount()
    {
        return pageCount;
    }
    public String getMessage(){
        return message;
    }

    public MutableLiveData<List<GetMegaMenuQuery.CategoryList>> getCategoryList(){ return categoryLists; }

}
