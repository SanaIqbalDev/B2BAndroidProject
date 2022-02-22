package com.codeseven.pos.ui;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.FragmentShortListedItemsBinding;
import com.codeseven.pos.model.ProductSelectionAdapter;
import com.codeseven.pos.util.AddToCartViewModel;
import com.codeseven.pos.util.GetProductByNameViewModel;
import com.codeseven.pos.util.ProductSelectionListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import apollo.pos.GetAutocompleteResultsQuery;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShortListedItemsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
public class ShortListedItemsFragment extends Fragment {

    FragmentShortListedItemsBinding fragmentShortListedItemsBinding;
    GetProductByNameViewModel getProductByNameViewModel;
    @Inject GetProductByNameViewModel.GetProductsByNameObserver getProductsByNameObserver;
    ProductSelectionAdapter adapter;

    List<GetAutocompleteResultsQuery.Item> itemList = new ArrayList<>();

    ProgressDialog progressDialog;
    private String itemName;


    public AddToCartViewModel addToCartViewModel;

    @Inject
    public AddToCartViewModel.AddToCartObsrever addToCartObsrever;



    public ShortListedItemsFragment() {
        // Required empty public constructor
    }

    public static ShortListedItemsFragment newInstance() {
        ShortListedItemsFragment fragment = new ShortListedItemsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        itemName = bundle.getString("itemName");
        getProductByNameViewModel = (new ViewModelProvider(requireActivity())).get(GetProductByNameViewModel.class);


        if(addToCartViewModel ==null){
            addToCartViewModel = new ViewModelProvider(requireActivity()).get(AddToCartViewModel.class);
        }
        progressDialog = new ProgressDialog(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentShortListedItemsBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_short_listed_items,container,false);
        fragmentShortListedItemsBinding.setProductByNameViewModel(getProductsByNameObserver);

        progressDialog.StartLoadingdialog();
        getProductsByNameObserver.getProductsByName(itemName,1,12);


        adapter = new ProductSelectionAdapter(requireContext(), itemList, new ProductSelectionListener() {
            @Override
            public void onProductClicked(GetAutocompleteResultsQuery.Item item) {


                addToCartObsrever.setProductSku(item.sku());
                addToCartObsrever.setProductQuantity("1");
                
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false);
        fragmentShortListedItemsBinding.rvShortListedItems.setLayoutManager(linearLayoutManager);
        fragmentShortListedItemsBinding.rvShortListedItems.setAdapter(adapter);




        getProductsByNameObserver.getItemsList().observe(getViewLifecycleOwner(), new Observer<List<GetAutocompleteResultsQuery.Item>>() {
            @Override
            public void onChanged(List<GetAutocompleteResultsQuery.Item> items) {
                progressDialog.dismissDialog();
                if(items.size()>0) {
                    String ab = items.get(0).name();
                    Toast.makeText(requireContext(), ab, Toast.LENGTH_LONG).show();

                    for(int i=0; i<items.size();i++){
                        itemList.add(items.get(i));
                    }
                    adapter.notifyDataSetChanged();


                }
            }
        });

        fragmentShortListedItemsBinding.btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addToCartObsrever.getProductSku().length()<1)
                {
                    Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.select_item_before_order), Toast.LENGTH_LONG).show();
                }
                else
                    {
                        addToCartObsrever.placeOrder();
                    }
            }
        });

        addToCartObsrever.getRepositoryResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                    Toast.makeText(requireContext(), s, Toast.LENGTH_LONG).show();
            }
        });

        fragmentShortListedItemsBinding.shortListedItemsToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.dismissDialog();
                NavHostFragment.findNavController(ShortListedItemsFragment.this).popBackStack();
            }
        });



        return fragmentShortListedItemsBinding.getRoot();
    }
}