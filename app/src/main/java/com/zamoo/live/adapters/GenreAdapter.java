package com.zamoo.live.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.zamoo.live.ItemMovieActivity;
import com.zamoo.live.R;
import com.zamoo.live.models.CommonModels;
import com.zamoo.live.utils.ItemAnimation;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.ViewHolder> {


    private Context context;
    private List<CommonModels> commonModels;
    private String type;
    private String layout;
    private int c;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;

    public GenreAdapter(Context context, List<CommonModels> commonModels, String type, String layout) {
        this.context = context;
        this.commonModels = commonModels;
        this.type = type;
        this.layout = layout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_genre_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final CommonModels commonModel = commonModels.get(position);
        if (commonModel != null) {

            holder.cardView.requestFocus();

            holder.nameTv.setText(commonModel.getTitle());
            Picasso.get()
                    .load(commonModel.getImageUrl())
                    .resize(Double.valueOf(122).intValue(), 70)
                    .centerCrop()
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(holder.background_image);

            holder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent=new Intent(context, ItemMovieActivity.class);
                    intent.putExtra("id",commonModel.getId());
                    intent.putExtra("title",commonModel.getTitle());
                    intent.putExtra("type",type);
                    context.startActivity(intent);
                }
            });

        }

        setAnimation(holder.itemView, position);

    }

    @Override
    public int getItemCount() {
        return commonModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv;
        CardView cardView;
        RelativeLayout itemLayout;
        ImageView background_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.genre_name_tv);
            cardView = itemView.findViewById(R.id.card_view);
            background_image = itemView.findViewById(R.id.background_image);
            itemLayout = itemView.findViewById(R.id.item_layout);

        }
    }

    private int getColor(){

        int colorList[] = {R.color.red_400, R.color.blue_400, R.color.indigo_400, R.color.orange_400, R.color.light_green_400, R.color.blue_grey_400};
        int colorList2[] = {R.drawable.gradient_1 , R.drawable.gradient_2, R.drawable.gradient_3, R.drawable.gradient_4, R.drawable.gradient_5, R.drawable.gradient_6};

        if (c >= 6){
            c = 0;
        }

        int color = colorList[c];
        c++;

        return color;

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
