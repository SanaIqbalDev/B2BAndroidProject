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
import com.codeseven.pos.databinding.FragmentCartBinding;
import com.codeseven.pos.model.CartItemAdapter;
import com.codeseven.pos.model.CatalogItem;
import com.codeseven.pos.util.CartItemClickListener;
import com.codeseven.pos.util.CartPreference;
import com.codeseven.pos.util.CartViewModel;
import com.codeseven.pos.util.ProcessItemViewModel;

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


    private  int sub_total = 0;
    ProcessItemViewModel removeItemViewModel;
    @Inject
    ProcessItemViewModel.ProcessItemObserver processItemObserver;

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
        removeItemViewModel = new ViewModelProvider(requireActivity()).get(ProcessItemViewModel.class);
        customerCartId = cartPreference.GetCartId("cart_id");
        getMoreProducts = true;
        if(!customerCartId.equals(""))
            cartObserver.getCartItems(customerCartId);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        FragmentCartBinding fragmentCartBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_cart,container,false);
        View view = fragmentCartBinding.getRoot();
        fragmentCartBinding.setCartViewModel(cartObserver);
        fragmentCartBinding.setLifecycleOwner(getViewLifecycleOwner());
        fragmentCartBinding.rvCartItems.setHasFixedSize(false);

        progressDialog.StartLoadingdialog();

        CartItemAdapter cartItemAdapter = new CartItemAdapter(requireContext(), cartItemArrayList, new CartItemClickListener() {
            @Override
            public void onItemCLicked(View view, CatalogItem catalogItem) {

                if(view.getTag().equals("remove")) {

                    Toast.makeText(requireContext(), "remove Button CLicked", Toast.LENGTH_SHORT).show();
                    processItemObserver.RemoveCartItem(catalogItem.getItemUid());

                }
                if(view.getTag().equals("increase")) {
                    Toast.makeText(requireContext(), "increase Button CLicked", Toast.LENGTH_SHORT).show();

                    catalogItem.setItemQuantity(String.valueOf(Integer.parseInt(catalogItem.getItemQuantity()) + 1));
                    processItemObserver.UpdateCartItem(catalogItem);
                }
                if(view.getTag().equals("decrease")) {
                    Toast.makeText(requireContext(), " decrease Button CLicked", Toast.LENGTH_SHORT).show();

                    if((Integer.parseInt(catalogItem.getItemQuantity())) > 1)
                    {
                        catalogItem.setItemQuantity(String.valueOf(Integer.valueOf(catalogItem.getItemQuantity()) - 1));
                    }
                    processItemObserver.UpdateCartItem(catalogItem);

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


        cartObserver.getCartItemsList().observe(getViewLifecycleOwner(), new Observer<List<GetCartByIdQuery.Item>>() {
            @Override
            public void onChanged(List<GetCartByIdQuery.Item> items) {
                List<GetCartByIdQuery.Item> ab = items;

                String name, price, image_url, quantity, itemsku, itemUid;

                if(getMoreProducts == true) {

                        for (int i = 0; i < items.size(); i++) {

                            itemsku = (items.get(i).product().sku());
                            name = (items.get(i).product().name());
                            price = (String.valueOf(items.get(i).product().price().regularPrice().amount().value().intValue())) + " Rs.";
                            image_url = items.get(i).product().small_image().url();
                            quantity = String.valueOf((int) items.get(i).quantity());
                            itemUid = items.get(i).uid();
                            sub_total += (items.get(i).product().price().regularPrice().amount().value()) * (items.get(i).quantity());
                            cartItemArrayList.add(new CatalogItem(itemsku, name, price, image_url, quantity,itemUid));

                        }
                        if(items.size()>0)
                        {
                            cartItemAdapter.notifyDataSetChanged();
                            getMoreProducts = false;
                            fragmentCartBinding.tvSubtotalValue.setText(String.valueOf(sub_total)+ " Rs.");
                            fragmentCartBinding.tvEstimatedTotalValue.setText(String.valueOf(sub_total) + " Rs.");
                            progressDialog.dismissDialog();
                            fragmentCartBinding.rvCartItems.setVisibility(View.VISIBLE);
                            fragmentCartBinding.tvNoItems.setVisibility(View.GONE);
                        }

            }
        }
        });

        cartObserver.getCartRequestResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                progressDialog.dismissDialog();
                Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
            }
        });
        return view;

    }

}