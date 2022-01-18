package com.codeseven.pos.model;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codeseven.pos.util.ProductSelectionListener;

import java.util.ArrayList;
import java.util.List;

import apollo.pos.GetAutocompleteResultsQuery;

public class ProductSelectionAdapter extends RecyclerView.Adapter<ProductSelectionAdapter.ViewHolder>{

    private Context context;
    private List<GetAutocompleteResultsQuery.Item> itemsList = new ArrayList<>();
    private ProductSelectionListener productSelectionListener;

    public ProductSelectionAdapter(Context context, List<GetAutocompleteResultsQuery.Item> itemsList, ProductSelectionListener productSelectionListener) {
        this.context = context;
        this.itemsList = itemsList;
        this.productSelectionListener = productSelectionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


}
