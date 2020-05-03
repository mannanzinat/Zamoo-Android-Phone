package com.zamoo.live.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

public class EventHomeAdapter extends RecyclerView.Adapter<EventHomeAdapter.OriginalViewHolder> {

    private List<CommonModels> items = new ArrayList<>();
    private Context ctx;
    private String fromActivity;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;

    public EventHomeAdapter(Context context, List<CommonModels> items, String fromActivity) {
        this.items = items;
        ctx = context;
        this.fromActivity = fromActivity;
    }


    @Override
    public EventHomeAdapter.OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        EventHomeAdapter.OriginalViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_event_home, parent, false);
        vh = new EventHomeAdapter.OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(EventHomeAdapter.OriginalViewHolder holder, final int position) {

        final CommonModels obj = items.get(position);

        /*holder.name.setText(obj.getTitle());
        holder.price.setText(obj.getPrice());*/
        Picasso.get().load(obj.getImageUrl()).into(holder.image);
        holder.lyt_detail.setVisibility(View.GONE);

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String eventList = PreferenceUtils.getEvents(ctx);

                if (PreferenceUtils.isMandatoryLogin(ctx)) {
                    if (PreferenceUtils.isLoggedIn(ctx)) {
                        if (obj.getIsPaid().equals("1")) {
                            if (!eventList.equals("")) {
                                if (eventList.contains(obj.getId())) {
                                    if (PreferenceUtils.isValid(ctx)){
                                        //stored time is not more than one hour
                                        playContent(obj);
                                        Log.e("Subscription", "valid");
                                    }else {
                                        Log.e("Subscription", "is not valid");
                                        PreferenceUtils.updateSubscriptionStatus(ctx);
                                    }
                                } else {
                                    //user doesn't have subscription of this event
                                    ctx.startActivity(new Intent(ctx, SubscriptionActivity.class));
                                    Log.e("Subscription", "User doesn't have subscription of this event");
                                }
                            } else {
                                //event list empty
                                ctx.startActivity(new Intent(ctx, SubscriptionActivity.class));
                                Log.e("Subscription", "event list is empty");
                            }
                        } else {
                            Log.e("Subscription", "is not paid ");
                            //free content
                            playContent(obj);
                        }
                    } else {
                        Log.e("Subscription", "not loggedIn");
                        // go to login activity
                        ctx.startActivity(new Intent(ctx, LoginActivity.class));
                    }
                } else {
                    //login is mandatory
                    Log.e("Subscription", "not mandatory");
                    if (obj.getIsPaid().equals("1")) {
                        if (!eventList.isEmpty()) {
                            if (eventList.contains(obj.getId())) {
                                if (PreferenceUtils.isValid(ctx)){
                                    //stored time is not more than one hour
                                    playContent(obj);
                                    Log.e("Subscription", "valid");
                                }else {
                                    Log.e("Subscription", "is not valid");
                                    PreferenceUtils.updateSubscriptionStatus(ctx);
                                }
                            } else {
                                //user doesn't have subscription of this event
                                ctx.startActivity(new Intent(ctx, SubscriptionActivity.class));
                                Log.e("Subscription", "User doesn't have subscription of this event");
                            }
                        } else {
                            //event list empty
                            ctx.startActivity(new Intent(ctx, SubscriptionActivity.class));
                            Log.e("Subscription", "event list is empty");
                        }
                    } else {
                        Log.e("Subscription", "is not paid ");
                        //free content
                        playContent(obj);
                    }
                }


            }
        });

        setAnimation(holder.itemView, position);

    }

    private void playContent(CommonModels obj) {
        Intent intent = new Intent(ctx, DetailsActivity.class);
        intent.putExtra("vType", obj.getVideoType());
        intent.putExtra("id", obj.getId());
        if (fromActivity.equals(DetailsActivity.TAG)) {
            boolean castSession = ((DetailsActivity) ctx).getCastSession();
            //Toast.makeText(ctx, "castSession in"+castSession, Toast.LENGTH_SHORT).show();
            intent.putExtra("castSession", castSession);
        }

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
        public TextView price;
        public View lyt_parent;
        public View lyt_detail;


        public OriginalViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            name = v.findViewById(R.id.name);
            price = v.findViewById(R.id.price);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            lyt_detail = v.findViewById(R.id.lyt_detail);
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

