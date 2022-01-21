package com.codeseven.pos.ui;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.FragmentProductDetailBinding;
import com.codeseven.pos.model.CatalogItem;
import com.codeseven.pos.util.AddToCartViewModel;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProductDetailFragment extends Fragment {

    public AddToCartViewModel productViewModel;

    @Inject
    public AddToCartViewModel.AddToCartObsrever productObserver;

    CatalogItem catalogItem;

    public ProductDetailFragment() {
        // Required empty public constructor
    }

    public static ProductDetailFragment newInstance() {
        ProductDetailFragment fragment = new ProductDetailFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        catalogItem = bundle.getParcelable("catalogItem");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentProductDetailBinding fragmentProductDetailBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_product_detail,container,false);
        View view = fragmentProductDetailBinding.getRoot();

        if(productViewModel==null){
            productViewModel = new ViewModelProvider(requireActivity()).get(AddToCartViewModel.class);
        }

        productObserver.setProductSku(catalogItem.getItemSku());
        productObserver.setProductName(catalogItem.getItemName());
        productObserver.setProductPrice(catalogItem.getItemPrice());
        productObserver.setProductDescription(catalogItem.getItemDescription());
        fragmentProductDetailBinding.setViewModel(productObserver);

        fragmentProductDetailBinding.tvItemDetailContent.setMovementMethod(new ScrollingMovementMethod());
        Picasso.get().load(catalogItem.getItemImage()).into(fragmentProductDetailBinding.ivProductImg);
        fragmentProductDetailBinding.btnPlaceOrder.setCompoundDrawablePadding(20);


        fragmentProductDetailBinding.topAppBarDetail.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(ProductDetailFragment.this).popBackStack();
            }
        });
        
        productObserver.getRepositoryResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.equals("Error"))
                    Toast.makeText(requireContext(), "Item not added to cart, please check your network connection.", Toast.LENGTH_SHORT).show();
                else if(!s.equals(""))
                    Toast.makeText(requireContext(), "Item is added to cart.", Toast.LENGTH_SHORT).show();
            }
        });
        
        
        
        
        
        
        
        
        
        return view;
    }



}