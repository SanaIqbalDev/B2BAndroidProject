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
import com.codeseven.pos.databinding.CardviewProductSelectionItemBinding;
import com.codeseven.pos.util.ProductSelectionListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import apollo.pos.GetAutocompleteResultsQuery;

public class ProductSelectionAdapter extends RecyclerView.Adapter<ProductSelectionAdapter.ViewHolder>{

    private Context context;
    private List<GetAutocompleteResultsQuery.Item> itemsList = new ArrayList<>();
    private ProductSelectionListener productSelectionListener;
    private int selectedPos = RecyclerView.NO_POSITION;

    public ProductSelectionAdapter(Context context, List<GetAutocompleteResultsQuery.Item> itemsList, ProductSelectionListener productSelectionListener) {
        this.context = context;
        this.itemsList = itemsList;
        this.productSelectionListener = productSelectionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_product_selection_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GetAutocompleteResultsQuery.Item item = itemsList.get(position);
        holder.binding.tvProductName.setText(item.name());
        holder.binding.tvProductPrice.setText(context.getResources().getString(R.string.pkr) +  item.price().regularPrice().amount().value());
        Picasso.get().load(item.small_image().url()).into(holder.binding.ivProductImage);
        holder.bind(itemsList.get(position),productSelectionListener);

        holder.itemView.setBackgroundColor(selectedPos == position ? context.getResources().getColor(R.color.primaryDarkColor) : Color.TRANSPARENT);
        if(selectedPos == position)
        {
            holder.binding.tvProductName.setTextColor(context.getResources().getColor(R.color.white));
            holder.binding.tvProductPrice.setTextColor(context.getResources().getColor(R.color.colorDivider));
        }
        else
        {
            holder.binding.tvProductName.setTextColor(context.getResources().getColor(R.color.black));
            holder.binding.tvProductPrice.setTextColor(context.getResources().getColor(R.color.primaryDarkColor));


        }

    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        CardviewProductSelectionItemBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }


        public void bind(GetAutocompleteResultsQuery.Item item, ProductSelectionListener productSelectionListener) {

            binding.cardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    notifyItemChanged(selectedPos);
                    selectedPos = getLayoutPosition();
                    notifyItemChanged(selectedPos);

//
//                    binding.cardview.setCardBackgroundColor(context.getResources().getColor(R.color.secondaryDarkColor));
//
                    productSelectionListener.onProductClicked(item);
                }
            });
        }
    }


}
