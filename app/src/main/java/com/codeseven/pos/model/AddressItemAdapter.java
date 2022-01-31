package com.codeseven.pos.model;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codeseven.pos.R;
import com.codeseven.pos.databinding.AddAddressItemBinding;
import com.codeseven.pos.databinding.CardviewAddressItemBinding;
import com.codeseven.pos.util.AddressItemClickListener;
import com.codeseven.pos.util.CartItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class AddressItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context context;
    private List<String> addressesArrayList;
    private AddressItemClickListener clickListener;

    private int selectedPos = 0;
    private boolean setChecked = false;

    private int viewTypeOne = 0;
    private int viewTypeTwo = 1;


    public AddressItemAdapter(Context context, List<String> addressesArrayList, AddressItemClickListener clickListener) {
        this.context = context;
        this.addressesArrayList = addressesArrayList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == viewTypeOne) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_address_item, parent, false);
            return new AddressViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_address_item,parent,false);
            return new AddNewAddressViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final int itemType = getItemViewType(position);


        if(itemType == viewTypeOne) {
            AddressViewHolder addressViewHolder = (AddressViewHolder) holder;

            addressViewHolder.cardviewAddressItemBinding.tvCustomerAddress.setText(addressesArrayList.get(position));
            addressViewHolder.bind(addressesArrayList.get(position), clickListener);
//        holder.cardviewAddressItemBinding.cardviewItem.setBackground(selectedPos == position ? context.getResources().getDrawable(R.drawable.border_selected) : context.getResources().getDrawable(R.drawable.border_unselected));


            if (selectedPos == position) {
                addressViewHolder.cardviewAddressItemBinding.ibEdit.setVisibility(View.VISIBLE);
                addressViewHolder.cardviewAddressItemBinding.cardviewItem.setBackground(context.getResources().getDrawable(R.drawable.border_selected));
            } else {
                addressViewHolder.cardviewAddressItemBinding.ibEdit.setVisibility(View.GONE);
                addressViewHolder.cardviewAddressItemBinding.cardviewItem.setBackground(context.getResources().getDrawable(R.drawable.border_unselected));

            }
//
//        if(selectedPos == RecyclerView.NO_POSITION && position == 0 )
//        {
//            if(!setChecked)
//                holder.cardviewAddressItemBinding.cardviewItem.setBackground(context.getResources().getDrawable(R.drawable.border_selected));
//            else
//                holder.cardviewAddressItemBinding.cardviewItem.setBackground(context.getResources().getDrawable(R.drawable.border_unselected));
//
//        }
//        if((selectedPos!=RecyclerView.NO_POSITION)  &&  selectedPos!=position)
//        {
//
//        }


            if (position == 0) {
                addressViewHolder.cardviewAddressItemBinding.tvDefault.setVisibility(View.VISIBLE);
            } else {
                addressViewHolder.cardviewAddressItemBinding.tvDefault.setVisibility(View.GONE);
            }
        }

        else
        {
            AddNewAddressViewHolder viewHolder = (AddNewAddressViewHolder)holder;
            viewHolder.bind(addressesArrayList.get(position), clickListener);


        }
    }


    @Override
    public int getItemViewType(int position) {

        if(position >= (addressesArrayList.size()-1))
            return viewTypeTwo;
        else
            return viewTypeOne;
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

            cardviewAddressItemBinding.cardviewItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    setChecked= true;
                    notifyItemChanged(selectedPos);
                    selectedPos = getLayoutPosition();
                    notifyItemChanged(selectedPos);
                    addressItemClickListener.onItemClicked(view, false, selectedPos);

                }
            });


            cardviewAddressItemBinding.ibEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    addressItemClickListener.onItemClicked(view, false,selectedPos);

                }
            });
        }
    }

    public class AddNewAddressViewHolder extends RecyclerView.ViewHolder{

        AddAddressItemBinding binding;
        public AddNewAddressViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);

        }
        public void bind(String item, AddressItemClickListener listener){
            binding.cardviewItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClicked(view, true, selectedPos);
                }
            });
        }


    }
}
