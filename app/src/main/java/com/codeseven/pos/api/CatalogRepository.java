package com.codeseven.pos.api;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.fetcher.ApolloResponseFetchers;
import com.codeseven.pos.ApolloClientClass;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import apollo.pos.GetAllItemsQuery;
import apollo.pos.GetMegaMenuQuery;
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
    private MutableLiveData<Boolean> isTransactionComplete;
    int currentUnit = 0;


    public CatalogRepository() {
        CatalogRequestResponse = new MutableLiveData<>("");
        isTransactionComplete = new MutableLiveData<>();
        isTransactionComplete.postValue(false);
        productsFragment = new MutableLiveData<>();
        pageCount = new MutableLiveData<>(0);
        categoryLists = new MutableLiveData<>();
        apolloClientClass = new ApolloClientClass(true);
    }

    public void getAllCatalog(int currentPage, int pageSize, String category){


        currentUnit = currentPage;
        ArrayList<String> category_id = new ArrayList<>();
        category_id.add(category);
        Input<List<String>> in = new Input<List<String>>(category_id, true);
        Input<FilterEqualTypeInput> ab = new Input<>(FilterEqualTypeInput.builder().inInput(in).build(),true);

        SortEnum sortOrder = SortEnum.safeValueOf("ASC");
        Input<SortEnum> sortOrderInput = new Input<>(sortOrder,true);
        Input<ProductAttributeSortInput> sortOption = new Input<>(ProductAttributeSortInput.builder().positionInput(sortOrderInput).build(),true);

        GetProductsQuery a = new GetProductsQuery(pageSize,currentPage,
                ProductAttributeFilterInput.builder().category_id(FilterEqualTypeInput.builder().eq(category).build()).build()
                , sortOption);

        apolloClientClass.apolloClient.query(a).toBuilder().responseFetcher(ApolloResponseFetchers.CACHE_FIRST).build().watcher().enqueueAndWatch(new ApolloCall.Callback<GetProductsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetProductsQuery.Data> response) {
                if(response.hasErrors()) {
                    if (response.getErrors().size() > 0)
                        CatalogRequestResponse.postValue(response.getErrors().get(0).getMessage());
                }
                else
                {
                    if(currentUnit == 1){
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
                    else {
                        int len = response.getData().products().fragments().productsFragment().items().size();
                        for(int i=0;i<len; i++){

                            Picasso.get().load(response.getData().products().fragments().productsFragment().items().get(i).small_image().url());
                        }
                    }

                    if(currentUnit<= pageCount.getValue()){
                        currentUnit++;
                        getAllCatalog(currentUnit,pageSize,category);
                        if(currentUnit==pageCount.getValue()){
                            currentUnit++;
                            isTransactionComplete.postValue(true);
                        }
                    }
                    else{

                    }

                    Log.d("count", String.valueOf(currentUnit));


                }


            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                message = e.getMessage();
                CatalogRequestResponse.postValue(e.getLocalizedMessage());

            }
        });
    }


    public void getCatalog(int currentPage, int pageSize, String category){


        ArrayList<String> category_id = new ArrayList<>();
        category_id.add(category);
        Input<List<String>> in = new Input<List<String>>(category_id, true);
        Input<FilterEqualTypeInput> ab = new Input<>(FilterEqualTypeInput.builder().inInput(in).build(),true);

        SortEnum sortOrder = SortEnum.safeValueOf("ASC");
        Input<SortEnum> sortOrderInput = new Input<>(sortOrder,true);
        Input<ProductAttributeSortInput> sortOption = new Input<>(ProductAttributeSortInput.builder().positionInput(sortOrderInput).build(),true);

        GetProductsQuery a = new GetProductsQuery(pageSize,currentPage,
                ProductAttributeFilterInput.builder().category_id(FilterEqualTypeInput.builder().eq(category).build()).build()
                , sortOption);

        apolloClientClass.apolloClient.query(a).toBuilder().responseFetcher(ApolloResponseFetchers.CACHE_FIRST).build().watcher().enqueueAndWatch(new ApolloCall.Callback<GetProductsQuery.Data>() {
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

//        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
//        requestHeader.addHeader("store","ur");

        apolloClientClass.apolloClient.query(new GetMegaMenuQuery()).toBuilder().requestHeaders(apolloClientClass.getRequestHeader()).build().enqueue(new ApolloCall.Callback<GetMegaMenuQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetMegaMenuQuery.Data> response) {
                String ab ="";
                if(response.hasErrors())
                {
                    if(response.getErrors().get(0)!=null)
                        CatalogRequestResponse.postValue(response.getErrors().get(0).getMessage());
                }
                else
                {
                    categoryLists.postValue(response.getData().categoryList());
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                String ab ="";
                CatalogRequestResponse.postValue(e.getMessage());
            }
        });
    }



    public void getAllItemsForCache(){

        ArrayList<String> category_id = new ArrayList<>();
        category_id.add("2");
        Input<List<String>> in = new Input<List<String>>(category_id, true);
        Input<FilterEqualTypeInput> ab = new Input<>(FilterEqualTypeInput.builder().inInput(in).build(),true);

        SortEnum sortOrder = SortEnum.safeValueOf("ASC");
        Input<SortEnum> sortOrderInput = new Input<>(sortOrder,true);
        Input<ProductAttributeSortInput> sortOption = new Input<>(ProductAttributeSortInput.builder().positionInput(sortOrderInput).build(),true);

        GetAllItemsQuery a = new GetAllItemsQuery(
                ProductAttributeFilterInput.builder().category_id(FilterEqualTypeInput.builder().eq("2").build()).build()
                , sortOption);

        apolloClientClass.apolloClient.query(a).toBuilder().
                responseFetcher(ApolloResponseFetchers.CACHE_AND_NETWORK).build().watcher().
                enqueueAndWatch(new ApolloCall.Callback<GetAllItemsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetAllItemsQuery.Data> response) {
                String ab ="";
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {

            }
        });

    }
    public MutableLiveData<List<ProductsFragment.Item>> getProductsFragment(){
        return productsFragment;
    }
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

    public MutableLiveData<List<GetMegaMenuQuery.CategoryList>> getCategoryList(){
        return categoryLists;
    }

    public MutableLiveData<Boolean> getIsTransactionComplete() {
        return isTransactionComplete;
    }
}
