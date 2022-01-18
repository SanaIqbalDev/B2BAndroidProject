package com.codeseven.pos.ui;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codeseven.pos.R;
import com.codeseven.pos.api.GetProductWithNameRepository;
import com.codeseven.pos.databinding.FragmentShortListedItemsBinding;
import com.codeseven.pos.util.GetProductByNameViewModel;

import java.util.List;

import javax.inject.Inject;

import apollo.pos.GetAutocompleteResultsQuery;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShortListedItemsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
public class ShortListedItemsFragment extends Fragment {

    FragmentShortListedItemsBinding fragmentShortListedItemsBinding;
    GetProductByNameViewModel getProductByNameViewModel;
    @Inject GetProductByNameViewModel.GetProductsByNameObserver getProductsByNameObserver;

    ProgressDialog progressDialog;
    private String itemName;
    public ShortListedItemsFragment() {
        // Required empty public constructor
    }

    public static ShortListedItemsFragment newInstance() {
        ShortListedItemsFragment fragment = new ShortListedItemsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        itemName = bundle.getString("itemName");
        getProductByNameViewModel = (new ViewModelProvider(requireActivity())).get(GetProductByNameViewModel.class);
        progressDialog = new ProgressDialog(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentShortListedItemsBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_short_listed_items,container,false);
        fragmentShortListedItemsBinding.setProductByNameViewModel(getProductsByNameObserver);


        progressDialog.StartLoadingdialog();
        getProductsByNameObserver.getProductsByName(itemName);

        getProductsByNameObserver.getItemsList().observe(getViewLifecycleOwner(), new Observer<List<GetAutocompleteResultsQuery.Item>>() {
            @Override
            public void onChanged(List<GetAutocompleteResultsQuery.Item> items) {
                progressDialog.dismissDialog();
                if(items.size()>0) {
                    String ab = items.get(0).name();
                    Toast.makeText(requireContext(), ab, Toast.LENGTH_SHORT).show();
                }
            }
        });


         return fragmentShortListedItemsBinding.getRoot();
    }
}