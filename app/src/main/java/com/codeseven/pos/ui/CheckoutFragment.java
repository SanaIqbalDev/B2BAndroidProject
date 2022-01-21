package com.codeseven.pos.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Toast;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.FragmentCheckoutBinding;
import com.codeseven.pos.util.CheckOutViewModel;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import apollo.pos.GetCustomerDetailsQuery;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CheckoutFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    FragmentCheckoutBinding fragmentCheckoutBinding;
    CheckOutViewModel checkOutViewModel;
    @Inject CheckOutViewModel.CheckoutObserver checkoutObserver;
    ProgressDialog progressDialog;
    private String timePeriod= "am";
    private int selected_time_frame = 0;

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
        progressDialog.StartLoadingdialog();
        //Set Date...
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
                fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_enable));
                fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.primaryDarkColor));


                fragmentCheckoutBinding.clockB.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateB.setTextColor(requireContext().getResources().getColor(R.color.black));

                fragmentCheckoutBinding.clockC.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateC.setTextColor(requireContext().getResources().getColor(R.color.black));

                fragmentCheckoutBinding.clockD.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateD.setTextColor(requireContext().getResources().getColor(R.color.black));
            }
        });

        fragmentCheckoutBinding.layoutTimeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selected_time_frame = 2;
                fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.black));


                fragmentCheckoutBinding.clockB.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_enable));
                fragmentCheckoutBinding.dateB.setTextColor(requireContext().getResources().getColor(R.color.primaryDarkColor));

                fragmentCheckoutBinding.clockC.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateC.setTextColor(requireContext().getResources().getColor(R.color.black));

                fragmentCheckoutBinding.clockD.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateD.setTextColor(requireContext().getResources().getColor(R.color.black));
            }
        });


        fragmentCheckoutBinding.layoutTimeC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selected_time_frame = 3;
                fragmentCheckoutBinding.clockC.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_enable));
                fragmentCheckoutBinding.dateC.setTextColor(requireContext().getResources().getColor(R.color.primaryDarkColor));


                fragmentCheckoutBinding.clockB.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateB.setTextColor(requireContext().getResources().getColor(R.color.black));

                fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.black));

                fragmentCheckoutBinding.clockD.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateD.setTextColor(requireContext().getResources().getColor(R.color.black));
            }
        });


        fragmentCheckoutBinding.layoutTimeD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selected_time_frame = 4;
                fragmentCheckoutBinding.clockD.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_enable));
                fragmentCheckoutBinding.dateD.setTextColor(requireContext().getResources().getColor(R.color.primaryDarkColor));


                fragmentCheckoutBinding.clockB.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateB.setTextColor(requireContext().getResources().getColor(R.color.black));

                fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.black));

                fragmentCheckoutBinding.clockC.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                fragmentCheckoutBinding.dateC.setTextColor(requireContext().getResources().getColor(R.color.black));
            }
        });


        // Shipping Address...
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

                String shipping_address = customer.firstname() + " " + customer.lastname() + "\n" +
                        customer.addresses().get(0).street().get(0) + "\n" +
                        customer.addresses().get(0).city() + ", " + customer.addresses().get(0).region().region() + " " + customer.addresses().get(0).country_code() + "\n"
                        ;


                fragmentCheckoutBinding.tvShippingAddress.setText(shipping_address);
                fragmentCheckoutBinding.tvPhoneNumber.setText(customer.addresses().get(0).telephone());

                progressDialog.dismissDialog();
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
    public int getTimeNow(){
        Date currentTime = Calendar.getInstance().getTime();
        int hours = currentTime.getHours();

        if(hours<13) {
            timePeriod = "am";
            Toast.makeText(requireContext(), String.valueOf(hours) + timePeriod, Toast.LENGTH_SHORT).show();
            return hours;
        }
        else
        {
            timePeriod = "pm";
            Toast.makeText(requireContext(), String.valueOf(hours-12) + timePeriod, Toast.LENGTH_SHORT).show();

            return hours-12;
        }


    }

    public void isToday(){
        int timeNow = getTimeNow();

        if((timeNow<10) &&(timePeriod.equals("am")) )
        {
            fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
            fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.black));

            fragmentCheckoutBinding.clockB.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
            fragmentCheckoutBinding.dateB.setTextColor(requireContext().getResources().getColor(R.color.black));

            fragmentCheckoutBinding.clockC.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
            fragmentCheckoutBinding.dateC.setTextColor(requireContext().getResources().getColor(R.color.black));

            fragmentCheckoutBinding.clockD.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
            fragmentCheckoutBinding.dateD.setTextColor(requireContext().getResources().getColor(R.color.black));
            //all visible...

        }
        else if((timeNow>2) && (timePeriod.equals("pm")))
        {
            fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_disable));
            fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.dark_gray));

            fragmentCheckoutBinding.clockB.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
            fragmentCheckoutBinding.dateB.setTextColor(requireContext().getResources().getColor(R.color.black));

            fragmentCheckoutBinding.clockC.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
            fragmentCheckoutBinding.dateC.setTextColor(requireContext().getResources().getColor(R.color.black));

            fragmentCheckoutBinding.clockD.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
            fragmentCheckoutBinding.dateD.setTextColor(requireContext().getResources().getColor(R.color.black));

            //last three visible...
        }
        else if((timeNow>6) && (timePeriod.equals("pm")))
        {
            fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_disable));
            fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.dark_gray));

            fragmentCheckoutBinding.clockB.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_disable));
            fragmentCheckoutBinding.dateB.setTextColor(requireContext().getResources().getColor(R.color.dark_gray));

            fragmentCheckoutBinding.clockC.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
            fragmentCheckoutBinding.dateC.setTextColor(requireContext().getResources().getColor(R.color.black));

            fragmentCheckoutBinding.clockD.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
            fragmentCheckoutBinding.dateD.setTextColor(requireContext().getResources().getColor(R.color.black));

            //last two visible...
        }
        else if((timeNow>9) && (timePeriod.equals("pm")))
        {
            fragmentCheckoutBinding.clockA.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_disable));
            fragmentCheckoutBinding.dateA.setTextColor(requireContext().getResources().getColor(R.color.dark_gray));

            fragmentCheckoutBinding.clockB.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_disable));
            fragmentCheckoutBinding.dateB.setTextColor(requireContext().getResources().getColor(R.color.dark_gray));

            fragmentCheckoutBinding.clockC.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock_disable));
            fragmentCheckoutBinding.dateC.setTextColor(requireContext().getResources().getColor(R.color.dark_gray));

            fragmentCheckoutBinding.clockD.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
            fragmentCheckoutBinding.dateD.setTextColor(requireContext().getResources().getColor(R.color.black));

            //only last visible...
        }

    }
}