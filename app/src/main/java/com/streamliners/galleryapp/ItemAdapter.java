package com.streamliners.galleryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.streamliners.galleryapp.databinding.ItemCardBinding;
import com.streamliners.galleryapp.models.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> implements Filterable {

    Context context;
    List<Item> itemList;
    List<Item> requiredItemList;

    /**
     * This is needed when we are fetching new image.
     * @param context
     * @param itemList
     */

    public ItemAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
        requiredItemList = new ArrayList<>(itemList);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardBinding binding = ItemCardBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {

        Item item = itemList.get(position);
        Glide.with(context)
                .load(item.imageUrl)
                .into(holder.b.imageView);
        holder.b.title.setText(item.label);
        holder.b.title.setBackgroundColor(item.color);

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


    static class ItemViewHolder extends RecyclerView.ViewHolder{
        ItemCardBinding b;
        public ItemViewHolder(ItemCardBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        //background thread
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Item> filterdata = new ArrayList<>() ;
            if(constraint == null || constraint.length() == 0){
                filterdata.addAll(requiredItemList);
            }
            else {
                for(Item item : requiredItemList){
                    if(item.label.toLowerCase().contains(constraint.toString().toLowerCase())){
                        filterdata.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filterdata;
            return results;
        }

        @Override
        //main thread
        protected void publishResults(CharSequence constraint, FilterResults results) {
            itemList.clear();
            itemList.addAll((List<Item>) results.values);
            notifyDataSetChanged();
        }
    };

    public void showSortedItems() {
        notifyDataSetChanged();
    }
}
