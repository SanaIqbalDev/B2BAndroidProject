package com.codeseven.pos.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.codeseven.pos.util.ItemClickListener;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

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


        catalogObserver.getCatalogRequestResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(getViewLifecycleOwner().getLifecycle().getCurrentState()== Lifecycle.State.RESUMED){
                    {
                        if (s.length() > 0) {
                            fragmentCatalogBinding.btnRefresh.setVisibility(View.VISIBLE);
                            progressDialog.dismissDialog();
                            fragmentCatalogBinding.loadingProgressbar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), catalogObserver.getCatalogRequestResponse().getValue(), Toast.LENGTH_SHORT).show();
                        }
                    }
            }}
        });

        catalogObserver.getProductFragments().observe(getViewLifecycleOwner(), new Observer<List<ProductsFragment.Item>>() {
            @Override
            public void onChanged(List<ProductsFragment.Item> items) {

                String name, price, image_url, description, itemsku;
                if(updateProducts){
                    catalogItemArrayList = new ArrayList<>();
                    catalogItemAdapter.notifyDataSetChanged();
//                    updateProducts = false;
//                    getMoreProducts = true;
                }
                if(getMoreProducts == true || updateProducts) {
                    progressDialog.dismissDialog();
                    fragmentCatalogBinding.btnRefresh.setVisibility(View.GONE);
                    fragmentCatalogBinding.loadingProgressbar.setVisibility(View.GONE);
                    dataFound = true;
                    if(items.size()<1){
//                        Toast.makeText(requireContext(), "No data available for this category.", Toast.LENGTH_SHORT).show();
                        fragmentCatalogBinding.tvNoItemsFound.setVisibility(View.VISIBLE);
                        fragmentCatalogBinding.nestedScrollView.setVisibility(View.GONE);
                    }
                    else {
                        fragmentCatalogBinding.tvNoItemsFound.setVisibility(View.GONE);
                        fragmentCatalogBinding.nestedScrollView.setVisibility(View.VISIBLE);

                        for (int i = 0; i < items.size(); i++) {
                            itemsku = (items.get(i).sku());
                            name = (items.get(i).name());
                            price = ("PKR " + String.valueOf(items.get(i).price().regularPrice().amount().value().intValue()));
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
                    totalPages= catalogObserver.getPageCount().getValue();
                    currentPage++;
                    if(currentPage<= totalPages) {
                        fragmentCatalogBinding.loadingProgressbar.setVisibility(View.VISIBLE);
                        getMoreProducts = true;
                        catalogObserver.getUpdatedcatalog(currentPage, pageSize,selected_category);
                    }
                    else
                    {
                        progressDialog.dismissDialog();
                        fragmentCatalogBinding.loadingProgressbar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "No more data available.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        fragmentCatalogBinding.btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.StartLoadingdialog();

                catalogObserver.getUpdatedcatalog(currentPage,pageSize, selected_category);
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


        return view;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }


    private void prepareMenuData(List<GetMegaMenuQuery.CategoryList> categoryList) {

        String menu_name,category_id;
        boolean has_children,is_group;
        NavMenuItem menuModel = new NavMenuItem("All Categories","2",false,false,""); //Menu of Android Tutorial. No sub menus
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
//                    Toast.makeText(requireContext(), "Group clicked", Toast.LENGTH_SHORT).show();
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
//                    Toast.makeText(requireContext(), "Child clicked", Toast.LENGTH_SHORT).show();
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

}