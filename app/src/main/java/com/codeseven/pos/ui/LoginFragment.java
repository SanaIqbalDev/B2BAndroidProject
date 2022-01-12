package com.codeseven.pos.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
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

        FragmentLoginBinding fragmentLoginBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_login,
                container,false);


        if(loginViewModel == null)
            loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);


        fragmentLoginBinding.setViewModel(loginObserver);

        View view = fragmentLoginBinding.getRoot();

        fragmentLoginBinding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginObserver.verifyLoginInformation();
                progressDialog.StartLoadingdialog();

                loginObserver.getLoginResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        if(s.contains("Generated Token:"))
                        {
                            loginObserver.saveLoginData();
                            loginObserver.savePreferenceLoginState();

                            progressDialog.dismissDialog();
                            NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_loginFragment_to_homeFragment);
                            loginObserver.getLoginResponse().removeObservers(getViewLifecycleOwner());
                        }
                        else if(!s.equals(""))
                        {
                            progressDialog.dismissDialog();
                            Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        loginObserver.getLoginResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
            }
        });
        fragmentLoginBinding.executePendingBindings();
        return view;
    }
}