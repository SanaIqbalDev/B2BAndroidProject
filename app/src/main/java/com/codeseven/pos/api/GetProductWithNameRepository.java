package com.codeseven.pos.api;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.codeseven.pos.ApolloClientClass;

import java.util.ArrayList;
import java.util.List;

import apollo.pos.GetAutocompleteResultsQuery;

public class GetProductWithNameRepository {

    private MutableLiveData<List<GetAutocompleteResultsQuery.Item>> itemsList = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Integer> total_pages;
    private MutableLiveData<String> responseThis ;
    private ApolloClientClass apolloClientClass;

    public GetProductWithNameRepository() {
        itemsList = new MutableLiveData<>(new ArrayList<>());
        responseThis = new MutableLiveData<>("");
        total_pages = new MutableLiveData<>(0);
        apolloClientClass = new ApolloClientClass();
    }

    public void getProducts(String itemName, int currentPage, int pageSize)
    {
        apolloClientClass.apolloClient.query(new GetAutocompleteResultsQuery(itemName,currentPage,pageSize)).enqueue(new ApolloCall.Callback<GetAutocompleteResultsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetAutocompleteResultsQuery.Data> response) {
                if(response.hasErrors()) {
                    responseThis.postValue(response.getErrors().get(0).getMessage());
                }
                else {
                    itemsList.postValue(response.getData().products().items());
                    total_pages.postValue(response.getData().products().page_info().total_pages());
                }

            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                responseThis.postValue(e.getLocalizedMessage());
            }
        });

    }

    public MutableLiveData<List<GetAutocompleteResultsQuery.Item>> GetItems()
    {
        return  itemsList;
    }

    public MutableLiveData<Integer> GetPagesCount(){
        return total_pages;
    }

    public MutableLiveData<String> getResponseThis(){
        return responseThis;
    }
}
