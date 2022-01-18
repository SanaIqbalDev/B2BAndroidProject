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

    MutableLiveData<List<GetAutocompleteResultsQuery.Item>> itemsList = new MutableLiveData<>(new ArrayList<>());
    public GetProductWithNameRepository() {
        itemsList = new MutableLiveData<>(new ArrayList<>());
    }

    public void getProducts(String itemName)
    {
        (new ApolloClientClass()).apolloClient.query(new GetAutocompleteResultsQuery(itemName)).enqueue(new ApolloCall.Callback<GetAutocompleteResultsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetAutocompleteResultsQuery.Data> response) {
                String ab ="";

                itemsList.postValue(response.getData().products().items());

            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                String ab ="";
            }
        });

    }

    public MutableLiveData<List<GetAutocompleteResultsQuery.Item>> GetItems()
    {
        return  itemsList;
    }

}
