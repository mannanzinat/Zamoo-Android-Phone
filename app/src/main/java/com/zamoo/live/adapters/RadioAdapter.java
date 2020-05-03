package com.zamoo.live.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zamoo.live.DetailsActivity;
import com.zamoo.live.LoginActivity;
import com.zamoo.live.R;
import com.zamoo.live.network.model.RadioModel;
import com.zamoo.live.utils.PreferenceUtils;
import com.zamoo.live.utils.ItemAnimation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RadioAdapter extends RecyclerView.Adapter<RadioAdapter.OriginalViewHolder>{
    private List<RadioModel> items = new ArrayList<>();
    private Context ctx;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;

    public RadioAdapter(Context ctx, List<RadioModel> items) {
        this.items = items;
        this.ctx = ctx;
    }


    @NonNull
    @Override
    public OriginalViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RadioAdapter.OriginalViewHolder vh;
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_live_tv_home, viewGroup, false);
        vh = new RadioAdapter.OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull OriginalViewHolder holder, int position) {

        final RadioModel obj = (RadioModel) items.get(position);

        holder.name.setText(obj.getRadioName());

        Picasso.get().load(obj.getPosterUrl()).into(holder.image);

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (PreferenceUtils.isMandatoryLogin(ctx)){
                    if (PreferenceUtils.isLoggedIn(ctx)){
                        goToDetailsActivity(obj);
                    }else {
                        ctx.startActivity(new Intent(ctx, LoginActivity.class));
                    }
                }else {
                    goToDetailsActivity(obj);
                }
            }
        });

        setAnimation(holder.itemView, position);
    }

    private void goToDetailsActivity(RadioModel obj) {
        Intent intent=new Intent(ctx, DetailsActivity.class);
        intent.putExtra("vType","radio");
        intent.putExtra("id",obj.getRadioId());
        ctx.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView name;
        public TextView counter;
        public View lyt_parent;


        public OriginalViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            name = v.findViewById(R.id.name);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                on_attach = false;
                super.onScrollStateChanged(recyclerView, newState);
            }

        });



        super.onAttachedToRecyclerView(recyclerView);
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            ItemAnimation.animate(view, on_attach ? position : -1, animation_type);
            lastPosition = position;
        }
    }
}
