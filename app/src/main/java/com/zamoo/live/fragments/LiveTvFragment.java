package com.zamoo.live.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.zamoo.live.Config;
import com.zamoo.live.MainActivity;
import com.zamoo.live.R;
import com.zamoo.live.adapters.LiveTvCategoryAdapter;
import com.zamoo.live.network.RetrofitClient;
import com.zamoo.live.network.apis.LiveTvApi;
import com.zamoo.live.network.model.LiveTvCategory;
import com.zamoo.live.utils.ApiResources;
import com.zamoo.live.utils.BannerAds;
import com.zamoo.live.utils.Constants;
import com.zamoo.live.utils.NetworkInst;
import com.zamoo.live.utils.ToastMsg;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class LiveTvFragment extends Fragment {

    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recyclerView;
    private LiveTvCategoryAdapter adapter;
    private List<LiveTvCategory> liveTvCategories =new ArrayList<>();

    private ApiResources apiResources;

    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;




    private CoordinatorLayout coordinatorLayout;
    private TextView tvNoItem;

    private RelativeLayout adView;

    private MainActivity activity;



    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        activity.setTitle(R.string.live_tv);
        return inflater.inflate(R.layout.fragment_livetv,null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //getActivity().setTitle(getResources().getString(R.string.live_tv));

        initComponent(view);



         //is dark




    }


    private void initComponent(View view) {

        adView=view.findViewById(R.id.adView);
        apiResources=new ApiResources();
        shimmerFrameLayout=view.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();
        progressBar=view.findViewById(R.id.item_progress_bar);
        swipeRefreshLayout=view.findViewById(R.id.swipe_layout);
        coordinatorLayout=view.findViewById(R.id.coordinator_lyt);
        tvNoItem=view.findViewById(R.id.tv_noitem);


        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new LiveTvCategoryAdapter(activity, liveTvCategories);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {

                    controlsVisible = false;
                    scrolledDistance = 0;
                } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {

                    controlsVisible = true;
                    scrolledDistance = 0;
                }

                if((controlsVisible && dy>0) || (!controlsVisible && dy<0)) {
                    scrolledDistance += dy;
                }


            }
        });



        if (new NetworkInst(activity).isNetworkAvailable()){
            getLiveTvData();
        }else {
            tvNoItem.setText(getString(R.string.no_internet));
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            coordinatorLayout.setVisibility(View.VISIBLE);
        }


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                coordinatorLayout.setVisibility(View.GONE);
                liveTvCategories.clear();
                recyclerView.removeAllViews();
                adapter.notifyDataSetChanged();
                if (new NetworkInst(activity).isNetworkAvailable()){
                    getLiveTvData();
                }else {
                    tvNoItem.setText(getString(R.string.no_internet));
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        loadAd();

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void loadAd(){
        if (ApiResources.adStatus.equals("1")) {

            if (ApiResources.adType.equals(Constants.ADMOB)) {
                BannerAds.ShowBannerAds(activity, adView);
            } else if (ApiResources.adType.equals(Constants.START_APP)) {

                BannerAds.showStartAppBanner(activity, adView);


            } else if(ApiResources.adType.equals(Constants.NETWORK_AUDIENCE)) {


            }
        }
    }


    private void getLiveTvData() {

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        LiveTvApi api = retrofit.create(LiveTvApi.class);
        api.getLiveTvCategories(Config.API_KEY).enqueue(new Callback<List<LiveTvCategory>>() {
            @Override
            public void onResponse(Call<List<LiveTvCategory>> call, retrofit2.Response<List<LiveTvCategory>> response) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                if (response.code() == 200) {
                    liveTvCategories.addAll(response.body());

                    if (liveTvCategories.size() == 0) {
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    } else {
                        adapter.notifyDataSetChanged();
                    }

                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);

                    coordinatorLayout.setVisibility(View.VISIBLE);
                    tvNoItem.setText(getResources().getString(R.string.something_wront_text));
                    new ToastMsg(activity).toastIconError("Something went wrong...");
                }

            }

            @Override
            public void onFailure(Call<List<LiveTvCategory>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

                coordinatorLayout.setVisibility(View.VISIBLE);
                tvNoItem.setText(getResources().getString(R.string.something_wront_text));

                t.printStackTrace();
            }
        });

    }

}