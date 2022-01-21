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
    private MutableLiveData<String> responseThis ;


    public GetProductWithNameRepository() {
        itemsList = new MutableLiveData<>(new ArrayList<>());
        responseThis = new MutableLiveData<>("");
    }

    public void getProducts(String itemName)
    {
        (new ApolloClientClass()).apolloClient.query(new GetAutocompleteResultsQuery(itemName)).enqueue(new ApolloCall.Callback<GetAutocompleteResultsQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetAutocompleteResultsQuery.Data> response) {
                String ab ="";
                if(response.getErrors()!=null) {
                    if (response.getErrors().size() > 0) {
                        responseThis.postValue(response.getErrors().get(0).getMessage());
                    }
                    }
                else
                    itemsList.postValue(response.getData().products().items());

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

}
