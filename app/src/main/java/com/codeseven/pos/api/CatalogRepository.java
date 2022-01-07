package com.codeseven.pos.api;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.codeseven.pos.ApolloClientClass;

import java.util.ArrayList;
import java.util.List;

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




    public CatalogRepository() {
        CatalogRequestResponse = new MutableLiveData<>("");
        productsFragment = new MutableLiveData<>();
        pageCount = new MutableLiveData<>(0);
    }

    public void getCatalog(int currentPage, int pageSize){

        ArrayList<String> category_id = new ArrayList<>();
        category_id.add("2");
        Input<List<String>> in = new Input<List<String>>(category_id, true);
        Input<FilterEqualTypeInput> ab = new Input<>(FilterEqualTypeInput.builder().inInput(in).build(),true);

        SortEnum sortOrder = SortEnum.safeValueOf("ASC");
        Input<SortEnum> sortOrderInput = new Input<>(sortOrder,true);
        Input<ProductAttributeSortInput> sortOption = new Input<>(ProductAttributeSortInput.builder().positionInput(sortOrderInput).build(),true);


        (new ApolloClientClass()).apolloClient.query(new GetProductsQuery(pageSize,currentPage,
                ProductAttributeFilterInput.builder().category_id(FilterEqualTypeInput.builder().build()).category_idInput(ab).build(),
                sortOption)).enqueue(new ApolloCall.Callback<GetProductsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetProductsQuery.Data> response) {
                CatalogRequestResponse.postValue("Success");
                message = "success";

                if(response.getData().products().fragments().productsFragment().page_info().total_pages()!= null)
                {
                    pageCount.postValue(response.getData().products().fragments().productsFragment().page_info().total_pages());
                }
                productsFragment.postValue(response.getData().products().fragments().productsFragment().items());

            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                message = e.getMessage();
                CatalogRequestResponse.postValue("Failure");

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

}
