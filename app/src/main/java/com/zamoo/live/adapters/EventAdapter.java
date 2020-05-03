package com.zamoo.live.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
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
import com.zamoo.live.models.Event;
import com.zamoo.live.utils.PreferenceUtils;
import com.zamoo.live.utils.ItemAnimation;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder>{
    private Context ctx;
    private List<Event> eventList;
    private OnItemClickListener itemClickListener;
    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;

    public EventAdapter(Context ctx, List<Event> eventList) {
        this.ctx = ctx;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_event, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        //get currency symbol
        SharedPreferences preferences = ctx.getSharedPreferences("paymentConfig", Context.MODE_PRIVATE);
        String currencySymbol = preferences.getString("currencySymbol", null);

        final Event obj = (Event) eventList.get(position);

        holder.name.setText(obj.getEventName());
        holder.price.setText(currencySymbol + " " + obj.getPrice());
        holder.description.setText(obj.getDescription());

        Picasso.get().load(obj.getPosterUrl()).into(holder.image);

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventList = PreferenceUtils.getEvents(ctx);

                if (PreferenceUtils.isMandatoryLogin(ctx)) {
                    if (PreferenceUtils.isLoggedIn(ctx)) {
                        if (obj.getIsPaid().equals("1")) {
                            if (!eventList.equals("")) {
                                if (eventList.contains(obj.getEventId())) {
                                    if (PreferenceUtils.isValid(ctx)){
                                        //stored time is not more than one hour
                                        playContent(obj);
                                        Log.e("Subscription", "valid");
                                    }else {
                                        Log.e("Subscription", "is not valid");
                                        PreferenceUtils.updateSubscriptionStatus(ctx);
                                    }
                                } else {
                                    purchaseClicked(obj);
                                    Log.e("Subscription", "event id doesn't contain");
                                }
                            } else {
                                //event list empty
                                purchaseClicked(obj);
                                Log.e("Subscription", "event list empty");
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
                            if (eventList.contains(obj.getEventId())) {
                                if (PreferenceUtils.isValid(ctx)){
                                    //stored time is not more than one hour
                                    playContent(obj);
                                    Log.e("Subscription", "valid");
                                }else {
                                    Log.e("Subscription", "is not valid");
                                    PreferenceUtils.updateSubscriptionStatus(ctx);
                                }
                            } else {
                                purchaseClicked(obj);
                            }
                        } else {
                            Log.e("Subscription", "event list empty");
                            purchaseClicked(obj);
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

    private void playContent(Event obj){
        Intent intent=new Intent(ctx, DetailsActivity.class);
        intent.putExtra("vType","event");
        intent.putExtra("id",obj.getEventId());
        ctx.startActivity(intent);
    }

    private void purchaseClicked(Event event){
        if (itemClickListener != null) {
            itemClickListener.onItemClick(event);
        }

    }
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView name;
        public TextView description;
        public TextView price;
        public View lyt_parent;


        public ViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            name = v.findViewById(R.id.name);
            price = v.findViewById(R.id.price);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            description = v.findViewById(R.id.description);
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

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }


    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
