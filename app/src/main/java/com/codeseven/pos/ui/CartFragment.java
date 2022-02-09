package com.codeseven.pos.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.FragmentCartBinding;
import com.codeseven.pos.model.CartItemAdapter;
import com.codeseven.pos.model.CatalogItem;
import com.codeseven.pos.util.CartItemClickListener;
import com.codeseven.pos.util.CartPreference;
import com.codeseven.pos.util.CartViewModel;
import com.codeseven.pos.util.ProcessCartViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import apollo.pos.GetCartByIdQuery;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CartFragment extends Fragment {


    CartViewModel cartViewModel;
    @Inject CartViewModel.CartObserver cartObserver;
    CartPreference cartPreference;
    public String customerCartId;
    public boolean getMoreProducts = false;
    private ArrayList<CatalogItem> cartItemArrayList = new ArrayList<>();


    public static Double sub_total;
    public static Double est_total;
    public static Double saved_total;
    public int total_items_in_cart = 0;
    ProcessCartViewModel removeItemViewModel;
    @Inject
    ProcessCartViewModel.ProcessCartObserver processItemObserver;

    FragmentCartBinding fragmentCartBinding ;
    private ProgressDialog progressDialog;
    public CartFragment() {
        // Required empty public constructor
    }
    public static CartFragment newInstance() {
        CartFragment fragment = new CartFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(requireActivity());
        cartPreference = new CartPreference();
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        removeItemViewModel = new ViewModelProvider(requireActivity()).get(ProcessCartViewModel.class);
        customerCartId = cartPreference.GetCartId("cart_id");
        getMoreProducts = true;
        if(!customerCartId.equals("")) {
            progressDialog.StartLoadingdialog();
            cartObserver.getCartItems(customerCartId);
        }
        sub_total = 0.0;
        est_total = 0.0;
        saved_total = 0.0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        fragmentCartBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_cart,container,false);
        View view = fragmentCartBinding.getRoot();
        fragmentCartBinding.setCartViewModel(cartObserver);
        fragmentCartBinding.setLifecycleOwner(getViewLifecycleOwner());
        fragmentCartBinding.rvCartItems.setHasFixedSize(false);


        CartItemAdapter cartItemAdapter = new CartItemAdapter(requireContext(), cartItemArrayList, new CartItemClickListener() {
            @Override
            public void onItemCLicked(View view, CatalogItem catalogItem) {

                if(view.getTag().equals("remove")) {

//                    Toast.makeText(requireContext(), "remove Button CLicked", Toast.LENGTH_SHORT).show();
                    processItemObserver.RemoveCartItem(catalogItem.getItemUid());
                    ItemRemoved(Integer.parseInt(catalogItem.getItemQuantity()),Double.parseDouble(catalogItem.getItemMinimalPrice()), Double.parseDouble(catalogItem.getItemRegularPrice()));
                }
                if(view.getTag().equals("increase")) {
                    catalogItem.setItemQuantity(String.valueOf(Integer.parseInt(catalogItem.getItemQuantity()) + 1));
                    processItemObserver.UpdateCartItem(catalogItem);

                    updateSubtotal(Double.parseDouble(catalogItem.getItemMinimalPrice()), Double.parseDouble(catalogItem.getItemRegularPrice()), true);

                }
                if(view.getTag().equals("decrease")) {

                    if((Integer.parseInt(catalogItem.getItemQuantity())) > 1)
                    {
                        catalogItem.setItemQuantity(String.valueOf(Integer.valueOf(catalogItem.getItemQuantity()) - 1));

                        processItemObserver.UpdateCartItem(catalogItem);

                        updateSubtotal(Double.parseDouble(catalogItem.getItemMinimalPrice()), Double.parseDouble(catalogItem.getItemRegularPrice()), false);

                    }
                }
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false);
        fragmentCartBinding.rvCartItems.setLayoutManager(linearLayoutManager);
        fragmentCartBinding.rvCartItems.setAdapter(cartItemAdapter);


        fragmentCartBinding.cartToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.dismissDialog();
                NavHostFragment.findNavController(CartFragment.this).popBackStack();
            }
        });

        fragmentCartBinding.btnApplyCoupon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fragmentCartBinding.etCouponCode.getText().toString().length()>0) {
                    progressDialog.StartLoadingdialog();
                    processItemObserver.ApplyCouponToCart(fragmentCartBinding.etCouponCode.getText().toString());
                }
                else {
                    Toast.makeText(requireContext(), "Please enter coupon code.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cartObserver.getCartItemsList().observe(getViewLifecycleOwner(), new Observer<List<GetCartByIdQuery.Item>>() {
            @Override
            public void onChanged(List<GetCartByIdQuery.Item> items) {
                if(getViewLifecycleOwner().getLifecycle().getCurrentState()== Lifecycle.State.RESUMED){

                List<GetCartByIdQuery.Item> ab = items;
                total_items_in_cart = items.size();
                String name, image_url, quantity, itemsku, itemUid;
                Double priceThis, regular_price;

                progressDialog.dismissDialog();
                if(getMoreProducts == true) {
                    if (items.size() < 1) {
                        fragmentCartBinding.topLayout.setVisibility(View.GONE);
                        fragmentCartBinding.tvNoItems.setVisibility(View.VISIBLE);
                    } else
                    {
                        fragmentCartBinding.topLayout.setVisibility(View.VISIBLE);
                        fragmentCartBinding.tvNoItems.setVisibility(View.GONE);

                    }
                        for (int i = 0; i < items.size(); i++) {

                            itemsku = (items.get(i).product().sku());
                            name = (items.get(i).product().name());
                            regular_price = items.get(i).product().price().regularPrice().amount().value();

                            if ((items.get(i).product().price().minimalPrice().amount().value() != null) &&
                                    (items.get(i).product().price().minimalPrice().amount().value() < items.get(i).product().price().regularPrice().amount().value())) {
                                priceThis = items.get(i).product().price().minimalPrice().amount().value();
                            } else {
                                priceThis = items.get(i).product().price().regularPrice().amount().value();

                            }
                            image_url = items.get(i).product().small_image().url();
                            quantity = String.valueOf((int) items.get(i).quantity());
                            itemUid = items.get(i).uid();
                            sub_total += (priceThis) * (items.get(i).quantity());
                            saved_total += (regular_price - priceThis) * (items.get(i).quantity());
                            cartItemArrayList.add(new CatalogItem(itemsku, name, String.valueOf(priceThis), String.valueOf(regular_price), image_url, quantity, itemUid));

                        }
                        est_total = sub_total + 50;
                        if (items.size() > 0) {
                            cartItemAdapter.notifyDataSetChanged();
                            getMoreProducts = false;
                            fragmentCartBinding.tvSubtotalValue.setText("PKR " + String.format("%.2f", sub_total));
                            fragmentCartBinding.tvEstimatedTotalValue.setText("PKR " + String.format("%.2f", est_total));
                            progressDialog.dismissDialog();
                            fragmentCartBinding.rvCartItems.setVisibility(View.VISIBLE);
                            fragmentCartBinding.tvNoItems.setVisibility(View.GONE);
                        }
                }
                fragmentCartBinding.tvSubtotalValue.setText("PKR " + String.format("%.2f", sub_total));
                fragmentCartBinding.tvEstimatedTotalValue.setText("PKR " + String.format("%.2f", est_total));

                }
            }
        });

        cartObserver.getCartRequestResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                progressDialog.dismissDialog();
                if(s.contains("The cart isn't active."))
                {
                    cartObserver.GetCustomerCart();
                }
                if(s.contains("The current user cannot perform operations on cart")){

                    Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
                    builder1.setMessage("Please sign in again to perform this operation.");
                    builder1.setCancelable(false);
                    builder1.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    NavHostFragment.findNavController(CartFragment.this).navigate(R.id.action_cartFragment_to_loginFragment);
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
            }
        });
        processItemObserver.getApplyCouponResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                progressDialog.dismissDialog();
                if(getViewLifecycleOwner().getLifecycle().getCurrentState()== Lifecycle.State.RESUMED){
                    {
                        Toast.makeText(requireContext(), s, Toast.LENGTH_LONG).show();
                    }
            }
        }});


        fragmentCartBinding.btnProceedToCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("cart_items", cartItemArrayList);
                NavHostFragment.findNavController(CartFragment.this).navigate(R.id.action_cartFragment_to_checkoutFragment,bundle);
            }
        });

        return view;

    }

    private void ItemRemoved(int quantity, double price, double regular_price) {


        sub_total= sub_total-(price*quantity);
        est_total = sub_total+50;

        saved_total = saved_total - ((regular_price-price)*quantity);
        fragmentCartBinding.tvSubtotalValue.setText("PKR " + String.format("%.2f", sub_total));
        fragmentCartBinding.tvEstimatedTotalValue.setText("PKR " +  String.format("%.2f", est_total));
        total_items_in_cart--;

        if(total_items_in_cart == 0){
         fragmentCartBinding.tvNoItems.setVisibility(View.VISIBLE);
         fragmentCartBinding.topLayout.setVisibility(View.GONE);
        }
    }

    public void updateSubtotal(Double item_price,Double regular_price, boolean isIncrement){
        if(isIncrement)
        {
            sub_total = sub_total+item_price;
            saved_total= saved_total + (regular_price-item_price);
        }
        else
        {
            sub_total = sub_total-item_price;
            saved_total = saved_total - (regular_price - item_price);
        }

        est_total = sub_total+50;

        fragmentCartBinding.tvSubtotalValue.setText("PKR " + String.format("%.2f", sub_total));
        fragmentCartBinding.tvEstimatedTotalValue.setText("PKR " + String.format("%.2f", est_total));

    }

}