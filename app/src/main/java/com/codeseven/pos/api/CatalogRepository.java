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
    private MutableLiveData<Boolean> isTransactionComplete;
    private MutableLiveData<Boolean> isDefCatCacheComplete;
    private MutableLiveData<Integer> isCurrentCategoryComplete;

    int currentUnit = 0;
    private ArrayList<String> allCategories = new ArrayList<>();
    private int allCategoriesIndexCurrent = 0;
    private int currentCategoryPageCount = 1;
    private int currentCategoryCurrentPage = 1;

    private LoginPreference loginPreference;

    public CatalogRepository() {
        CatalogRequestResponse = new MutableLiveData<>("");
        isTransactionComplete = new MutableLiveData<>();
        isTransactionComplete.postValue(false);

        isDefCatCacheComplete = new MutableLiveData<>();
        isDefCatCacheComplete.postValue(false);

        isCurrentCategoryComplete = new MutableLiveData<>();
        isCurrentCategoryComplete.postValue(0);

        productsFragment = new MutableLiveData<>();
        pageCount = new MutableLiveData<>(0);
        categoryLists = new MutableLiveData<>();
        apolloClientClass = new ApolloClientClass(true);

        loginPreference = new LoginPreference();
    }

    public void CacheAllCatalog(int currentPage, int pageSize, String category){


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
                        if(!response.getErrors().get(0).getMessage().contains("specified is greater than the"))
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

                                Picasso.get().
                                        load(response.getData().products().fragments().productsFragment().
                                                items().get(i).small_image().url())
                                    .fetch();

                        }
                    }

                    if(loginPreference.GetFirstTimePreference()) {
                        Log.d("cache All Category ", String.valueOf(currentUnit));
                        if (currentUnit <= pageCount.getValue()) {
                            currentUnit++;
                            CacheAllCatalog(currentUnit, pageSize, category);
                            if(currentUnit == 200){
                                String ab = "";
                            }
                            if (pageCount.getValue().equals(currentUnit)) {
                                Log.d("cache All complete", String.valueOf(currentUnit));

                                isDefCatCacheComplete.postValue(true);
//                            apolloClientClass.apolloClient.getApolloStore();
//                                isTransactionComplete.postValue(true);
                            }
                        }
                    }
                    else {
                        isDefCatCacheComplete.postValue(true);
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

    public void CacheAllCategoriesData(ArrayList<String> categories) {

        allCategories = categories;

        ArrayList<String> category = new ArrayList<>();
        if (allCategoriesIndexCurrent < allCategories.size())
            category.add(allCategories.get(allCategoriesIndexCurrent));


        SortEnum sortOrder = SortEnum.safeValueOf("ASC");
        Input<SortEnum> sortOrderInput = new Input<>(sortOrder, true);
        Input<ProductAttributeSortInput> sortOption = new Input<>(ProductAttributeSortInput.builder().positionInput(sortOrderInput).build(), true);

        GetProductsQuery a = new GetProductsQuery(12, currentCategoryCurrentPage,
                ProductAttributeFilterInput.builder().category_id(FilterEqualTypeInput.builder().eq(String.valueOf(allCategories.get(allCategoriesIndexCurrent))).build()).build()
                , sortOption);

            apolloClientClass.apolloClient.query(a).toBuilder().responseFetcher(ApolloResponseFetchers.CACHE_FIRST).build().watcher().enqueueAndWatch(new ApolloCall.Callback<GetProductsQuery.Data>() {
                @Override
                public void onResponse(@NonNull Response<GetProductsQuery.Data> response) {
                    if (response.hasErrors()) {
                        if (response.getErrors().size() > 0)
                            CatalogRequestResponse.postValue(response.getErrors().get(0).getMessage());
                        Log.d("cache error response", response.getErrors().get(0).getMessage());
                        String res = response.getErrors().get(0).getMessage();
//                    if(res.contains("specified is greater than the")){
//                        currentCategoryCurrentPage = 1;
//                        allCategoriesIndexCurrent++;
//                        CacheAllCategoriesData(allCategories);
//                    }

                    } else {
                        if (currentCategoryCurrentPage == 1) {
                            try {
                                if (response.getData().products().fragments().productsFragment().page_info().total_pages() != null) {

                                    currentCategoryPageCount = response.getData().products().fragments().productsFragment().page_info().total_pages();
                                    Log.d("cache page_count", String.valueOf(currentCategoryPageCount));


                                }
                            } catch (NullPointerException nullPointerException) {
                            }
                        } else {
                            int len = response.getData().products().fragments().productsFragment().items().size();
                            for (int i = 0; i < len; i++) {

//                                Picasso.get().load(response.getData().products().fragments().productsFragment().items().get(i).small_image().url()).fetch();

                            }
                        }

                        if (currentCategoryCurrentPage < currentCategoryPageCount) {
                            currentCategoryCurrentPage++;
                        }
                        if (currentCategoryCurrentPage >= currentCategoryPageCount) {

                            Log.d("cache category", String.valueOf(allCategories.get(allCategoriesIndexCurrent)));
                            Log.d("cache pages", String.valueOf(currentCategoryCurrentPage));

                            currentCategoryCurrentPage = 1;
                            allCategoriesIndexCurrent++;
                        }


                        CacheAllCategoriesData(allCategories);

                    }
                }

                @Override
                public void onFailure(@NonNull ApolloException e) {
                    message = e.getMessage();
                    CatalogRequestResponse.postValue(e.getLocalizedMessage());

                }
            });

    }

    public void CacheThisCategoryData(String category, int index) {

        SortEnum sortOrder = SortEnum.safeValueOf("ASC");
        Input<SortEnum> sortOrderInput = new Input<>(sortOrder, true);
        Input<ProductAttributeSortInput> sortOption = new Input<>(ProductAttributeSortInput.builder().positionInput(sortOrderInput).build(), true);

        if (currentCategoryCurrentPage <= currentCategoryPageCount) {
            GetProductsQuery a = new GetProductsQuery(12, index,
                    ProductAttributeFilterInput.builder().category_id(FilterEqualTypeInput.builder().eq(category).build()).build()
                    , sortOption);
            Log.d("cache", category+"     "+index);

            apolloClientClass.apolloClient.query(a).toBuilder().responseFetcher(ApolloResponseFetchers.CACHE_FIRST).build().watcher().enqueueAndWatch(new ApolloCall.Callback<GetProductsQuery.Data>() {
                @Override
                public void onResponse(@NonNull Response<GetProductsQuery.Data> response) {
                    Log.d("response", category+"     "+index);

                    if (response.hasErrors()) {
                        if (response.getErrors().size() > 0)
                            CatalogRequestResponse.postValue(response.getErrors().get(0).getMessage());
                    } else {

//                        Log.d("cache", category+"     "+index);
                        int len = response.getData().products().fragments().productsFragment().items().size();
                        for (int i = 0; i < len; i++) {

//                            Picasso.get().load(response.getData().products().fragments().productsFragment().items().get(i).small_image().url()).fetch();

                        }
                        if(loginPreference.GetCategoryLastItem().equals(category)  &&  loginPreference.GetLastpageItemCount()==index){

                            isTransactionComplete.postValue(true);
                        }


//                        if (currentCategoryCurrentPage == 1) {
//                            try {
//                                if (response.getData().products().fragments().productsFragment().page_info().total_pages() != null) {
//                                    currentCategoryPageCount = (response.getData().products().fragments().productsFragment().page_info().total_pages());
//                                }
////                                int len = response.getData().products().fragments().productsFragment().items().size();
////                                for (int i = 0; i < len; i++) {
////
////                                    Picasso.get().load(response.getData().products().fragments().productsFragment().items().get(i).small_image().url()).fetch();
////
////                                }
//                            } catch (NullPointerException nullPointerException) {
//                            }
//                        }

//                        if (currentCategoryCurrentPage < currentCategoryPageCount) {
//                            currentCategoryCurrentPage++;
//                            CacheThisCategoryData(category, index);
//
//                        }
//                        if (currentCategoryCurrentPage >= currentCategoryPageCount) {
//
//                            Log.d("cache pages", String.valueOf(currentCategoryPageCount));
//                            currentCategoryCurrentPage = 1;
//
//                            isCurrentCategoryComplete.postValue(index);
//                        }
//
//
//

                    }
                }

                @Override
                public void onFailure(@NonNull ApolloException e) {
                    message = e.getMessage();
                    CatalogRequestResponse.postValue(e.getLocalizedMessage());

                }
            });
        }
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
                    for(int a= 1; a<=pageCount ; a++){
                        CacheThisCategoryData(category, (a));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {

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

        RequestHeaders.Builder requestHeader = RequestHeaders.builder();
//        requestHeader.addHeader("authorization","bearer "+loginPreference.GetLoginPreference("token"));
        requestHeader.addHeader("store","ur");

        apolloClientClass.apolloClient.query(new GetMegaMenuQuery()).toBuilder().requestHeaders(RequestHeaders.builder().build()).build().enqueue(new ApolloCall.Callback<GetMegaMenuQuery.Data>() {
            @Override
            public void onResponse(@NonNull Response<GetMegaMenuQuery.Data> response) {
                String ab ="";
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
                String ab ="";
                Log.d("cache","Category List error" + e.getMessage());
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
                responseFetcher(ApolloResponseFetchers.CACHE_FIRST).build().watcher().
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

    public MutableLiveData<Integer> getIsCurrentCategoryComplete() {
        return isCurrentCategoryComplete;
    }

    public MutableLiveData<Boolean> getIsDefCatCacheComplete() {
        return isDefCatCacheComplete;
    }
}
