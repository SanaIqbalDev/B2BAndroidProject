package com.codeseven.pos.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.FragmentProductDetailBinding;
import com.codeseven.pos.model.CatalogItem;
import com.codeseven.pos.util.AddToCartViewModel;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProductDetailFragment extends Fragment {

    public AddToCartViewModel productViewModel;

    @Inject
    public AddToCartViewModel.AddToCartObsrever productObserver;

    CatalogItem catalogItem;
    ProgressDialog progressDialog;

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

    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentProductDetailBinding fragmentProductDetailBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_product_detail,container,false);
        View view = fragmentProductDetailBinding.getRoot();
        progressDialog = new ProgressDialog(requireActivity());
        if(productViewModel==null){
            productViewModel = new ViewModelProvider(requireActivity()).get(AddToCartViewModel.class);
        }

        BadgeDrawable badge = BadgeDrawable.create(requireContext());
        badge.setBackgroundColor(requireContext().getResources().getColor(R.color.red_200));
        badge.setBadgeGravity(BadgeDrawable.TOP_START);
        badge.setHorizontalOffset(8);
        BadgeUtils.attachBadgeDrawable(badge, fragmentProductDetailBinding.topAppBarDetail, R.id.menu_cart);
        productObserver.getCartCount().observe(requireActivity(),cartCount->{
            if (cartCount!=null) badge.setNumber(cartCount.intValue());
            else badge.setNumber(0);
        });

        productObserver.setProductSku(catalogItem.getItemSku());
        productObserver.setProductName(catalogItem.getItemName());
        productObserver.setProductPrice(catalogItem.getItemMinimalPrice());
        productObserver.setProductDescription(catalogItem.getItemDescription());
        fragmentProductDetailBinding.setViewModel(productObserver);

        fragmentProductDetailBinding.tvItemDetailContent.setMovementMethod(new ScrollingMovementMethod());
        Picasso.get().load(catalogItem.getItemImage()).into(fragmentProductDetailBinding.ivProductImg);
        fragmentProductDetailBinding.btnPlaceOrder.setCompoundDrawablePadding(20);

        fragmentProductDetailBinding.btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(productObserver.getProductQuantity().length()>0) {
                    if (Integer.parseInt(productObserver.getProductQuantity()) > 0 ) {

                        //hide keyboard
                       closeKeyboard();

                        progressDialog.StartLoadingdialog();
                        productObserver.placeOrder();
                        productObserver.getCartCount();
                    }
                    else {
                        Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.enter_quantity), Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.enter_quantity), Toast.LENGTH_LONG).show();
                }
            }
        });


        fragmentProductDetailBinding.topAppBarDetail.setNavigationOnClickListener(view1 -> NavHostFragment.findNavController(ProductDetailFragment.this).popBackStack());

        fragmentProductDetailBinding.topAppBarDetail.setOnMenuItemClickListener(item -> {

            if(item.getTitle().equals("ViewCart"))
            {
                NavHostFragment.findNavController(ProductDetailFragment.this).navigate(R.id.action_productDetailFragment_to_cartFragment);
            }
            return false;
        });
        
        productObserver.getRepositoryResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                progressDialog.dismissDialog();
                if(s.length()>0) {
                    if (s.equals("Error"))
                        Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.check_internet_connection), Toast.LENGTH_LONG).show();
                    else if(s.contains("The current user cannot perform")){

                        Toast.makeText(requireContext(), s, Toast.LENGTH_LONG).show();

                        AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
                        builder1.setMessage(requireContext().getResources().getString(R.string.sign_in_again));
                        builder1.setCancelable(false);
                        builder1.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        NavHostFragment.findNavController(ProductDetailFragment.this).navigate(R.id.action_productDetailFragment_to_loginFragment);
                                        dialog.cancel();
                                    }
                                });
                        builder1.setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    }
                    else {


                        Toast.makeText(requireContext(), s, Toast.LENGTH_LONG).show();

                    }
                }
            }
        });

        return view;
    }


    private void closeKeyboard()
    {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {

            InputMethodManager manager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}