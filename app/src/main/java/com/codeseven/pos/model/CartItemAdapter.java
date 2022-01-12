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
import com.codeseven.pos.util.CartItemClickListener;
import com.codeseven.pos.util.ItemClickListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private Context context;
    private ArrayList<CatalogItem> cartItemsArrayList;
    private CartItemClickListener onCartItemClickListener;

    public CartItemAdapter(Context context, ArrayList<CatalogItem> cartItemsArrayList, CartItemClickListener cartItemClickListener) {
        this.context = context;
        this.cartItemsArrayList = cartItemsArrayList;
        this.onCartItemClickListener = cartItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_cart_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CatalogItem cartItem = cartItemsArrayList.get(position);
        holder.cardviewCartItemBinding.tvProductName.setText(cartItem.getItemName());
        holder.cardviewCartItemBinding.tvProductPrice.setText(cartItem.getItemPrice());
        holder.cardviewCartItemBinding.etQuantity.setText(cartItem.getItemQuantity());
        Picasso.get().load(cartItem.getItemImage()).into(holder.cardviewCartItemBinding.ivProductImage);

        holder.bind(cartItemsArrayList.get(position),onCartItemClickListener);

//
//        holder.cardviewCartItemBinding.btnCartItemRemove.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });


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


        public void bind(CatalogItem item, CartItemClickListener listener){
            cardviewCartItemBinding.btnCartItemRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cartItemsArrayList.remove(getAdapterPosition());
                    notifyDataSetChanged();
                    listener.onItemCLicked(view, item);
                }
            });

            cardviewCartItemBinding.btnDecrementCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(Integer.parseInt(cardviewCartItemBinding.etQuantity.getText().toString()) > 1)
                    {
                        cardviewCartItemBinding.etQuantity.setText(String.valueOf(Integer.parseInt(cardviewCartItemBinding.etQuantity.getText().toString()) - 1));
                    }
                    listener.onItemCLicked(view, item);
                }
            });

            cardviewCartItemBinding.btnIncrementCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cardviewCartItemBinding.etQuantity.setText(String.valueOf(Integer.parseInt(cardviewCartItemBinding.etQuantity.getText().toString()) + 1));
                    listener.onItemCLicked(view, item);
                }
            });
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if(view== cardviewCartItemBinding.btnCartItemRemove)
//                        listener.onItemCLicked(item);
//                }
//            });
        }
    }
}
