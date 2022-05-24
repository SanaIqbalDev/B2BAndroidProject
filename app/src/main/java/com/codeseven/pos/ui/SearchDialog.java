package com.codeseven.pos.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.codeseven.pos.R;


public class SearchDialog extends DialogFragment {

    EditText etSearchQuery;
    Button btnSearch;
    ImageView btnCancel;


    public interface SearchQuerySubmitListener {
        void onStartSearch(String inputText);
        void onExitDialog();
    }

    public SearchDialog() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_search_dialog, container, false);

        btnSearch = root.findViewById(R.id.btn_search_by_phone);
        btnCancel = root.findViewById(R.id.btn_close);
        etSearchQuery = root.findViewById(R.id.et_search_query);


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchQuerySubmitListener listener = (SearchQuerySubmitListener) getParentFragment();
                listener.onStartSearch(etSearchQuery.getText().toString());
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return root;
    }
}