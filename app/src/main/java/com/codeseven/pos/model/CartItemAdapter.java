package com.codeseven.pos.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.CardviewCartItemBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private Context context;
    private ArrayList<CatalogItem> cartItemsArrayList;

    public CartItemAdapter(Context context, ArrayList<CatalogItem> cartItemsArrayList) {
        this.context = context;
        this.cartItemsArrayList = cartItemsArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_cart_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CatalogItem catalogItem = cartItemsArrayList.get(position);
        holder.cardviewCartItemBinding.tvProductName.setText(catalogItem.getItemName());
        holder.cardviewCartItemBinding.tvProductPrice.setText(catalogItem.getItemPrice());
        holder.cardviewCartItemBinding.etQuantity.setText(catalogItem.getItemDescription());
        Picasso.get().load(catalogItem.getItemImage()).into(holder.cardviewCartItemBinding.ivProductImage);

    }

    @Override
    public int getItemCount() {
        return cartItemsArrayList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        CardviewCartItemBinding cardviewCartItemBinding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardviewCartItemBinding = DataBindingUtil.bind(itemView);
        }
    }
}
