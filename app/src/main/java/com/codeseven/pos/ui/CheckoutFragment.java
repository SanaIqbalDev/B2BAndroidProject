package com.codeseven.pos.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.Toast;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.FragmentCheckoutBinding;
import com.codeseven.pos.util.CheckOutViewModel;
import com.google.android.gms.common.util.DataUtils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import apollo.pos.GetAvailablePaymentMethodsQuery;
import apollo.pos.GetCustomerDetailsQuery;
import apollo.pos.GetCustomerWalletQuery;
import apollo.pos.fragment.AvailableShippingMethodsCheckoutFragment;
import apollo.pos.fragment.ShippingInformationFragment;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CheckoutFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    FragmentCheckoutBinding fragmentCheckoutBinding;
    CheckOutViewModel checkOutViewModel;
    @Inject CheckOutViewModel.CheckoutObserver checkoutObserver;
    ProgressDialog progressDialog;
    private String timePeriod= "am";
    private int selected_time_frame = 0;
    String selectedDate="";
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


        fragmentCheckoutBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_checkout,container,false );

        contextCheckOut = requireContext();
        fragmentCheckoutBinding.setCheckOutviewModel(checkoutObserver);
        fragmentCheckoutBinding.setLifecycleOwner(getViewLifecycleOwner());
        progressDialog = new ProgressDialog(requireActivity());
//        progressDialog.StartLoadingdialog();
        //Set Date...
        Calendar mCalendar = Calendar.getInstance();
        String today = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(mCalendar.getTime());

        fragmentCheckoutBinding.tvDate.setText(today);

        isToday(DateUtils.isToday(mCalendar.getTime().getTime()));

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


        // Toolbar handling...
        fragmentCheckoutBinding.checkoutToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                NavHostFragment.findNavController(CheckoutFragment.this).popBackStack();
            }
        });


        // Timeframe Input...
        fragmentCheckoutBinding.layoutTimeA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected_time_frame = 1;
                if(fragmentCheckoutBinding.layoutTimeA.isEnabled()) {
                    fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_enable));
                    fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.primaryColor));
                }
                if(fragmentCheckoutBinding.layoutTimeB.isEnabled()) {
                    fragmentCheckoutBinding.clockB.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                    fragmentCheckoutBinding.dateB.setTextColor(requireContext().getResources().getColor(R.color.black));
                }
                if (fragmentCheckoutBinding.layoutTimeC.isEnabled()) {
                    fragmentCheckoutBinding.clockC.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                    fragmentCheckoutBinding.dateC.setTextColor(requireContext().getResources().getColor(R.color.black));
                }
                if(fragmentCheckoutBinding.layoutTimeD.isEnabled()){
                fragmentCheckoutBinding.clockD.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateD.setTextColor(requireContext().getResources().getColor(R.color.black));
                }
            }
        });

        fragmentCheckoutBinding.layoutTimeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selected_time_frame = 2;
                if(fragmentCheckoutBinding.layoutTimeA.isEnabled()) {

                    fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                    fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.black));
                }
                if(fragmentCheckoutBinding.layoutTimeB.isEnabled()) {

                    fragmentCheckoutBinding.clockB.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_enable));
                    fragmentCheckoutBinding.dateB.setTextColor(requireContext().getResources().getColor(R.color.primaryColor));
                }
                if (fragmentCheckoutBinding.layoutTimeC.isEnabled()) {

                    fragmentCheckoutBinding.clockC.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                    fragmentCheckoutBinding.dateC.setTextColor(requireContext().getResources().getColor(R.color.black));
                }
                if(fragmentCheckoutBinding.layoutTimeD.isEnabled()){
                    fragmentCheckoutBinding.clockD.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateD.setTextColor(requireContext().getResources().getColor(R.color.black));
            }
            }
        });


        fragmentCheckoutBinding.layoutTimeC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selected_time_frame = 3;
                if(fragmentCheckoutBinding.layoutTimeC.isEnabled()) {

                    fragmentCheckoutBinding.clockC.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_enable));
                    fragmentCheckoutBinding.dateC.setTextColor(requireContext().getResources().getColor(R.color.primaryColor));
                }
                if(fragmentCheckoutBinding.layoutTimeB.isEnabled()) {

                    fragmentCheckoutBinding.clockB.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                    fragmentCheckoutBinding.dateB.setTextColor(requireContext().getResources().getColor(R.color.black));
                }
                if (fragmentCheckoutBinding.layoutTimeA.isEnabled()) {
                    fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                    fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.black));
                }
                if(fragmentCheckoutBinding.layoutTimeD.isEnabled()) {
                    fragmentCheckoutBinding.clockD.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                    fragmentCheckoutBinding.dateD.setTextColor(requireContext().getResources().getColor(R.color.black));
                }
            }
        });


        fragmentCheckoutBinding.layoutTimeD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selected_time_frame = 4;
                if (fragmentCheckoutBinding.layoutTimeD.isEnabled()) {
                    fragmentCheckoutBinding.clockD.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_enable));
                    fragmentCheckoutBinding.dateD.setTextColor(requireContext().getResources().getColor(R.color.primaryColor));
                }
                if (fragmentCheckoutBinding.layoutTimeB.isEnabled()) {

                    fragmentCheckoutBinding.clockB.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                    fragmentCheckoutBinding.dateB.setTextColor(requireContext().getResources().getColor(R.color.black));
                }
                if (fragmentCheckoutBinding.layoutTimeA.isEnabled()) {
                    fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                    fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.black));
                }
                if (fragmentCheckoutBinding.layoutTimeA.isEnabled()) {
                    fragmentCheckoutBinding.clockC.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                    fragmentCheckoutBinding.dateC.setTextColor(requireContext().getResources().getColor(R.color.black));
                }
            }
        });


        progressDialog.StartLoadingdialog();
        // Shipping Address...
        checkoutObserver.GetCustomerAddressDetails();

        checkoutObserver.getCustomerInfoResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.length()>0){
                    if(s.contains("The current user cannot perform")){
                        signInDialog();
                    }
                    else
                        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
                }

            }
        });

        checkoutObserver.getCustomerShippingData().observe(getViewLifecycleOwner(), new Observer<ShippingInformationFragment.Shipping_address>() {
            @Override
            public void onChanged(ShippingInformationFragment.Shipping_address customer) {

                String shipping_address = customer.firstname() + " " + customer.lastname() + "\n" +
                        customer.street().get(0) + "\n" +
                        customer.city() + ", " + customer.region().label() + " " +  customer.country().code()
                        ;


                fragmentCheckoutBinding.tvShippingAddress.setText(shipping_address);
                fragmentCheckoutBinding.tvPhoneNumber.setText(customer.telephone());

                progressDialog.dismissDialog();
            }
        });
        checkoutObserver.GetShippingMethod().observe(getViewLifecycleOwner(), new Observer<AvailableShippingMethodsCheckoutFragment.Available_shipping_method>() {
            @Override
            public void onChanged(AvailableShippingMethodsCheckoutFragment.Available_shipping_method available_shipping_method) {

                fragmentCheckoutBinding.tvShippingMethod.setText(available_shipping_method.carrier_title()+" "+available_shipping_method.amount().currency()+" "+
                        available_shipping_method.amount().value().intValue());
                progressDialog.dismissDialog();
            }
        });
        checkoutObserver.getCustomerWallet();
        checkoutObserver.getCustomerWalletData().observe(getViewLifecycleOwner(), new Observer<GetCustomerWalletQuery.Wallet>() {
            @Override
            public void onChanged(GetCustomerWalletQuery.Wallet wallet) {
                String wallet_amount =  wallet.wallet_amount();
                fragmentCheckoutBinding.tvWalletValue.setText("Your wallet balance is:"+ " "+ wallet_amount);
            }
        });

        fragmentCheckoutBinding.cbApplyWallet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkoutObserver.ApplyWallet(b);
            }
        });
        checkoutObserver.getApplyWalletQueryResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.length()>0)
                {
                if(s.contains("The current user cannot perform")){
                 signInDialog();
                }
                else {
                Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
                }
                }
            }
        });






        //Get Customer Payment Method...

        checkoutObserver.GetListOfAvailablePaymentMethods();
        checkoutObserver.getGetAvailablePaymentMethods().observe(getViewLifecycleOwner(), new Observer<List<GetAvailablePaymentMethodsQuery.Available_payment_method>>() {
            @Override
            public void onChanged(List<GetAvailablePaymentMethodsQuery.Available_payment_method> available_payment_methods) {

                if(available_payment_methods.size()>0 && available_payment_methods.size()<2)
                {
                    fragmentCheckoutBinding.rbSlectionPaymentMethodA.setText(available_payment_methods.get(0).title());
                    fragmentCheckoutBinding.layoutPaymentB.setVisibility(View.GONE);
                    fragmentCheckoutBinding.cbPaymentMethodA.setChecked(true);
                    fragmentCheckoutBinding.layoutNewAddressA.setVisibility(View.GONE);

                }
                else if(available_payment_methods.size()>=2 &&available_payment_methods.size()<=3)
                {
                    fragmentCheckoutBinding.cbPaymentMethodA.setChecked(true);
                    fragmentCheckoutBinding.layoutNewAddressA.setVisibility(View.GONE);

                    fragmentCheckoutBinding.layoutPaymentB.setVisibility(View.VISIBLE);
                    fragmentCheckoutBinding.cbPaymentMethodB.setVisibility(View.GONE);
                    fragmentCheckoutBinding.layoutNewAddressB.setVisibility(View.GONE);

                    fragmentCheckoutBinding.rbSlectionPaymentMethodA.setText(available_payment_methods.get(0).title());
                    fragmentCheckoutBinding.rbSlectionPaymentMethodB.setText(available_payment_methods.get(1).title());
                }
            }
        });
        checkoutObserver.getPaymentMethodsResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.length()>0){
                    if(s.contains("The current user cannot perform")){
                        signInDialog();
                    }
                    else {
                        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        fragmentCheckoutBinding.rbSlectionPaymentMethodA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    fragmentCheckoutBinding.rbSlectionPaymentMethodB.setChecked(false);
                    fragmentCheckoutBinding.cbPaymentMethodB.setVisibility(View.GONE);
                    fragmentCheckoutBinding.layoutNewAddressB.setVisibility(View.GONE);
                    fragmentCheckoutBinding.cbPaymentMethodA.setVisibility(View.VISIBLE);
                    fragmentCheckoutBinding.cbPaymentMethodA.setChecked(true);
                }
            }
        });
        fragmentCheckoutBinding.rbSlectionPaymentMethodB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    fragmentCheckoutBinding.rbSlectionPaymentMethodA.setChecked(false);
                    fragmentCheckoutBinding.cbPaymentMethodA.setVisibility(View.GONE);
                    fragmentCheckoutBinding.cbPaymentMethodB.setVisibility(View.VISIBLE);
                    fragmentCheckoutBinding.layoutNewAddressA.setVisibility(View.GONE);
                    fragmentCheckoutBinding.cbPaymentMethodB.setChecked(true);
                }

            }
        });

        fragmentCheckoutBinding.cbPaymentMethodA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b)
                {
                    fragmentCheckoutBinding.layoutNewAddressA.setVisibility(View.VISIBLE);
                }
                else
                {
                    fragmentCheckoutBinding.layoutNewAddressA.setVisibility(View.GONE);

                }

            }
        });

        fragmentCheckoutBinding.cbPaymentMethodB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b)
                {
                    fragmentCheckoutBinding.layoutNewAddressB.setVisibility(View.VISIBLE);
                }
                else
                {
                    fragmentCheckoutBinding.layoutNewAddressB.setVisibility(View.GONE);

                }
            }
        });


        fragmentCheckoutBinding.tvChangeShippingAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(CheckoutFragment.this).navigate(R.id.action_checkoutFragment_to_customerAddressesFragment);
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fragmentCheckoutBinding.spinnerCountriesA.setAdapter(adapter);
        fragmentCheckoutBinding.spinnerCountriesB.setAdapter(adapter);


//        isToday();
        return fragmentCheckoutBinding.getRoot();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.YEAR,i);
        mCalendar.set(Calendar.MONTH,i1);
        mCalendar.set(Calendar.DAY_OF_MONTH,i2);

        selectedDate = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(mCalendar.getTime());


        fragmentCheckoutBinding.tvDate.setText(selectedDate);
        isToday(DateUtils.isToday(mCalendar.getTime().getTime()));
    }
    public int getTimeNow(){
        Date currentTime = Calendar.getInstance().getTime();
        int hours = currentTime.getHours();

        if(hours<13) {
            timePeriod = "am";
            Toast.makeText(requireContext(), String.valueOf(hours) + timePeriod, Toast.LENGTH_SHORT).show();
        }
        else
        {
            timePeriod = "pm";
            Toast.makeText(requireContext(), String.valueOf(hours-12) + timePeriod, Toast.LENGTH_SHORT).show();
        }

        return hours;
    }

    public void isToday(boolean isDateToday){
        int timeNow = getTimeNow();
        if(isDateToday) {
            if (timeNow >= 6 && timeNow < 10) {
                fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_disable));
                fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.dark_gray));
                fragmentCheckoutBinding.layoutTimeA.setEnabled(false);

            } else if ((timeNow >= 10) && timeNow < 14) {
                fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_disable));
                fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.dark_gray));
                fragmentCheckoutBinding.layoutTimeA.setEnabled(false);

                fragmentCheckoutBinding.clockB.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_disable));
                fragmentCheckoutBinding.dateB.setTextColor(requireContext().getResources().getColor(R.color.dark_gray));
                fragmentCheckoutBinding.layoutTimeB.setEnabled(false);

            } else if ((timeNow >= 14) && timeNow < 18) {
                fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_disable));
                fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.dark_gray));
                fragmentCheckoutBinding.layoutTimeA.setEnabled(false);

                fragmentCheckoutBinding.clockB.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_disable));
                fragmentCheckoutBinding.dateB.setTextColor(requireContext().getResources().getColor(R.color.dark_gray));
                fragmentCheckoutBinding.layoutTimeB.setEnabled(false);

                fragmentCheckoutBinding.clockC.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_disable));
                fragmentCheckoutBinding.dateC.setTextColor(requireContext().getResources().getColor(R.color.dark_gray));
                fragmentCheckoutBinding.layoutTimeC.setEnabled(false);

                //last two visible...
            } else if ((timeNow >= 18)) {
                fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_disable));
                fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.dark_gray));
                fragmentCheckoutBinding.layoutTimeA.setEnabled(false);

                fragmentCheckoutBinding.clockB.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_disable));
                fragmentCheckoutBinding.dateB.setTextColor(requireContext().getResources().getColor(R.color.dark_gray));
                fragmentCheckoutBinding.layoutTimeB.setEnabled(false);

                fragmentCheckoutBinding.clockC.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_disable));
                fragmentCheckoutBinding.dateC.setTextColor(requireContext().getResources().getColor(R.color.dark_gray));
                fragmentCheckoutBinding.layoutTimeC.setEnabled(false);

                fragmentCheckoutBinding.clockD.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_disable));
                fragmentCheckoutBinding.dateD.setTextColor(requireContext().getResources().getColor(R.color.dark_gray));
                fragmentCheckoutBinding.layoutTimeD.setEnabled(false);

            }
        }
        else {
                fragmentCheckoutBinding.clockD.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateD.setTextColor(requireContext().getResources().getColor(R.color.black));
                fragmentCheckoutBinding.layoutTimeD.setEnabled(true);

                fragmentCheckoutBinding.clockB.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateB.setTextColor(requireContext().getResources().getColor(R.color.black));
                fragmentCheckoutBinding.layoutTimeB.setEnabled(true);

                fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.black));
                fragmentCheckoutBinding.layoutTimeA.setEnabled(true);

                fragmentCheckoutBinding.clockC.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateC.setTextColor(requireContext().getResources().getColor(R.color.black));
                fragmentCheckoutBinding.layoutTimeC.setEnabled(true);

        }

    }
    public void signInDialog()
    {
            progressDialog.dismissDialog();
            AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
            builder1.setMessage("Please sign in again to perform this operation.");
            builder1.setCancelable(false);
            builder1.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            NavHostFragment.findNavController(CheckoutFragment.this).navigate(R.id.action_checkoutFragment_to_loginFragment);
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