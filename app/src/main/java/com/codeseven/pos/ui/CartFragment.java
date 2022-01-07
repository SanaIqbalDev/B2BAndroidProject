package com.codeseven.pos.ui;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
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
import com.codeseven.pos.util.CartPreference;
import com.codeseven.pos.util.CartViewModel;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import apollo.pos.GetCartByIdQuery;
import apollo.pos.type.CartItemInput;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CartFragment extends Fragment {


    CartViewModel cartViewModel;
    @Inject CartViewModel.CartObserver cartObserver;
    CartPreference cartPreference;
    public String customerCartId;
    public boolean getMoreProducts = false;
    private ArrayList<CatalogItem> cartItemArrayList = new ArrayList<>();

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
        cartPreference = new CartPreference();
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
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


        CartItemAdapter cartItemAdapter = new CartItemAdapter(requireContext(),cartItemArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false);
        fragmentCartBinding.rvCartItems.setLayoutManager(linearLayoutManager);
        fragmentCartBinding.rvCartItems.setAdapter(cartItemAdapter);

//        cartObserver.getCartItems(customerCartId);

        //
        fragmentCartBinding.cartToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(CartFragment.this).popBackStack();
            }
        });



//        cartObserver.getCartItems(customerCartId);

        cartObserver.getCartItemsList().observe(getViewLifecycleOwner(), new Observer<List<GetCartByIdQuery.Item>>() {
            @Override
            public void onChanged(List<GetCartByIdQuery.Item> items) {
                List<GetCartByIdQuery.Item> ab = items;

                String name, price, image_url, quantity, itemsku;

                if(getMoreProducts == true) {
                    for (int i = 0; i < items.size(); i++) {

                        itemsku = (items.get(i).product().sku());
                        name = (items.get(i).product().name());
                        price = (String.valueOf(items.get(i).product().price().regularPrice().amount().value().intValue())) + " Rs.";
                        image_url = items.get(i).product().small_image().url();
                        quantity = String.valueOf((int) items.get(i).quantity());
                        cartItemArrayList.add(new CatalogItem(itemsku, name, price, image_url, quantity));

                    }
                    if(items.size()>0)
                    {
                        cartItemAdapter.notifyDataSetChanged();
                        getMoreProducts = false;
                    }
                if(ab.size()>0) {
                    Toast.makeText(requireContext(), ab.get(ab.size()-1).product().name(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        });
        return view;

    }

}