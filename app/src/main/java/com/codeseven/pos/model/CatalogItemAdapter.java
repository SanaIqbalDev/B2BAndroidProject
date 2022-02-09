package com.codeseven.pos.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.CardviewCatalogItemBinding;
import com.codeseven.pos.util.ItemClickListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CatalogItemAdapter extends RecyclerView.Adapter<CatalogItemAdapter.ViewHolder>{


    private Context context;
    private ArrayList<CatalogItem> catalogItemsArrayList;
    private final ItemClickListener onItemClickListenerThis;

    public CatalogItemAdapter(Context context, ArrayList<CatalogItem> catalogItemsArrayList, ItemClickListener onItemClickListener) {
        this.context = context;
        this.catalogItemsArrayList = catalogItemsArrayList;
        onItemClickListenerThis = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_catalog_item,parent,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CatalogItem catalogItem = catalogItemsArrayList.get(position);
        holder.cardviewItemsBinding.tvProductName.setText(catalogItem.getItemName());
        holder.cardviewItemsBinding.tvProductPrice.setText(catalogItem.getItemMinimalPrice());
        Picasso.get().load(catalogItem.getItemImage()).into(holder.cardviewItemsBinding.ivProductImage);

        holder.bind(catalogItemsArrayList.get(position), onItemClickListenerThis);
    }

    @Override
    public int getItemCount() {
        return catalogItemsArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CardviewCatalogItemBinding cardviewItemsBinding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardviewItemsBinding = DataBindingUtil.bind(itemView);

        }

        public void bind(CatalogItem item, ItemClickListener listener){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClicked(item);
                }
            });
        }
    }
}
