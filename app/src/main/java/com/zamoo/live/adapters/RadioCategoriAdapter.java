package com.zamoo.live.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zamoo.live.R;
import com.zamoo.live.network.model.RadioCategory;

import java.util.List;

public class RadioCategoriAdapter extends RecyclerView.Adapter<RadioCategoriAdapter.ViewHolder>{

    private Context context;
    private List<RadioCategory> radioCategories;

    public RadioCategoriAdapter(Context context, List<RadioCategory> radioCategories) {
        this.context = context;
        this.radioCategories = radioCategories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_livetv_category_item, parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RadioCategory radioCategory = radioCategories.get(position);

        if (radioCategory != null) {

            holder.categoryNameTv.setText(radioCategory.getTitle());

            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            layoutManager.setOrientation(RecyclerView.HORIZONTAL);

            RadioAdapter radioAdapter = new RadioAdapter(context, radioCategory.getRadios());

            holder.channelRv.setLayoutManager(layoutManager);
            holder.channelRv.setAdapter(radioAdapter);
            holder.channelRv.setHasFixedSize(true);

        }

    }

    @Override
    public int getItemCount() {
        return radioCategories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView categoryNameTv;
        Button moreBt;
        RecyclerView channelRv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            categoryNameTv = itemView.findViewById(R.id.tv_name);
            moreBt = itemView.findViewById(R.id.btn_more);
            channelRv = itemView.findViewById(R.id.recyclerView);
            moreBt.setVisibility(View.GONE);
        }
    }
}
