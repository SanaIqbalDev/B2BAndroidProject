package com.codeseven.pos.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.FragmentBaseBinding;
import com.codeseven.pos.util.LoginViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BaseFragment extends Fragment {



    Context context;
    LoginViewModel loginViewModel;
    @Inject
    LoginViewModel.LoginObserver loginObserver;


    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentBaseBinding fragmentBaseBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_base,container,false);

        if(loginViewModel == null)
            loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        fragmentBaseBinding.setViewModel(loginObserver);
        View view = fragmentBaseBinding.getRoot();
        fragmentBaseBinding.executePendingBindings();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Checking user Login state.
                if(!loginObserver.getPhoneNumberPreference().equals("") && !loginObserver.getPasswordPreference().equals("") && loginObserver.getPreferenceLoginState())
                    NavHostFragment.findNavController(BaseFragment.this).navigate(R.id.action_baseFragment_to_homeFragment);
                else
                    NavHostFragment.findNavController(BaseFragment.this).navigate(R.id.action_baseFragment_to_loginFragment);

            }
        }, 2000);




        return view;
    }
}