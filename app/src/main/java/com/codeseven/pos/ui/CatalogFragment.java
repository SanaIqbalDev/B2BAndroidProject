package com.codeseven.pos.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
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
import com.codeseven.pos.util.CartPreference;
import com.codeseven.pos.util.CartViewModel;
import com.codeseven.pos.util.CatalogViewModel;
import com.codeseven.pos.util.ItemClickListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import apollo.pos.fragment.ProductsFragment;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CatalogFragment extends Fragment {



    CatalogViewModel catalogViewModel;
    @Inject
    CatalogViewModel.CatalogObserver catalogObserver;
    int currentPage = 1, pageSize = 12, totalPages = 0;
    public boolean getMoreProducts = false;


    CartViewModel cartViewModel;
    @Inject CartViewModel.CartObserver cartObserver;
    CartPreference cartPreference;
    public String customerCartId;
    boolean dataFound=false;


    ProgressDialog progressDialog;
    @Inject
    public CatalogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog= new ProgressDialog(requireActivity());
        catalogViewModel = new ViewModelProvider(requireActivity()).get(CatalogViewModel.class);
        getMoreProducts = true;

        // Getting customer catalog...
        progressDialog.StartLoadingdialog();
        catalogObserver.getUpdatedcatalog(currentPage,pageSize);
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

        FragmentCatalogBinding fragmentCatalogBinding= DataBindingUtil.inflate(inflater,R.layout.fragment_catalog,container,false);
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


        CatalogItemAdapter catalogItemAdapter= new CatalogItemAdapter(requireContext(), catalogItemArrayList, new ItemClickListener() {
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
                if(s.length()>0){
                    fragmentCatalogBinding.btnRefresh.setVisibility(View.VISIBLE);
                    progressDialog.dismissDialog();
                    fragmentCatalogBinding.loadingProgressbar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),catalogObserver.getCatalogRequestResponse().getValue() , Toast.LENGTH_SHORT).show();
                }
            }
        });
        cartObserver.getCartRequestResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
//                if(s.equals("New cart Created")){
//                    Toast.makeText(requireContext(),s , Toast.LENGTH_SHORT).show();
//                }
            }
        });

        catalogObserver.getProductFragments().observe(getViewLifecycleOwner(), new Observer<List<ProductsFragment.Item>>() {
            @Override
            public void onChanged(List<ProductsFragment.Item> items) {

                String name, price, image_url, description, itemsku;

                if(getMoreProducts == true) {
                    for (int i = 0; i < items.size(); i++) {
                        progressDialog.dismissDialog();
                        fragmentCatalogBinding.btnRefresh.setVisibility(View.GONE);
                        dataFound = true;
                        itemsku = (items.get(i).sku());
                        name = (items.get(i).name());
                        price = (String.valueOf(items.get(i).price().regularPrice().amount().value().intValue())) + " Rs.";
                        image_url = items.get(i).small_image().url();
                        description = items.get(i).description().html();
                        catalogItemArrayList.add(new CatalogItem(itemsku, name, price, image_url, description));
                    }

                    catalogItemAdapter.notifyDataSetChanged();
                    getMoreProducts = false;

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
                        catalogObserver.getUpdatedcatalog(currentPage, pageSize);
                    }
                    else
                    {
                        Toast.makeText(requireContext(), "No more data available.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        fragmentCatalogBinding.btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.StartLoadingdialog();

                catalogObserver.getUpdatedcatalog(currentPage,pageSize);

            }
        });

//        view.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
//                    if(!dataFound)
//                    {
//                        catalogObserver.getUpdatedcatalog(currentPage,pageSize);
//                    }
//                }
//                return false;
//            }
//        });
        return view;
    }


}