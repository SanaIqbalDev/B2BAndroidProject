package com.codeseven.pos.ui;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.FragmentCustomerAddressesBinding;
import com.codeseven.pos.model.AddressItemAdapter;
import com.codeseven.pos.util.AddressItemClickListener;

import java.util.ArrayList;

public class CustomerAddressesFragment extends Fragment {

    FragmentCustomerAddressesBinding addressesBinding;
    ArrayList<String> addressesList= new ArrayList<>();
    ProgressDialog progressDialog;
    public CustomerAddressesFragment() {
    }

    public static CustomerAddressesFragment newInstance() {
        CustomerAddressesFragment fragment = new CustomerAddressesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(requireActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        addressesBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer_addresses,container,false);

        View view = addressesBinding.getRoot();

        AddressItemAdapter addressItemAdapter = new AddressItemAdapter(requireContext(), addressesList, new AddressItemClickListener() {
            @Override
            public void onItemClicked(String address) {

                Toast.makeText(requireContext(), address, Toast.LENGTH_SHORT).show();
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


        return view;
    }
}