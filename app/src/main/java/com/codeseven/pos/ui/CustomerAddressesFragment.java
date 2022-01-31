package com.codeseven.pos.ui;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Parcelable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.FragmentCustomerAddressesBinding;
import com.codeseven.pos.model.AddressItem;
import com.codeseven.pos.model.AddressItemAdapter;
import com.codeseven.pos.util.AddressItemClickListener;
import com.codeseven.pos.util.CheckOutViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import apollo.pos.GetCustomerAddressesQuery;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CustomerAddressesFragment extends Fragment {

    FragmentCustomerAddressesBinding addressesBinding;
    List<String> addressesList= new ArrayList<>();
    ProgressDialog progressDialog;

    List<AddressItem> addressList = new ArrayList<>();
    CheckOutViewModel checkOutViewModel;
    @Inject
    CheckOutViewModel.CheckoutObserver checkoutObserver;


    public CustomerAddressesFragment() {
    }

    public static CustomerAddressesFragment newInstance() {
        CustomerAddressesFragment fragment = new CustomerAddressesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkOutViewModel = (new ViewModelProvider(requireActivity())).get(CheckOutViewModel.class);
        progressDialog = new ProgressDialog(requireActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        addressesBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer_addresses,container,false);

        View view = addressesBinding.getRoot();
        progressDialog.StartLoadingdialog();

        AddressItemAdapter addressItemAdapter = new AddressItemAdapter(requireContext(), addressesList, new AddressItemClickListener() {
            @Override
            public void onItemClicked(View view, boolean isAddNew, int position) {
               if(!isAddNew) {
                   if (view.getTag().equals("edit")) {
                       Bundle bundle = new Bundle();
                       if (addressList != null) {
                           bundle.putParcelable("address", addressList.get(position));
                           NavHostFragment.findNavController(CustomerAddressesFragment.this).navigate(R.id.action_customerAddressesFragment_to_editAddressFragment, bundle);
                       }
                   }

                   if (view.getTag().equals("item")) {
//                    Toast.makeText(requireContext(), "item", Toast.LENGTH_SHORT).show();
                   }
               }
               else
               {
                   Bundle bundle = new Bundle();
//                   bundle.putParcelable("address", "addressList.get(position)");
                   NavHostFragment.findNavController(CustomerAddressesFragment.this).navigate(R.id.action_customerAddressesFragment_to_editAddressFragment,bundle);
               }
            }
        });


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false);
        addressesBinding.rvAddresses.setLayoutManager(linearLayoutManager);
        addressesBinding.rvAddresses.setAdapter(addressItemAdapter);


        addressesBinding.addressesToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.dismissDialog();
                NavHostFragment.findNavController(CustomerAddressesFragment.this).popBackStack();
            }
        });



        checkoutObserver.GetCustomerAvailableAddresses();
        checkoutObserver.getListOfAddresses().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                addressesList.addAll(strings);
                addressItemAdapter.notifyDataSetChanged();
                progressDialog.dismissDialog();
            }
        });
        checkoutObserver.getAddressListResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.length()>0) {
                    progressDialog.dismissDialog();
                    if (s.contains("Failed to execute http call for operation"))
                        Toast.makeText(requireContext(), "Please check your internet connection.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        checkoutObserver.getAddressObjects().observe(getViewLifecycleOwner(), new Observer<List<AddressItem>>() {
            @Override
            public void onChanged(List<AddressItem> addresses) {

                addressList = addresses;
                progressDialog.dismissDialog();

            }
        });
        return view;
    }
}