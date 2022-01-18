package com.codeseven.pos.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.FragmentCheckoutBinding;
import com.codeseven.pos.util.CheckOutViewModel;

import java.text.DateFormat;
import java.util.Calendar;

import javax.inject.Inject;

import apollo.pos.GetCustomerDetailsQuery;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CheckoutFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    FragmentCheckoutBinding fragmentCheckoutBinding;
    CheckOutViewModel checkOutViewModel;
    @Inject CheckOutViewModel.CheckoutObserver checkoutObserver;

    public static Context contextCheckOut;
    public CheckoutFragment() {
        // Required empty public constructor
    }

    public static CheckoutFragment newInstance(String param1, String param2) {
        CheckoutFragment fragment = new CheckoutFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkOutViewModel = (new ViewModelProvider(requireActivity())).get(CheckOutViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentCheckoutBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_checkout,container,false );

//        fragmentCheckoutBinding.setViewModel(checkoutObserver);
        contextCheckOut = requireContext();
        fragmentCheckoutBinding.setCheckOutviewModel(checkoutObserver);
        fragmentCheckoutBinding.setLifecycleOwner(getViewLifecycleOwner());
        Calendar mCalendar = Calendar.getInstance();

        String today = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(mCalendar.getTime());

        fragmentCheckoutBinding.tvDate.setText(today);
        fragmentCheckoutBinding.tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                DatePickerCustom datePicker = new DatePickerCustom();
                datePicker.setTargetFragment(CheckoutFragment.this, 0);
                datePicker.show(getFragmentManager(), "date picker");
            }
        });

        fragmentCheckoutBinding.ivCalender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerCustom datePicker = new DatePickerCustom();
                datePicker.setTargetFragment(CheckoutFragment.this, 0);
                datePicker.show(getFragmentManager(), "date picker");
            }
        });

        checkoutObserver.GetCustomerAddressDetails();

        checkoutObserver.getCustomerInfoResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.length()>0)
                    Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();

            }
        });

        checkoutObserver.getCustomerShippingData().observe(getViewLifecycleOwner(), new Observer<GetCustomerDetailsQuery.Customer>() {
            @Override
            public void onChanged(GetCustomerDetailsQuery.Customer customer) {

//                Toast.makeText(requireContext(), Integer.parseInt("asd98"), Toast.LENGTH_SHORT).show();

//                Stringab =
            }
        });
        fragmentCheckoutBinding.layoutTimeA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(requireContext(), "nnnnmmnm", Toast.LENGTH_SHORT).show();
            }
        });

        return fragmentCheckoutBinding.getRoot();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.YEAR,i);
        mCalendar.set(Calendar.MONTH,i1);
        mCalendar.set(Calendar.DAY_OF_MONTH,i2);

        String selectedDate = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(mCalendar.getTime());

        fragmentCheckoutBinding.tvDate.setText(selectedDate);
    }
}