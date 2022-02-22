package com.codeseven.pos.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.CardviewCartSummaryItemBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CartSummaryAdapter  extends RecyclerView.Adapter<CartSummaryAdapter.SummaryViewHolder>{


    private Context context;
    private ArrayList<CatalogItem> cartItemsArrayList;

    public CartSummaryAdapter(Context context, ArrayList<CatalogItem> cartItemsArrayList) {
        this.context = context;
        this.cartItemsArrayList = cartItemsArrayList;
    }

    @NonNull
    @Override
    public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_cart_summary_item,parent,false);
        return new SummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
        CatalogItem cartItem = cartItemsArrayList.get(position);
        holder.binding.tvItemName.setText(cartItem.getItemName());
        holder.binding.tvItemPrice.setText(context.getResources().getString(R.string.pkr) + cartItem.getItemMinimalPrice());
        Picasso.get().load(cartItem.getItemImage()).into(holder.binding.ivItemImage);

    }

    @Override
    public int getItemCount() {
        return cartItemsArrayList.size();
    }

    public class SummaryViewHolder extends RecyclerView.ViewHolder{

        CardviewCartSummaryItemBinding binding;
        public SummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }
}
