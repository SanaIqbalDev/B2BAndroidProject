package com.codeseven.pos.ui;

import static com.codeseven.pos.ui.CartFragment.saved_total;
import static com.codeseven.pos.ui.CustomerAddressesFragment.telephone;



import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Toast;

import com.apollographql.apollo.api.Input;
import com.codeseven.pos.R;
import com.codeseven.pos.databinding.FragmentCheckoutBinding;
import com.codeseven.pos.model.CartSummaryAdapter;
import com.codeseven.pos.model.CatalogItem;
import com.codeseven.pos.util.CartPreference;
import com.codeseven.pos.util.CartViewModel;
import com.codeseven.pos.util.CheckOutViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import apollo.pos.GetAvailablePaymentMethodsQuery;
import apollo.pos.GetCustomerAddressesQuery;
import apollo.pos.GetCustomerWalletQuery;
import apollo.pos.fragment.AvailableShippingMethodsCheckoutFragment;
import apollo.pos.fragment.ShippingInformationFragment;
import apollo.pos.type.CartAddressInput;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CheckoutFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    FragmentCheckoutBinding fragmentCheckoutBinding;
    CheckOutViewModel checkOutViewModel;
    @Inject CheckOutViewModel.CheckoutObserver checkoutObserver;
    ProgressDialog progressDialog;
    private String timePeriod= "am";
    private String selected_time_slot = "";
    String selectedDate="";
    public static Context contextCheckOut;
    CartPreference cartPreference;
    private ArrayList<CatalogItem> cartItemsList = new ArrayList<>();
    CartAddressInput addressInput;
    public static String selected_shipping_id;
    public static String selected_shipping_id_original;
    public static  boolean is_address_changed = false;
    public static String shipping_address = "";
    private ShippingInformationFragment.Shipping_address customer_shipping_address;
    private  boolean is_time_slot_selected = false;
    private String carrier_code = "";
    private String method_code = "";
    private CartAddressInput.Builder shipping_address_selected;
    private CartAddressInput.Builder shipping_address_new;

    CartViewModel cartViewModel;
    @Inject CartViewModel.CartObserver cartObserver;
    public String customerCartId;

    public CheckoutFragment() {
    }

    public static CheckoutFragment newInstance(String param1, String param2) {
        CheckoutFragment fragment = new CheckoutFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkOutViewModel = (new ViewModelProvider(requireActivity())).get(CheckOutViewModel.class);
        cartPreference = new CartPreference();
        progressDialog = new ProgressDialog(requireActivity());
        contextCheckOut = requireContext();
        Bundle bundle = this.getArguments();
        cartItemsList = bundle.getParcelableArrayList("cart_items");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        fragmentCheckoutBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_checkout,container,false );
        fragmentCheckoutBinding.setCheckOutviewModel(checkoutObserver);
        fragmentCheckoutBinding.setLifecycleOwner(getViewLifecycleOwner());


        // Toolbar handling...
        fragmentCheckoutBinding.checkoutToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                NavHostFragment.findNavController(CheckoutFragment.this).popBackStack();
            }
        });



        //Set Calender...

        setCalenderLayout();


        // Timeframe Input...

        setTimeFrame();


        // Shipping Address...

        setShippingDetails();


        ///// Payment Method Logic...

        setPaymentMethod();

        setCartSummary();

        fragmentCheckoutBinding.btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.StartLoadingdialog();
                // 1. Setting Shipping Address...
                 checkoutObserver.SetCustomerShippingAddress(Integer.parseInt(selected_shipping_id), "", "");
                 checkoutObserver.getGetShouldPlaceOrder().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
//                        if(aBoolean)
//                            checkoutObserver.placeOrder();
                    }
                });


                checkoutObserver.getGetPlaceOrderResponseMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        if(getViewLifecycleOwner().getLifecycle().getCurrentState()== Lifecycle.State.RESUMED)
                        {
                            Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
                        }
                            if(s.contains("The cart isn't active")) {
                                progressDialog.dismissDialog();
                                cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
                                customerCartId = cartPreference.GetCartId("cart_id");
                                cartObserver.getCartItems(customerCartId);
                                cartObserver.getCartRequestResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
                                    @Override
                                    public void onChanged(String s) {
                                        Toast.makeText(requireContext(),s, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            else if(s.equals("Shipping Address success")){
                                checkoutObserver.SetShippingMethodOnCart(method_code,carrier_code);
                            }
                            else if(s.equals("Shipping Method success")){
                                if(fragmentCheckoutBinding.layoutPayment.getVisibility() == View.VISIBLE )
                                {
                                    if(fragmentCheckoutBinding.cbPaymentMethod.isChecked()) {
                                        checkoutObserver.SetBillingAddress(null,Integer.parseInt(selected_shipping_id_original),true);
                                    }else{
                                        List<String> address_list = new ArrayList<>();
                                        address_list.add(fragmentCheckoutBinding.etStreetAddress.getText().toString());
                                        if(!fragmentCheckoutBinding.etStreetAddressOptional.getText().toString().equals(""))
                                            address_list.add(fragmentCheckoutBinding.etStreetAddressOptional.getText().toString());


                                        shipping_address_new   = CartAddressInput.builder().city(fragmentCheckoutBinding.etCity.getText().toString())
                                                .companyInput(new Input<>("",true))
                                                .country_code(fragmentCheckoutBinding.spinnerCountries.getSelectedItem().toString())
                                                .firstname(fragmentCheckoutBinding.etFirstName.getText().toString())
                                                .lastname(fragmentCheckoutBinding.etLastName.getText().toString())
                                                .postcode("")
                                                .region("")
                                                .region_id(null)
                                                .save_in_address_book(false)
                                                .street(address_list)
                                                .telephone(fragmentCheckoutBinding.etPhoneNumber.getText().toString());

                                        checkoutObserver.SetBillingAddress(null,Integer.parseInt(selected_shipping_id_original),false);


                                    }

                                }
                                else {
                                    checkoutObserver.SetBillingAddress(null,Integer.parseInt(selected_shipping_id_original),true);
                                }
                            }
                            else if(s.equals("Payment Method success"))
                            {
//
                                checkoutObserver.GetCartDetails();
                                progressDialog.dismissDialog();

                                checkoutObserver.placeOrder();

                            }
                            else if (s.equals("Billing Address Success")){
                                String comments_ = fragmentCheckoutBinding.etComments.getText().toString();
                                String date_ = fragmentCheckoutBinding.tvDate.getText().toString();
                                String time_slot_ = selected_time_slot;

                                if(is_time_slot_selected) {
                                    checkoutObserver.applyDeliveryCart(comments_, date_, time_slot_);
                                }
                                else
                                    Toast.makeText(requireContext(),"select a time slot",Toast.LENGTH_SHORT).show();

                            }
                            else if(s.equals("Delivery Cart success")){

                                checkoutObserver.SetPaymentMethodOnCart("cashondelivery");

//                                checkoutObserver.checkCount();
                            }
                           else if(s.equals("Place order success")){
                                progressDialog.dismissDialog();

                                AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
                                builder1.setMessage("Your order has been placed successfully.");
                                builder1.setCancelable(false);
                                builder1.setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                NavHostFragment.findNavController(CheckoutFragment.this).navigate(R.id.action_checkoutFragment_to_homeFragment);
                                                dialog.cancel();
                                            }
                                        });
                                AlertDialog alert11 = builder1.create();
                                alert11.show();
                            }
                            else if(s.equals("Place order failure")){
                                progressDialog.dismissDialog();

                                Toast.makeText(requireContext(),"Order is not placed successfully.",Toast.LENGTH_SHORT).show();
//                                checkoutObserver.checkCount();
                            }
//                            else if(s.equals("Delivery Cart failure")){
//                                String comments_ = fragmentCheckoutBinding.etComments.getText().toString();
//                                String date_ = fragmentCheckoutBinding.tvDate.getText().toString();
//                                String time_slot_ = selected_time_slot;
//
//                                if(is_time_slot_selected) {
//                                    checkoutObserver.applyDeliveryCart(comments_, date_, time_slot_);
//                                }
//                                else
//                                    Toast.makeText(requireContext(),"select a time slot",Toast.LENGTH_SHORT).show();
//                            }
//                            else if(s.equals("Billing Address failure")){
//                                if(fragmentCheckoutBinding.layoutPayment.getVisibility() == View.VISIBLE )
//                                {
//                                    if(fragmentCheckoutBinding.cbPaymentMethod.isChecked()) {
//                                        checkoutObserver.SetBillingAddress(null,Integer.parseInt(selected_shipping_id_original),true);
//                                    }else{
//                                        List<String> address_list = new ArrayList<>();
//                                        address_list.add(fragmentCheckoutBinding.etStreetAddress.getText().toString());
//                                        if(!fragmentCheckoutBinding.etStreetAddressOptional.getText().toString().equals(""))
//                                            address_list.add(fragmentCheckoutBinding.etStreetAddressOptional.getText().toString());
//
//
//                                        shipping_address_new   = CartAddressInput.builder().city(fragmentCheckoutBinding.etCity.getText().toString())
//                                                .companyInput(new Input<>("",true))
//                                                .country_code(fragmentCheckoutBinding.spinnerCountries.getSelectedItem().toString())
//                                                .firstname(fragmentCheckoutBinding.etFirstName.getText().toString())
//                                                .lastname(fragmentCheckoutBinding.etLastName.getText().toString())
//                                                .postcode("")
//                                                .region("")
//                                                .region_id(null)
//                                                .save_in_address_book(false)
//                                                .street(address_list)
//                                                .telephone(fragmentCheckoutBinding.etPhoneNumber.getText().toString());
//
//                                        checkoutObserver.SetBillingAddress(null,Integer.parseInt(selected_shipping_id_original),false);
//
//
//                                    }
//
//                                }
//                                else {
//                                    checkoutObserver.SetBillingAddress(null,Integer.parseInt(selected_shipping_id_original),true);
//                                }
//                            }
//                            else if (s.equals("Payment Method failure")){
//                                checkoutObserver.SetPaymentMethodOnCart("cashondelivery");
//                            }
//                            else if (s.equals("Shipping Method failure")){
//                                checkoutObserver.SetShippingMethodOnCart(method_code,carrier_code);
//                            }
//                            else if(s.equals("Shipping Address failure")){
//                                shipping_address_selected   = CartAddressInput.builder().city(customer_shipping_address.city()).companyInput(new Input<>("",true))
//                                        .country_code(customer_shipping_address.country().code()).firstname(customer_shipping_address.firstname()).lastname(customer_shipping_address.lastname())
//                                        .postcode(customer_shipping_address.postcode()).region(customer_shipping_address.region().code())
//                                        .region_id(customer_shipping_address.region().region_id()).save_in_address_book(false)
//                                        .street(customer_shipping_address.street()).telephone(customer_shipping_address.telephone());
//
//                                CartAddressInput bb = shipping_address_selected.build();
//                                checkoutObserver.SetCustomerShippingAddress(Integer.parseInt(selected_shipping_id),"","");
//                            }
//                            else if(s.equals("Unable to place order: The shipping method is missing. Select the shipping method and try again."))
//                            {
////                                checkoutObserver.GetCartDetails();
//
//                                checkoutObserver.SetShippingMethodOnCart(method_code,carrier_code);
//                            }

//                                AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
//                                builder1.setMessage("Your order has been placed successfully.");
//                                builder1.setCancelable(false);
//                                builder1.setPositiveButton("Ok",
//                                        new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int id) {
//                                                NavHostFragment.findNavController(CheckoutFragment.this).navigate(R.id.action_checkoutFragment_to_homeFragment);
//                                                dialog.cancel();
//                                            }
//                                        });
//                                AlertDialog alert11 = builder1.create();
//                                alert11.show();
                    }
                });
            }
        });

        return fragmentCheckoutBinding.getRoot();
    }

    private void setCartSummary() {

        CartSummaryAdapter cartSummaryAdapter = new CartSummaryAdapter(requireContext(),cartItemsList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false);
        fragmentCheckoutBinding.rvCartSummaryItems.setLayoutManager(linearLayoutManager);
        fragmentCheckoutBinding.rvCartSummaryItems.setAdapter(cartSummaryAdapter);
        fragmentCheckoutBinding.tvSavedAmount.setText("PKR " + String.format("%.2f", saved_total));

    }

    private void setCalenderLayout() {

        Calendar mCalendar = Calendar.getInstance();
//        String today = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(mCalendar.getTime());

        int year = mCalendar.get(Calendar.YEAR);

        int month = mCalendar.get(Calendar.MONTH);

        int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);


        fragmentCheckoutBinding.tvDate.setText(month+"/"+dayOfMonth+"/"+year);

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

    }

    private void setPaymentMethod() {

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.countries_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fragmentCheckoutBinding.spinnerCountriesA.setAdapter(adapter);
        fragmentCheckoutBinding.spinnerCountriesB.setAdapter(adapter);
        fragmentCheckoutBinding.spinnerCountries.setAdapter(adapter);


        //
//        checkoutObserver.GetListOfAvailablePaymentMethods();
        checkoutObserver.getCustomerWallet();
        checkoutObserver.getCustomerWalletData().observe(getViewLifecycleOwner(), new Observer<GetCustomerWalletQuery.Wallet>() {
            @Override
            public void onChanged(GetCustomerWalletQuery.Wallet wallet) {

                progressDialog.dismissDialog();
                fragmentCheckoutBinding.tvWalletValue.setText("Your credit is: "+ wallet.wallet_amount());
            }
        });



        fragmentCheckoutBinding.cbApplyWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.StartLoadingdialog();
                checkoutObserver.ApplyWallet(fragmentCheckoutBinding.cbApplyWallet.isChecked());
            }
        });
        checkoutObserver.getApplyWalletQueryResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.length()>0) {
                    progressDialog.dismissDialog();

                    if (s.contains("The current user cannot perform")) {

                        fragmentCheckoutBinding.cbApplyWallet.setChecked(false);
                        signInDialog();

                    } else if(s.contains("Failed to execute HTTP")){

                        fragmentCheckoutBinding.cbApplyWallet.setChecked(false);
                        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();

                    }else if (s.equals("Success")) {

                        if(fragmentCheckoutBinding.cbApplyWallet.isChecked()){
                            String wallet_msg = "Pay amount with wallet : " + cartPreference.GetNeedToPay() +"\n"+"Order amount : "+
                                    cartPreference.GetOrderTotal() + "\n"+ "Amount left in wallet : "+cartPreference.GetRemainingWalletAmount();
                            fragmentCheckoutBinding.tvWalletValue.setText(wallet_msg);
                            fragmentCheckoutBinding.tvWalletValueS.setText("PKR " + cartPreference.GetNeedToPay());

                        }
                        else {
                            fragmentCheckoutBinding.tvWalletValue.setText("Your credit is:" + " " + cartPreference.GetRemainingWalletAmount());
                            fragmentCheckoutBinding.tvWalletValueS.setText("PKR 0.0");

                        }

                        checkoutObserver.GetListOfAvailablePaymentMethods();

                    }else{
                        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });



        checkoutObserver.getGetAvailablePaymentMethods().observe(getViewLifecycleOwner(), new Observer<List<GetAvailablePaymentMethodsQuery.Available_payment_method>>() {
            @Override
            public void onChanged(List<GetAvailablePaymentMethodsQuery.Available_payment_method> available_payment_methods) {


                boolean showPaymentMethods = true;
                for(int i=0;i<available_payment_methods.size();i++){
                    if(available_payment_methods.get(i).title().equals("No Payment Information Required")) {
                        showPaymentMethods = false;
                        fragmentCheckoutBinding.cbApplyWallet.setChecked(true);
                        fragmentCheckoutBinding.tvWalletValueS.setText(cartPreference.GetNeedToPay());

                        String wallet_msg = "Pay amount with wallet : " + cartPreference.GetNeedToPay() +"\n"+"Order amount : "+
                                cartPreference.GetOrderTotal() + "\n"+ "Amount left in wallet : "+cartPreference.GetRemainingWalletAmount();
                        fragmentCheckoutBinding.tvWalletValue.setText(wallet_msg);

                    }
                }

                if(showPaymentMethods) {
//                    fragmentCheckoutBinding.tvNoPaymentInfoRequired.setVisibility(View.GONE);
                    fragmentCheckoutBinding.dividerPaymentMethod.setVisibility(View.VISIBLE);
                    if (available_payment_methods.size() > 0 && available_payment_methods.size() < 2) {
                        fragmentCheckoutBinding.rbSlectionPaymentMethodA.setText(available_payment_methods.get(0).title());
                        fragmentCheckoutBinding.layoutPaymentB.setVisibility(View.GONE);
                        fragmentCheckoutBinding.cbPaymentMethodA.setChecked(true);
                        fragmentCheckoutBinding.layoutNewAddressA.setVisibility(View.GONE);

                    }
                    else if (available_payment_methods.size() >= 2 && available_payment_methods.size() <= 3) {
                        fragmentCheckoutBinding.cbPaymentMethodA.setChecked(true);
                        fragmentCheckoutBinding.layoutPaymentA.setVisibility(View.VISIBLE);fragmentCheckoutBinding.layoutNewAddressA.setVisibility(View.GONE);

                        fragmentCheckoutBinding.layoutPaymentB.setVisibility(View.VISIBLE);
                        fragmentCheckoutBinding.cbPaymentMethodB.setVisibility(View.GONE);
                        fragmentCheckoutBinding.layoutNewAddressB.setVisibility(View.GONE);

                        fragmentCheckoutBinding.rbSlectionPaymentMethodA.setText(available_payment_methods.get(0).title());
                        fragmentCheckoutBinding.rbSlectionPaymentMethodB.setText(available_payment_methods.get(1).title());
                    }
                }
                else
                {
                    fragmentCheckoutBinding.layoutPayment.setVisibility(View.GONE);
                    fragmentCheckoutBinding.layoutPaymentA.setVisibility(View.GONE);
                    fragmentCheckoutBinding.layoutPaymentB.setVisibility(View.GONE);
                    fragmentCheckoutBinding.dividerPaymentMethod.setVisibility(View.GONE);
//                    fragmentCheckoutBinding.tvNoPaymentInfoRequired.setVisibility(View.VISIBLE);
                }

                progressDialog.dismissDialog();
            }
        });
        checkoutObserver.getPaymentMethodsResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.length()>0){
                    progressDialog.dismissDialog();
                    if(s.contains("The current user cannot perform")){
                        signInDialog();
                    }
                    else {
                        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        fragmentCheckoutBinding.cbPaymentMethod.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b)
                {
                    fragmentCheckoutBinding.layoutNewAddress.setVisibility(View.VISIBLE);
                }
                else
                {
                    fragmentCheckoutBinding.layoutNewAddress.setVisibility(View.GONE);

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

    }

    private void setShippingDetails() {
        if(is_address_changed && !(selected_shipping_id_original.equals(selected_shipping_id)))
        {
            fragmentCheckoutBinding.tvShippingAddress.setText(shipping_address);
            fragmentCheckoutBinding.tvPhoneNumber.setText(telephone);
            Toast.makeText(requireContext(), String.valueOf(is_address_changed),Toast.LENGTH_SHORT).show();

            selected_shipping_id_original = selected_shipping_id;
        }
        else {
            progressDialog.StartLoadingdialog();
            checkoutObserver.GetCustomerAddressDetails();
            checkoutObserver.GetCustomerAvailableAddresses();
        }
            checkoutObserver.getCustomerInfoResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    progressDialog.dismissDialog();
                    if (s.length() > 0) {
                        if (s.contains("The current user cannot perform")) {
                            signInDialog();
                        } else
                            Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
                    }

                }
            });

            checkoutObserver.getCustomerShippingData().observe(getViewLifecycleOwner(), new Observer<ShippingInformationFragment.Shipping_address>() {
                @Override
                public void onChanged(ShippingInformationFragment.Shipping_address customer) {

                    customer_shipping_address = customer;
//                String shipping_address = customer.firstname() + " " + customer.lastname() + "\n" +
//                        customer.street().get(0) + "\n" +
//                        customer.city() + ", " + customer.region().label() + " " +  customer.country().code()
//                        ;
//
//
//                fragmentCheckoutBinding.tvShippingAddress.setText(shipping_address);
//                fragmentCheckoutBinding.tvPhoneNumber.setText(customer.telephone());

                    progressDialog.dismissDialog();
                }
            });
            checkoutObserver.GetShippingMethod().observe(getViewLifecycleOwner(), new Observer<AvailableShippingMethodsCheckoutFragment.Available_shipping_method>() {
                @Override
                public void onChanged(AvailableShippingMethodsCheckoutFragment.Available_shipping_method available_shipping_method) {

                    fragmentCheckoutBinding.tvShippingMethod.setText(available_shipping_method.carrier_title() + " " + available_shipping_method.amount().currency() + " " +
                            available_shipping_method.amount().value().intValue());

                    carrier_code = available_shipping_method.carrier_code();
                    method_code = available_shipping_method.method_code();

                    progressDialog.dismissDialog();
                }
            });

            fragmentCheckoutBinding.tvChangeShippingAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NavHostFragment.findNavController(CheckoutFragment.this).navigate(R.id.action_checkoutFragment_to_customerAddressesFragment);
                }
            });


            checkoutObserver.getDefault_shipping_id().observe(getViewLifecycleOwner(), new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    if(getViewLifecycleOwner().getLifecycle().getCurrentState()== Lifecycle.State.RESUMED) {
                        selected_shipping_id = s;
                        selected_shipping_id_original = s;
                    }
                }
            });

            checkoutObserver.getDefault_shipping_Address().observe(getViewLifecycleOwner(), new Observer<GetCustomerAddressesQuery.Address>() {
                @Override
                public void onChanged(GetCustomerAddressesQuery.Address address) {
                    if(getViewLifecycleOwner().getLifecycle().getCurrentState()== Lifecycle.State.RESUMED) {

                        shipping_address = address.firstname() + " " + address.lastname() + "\n" +
                                address.street().get(0) + "\n" +
                                address.city() + ", " + address.region().region_code() + " " + address.country_code()
                        ;

                        fragmentCheckoutBinding.tvShippingAddress.setText(shipping_address);
                        fragmentCheckoutBinding.tvPhoneNumber.setText(address.telephone());
                    }
                }
            });
    }

    private void setTimeFrame() {
        fragmentCheckoutBinding.layoutTimeA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected_time_slot = fragmentCheckoutBinding.dateA.getText().toString();
                is_time_slot_selected = true;
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

                selected_time_slot = fragmentCheckoutBinding.dateB.getText().toString();
                is_time_slot_selected = true;

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

                selected_time_slot = fragmentCheckoutBinding.dateC.getText().toString();
                is_time_slot_selected = true;

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

                selected_time_slot = fragmentCheckoutBinding.dateD.getText().toString();
                is_time_slot_selected = true;

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
                if (fragmentCheckoutBinding.layoutTimeC.isEnabled()) {
                    fragmentCheckoutBinding.clockC.setImageDrawable(requireContext().getResources().getDrawable(R.drawable.ic_clock));
                    fragmentCheckoutBinding.dateC.setTextColor(requireContext().getResources().getColor(R.color.black));
                }
            }
        });

    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.YEAR,i);
        mCalendar.set(Calendar.MONTH,i1);
        mCalendar.set(Calendar.DAY_OF_MONTH,i2);

//        selectedDate = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(mCalendar.getTime());
        selectedDate = i1+"/"+i2+"/"+i;

        fragmentCheckoutBinding.tvDate.setText(selectedDate);
        isToday(DateUtils.isToday(mCalendar.getTime().getTime()));
    }
    public int getTimeNow(){
        Date currentTime = Calendar.getInstance().getTime();
        int hours = currentTime.getHours();

        if(hours<13) {
            timePeriod = "am";
//            Toast.makeText(requireContext(), String.valueOf(hours) + timePeriod, Toast.LENGTH_SHORT).show();
        }
        else
        {
            timePeriod = "pm";
//            Toast.makeText(requireContext(), String.valueOf(hours-12) + timePeriod, Toast.LENGTH_SHORT).show();
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
            builder1.setMessage(requireContext().getResources().getString(R.string.sign_in_again));
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