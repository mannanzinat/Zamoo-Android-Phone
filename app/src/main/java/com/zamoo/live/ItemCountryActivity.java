package com.zamoo.live;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.zamoo.live.adapters.FeaturedRadioAdapter;
import com.zamoo.live.adapters.HomePageAdapter;
import com.zamoo.live.adapters.LiveTvHomeAdapter;
import com.zamoo.live.models.CommonModels;
import com.zamoo.live.network.RetrofitClient;
import com.zamoo.live.network.apis.ItemCountryApi;
import com.zamoo.live.network.model.CommonModel;
import com.zamoo.live.network.model.ItemCountry;
import com.zamoo.live.network.model.RadioModel;
import com.zamoo.live.network.model.TvModel;
import com.zamoo.live.utils.ApiResources;
import com.zamoo.live.utils.BannerAds;
import com.zamoo.live.utils.Constants;
import com.zamoo.live.utils.NetworkInst;
import com.zamoo.live.utils.ToastMsg;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ItemCountryActivity extends AppCompatActivity {
    private ShimmerFrameLayout shimmerFrameLayout;
    private FirebaseAnalytics mFirebaseAnalytics;
    private CoordinatorLayout coordinatorLayout;
    private TextView tvNoItem;
    private RelativeLayout adView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView movie_recycler, tv_series_recycler, tv_recycler, radio_recycler;
    private List<CommonModels> movie_list = new ArrayList<>();
    private List<CommonModels> tv_series_list = new ArrayList<>();
    private List<CommonModels> tv_list = new ArrayList<>();
    private List<CommonModels> radio_list = new ArrayList<>();
    private HomePageAdapter adapterMovie, adapterSeries;
    private LiveTvHomeAdapter adapterTv;
    private FeaturedRadioAdapter adapterRadio;
    private RelativeLayout movie_rl, tvseries_rl, tv_rl, radio_rl;


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
        setContentView(R.layout.activity_item_country2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (!isDark) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        setSupportActionBar(toolbar);

        //---analytics-----------
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Item_country_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        getSupportActionBar().setTitle(getIntent().getStringExtra("title"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        adView = findViewById(R.id.adView);
        coordinatorLayout = findViewById(R.id.coordinator_lyt);
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();

        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        tvNoItem = findViewById(R.id.tv_noitem);

        //----country recycler view-----------------
        movie_rl = findViewById(R.id.movie_layout);
        movie_recycler = findViewById(R.id.movie_rv);
        movie_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        movie_recycler.setHasFixedSize(true);
        movie_recycler.setNestedScrollingEnabled(false);
        adapterMovie = new HomePageAdapter(this, movie_list);
        movie_recycler.setAdapter(adapterMovie);

        //----tv series recycler view-----------------
        tvseries_rl = findViewById(R.id.tvseries_layout);
        tv_series_recycler = findViewById(R.id.tvseries_rv);
        tv_series_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        tv_series_recycler.setHasFixedSize(true);
        tv_series_recycler.setNestedScrollingEnabled(false);
        adapterSeries = new HomePageAdapter(this, tv_series_list);
        tv_series_recycler.setAdapter(adapterSeries);

        //----featured tv recycler view-----------------
        tv_rl = findViewById(R.id.tv_layout);
        tv_recycler = findViewById(R.id.tv_rv);
        tv_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        tv_recycler.setHasFixedSize(true);
        tv_recycler.setNestedScrollingEnabled(false);
        adapterTv = new LiveTvHomeAdapter(this, tv_list, "MainActivity");
        tv_recycler.setAdapter(adapterTv);

        //-----Radio recycler view-----//
        radio_rl = findViewById(R.id.radio_layout);
        radio_recycler = findViewById(R.id.radio_rv);
        radio_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        radio_recycler.setHasFixedSize(true);
        radio_recycler.setNestedScrollingEnabled(false);
        adapterRadio = new FeaturedRadioAdapter(this, radio_list);
        radio_recycler.setAdapter(adapterRadio);


        if (new NetworkInst(this).isNetworkAvailable()) {
            initData();
        } else {
            tvNoItem.setText(getString(R.string.no_internet));
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            coordinatorLayout.setVisibility(View.VISIBLE);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                coordinatorLayout.setVisibility(View.GONE);

                movie_list.clear();
                tv_series_list.clear();
                tv_list.clear();
                radio_list.clear();
                movie_recycler.removeAllViews();
                tv_series_recycler.removeAllViews();
                tv_recycler.removeAllViews();
                radio_recycler.removeAllViews();

                adapterMovie.notifyDataSetChanged();
                adapterSeries.notifyDataSetChanged();
                adapterTv.notifyDataSetChanged();
                adapterRadio.notifyDataSetChanged();

                if (new NetworkInst(ItemCountryActivity.this).isNetworkAvailable()) {
                    initData();
                } else {
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

    private void loadAd() {
        if (ApiResources.adStatus.equals("1")) {

            if (ApiResources.adType.equals(Constants.ADMOB)) {
                BannerAds.ShowBannerAds(this, adView);
            } else if (ApiResources.adType.equals(Constants.START_APP)) {

                BannerAds.showStartAppBanner(this, adView);


            } else if (ApiResources.adType.equals(Constants.NETWORK_AUDIENCE)) {


            }

        }
    }

    private void initData() {
        String id = getIntent().getStringExtra("id");
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ItemCountryApi api = retrofit.create(ItemCountryApi.class);
        Call<ItemCountry> call = api.getContentByCountryId(Config.API_KEY, id);
        call.enqueue(new Callback<ItemCountry>() {
            @Override
            public void onResponse(Call<ItemCountry> call, Response<ItemCountry> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);

                        if (response.body().getMovies().size() == 0 && response.body().getTvseries().size() == 0 &&
                                response.body().getTv().size() == 0 && response.body().getRadio().size() == 0) {
                            swipeRefreshLayout.setRefreshing(false);
                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            coordinatorLayout.setVisibility(View.VISIBLE);
                            movie_rl.setVisibility(View.GONE);
                            tvseries_rl.setVisibility(View.GONE);
                            tv_rl.setVisibility(View.GONE);
                            radio_rl.setVisibility(View.GONE);
                            return;
                        }
                        //tv data
                        if (response.body().getTv().size() > 0) {
                            for (int i = 0; i < response.body().getTv().size(); i++) {
                                TvModel tvModel = response.body().getTv().get(i);
                                CommonModels models = new CommonModels();
                                models.setImageUrl(tvModel.getPosterUrl());
                                models.setTitle(tvModel.getTvName());
                                models.setVideoType("tv");
                                models.setReleaseDate("");
                                models.setQuality("");
                                models.setId(tvModel.getLiveTvId());
                                models.setIsPaid(tvModel.getIsPaid());
                                tv_list.add(models);
                            }
                        } else {
                            tv_rl.setVisibility(View.GONE);
                        }
                        //radio data

                        if (response.body().getRadio().size() != 0) {
                            for (int i = 0; i < response.body().getRadio().size(); i++) {
                                RadioModel radioModel = response.body().getRadio().get(i);
                                CommonModels models = new CommonModels();
                                models.setImageUrl(radioModel.getPosterUrl());
                                models.setTitle(radioModel.getRadioName());
                                models.setVideoType("radio");
                                models.setReleaseDate("");
                                models.setQuality("");
                                models.setId(radioModel.getRadioId());
                                models.setIsPaid(radioModel.getIsPaid());
                                radio_list.add(models);
                            }
                        } else {
                            radio_rl.setVisibility(View.GONE);
                        }
                        //tv series
                        if (response.body().getTvseries().size() > 0) {
                            for (int i = 0; i < response.body().getTvseries().size(); i++) {
                                CommonModel tvSeriesModel = response.body().getTvseries().get(i);
                                CommonModels models = new CommonModels();
                                models.setImageUrl(tvSeriesModel.getThumbnailUrl());
                                models.setTitle(tvSeriesModel.getTitle());
                                models.setVideoType("tvseries");
                                models.setReleaseDate(tvSeriesModel.getRelease());
                                models.setQuality(tvSeriesModel.getVideoQuality());
                                models.setId(tvSeriesModel.getVideosId());
                                models.setIsPaid(tvSeriesModel.getIsPaid());
                                tv_series_list.add(models);
                            }
                        } else {
                            tvseries_rl.setVisibility(View.GONE);
                        }
                        //movies data
                        if (response.body().getMovies().size() > 0) {
                            for (int i = 0; i < response.body().getMovies().size(); i++) {
                                CommonModel movieModel = response.body().getMovies().get(i);
                                CommonModels models = new CommonModels();
                                models.setImageUrl(movieModel.getThumbnailUrl());
                                models.setTitle(movieModel.getTitle());
                                models.setVideoType("movie");
                                models.setReleaseDate(movieModel.getRelease());
                                models.setQuality(movieModel.getVideoQuality());
                                models.setId(movieModel.getVideosId());
                                models.setIsPaid(movieModel.getIsPaid());
                                movie_list.add(models);
                            }
                        } else {
                            movie_rl.setVisibility(View.GONE);
                        }

                        adapterMovie.notifyDataSetChanged();
                        adapterSeries.notifyDataSetChanged();
                        adapterTv.notifyDataSetChanged();
                        adapterRadio.notifyDataSetChanged();
                    } else {
                        swipeRefreshLayout.setRefreshing(false);
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        new ToastMsg(ItemCountryActivity.this).toastIconError(getString(R.string.fetch_error));
                        coordinatorLayout.setVisibility(View.VISIBLE);
                        movie_rl.setVisibility(View.GONE);
                        tvseries_rl.setVisibility(View.GONE);
                        tv_rl.setVisibility(View.GONE);
                        radio_rl.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ItemCountry> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                new ToastMsg(ItemCountryActivity.this).toastIconError(getString(R.string.fetch_error));
                coordinatorLayout.setVisibility(View.VISIBLE);
                movie_rl.setVisibility(View.GONE);
                tvseries_rl.setVisibility(View.GONE);
                tv_rl.setVisibility(View.GONE);
                radio_rl.setVisibility(View.GONE);
            }
        });
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
