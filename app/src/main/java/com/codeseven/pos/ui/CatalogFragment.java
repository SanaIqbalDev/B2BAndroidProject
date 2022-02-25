package com.codeseven.pos.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.codeseven.pos.R;
import com.codeseven.pos.api.CatalogRepository;
import com.codeseven.pos.databinding.FragmentCatalogBinding;
import com.codeseven.pos.model.CatalogItem;
import com.codeseven.pos.model.CatalogItemAdapter;
import com.codeseven.pos.model.NavMenuItem;
import com.codeseven.pos.model.NavigationDrawerAdapter;
import com.codeseven.pos.util.CartPreference;
import com.codeseven.pos.util.CartViewModel;
import com.codeseven.pos.util.CatalogViewModel;
import com.codeseven.pos.util.GetProductByNameViewModel;
import com.codeseven.pos.util.ItemClickListener;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import apollo.pos.GetAutocompleteResultsQuery;
import apollo.pos.GetMegaMenuQuery;
import apollo.pos.fragment.ProductsFragment;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CatalogFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {



    CatalogViewModel catalogViewModel;
    @Inject
    CatalogViewModel.CatalogObserver catalogObserver;
    int currentPage = 1, pageSize = 12, totalPages = 0;
    String selected_category = "2";
    public boolean getMoreProducts = false;
    public boolean updateProducts = false;


    CartViewModel cartViewModel;
    @Inject CartViewModel.CartObserver cartObserver;
    CartPreference cartPreference;
    public String customerCartId;
    boolean dataFound=false;


    ProgressDialog progressDialog;
    CatalogItemAdapter catalogItemAdapter;

    // Expandable ListView...
    NavigationDrawerAdapter expandableListAdapter;
    List<NavMenuItem> headerList = new ArrayList<>();
    HashMap<NavMenuItem, List<NavMenuItem>> childList = new HashMap<>();
    FragmentCatalogBinding fragmentCatalogBinding;

    // Search Implementation...
    GetProductByNameViewModel getProductByNameViewModel;
    @Inject GetProductByNameViewModel.GetProductsByNameObserver getProductsByNameObserver;
    List<GetAutocompleteResultsQuery.Item> itemList = new ArrayList<>();
    Integer search_pages_count = 0;
    Integer search_current_page = 1;
    boolean isCategoryMenu = true;
    String user_query_text = "";

    String item_sku = "abcdef";
    private boolean shouldMakeCall = true;


    @Inject
    public CatalogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog= new ProgressDialog(requireActivity());
        catalogViewModel = new ViewModelProvider(this).get(CatalogViewModel.class);

        getMoreProducts = true;

        // Getting customer catalog...
        progressDialog.StartLoadingdialog();
        catalogObserver.getUpdatedcatalog(currentPage,pageSize,selected_category);
        catalogObserver.GetCategoryList();
        totalPages= catalogObserver.getPageCount().getValue();


        //Getting customer cart...
        cartPreference = new CartPreference();
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        customerCartId = cartPreference.GetCartId("cart_id");
        if(customerCartId.equals(""))
            cartObserver.getCartId();


        //Search implemenattion...
        getProductByNameViewModel = (new ViewModelProvider(requireActivity())).get(GetProductByNameViewModel.class);

//        BaseActivity.setSupportActionBar(toolbar)
        setHasOptionsMenu(true);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

    }
    ArrayList<CatalogItem> catalogItemArrayList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentCatalogBinding= DataBindingUtil.inflate(inflater,R.layout.fragment_catalog,container,false);
        View view = fragmentCatalogBinding.getRoot();
        fragmentCatalogBinding.topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCategoryMenu = true;
                fragmentCatalogBinding.drawerLayout.open();

            }
        });
        fragmentCatalogBinding.topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(item.getTitle().equals("ViewCart"))
                {
                    NavHostFragment.findNavController(CatalogFragment.this).navigate(R.id.action_homeFragment_to_cartFragment);
                }
                if(item.getTitle().equals("recordAudio"))
                {
                    NavHostFragment.findNavController(CatalogFragment.this).navigate(R.id.action_homeFragment_to_audioRecordingFragment);
                }
                if(item.getTitle().equals("Search")){
                    // Associate searchable configuration with the SearchView
                    search_current_page = 1;
                    InputMethodManager inputManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

                    if(requireActivity().getCurrentFocus() != null) {
                        inputManager.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), 0);
                    }
//                                        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    SearchManager searchManager = (SearchManager) requireContext().getSystemService(Context.SEARCH_SERVICE);
                    SearchView searchView = (SearchView) fragmentCatalogBinding.topAppBar.getMenu().findItem(R.id.search).getActionView();
                    searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));

                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String s) {

                            progressDialog.StartLoadingdialog();
                            updateProducts = true;
                            user_query_text = s;
                            search_current_page = 1;
                            selected_category = "";
                            SearchApiCall(s);

                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String s) {
                            return false;
                        }
                    });

                }
                return false;
            }
        });


        fragmentCatalogBinding.setViewModel(catalogObserver);
        fragmentCatalogBinding.setLifecycleOwner(requireActivity());


        //Prepare NavigationDrawerMenu...

        catalogItemAdapter= new CatalogItemAdapter(requireContext(), catalogItemArrayList, new ItemClickListener() {
            @Override
            public void onItemClicked(CatalogItem groceryItem) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("catalogItem", groceryItem);
                NavHostFragment.findNavController(CatalogFragment.this).navigate(R.id.action_homeFragment_to_productDetailFragment,bundle);

            }
        });
        LinearLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(),2);
        fragmentCatalogBinding.recyclerviewGroceryItems.setLayoutManager(gridLayoutManager);
        fragmentCatalogBinding.recyclerviewGroceryItems.setAdapter(catalogItemAdapter);



        catalogObserver.getProductFragments().observe(getViewLifecycleOwner(), new Observer<List<ProductsFragment.Item>>() {
            @Override
            public void onChanged(List<ProductsFragment.Item> items) {

                String name, price, image_url, description, itemsku;
                if(updateProducts){
                    catalogItemArrayList = new ArrayList<>();
                    catalogItemAdapter.notifyDataSetChanged();
                }
                if(getMoreProducts == true || updateProducts) {
                    progressDialog.dismissDialog();
                    fragmentCatalogBinding.btnRefresh.setVisibility(View.GONE);
                    fragmentCatalogBinding.loadingProgressbar.setVisibility(View.GONE);
                    dataFound = true;
                    if(items.size()<1){
                        fragmentCatalogBinding.tvTitle.setVisibility(View.GONE);
                        fragmentCatalogBinding.tvNoItemsFound.setVisibility(View.VISIBLE);
                        fragmentCatalogBinding.nestedScrollView.setVisibility(View.GONE);
                    }
                    else {
                        fragmentCatalogBinding.tvTitle.setVisibility(View.VISIBLE);

                        shouldMakeCall = true;
                        fragmentCatalogBinding.tvNoItemsFound.setVisibility(View.GONE);
                        fragmentCatalogBinding.nestedScrollView.setVisibility(View.VISIBLE);

                        for (int i = 0; i < items.size(); i++) {
                            itemsku = (items.get(i).sku());
                            name = (items.get(i).name());
                            price = ( String.valueOf(items.get(i).price().regularPrice().amount().value().intValue())+" "+requireContext().getResources().getString(R.string.pkr));
                            image_url = items.get(i).small_image().url();
                            description = items.get(i).description().html();
                            catalogItemArrayList.add(new CatalogItem(itemsku, name, price, image_url, description));
                        }
                    }
                        if (updateProducts) {
                            catalogItemAdapter = new CatalogItemAdapter(requireContext(), catalogItemArrayList, new ItemClickListener() {
                                @Override
                                public void onItemClicked(CatalogItem groceryItem) {
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable("catalogItem", groceryItem);
                                    NavHostFragment.findNavController(CatalogFragment.this).navigate(R.id.action_homeFragment_to_productDetailFragment, bundle);

                                }
                            });
                            LinearLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
                            fragmentCatalogBinding.recyclerviewGroceryItems.setLayoutManager(gridLayoutManager);
                            fragmentCatalogBinding.recyclerviewGroceryItems.setAdapter(catalogItemAdapter);
                        } else
                            catalogItemAdapter.notifyDataSetChanged();
                        getMoreProducts = false;
                        updateProducts = false;

                }

            }
        });

        fragmentCatalogBinding.nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    // in this method we are incrementing page number,
                    // making progress bar visible and calling get data method.
                     if(isCategoryMenu) {
                         if(shouldMakeCall) {
                             totalPages = catalogObserver.getPageCount().getValue();
                             currentPage++;
                             if (currentPage <= totalPages) {
                                 fragmentCatalogBinding.loadingProgressbar.setVisibility(View.VISIBLE);
                                 getMoreProducts = true;
                                 catalogObserver.getUpdatedcatalog(currentPage, pageSize, selected_category);
                                 shouldMakeCall = false;
                             } else {
                                 progressDialog.dismissDialog();
                                 fragmentCatalogBinding.loadingProgressbar.setVisibility(View.GONE);
                                 Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.no_more_data_available), Toast.LENGTH_LONG).show();
                             }
                         }
                    }
                    else {
                        if(shouldMakeCall) {
                            search_current_page++;
                            if (search_current_page <= search_pages_count) {
                                fragmentCatalogBinding.loadingProgressbar.setVisibility(View.VISIBLE);
                                getMoreProducts = true;
                                SearchApiCall(user_query_text);
                                shouldMakeCall = false;

                            } else {
                                progressDialog.dismissDialog();
                                fragmentCatalogBinding.loadingProgressbar.setVisibility(View.GONE);
                                Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.no_more_data_available), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            }
        });

        fragmentCatalogBinding.btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.StartLoadingdialog();

                catalogObserver.getUpdatedcatalog(currentPage,pageSize, selected_category);

//                headerList = new ArrayList<>();
//                childList = new HashMap<>();
//
                catalogObserver.GetCategoryList();

            }
        });

        catalogObserver.getCategoryLists().observe(getViewLifecycleOwner(), new Observer<List<GetMegaMenuQuery.CategoryList>>() {
            @Override
            public void onChanged(List<GetMegaMenuQuery.CategoryList> categoryLists) {

                prepareMenuData(categoryLists);
                populateExpandableList();




            }
        });

        setHasOptionsMenu(true);

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getProductsByNameObserver.getItemsList().observe(getViewLifecycleOwner(), new Observer<List<GetAutocompleteResultsQuery.Item>>() {
            @Override
            public void onChanged(List<GetAutocompleteResultsQuery.Item> items) {
                Log.d("TEST", "[onChanged]: ");

                if(getViewLifecycleOwner().getLifecycle().getCurrentState()== Lifecycle.State.RESUMED){
                    Log.d("TEST", "  resumed");

                    progressDialog.dismissDialog();


                    String name, price, image_url, description, itemsku;
                    if (updateProducts) {
                        catalogItemArrayList = new ArrayList<>();
                        catalogItemAdapter.notifyDataSetChanged();
                        fragmentCatalogBinding.nestedScrollView.scrollTo(0,0);

                    }
                    if (getMoreProducts == true || updateProducts) {
                        if(items.size()>0) {
                            fragmentCatalogBinding.tvTitle.setVisibility(View.VISIBLE);

                            if (!items.get(0).sku().equals(item_sku)) {
                                if(search_current_page == 1)
                                    item_sku = items.get(0).sku();
                                fragmentCatalogBinding.btnRefresh.setVisibility(View.GONE);
                                fragmentCatalogBinding.loadingProgressbar.setVisibility(View.GONE);
                                dataFound = true;
                                    shouldMakeCall = true;

                                    fragmentCatalogBinding.tvNoItemsFound.setVisibility(View.GONE);
                                    fragmentCatalogBinding.nestedScrollView.setVisibility(View.VISIBLE);

                                    for (int i = 0; i < items.size(); i++) {
                                        itemsku = (items.get(i).sku());
                                        name = (items.get(i).name());
                                        price = (String.valueOf(items.get(i).price().regularPrice().amount().value().intValue()) +" "+ requireContext().getResources().getString(R.string.pkr));
                                        image_url = items.get(i).small_image().url();
                                        description = items.get(i).description().html();
                                        catalogItemArrayList.add(new CatalogItem(itemsku, name, price, image_url, description));
                                    }

                                    catalogItemAdapter = new CatalogItemAdapter(requireContext(), catalogItemArrayList, new ItemClickListener() {
                                        @Override
                                        public void onItemClicked(CatalogItem groceryItem) {
                                            Bundle bundle = new Bundle();
                                            bundle.putParcelable("catalogItem", groceryItem);
                                            NavHostFragment.findNavController(CatalogFragment.this).navigate(R.id.action_homeFragment_to_productDetailFragment, bundle);

                                        }
                                    });
                                    LinearLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
                                    fragmentCatalogBinding.recyclerviewGroceryItems.setLayoutManager(gridLayoutManager);
                                    fragmentCatalogBinding.recyclerviewGroceryItems.setAdapter(catalogItemAdapter);
                                    updateProducts = false;
                                catalogItemAdapter.notifyDataSetChanged();
                                getMoreProducts = false;
                            }

                        }
                        else {
                            fragmentCatalogBinding.tvNoItemsFound.setVisibility(View.VISIBLE);
                            fragmentCatalogBinding.tvTitle.setVisibility(View.GONE);
                            fragmentCatalogBinding.nestedScrollView.setVisibility(View.GONE);
                            progressDialog.dismissDialog();
                        }
                    }

                }
            }
        });

        getProductsByNameObserver.getResponseSearchProducts().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.length()>0){

                  if(s.contains("Network error") || s.contains("http")){
                    Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.check_internet_connection), Toast.LENGTH_LONG).show();
                  }
                  else
                  {
                      Toast.makeText(requireContext(),s,Toast.LENGTH_LONG).show();
                  }

                }
            }
        });
        getProductsByNameObserver.getPages_count().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                search_pages_count = integer;
            }
        });

        catalogObserver.getCatalogRequestResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(getViewLifecycleOwner().getLifecycle().getCurrentState()== Lifecycle.State.RESUMED){
                    {
                        if (s.length() > 0) {

                            progressDialog.dismissDialog();
                            fragmentCatalogBinding.btnRefresh.setVisibility(View.VISIBLE);
                            fragmentCatalogBinding.loadingProgressbar.setVisibility(View.GONE);

                            if(s.contains("Network error") || s.contains("http"))
                            {
                                Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.check_internet_connection), Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                Toast.makeText(requireContext(), catalogObserver.getCatalogRequestResponse().getValue(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }}
        });

    }

    private void SearchApiCall(String queryText) {

        isCategoryMenu = false;

//        progressDialog.StartLoadingdialog();

        getProductsByNameObserver.getProductsByName(queryText,search_current_page,12);

//        updateProducts = true;


//        getProductsByNameObserver.getItemsList().observe(getViewLifecycleOwner(), new Observer<List<GetAutocompleteResultsQuery.Item>>() {
//            @Override
//            public void onChanged(List<GetAutocompleteResultsQuery.Item> items) {
//                Log.d("TEST", "[onChanged]: ");
//
//                if(getViewLifecycleOwner().getLifecycle().getCurrentState()== Lifecycle.State.RESUMED){
//                    Log.d("TEST", "  resumed");
//
//                    progressDialog.dismissDialog();
//
//
//                    String name, price, image_url, description, itemsku;
//                if (updateProducts) {
//                    catalogItemArrayList = new ArrayList<>();
//                    catalogItemAdapter.notifyDataSetChanged();
//                    fragmentCatalogBinding.nestedScrollView.scrollTo(0,0);
//
//                }
//                if (getMoreProducts == true || updateProducts) {
//                    if(items.size()>0) {
//                        if (!items.get(0).sku().equals(item_sku)) {
//                            if(search_current_page == 1)
//                                item_sku = items.get(0).sku();
//                            fragmentCatalogBinding.btnRefresh.setVisibility(View.GONE);
//                            fragmentCatalogBinding.loadingProgressbar.setVisibility(View.GONE);
//                            dataFound = true;
//                            if (items.size() < 1) {
//                                fragmentCatalogBinding.tvNoItemsFound.setVisibility(View.VISIBLE);
//                                fragmentCatalogBinding.nestedScrollView.setVisibility(View.GONE);
////                                progressDialog.dismissDialog();
//                            } else {
//                                fragmentCatalogBinding.tvNoItemsFound.setVisibility(View.GONE);
//                                fragmentCatalogBinding.nestedScrollView.setVisibility(View.VISIBLE);
//
//                                for (int i = 0; i < items.size(); i++) {
//                                    itemsku = (items.get(i).sku());
//                                    name = (items.get(i).name());
//                                    price = ("PKR " + String.valueOf(items.get(i).price().regularPrice().amount().value().intValue()));
//                                    image_url = items.get(i).small_image().url();
//                                    description = items.get(i).description().html();
//                                    catalogItemArrayList.add(new CatalogItem(itemsku, name, price, image_url, description));
//                                }
//                            }
//                            if (items.size() > 0) {
//                                catalogItemAdapter = new CatalogItemAdapter(requireContext(), catalogItemArrayList, new ItemClickListener() {
//                                    @Override
//                                    public void onItemClicked(CatalogItem groceryItem) {
//                                        Bundle bundle = new Bundle();
//                                        bundle.putParcelable("catalogItem", groceryItem);
//                                        NavHostFragment.findNavController(CatalogFragment.this).navigate(R.id.action_homeFragment_to_productDetailFragment, bundle);
//
//                                    }
//                                });
//                                LinearLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
//                                fragmentCatalogBinding.recyclerviewGroceryItems.setLayoutManager(gridLayoutManager);
//                                fragmentCatalogBinding.recyclerviewGroceryItems.setAdapter(catalogItemAdapter);
//                                updateProducts = false;
////                                progressDialog.dismissDialog();
//                            }
////                            progressDialog.dismissDialog();
//                            catalogItemAdapter.notifyDataSetChanged();
//                            getMoreProducts = false;
//                        }
//
//                    }
//                }
//
//            }
//        }
//        });
//
//        getProductsByNameObserver.getPages_count().observe(getViewLifecycleOwner(), new Observer<Integer>() {
//            @Override
//            public void onChanged(Integer integer) {
//                search_pages_count = integer;
//            }
//        });
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }


    private void prepareMenuData(List<GetMegaMenuQuery.CategoryList> categoryList) {

        headerList = new ArrayList<>();
        childList  = new HashMap<>();
        String menu_name,category_id;
        boolean has_children,is_group;
        NavMenuItem menuModel = new NavMenuItem("تمام مصنوعات","2",false,false,""); //Menu of Android Tutorial. No sub menus
        headerList.add(menuModel);
        for(int i=0;i<categoryList.get(0).children().size();i++){
            GetMegaMenuQuery.Child child = categoryList.get(0).children().get(i);
            menu_name = child.name();
            int children_size = child.children().size();
            has_children = children_size > 0;
            category_id = String.valueOf(child.id());
            is_group = false;
            List<NavMenuItem> childModelsList = new ArrayList<>();

            menuModel = new NavMenuItem(menu_name,category_id,has_children,is_group, "https://mcstaging.24seven.pk"+child.thumbnail());
            if(child.include_in_menu()==1) {
                headerList.add(menuModel);

                if (menuModel.hasChildren) {
                    for (int x = 0; x < children_size; x++) {

                        GetMegaMenuQuery.Child1 a = child.children().get(x);
                        menu_name = a.name();
                        category_id = String.valueOf(a.id());
                        has_children = (a.children().size()) > 0;
                        is_group = has_children;


                        NavMenuItem childModel = new NavMenuItem(menu_name, category_id, has_children, is_group, "https://mcstaging.24seven.pk"+child.thumbnail());
                        if(a.include_in_menu() == 1)
                        {
                            childModelsList.add(childModel);
                        }
                    }
                    childList.put(menuModel, childModelsList);

                } else {
                    childList.put(menuModel, null);
                }

            }

        }
    }

    private void populateExpandableList() {

        expandableListAdapter = new NavigationDrawerAdapter(requireContext(), headerList, childList);
        fragmentCatalogBinding.expandableListView.setAdapter(expandableListAdapter);

        fragmentCatalogBinding.expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

//                if (headerList.get(groupPosition).isGroup) {
//                    if (!headerList.get(groupPosition).hasChildren) {
//                    }
                if(selected_category == headerList.get(groupPosition).category_id)
                {
                    fragmentCatalogBinding.drawerLayout.close();
                }
                else {
                    selected_category = headerList.get(groupPosition).category_id;
                    currentPage = 1;
//                    Toast.makeText(requireContext(), "Group clicked", Toast.LENGTH_LONG).show();
                    if (!headerList.get(groupPosition).hasChildren) {
                        fragmentCatalogBinding.drawerLayout.close();
                        updateProducts = true;
                        catalogItemArrayList = new ArrayList<>();
                        catalogItemAdapter.notifyDataSetChanged();
                        progressDialog.StartLoadingdialog();
                        fragmentCatalogBinding.nestedScrollView.scrollTo(0, 0);
                        catalogObserver.getUpdatedcatalog(currentPage, pageSize, selected_category);

                    }

                }
//                }

                return false;
            }
        });

        fragmentCatalogBinding.expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (childList.get(headerList.get(groupPosition)) != null) {
                    NavMenuItem model = childList.get(headerList.get(groupPosition)).get(childPosition);
                    if(selected_category == childList.get(headerList.get(groupPosition)).get(childPosition).getCategory_id())
                    {
                        fragmentCatalogBinding.drawerLayout.close();
                    }

                    else {
                    selected_category = childList.get(headerList.get(groupPosition)).get(childPosition).getCategory_id();


                    currentPage = 1;
                    progressDialog.StartLoadingdialog();
//                    Toast.makeText(requireContext(), "Child clicked", Toast.LENGTH_LONG).show();
                    fragmentCatalogBinding.drawerLayout.close();
                    updateProducts = true;
                    catalogItemArrayList = new ArrayList<>();
                    catalogItemAdapter.notifyDataSetChanged();
                    fragmentCatalogBinding.nestedScrollView.scrollTo(0,0);
                    catalogObserver.getUpdatedcatalog(currentPage,pageSize, selected_category);



                }
                }

                return false;
            }
        });


    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu_catalog, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) requireContext().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(requireActivity().getComponentName()));

    }



}