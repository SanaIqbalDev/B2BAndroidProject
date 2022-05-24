package com.codeseven.pos.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
    ProgressDialog progressDialog;


    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();

        progressDialog = new ProgressDialog(requireActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentBaseBinding fragmentBaseBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_base, container, false);

        if (loginViewModel == null)
            loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        fragmentBaseBinding.setViewModel(loginObserver);
        View view = fragmentBaseBinding.getRoot();
        fragmentBaseBinding.executePendingBindings();

        String apniDukanEmail = requireActivity().getIntent().getStringExtra("ApniDukanEmail");
        String apniDukanPassword = requireActivity().getIntent().getStringExtra("ApniDukanPassword");
        if (apniDukanEmail != null && apniDukanPassword != null) {
            loginObserver.setUserEmail(apniDukanEmail);
            loginObserver.setUserPassword(apniDukanPassword);
            Toast.makeText(requireContext(), apniDukanEmail, Toast.LENGTH_SHORT).show();
            loginObserver.verifyLoginInformation();
        } else {
            new Handler().postDelayed(() -> {
                //Checking user Login state.
                if (!loginObserver.getPhoneNumberPreference().equals("") && !loginObserver.getPasswordPreference().equals("") && loginObserver.getPreferenceLoginState())
                    NavHostFragment.findNavController(BaseFragment.this).navigate(R.id.action_baseFragment_to_homeFragment);
                else
                    NavHostFragment.findNavController(BaseFragment.this).navigate(R.id.action_baseFragment_to_loginFragment);

            }, 2000);

        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loginObserver.getLoginResponse().observe(getViewLifecycleOwner(), s -> {
            if (getViewLifecycleOwner().getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
                if (s.contains("Generated Token:")) {
                    loginObserver.saveLoginData();
                    loginObserver.savePreferenceLoginState();
                    Navigation.findNavController(requireView()).navigate(R.id.homeFragment);

                    loginObserver.getLoginResponse().removeObservers(getViewLifecycleOwner());
                    progressDialog.dismissDialog();
                } else if (s.contains("Network error") || s.contains("http")) {
                    Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.check_internet_connection), Toast.LENGTH_LONG).show();
                } else if (!s.equals("")) {
                    Toast.makeText(requireContext(), s, Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}