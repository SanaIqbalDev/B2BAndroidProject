package com.codeseven.pos.ui;

import static com.codeseven.pos.ui.CartFragment.est_total;
import static com.codeseven.pos.ui.CartFragment.saved_total;
import static com.codeseven.pos.ui.CartFragment.sub_total;
import static com.codeseven.pos.ui.CustomerAddressesFragment.telephone;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.apollographql.apollo.api.Input;
import com.codeseven.pos.R;
import com.codeseven.pos.databinding.FragmentCheckoutBinding;
import com.codeseven.pos.model.CartSummaryAdapter;
import com.codeseven.pos.model.CatalogItem;
import com.codeseven.pos.util.CartPreference;
import com.codeseven.pos.util.CartViewModel;
import com.codeseven.pos.util.CheckOutViewModel;
import com.codeseven.pos.util.OrderPreference;

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

    public Boolean isFullWalletEnabled = true;
    public Boolean isPartialWalletEnabled = true;

    public CheckoutFragment() {
    }

    public static CheckoutFragment newInstance() {
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


        // Payment Method Logic...

        setPaymentMethod();

        setCartSummary();

        fragmentCheckoutBinding.btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!is_time_slot_selected) {
                    Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.select_time_slot), Toast.LENGTH_LONG).show();
                }
                else {
                    progressDialog.StartLoadingdialog();
                    // 1. Setting Shipping Address...
                    checkoutObserver.SetCustomerShippingAddress(Integer.parseInt(selected_shipping_id), "", "");

                }
            }
        });

        return fragmentCheckoutBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        checkoutObserver.getGetPlaceOrderResponseMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(getViewLifecycleOwner().getLifecycle().getCurrentState()== Lifecycle.State.RESUMED)
                {
                }
                if(s.contains("The cart isn't active")) {
                    progressDialog.dismissDialog();
                    cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
                    customerCartId = cartPreference.GetCartId("cart_id");
                    cartObserver.getCartItems(customerCartId);
                    cartObserver.getCartRequestResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
                        @Override
                        public void onChanged(String s) {
                            if(s.length()>0)
                                Toast.makeText(requireContext(),s, Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else if(s.contains("Network error") || s.contains("http")){
                    Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.check_internet_connection), Toast.LENGTH_LONG).show();
                }
                else if(s.equals("Shipping Address success")){
                    progressDialog.setDialogMessage("Shipping address is set on cart...");
                    checkoutObserver.SetShippingMethodOnCart(method_code,carrier_code);
                }
                else if(s.equals("Shipping Method success")){
                    progressDialog.setDialogMessage("Shipping method is set on cart...");

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
                else if (s.equals("Billing Address Success")){
                    String comments_ = fragmentCheckoutBinding.etComments.getText().toString();
                    String date_ = fragmentCheckoutBinding.tvDate.getText().toString();
                    String time_slot_ = selected_time_slot;

                    if(is_time_slot_selected) {
                        checkoutObserver.applyDeliveryCart(comments_, date_, time_slot_);
                    }
                    else {
                        progressDialog.dismissDialog();
                        Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.select_time_slot), Toast.LENGTH_LONG).show();
                    }

                }
                else if(s.equals("Delivery Cart success")){

                    checkoutObserver.SetPaymentMethodOnCart("cashondelivery");

                }
                else if(s.equals("Payment Method success"))
                {
                    checkoutObserver.placeOrder();
                }
                else if(s.equals("order_success")){

                    progressDialog.dismissDialog();

                    OrderPreference orderPreference = new OrderPreference();
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
                    builder1.setMessage(requireContext().getResources().getString(R.string.your_order_is_placed_successfully) +"\n\n ("+ orderPreference.GetOrderId() +")" );
                    builder1.setCancelable(false);
                    builder1.setPositiveButton(requireContext().getResources().getString(R.string.ok),
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

                    Toast.makeText(requireContext(),requireContext().getResources().getString(R.string.order_placed_failed),Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void setCartSummary() {

        CartSummaryAdapter cartSummaryAdapter = new CartSummaryAdapter(requireContext(),cartItemsList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false);
        fragmentCheckoutBinding.rvCartSummaryItems.setLayoutManager(linearLayoutManager);
        fragmentCheckoutBinding.rvCartSummaryItems.setAdapter(cartSummaryAdapter);
        fragmentCheckoutBinding.tvSavedAmount.setText(String.format("%.2f", saved_total) +" " + requireContext().getResources().getString(R.string.pkr) );
        fragmentCheckoutBinding.tvSubtotalValue.setText(String.format("%.2f", sub_total) +" " + requireContext().getResources().getString(R.string.pkr) );
        fragmentCheckoutBinding.tvShippingValue.setText("50"  +" " + requireContext().getResources().getString(R.string.pkr) );
    }

    private void setCalenderLayout() {

        Calendar mCalendar = Calendar.getInstance();

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


        checkoutObserver.getCustomerWallet();
        checkoutObserver.getCustomerWalletData().observe(getViewLifecycleOwner(), new Observer<GetCustomerWalletQuery.Wallet>() {
            @Override
            public void onChanged(GetCustomerWalletQuery.Wallet wallet) {

                progressDialog.dismissDialog();
                fragmentCheckoutBinding.tvWalletValue.setText(requireContext().getResources().getString(R.string.your_credit_is)+
                        wallet.wallet_amount() +
                        requireContext().getResources().getString(R.string.pkr));
            }
        });



        fragmentCheckoutBinding.btnApplyFullWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.StartLoadingdialog();
                fragmentCheckoutBinding.llPartialAmount.setVisibility(View.GONE);

                if(isFullWalletEnabled) {
                    fragmentCheckoutBinding.btnApplyFullWallet.setBackgroundColor(requireContext().getResources().getColor(R.color.dark_gray));
                    checkoutObserver.ApplyWallet(true, "0");
                    isFullWalletEnabled = false ;

                }
                else
                {
                    fragmentCheckoutBinding.btnApplyFullWallet.setBackgroundColor(requireContext().getResources().getColor(R.color.basic));
                    checkoutObserver.ApplyWallet(false,"0");
                    isFullWalletEnabled = true ;
                }
            }
        });

        fragmentCheckoutBinding.btnApplyPartialWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentCheckoutBinding.etGetFromCredit.setText(String.valueOf(sub_total));
                fragmentCheckoutBinding.llPartialAmount.setVisibility(View.VISIBLE);
                if(isPartialWalletEnabled) {
                    fragmentCheckoutBinding.btnApplyPartialWallet.setBackgroundColor(requireContext().getResources().getColor(R.color.dark_gray));
                    isPartialWalletEnabled = false ;
                }
                else
                {
                    fragmentCheckoutBinding.btnApplyPartialWallet.setBackgroundColor(requireContext().getResources().getColor(R.color.basic));
                    isPartialWalletEnabled = true ;
                }
            }
        });
        fragmentCheckoutBinding.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.StartLoadingdialog();

                if(isPartialWalletEnabled) {
                    checkoutObserver.ApplyWallet(true, fragmentCheckoutBinding.etGetFromCredit.getText().toString());
                }
                else
                {
                    checkoutObserver.ApplyWallet(false,fragmentCheckoutBinding.etGetFromCredit.getText().toString());
                }

            }
        });
        checkoutObserver.getApplyWalletQueryResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.length()>0) {
                    progressDialog.dismissDialog();

                    if (s.contains("The current user cannot perform")) {

                        isFullWalletEnabled = true;

                        fragmentCheckoutBinding.btnApplyFullWallet.setBackgroundColor(requireContext().getResources().getColor(R.color.basic));

                        signInDialog();

                    } else if(s.contains("Network error") || s.contains("http")){
                        Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.check_internet_connection), Toast.LENGTH_LONG).show();
                        if(isFullWalletEnabled){
                            isFullWalletEnabled = false;
                            fragmentCheckoutBinding.btnApplyFullWallet.setBackgroundColor(requireContext().getResources().getColor(R.color.dark_gray));
                        }
                        else
                        {
                            isFullWalletEnabled = true;
                            fragmentCheckoutBinding.btnApplyFullWallet.setBackgroundColor(requireContext().getResources().getColor(R.color.basic));
                        }


                        if(isPartialWalletEnabled){
                            isPartialWalletEnabled = false;
                            fragmentCheckoutBinding.btnApplyPartialWallet.setBackgroundColor(requireContext().getResources().getColor(R.color.dark_gray));
                        }
                        else {
                            isPartialWalletEnabled = true;
                            fragmentCheckoutBinding.btnApplyFullWallet.setBackgroundColor(requireContext().getResources().getColor(R.color.basic));
                        }

//                        fragmentCheckoutBinding.cbApplyWalletComplete.setChecked(false);
                        isFullWalletEnabled = true;

                        fragmentCheckoutBinding.btnApplyFullWallet.setBackgroundColor(requireContext().getResources().getColor(R.color.basic));

                    }else if (s.equals("Success")) {

                        if(isFullWalletEnabled || isPartialWalletEnabled){
                            String wallet_msg = requireContext().getResources().getString(R.string.pay_amount_with_credit) +"  "+
                                    cartPreference.GetNeedToPay() +"  "+requireContext().getResources().getString(R.string.pkr)+"\n"+
                                    requireContext().getResources().getString(R.string.order_amount)+"  "+
                                    cartPreference.GetOrderTotal() +"  "+requireContext().getResources().getString(R.string.pkr);

                            fragmentCheckoutBinding.tvWalletValue.setText(wallet_msg);
                            fragmentCheckoutBinding.tvWalletValueS.setText(cartPreference.GetNeedToPay() + " " + requireContext().getResources().getString(R.string.pkr));
                            fragmentCheckoutBinding.tvTotalValue.setText(cartPreference.GetOrderTotal() +"  "+requireContext().getResources().getString(R.string.pkr));

                        }
                        else {
                            fragmentCheckoutBinding.tvWalletValue.setText(requireContext().getResources().getString(R.string.your_credit_is)+
                                    cartPreference.GetRemainingWalletAmount()+
                                    requireContext().getResources().getString(R.string.pkr));
                            fragmentCheckoutBinding.tvWalletValueS.setText(" 0.0" + requireContext().getResources().getString(R.string.pkr));
                            fragmentCheckoutBinding.tvTotalValue.setText(String.format("%.2f", est_total) +" " + requireContext().getResources().getString(R.string.pkr) );

                        }


                        checkoutObserver.GetListOfAvailablePaymentMethods();

                    }else{
                        Toast.makeText(requireContext(), s, Toast.LENGTH_LONG).show();
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
//                        fragmentCheckoutBinding.cbApplyWalletComplete.setChecked(true);
                        isFullWalletEnabled = false;

                        fragmentCheckoutBinding.btnApplyFullWallet.setBackgroundColor(requireContext().getResources().getColor(R.color.dark_gray));

                        fragmentCheckoutBinding.tvWalletValueS.setText(cartPreference.GetNeedToPay());

                        String wallet_msg = requireContext().getResources().getString(R.string.pay_amount_with_credit) +"  "+
                                cartPreference.GetNeedToPay() +"  "+requireContext().getResources().getString(R.string.pkr)+"\n"+
                                requireContext().getResources().getString(R.string.order_amount)+"  "+
                                cartPreference.GetOrderTotal() +"  "+requireContext().getResources().getString(R.string.pkr);

                        fragmentCheckoutBinding.tvWalletValue.setText(wallet_msg);

                    }
                }

                if(showPaymentMethods) {

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
                    else if(s.contains("Network error") || s.contains("http")){
                        Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.check_internet_connection), Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(requireContext(), s, Toast.LENGTH_LONG).show();
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
            Toast.makeText(requireContext(), String.valueOf(is_address_changed),Toast.LENGTH_LONG).show();

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
                        } else if(s.contains("Network error") || s.contains("http")){
                            Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.check_internet_connection), Toast.LENGTH_LONG).show();
                        }else
                            Toast.makeText(requireContext(), s, Toast.LENGTH_LONG).show();
                    }

                }
            });

            checkoutObserver.getCustomerShippingData().observe(getViewLifecycleOwner(), new Observer<ShippingInformationFragment.Shipping_address>() {
                @Override
                public void onChanged(ShippingInformationFragment.Shipping_address customer) {

                    customer_shipping_address = customer;
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

        selectedDate = i1+"/"+i2+"/"+i;

        fragmentCheckoutBinding.tvDate.setText(selectedDate);
        isToday(DateUtils.isToday(mCalendar.getTime().getTime()));
    }
    public int getTimeNow(){
        Date currentTime = Calendar.getInstance().getTime();
        int hours = currentTime.getHours();

        if(hours<13) {
            timePeriod = "am";
        }
        else
        {
            timePeriod = "pm";
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