package com.codeseven.pos.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.FragmentLoginBinding;
import com.codeseven.pos.util.LoginViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    Context context;
    LoginViewModel loginViewModel;
    @Inject
    LoginViewModel.LoginObserver loginObserver;

    ProgressDialog progressDialog;

    CheckBox cb_keep_login;

    FragmentLoginBinding fragmentLoginBinding;

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        progressDialog = new ProgressDialog(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentLoginBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_login,
                container, false);

        if (loginViewModel == null)
            loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);


        fragmentLoginBinding.setViewModel(loginObserver);

        View view = fragmentLoginBinding.getRoot();


        fragmentLoginBinding.btnLogin.setOnClickListener(view1 -> {
            loginObserver.verifyLoginInformation();
            progressDialog.StartLoadingdialog();
        });

        fragmentLoginBinding.executePendingBindings();
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
//                    loginObserver.setU;
                    NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_loginFragment_to_homeFragment);
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(loginObserver.getPhoneNumberPreference().equals("") && !loginObserver.getPasswordPreference().equals("") && loginObserver.getPreferenceLoginState())
            NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_loginFragment_to_homeFragment);
    }
}