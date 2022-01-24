package com.codeseven.pos.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.CardviewAddressItemBinding;
import com.codeseven.pos.util.AddressItemClickListener;
import com.codeseven.pos.util.CartItemClickListener;

import java.util.ArrayList;

public class AddressItemAdapter extends RecyclerView.Adapter<AddressItemAdapter.AddressViewHolder> {


    private Context context;
    private ArrayList<String> addressesArrayList;
    private AddressItemClickListener clickListener;

    public AddressItemAdapter(Context context, ArrayList<String> addressesArrayList, AddressItemClickListener clickListener) {
        this.context = context;
        this.addressesArrayList = addressesArrayList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_address_item,parent,false);
        return new AddressViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {

        holder.cardviewAddressItemBinding.tvCustomerAddress.setText(addressesArrayList.get(position));
        holder.bind(addressesArrayList.get(position), clickListener);


    }

    @Override
    public int getItemCount() {
        return addressesArrayList.size();
    }


    public class AddressViewHolder extends RecyclerView.ViewHolder{

        CardviewAddressItemBinding cardviewAddressItemBinding;
        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);

            cardviewAddressItemBinding = DataBindingUtil.bind(itemView);
        }

        public void bind(String item, AddressItemClickListener addressItemClickListener){
            addressItemClickListener.onItemClicked(item);
        }
    }
}
