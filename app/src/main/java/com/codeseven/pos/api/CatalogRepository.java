package com.codeseven.pos.api;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.request.RequestHeaders;
import com.codeseven.pos.ApolloClientClass;

import java.util.ArrayList;
import java.util.List;

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


    public CatalogRepository() {
        CatalogRequestResponse = new MutableLiveData<>("");
        productsFragment = new MutableLiveData<>();
        pageCount = new MutableLiveData<>(0);
        categoryLists = new MutableLiveData<>();

    }

    public void getCatalog(int currentPage, int pageSize, String category){

        ArrayList<String> category_id = new ArrayList<>();
        category_id.add(category);
        Input<List<String>> in = new Input<List<String>>(category_id, true);
        Input<FilterEqualTypeInput> ab = new Input<>(FilterEqualTypeInput.builder().inInput(in).build(),true);

        SortEnum sortOrder = SortEnum.safeValueOf("ASC");
        Input<SortEnum> sortOrderInput = new Input<>(sortOrder,true);
        Input<ProductAttributeSortInput> sortOption = new Input<>(ProductAttributeSortInput.builder().positionInput(sortOrderInput).build(),true);


        (new ApolloClientClass()).apolloClient.query(new GetProductsQuery(pageSize,currentPage,
                ProductAttributeFilterInput.builder().category_id(FilterEqualTypeInput.builder().eq(category).build()).build()
                , sortOption)).enqueue(new ApolloCall.Callback<GetProductsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetProductsQuery.Data> response) {
                if(response.getErrors()!=null) {
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
        requestHeader.addHeader("store","ur");

        (new ApolloClientClass()).apolloClient.query(new GetMegaMenuQuery()).toBuilder().requestHeaders(requestHeader.build()).build().enqueue(new ApolloCall.Callback<GetMegaMenuQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetMegaMenuQuery.Data> response) {
                String ab ="";
                categoryLists.postValue(response.getData().categoryList());
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                String ab ="";
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

}
