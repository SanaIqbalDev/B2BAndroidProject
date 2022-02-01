package com.codeseven.pos.ui;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Layer;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.FragmentEditAddressBinding;
import com.codeseven.pos.model.AddressItem;

import apollo.pos.GetCustomerAddressesQuery;

public class EditAddressFragment extends DialogFragment {


   AddressItem addressThis;

    FragmentEditAddressBinding binding;
    public EditAddressFragment() {
        // Required empty public constructor
    }
    public static EditAddressFragment newInstance() {
        EditAddressFragment fragment = new EditAddressFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        addressThis = bundle.getParcelable("address");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_edit_address,container,false);

        View view = binding.getRoot();


        if(addressThis!=null)
        {
            binding.etFirstName.setText(addressThis.getFirstName());
            binding.etLastName.setText(addressThis.getLastName());
            binding.etStreetAddress.setText(addressThis.getAddress());
            binding.etCity.setText(addressThis.getCity());
//            binding.spinnerCountries.setSelection(addressThis.getCountry());
            binding.etPhoneNumber.setText(addressThis.getTelephone());
            binding.btnUpdate.setVisibility(View.VISIBLE);
            binding.btnCancel.setVisibility(View.VISIBLE);
            binding.btnAdd.setVisibility(View.INVISIBLE);
            binding.cbDefaultAddress.setEnabled(false);
            binding.cbDefaultAddress.setClickable(false);
        }
        else {
            binding.btnUpdate.setVisibility(View.INVISIBLE);
            binding.btnCancel.setVisibility(View.INVISIBLE);
            binding.btnAdd.setVisibility(View.VISIBLE);
            binding.cbDefaultAddress.setEnabled(true);
            binding.cbDefaultAddress.setClickable(true);
        }

        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();


            }
        });
        binding.btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        binding.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return view;
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
    }
}