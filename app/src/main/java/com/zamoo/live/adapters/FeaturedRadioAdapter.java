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
import com.zamoo.live.SubscriptionActivity;
import com.zamoo.live.models.CommonModels;
import com.zamoo.live.utils.PreferenceUtils;
import com.zamoo.live.utils.ItemAnimation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FeaturedRadioAdapter extends RecyclerView.Adapter<FeaturedRadioAdapter.OriginalViewHolder>{

    private List<CommonModels> items = new ArrayList<>();
    private Context ctx;
    private String fromActivity;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;

    public FeaturedRadioAdapter(Context ctx, List<CommonModels> items) {
        this.items = items;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public OriginalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FeaturedRadioAdapter.OriginalViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_live_tv_home, parent, false);
        vh = new FeaturedRadioAdapter.OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull OriginalViewHolder holder, int position) {
        final CommonModels obj = items.get(position);

        holder.name.setText(obj.getTitle());
        Picasso.get().load(obj.getImageUrl()).into(holder.image);

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check app config data
                //mandatory login enabled or not
                if (PreferenceUtils.isMandatoryLogin(ctx)){
                    if (PreferenceUtils.isLoggedIn(ctx)){
                        if (obj.isPaid.equals("1")){
                            if (PreferenceUtils.isActivePlan(ctx)){
                                playContent(obj);
                             }else {
                                //user doesnt have subscription
                                ctx.startActivity(new Intent(ctx, SubscriptionActivity.class));
                            }
                        }else {
                            //content is not paid
                            playContent(obj);
                        }

                    }else {
                        //user not logged in
                        ctx.startActivity(new Intent(ctx, LoginActivity.class));
                    }
                }else {
                    playContent(obj);
                }


            }
        });

        setAnimation(holder.itemView, position);
    }

    private void playContent(CommonModels obj) {
        Intent intent=new Intent(ctx, DetailsActivity.class);
        intent.putExtra("vType", "radio");
        intent.putExtra("id",obj.getId());

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView name;
        public View lyt_parent;


        public OriginalViewHolder(View v) {
            super(v);
            image =v.findViewById(R.id.image);
            name =  v.findViewById(R.id.name);
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