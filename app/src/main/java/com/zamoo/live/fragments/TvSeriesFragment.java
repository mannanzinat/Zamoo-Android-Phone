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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.zamoo.live.MainActivity;
import com.zamoo.live.R;
import com.zamoo.live.adapters.CommonGridAdapter;
import com.zamoo.live.models.CommonModels;
import com.zamoo.live.utils.ApiResources;
import com.zamoo.live.utils.BannerAds;
import com.zamoo.live.utils.Constants;
import com.zamoo.live.utils.NetworkInst;
import com.zamoo.live.utils.SpacingItemDecoration;
import com.zamoo.live.utils.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TvSeriesFragment extends Fragment {

    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recyclerView;
    private CommonGridAdapter mAdapter;
    private List<CommonModels> list =new ArrayList<>();

    private ApiResources apiResources;

    private String URL=null;
    private boolean isLoading=false;
    private ProgressBar progressBar;
    private int pageCount=1;
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
        activity.setTitle(R.string.tv_series);
        return inflater.inflate(R.layout.fragment_tvseries,null);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //getActivity().setTitle(getResources().getString(R.string.tv_series));

        initComponent(view);



    }

    private void initComponent(View view) {

        apiResources=new ApiResources();

        adView=view.findViewById(R.id.adView);
        progressBar=view.findViewById(R.id.item_progress_bar);
        shimmerFrameLayout=view.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();
        swipeRefreshLayout=view.findViewById(R.id.swipe_layout);
        coordinatorLayout=view.findViewById(R.id.coordinator_lyt);
        tvNoItem=view.findViewById(R.id.tv_noitem);




        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.addItemDecoration(new SpacingItemDecoration(3, Tools.dpToPx(getActivity(), 12), true));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        mAdapter = new CommonGridAdapter(getContext(), list);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && !isLoading) {

                    coordinatorLayout.setVisibility(View.GONE);

                    pageCount=pageCount+1;
                    isLoading = true;

                    progressBar.setVisibility(View.VISIBLE);

                    getData(apiResources.getTvSeries(),pageCount);
                }
            }

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

        if (new NetworkInst(getContext()).isNetworkAvailable()){
            getData(apiResources.getTvSeries(),pageCount);
        }else {
            tvNoItem.setText(getResources().getString(R.string.no_internet));
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            coordinatorLayout.setVisibility(View.VISIBLE);
        }


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                pageCount=1;
                coordinatorLayout.setVisibility(View.GONE);
                list.clear();
                recyclerView.removeAllViews();
                mAdapter.notifyDataSetChanged();
                if (new NetworkInst(getContext()).isNetworkAvailable()){
                    getData(apiResources.getTvSeries(),pageCount);
                }else {
                    tvNoItem.setText(getResources().getString(R.string.no_internet));
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

    private void getData(String url,int pageNum){

        String fullUrl = url+String.valueOf(pageNum);


        JsonArrayRequest jsonArrayRequest=new JsonArrayRequest(Request.Method.GET, fullUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                isLoading=false;
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                for (int i=0;i<response.length();i++){

                    try {
                        JSONObject jsonObject=response.getJSONObject(i);
                        CommonModels models =new CommonModels();
                        models.setImageUrl(jsonObject.getString("thumbnail_url"));
                        models.setTitle(jsonObject.getString("title"));
                        models.setVideoType("tvseries");
                        models.setReleaseDate(jsonObject.getString("release"));
                        models.setQuality(jsonObject.getString("video_quality"));
                        models.setId(jsonObject.getString("videos_id"));
                        list.add(models);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isLoading=false;
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (pageCount==1){
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        Volley.newRequestQueue(getContext()).add(jsonArrayRequest);

    }

 }
