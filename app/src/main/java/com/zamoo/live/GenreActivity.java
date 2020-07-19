package com.zamoo.live;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.zamoo.live.adapters.CountryAdapter;
import com.zamoo.live.adapters.GenreAdapter;
import com.zamoo.live.models.CommonModels;
import com.zamoo.live.utils.ApiResources;
import com.zamoo.live.utils.BannerAds;
import com.zamoo.live.utils.Constants;
import com.zamoo.live.utils.NetworkInst;
import com.zamoo.live.utils.SpacingItemDecoration;
import com.zamoo.live.utils.ToastMsg;
import com.zamoo.live.utils.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GenreActivity extends AppCompatActivity {
    ShimmerFrameLayout shimmerFrameLayout;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ApiResources apiResources;
    private RecyclerView recyclerView;
    private List<CommonModels> list = new ArrayList<>();
    private GenreAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CoordinatorLayout coordinatorLayout;

    private TextView tvNoItem;
    private RelativeLayout adView;

    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (!isDark) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        setSupportActionBar(toolbar);

        //---analytics-----------
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "genre_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        getSupportActionBar().setTitle("Country");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();

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

        apiResources=new ApiResources();
        shimmerFrameLayout.startShimmer();


        if (new NetworkInst(GenreActivity.this).isNetworkAvailable()){
            getAllGenre();
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
                recyclerView.removeAllViews();
                list.clear();
                mAdapter.notifyDataSetChanged();

                if (new NetworkInst(GenreActivity.this).isNetworkAvailable()){
                    getAllGenre();
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

    private void initView() {
        adView = findViewById(R.id.adView);
        coordinatorLayout = findViewById(R.id.coordinator_lyt);
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();

        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        tvNoItem = findViewById(R.id.tv_noitem);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.addItemDecoration(new SpacingItemDecoration(3, Tools.dpToPx(GenreActivity.this, 10), true));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        mAdapter = new GenreAdapter(GenreActivity.this, list,"genre", "");
        recyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void loadAd(){
        if (ApiResources.adStatus.equals("1")) {

            if (ApiResources.adType.equals(Constants.ADMOB)) {
                BannerAds.ShowBannerAds(this, adView);
            } else if (ApiResources.adType.equals(Constants.START_APP)) {
                BannerAds.showStartAppBanner(this, adView);

            } else if(ApiResources.adType.equals(Constants.NETWORK_AUDIENCE)) {

            }
        }
    }

    private void getAllGenre(){

        JsonArrayRequest jsonArrayRequest=new JsonArrayRequest(Request.Method.GET, apiResources.getAllGenre(), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (String.valueOf(response).length()<10){
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }else {
                    coordinatorLayout.setVisibility(View.GONE);
                }

                for (int i=0;i<response.length();i++){

                    try {
                        JSONObject jsonObject=response.getJSONObject(i);
                        CommonModels models =new CommonModels();
                        models.setId(jsonObject.getString("genre_id"));
                        models.setTitle(jsonObject.getString("name"));
                        models.setImageUrl(jsonObject.getString("image_url"));
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
                swipeRefreshLayout.setRefreshing(false);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                new ToastMsg(GenreActivity.this).toastIconError(getString(R.string.fetch_error));

                coordinatorLayout.setVisibility(View.VISIBLE);
            }
        });
        Volley.newRequestQueue(GenreActivity.this).add(jsonArrayRequest);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}