package com.zamoo.live.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.islamkhsh.CardSliderAdapter;
import com.github.islamkhsh.CardSliderViewPager;
import com.ixidev.gdpr.GDPRChecker;
import com.zamoo.live.Config;
import com.zamoo.live.CountryActivity;
import com.zamoo.live.DetailsActivity;
import com.zamoo.live.EventActivity;
import com.zamoo.live.GenreActivity;
import com.zamoo.live.ItemCountryActivity;
import com.zamoo.live.ItemMovieActivity;
import com.zamoo.live.ItemRadioActivity;
import com.zamoo.live.ItemSeriesActivity;
import com.zamoo.live.ItemTVActivity;
import com.zamoo.live.LiveTvActivity;
import com.zamoo.live.LoginActivity;
import com.zamoo.live.MainActivity;
import com.zamoo.live.R;
import com.zamoo.live.adapters.CountryAdapter;
import com.zamoo.live.adapters.EventHomeAdapter;
import com.zamoo.live.adapters.FeaturedRadioAdapter;
import com.zamoo.live.adapters.GenreAdapter;
import com.zamoo.live.adapters.GenreHomeAdapter;
import com.zamoo.live.adapters.HomePageAdapter;
import com.zamoo.live.adapters.LiveTvHomeAdapter;
import com.zamoo.live.models.CommonModels;
import com.zamoo.live.models.GenreModel;
import com.zamoo.live.nav_fragments.CountryFragment;
import com.zamoo.live.nav_fragments.EventFragment;
import com.zamoo.live.nav_fragments.GenreFragment;
import com.zamoo.live.utils.PreferenceUtils;
import com.zamoo.live.utils.ApiResources;
import com.zamoo.live.utils.BannerAds;
import com.zamoo.live.utils.Constants;
import com.zamoo.live.utils.NetworkInst;
import com.zamoo.live.utils.ToastMsg;
import com.zamoo.live.utils.VolleySingleton;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;


public class HomeFragment extends Fragment {
    CardSliderViewPager cViewPager;

    private ArrayList<CommonModels> listSlider = new ArrayList<>();

    private Timer timer;

    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recyclerViewMovie, recyclerViewTv, recyclerViewRadio, recyclerViewEvent, recyclerViewTvSeries, recyclerViewGenre;
    private RecyclerView genreRv;
    private RecyclerView countryRv;
    private GenreAdapter genreAdapter;
    private CountryAdapter countryAdapter;
    private RelativeLayout genreLayout, countryLayout;
    private HomePageAdapter adapterMovie, adapterSeries;
    private LiveTvHomeAdapter adapterTv;
    private FeaturedRadioAdapter adapterRadio;
    private EventHomeAdapter adapterEvent;
    private List<CommonModels> listMovie = new ArrayList<>();
    private List<CommonModels> listTv = new ArrayList<>();
    private List<CommonModels> listEvent = new ArrayList<>();
    private List<CommonModels> listRadio = new ArrayList<>();
    private List<CommonModels> listSeries = new ArrayList<>();
    private List<CommonModels> genreList = new ArrayList<>();
    private List<CommonModels> countryList = new ArrayList<>();
    private ApiResources apiResources;
    private Button btnMoreMovie, btnMoreTv, btnMoreRadio, btnMoreSeries, btnMoreGenre, btnMoreCountry, btnMoreEvent;
    private CSliderAdapter cSliderAdapter;

    private VolleySingleton singleton;
    private TextView tvNoItem;
    private CoordinatorLayout coordinatorLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NestedScrollView scrollView;

    private RelativeLayout adView, adView1;
    private List<GenreModel> listGenre = new ArrayList<>();
    private GenreHomeAdapter genreHomeAdapter;
    private View sliderLayout;

    private MainActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        activity = (MainActivity) getActivity();
        activity.setTitle(R.string.app_name);
        return inflater.inflate(R.layout.fragment_home, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiResources = new ApiResources();

        singleton = new VolleySingleton(getActivity());

        adView              = view.findViewById(R.id.adView);
        adView1             = view.findViewById(R.id.adView1);
        btnMoreSeries       = view.findViewById(R.id.btn_more_series);
        btnMoreTv           = view.findViewById(R.id.btn_more_tv);
        btnMoreRadio        = view.findViewById(R.id.btn_more_radio);
        btnMoreMovie        = view.findViewById(R.id.btn_more_movie);
        btnMoreGenre        = view.findViewById(R.id.btn_more_genre);
        btnMoreCountry      = view.findViewById(R.id.btn_more_country);
        btnMoreEvent        = view.findViewById(R.id.btn_more_event);
        shimmerFrameLayout  = view.findViewById(R.id.shimmer_view_container);
        tvNoItem            = view.findViewById(R.id.tv_noitem);
        coordinatorLayout   = view.findViewById(R.id.coordinator_lyt);
        swipeRefreshLayout  = view.findViewById(R.id.swipe_layout);
        scrollView          = view.findViewById(R.id.scrollView);
        sliderLayout        = view.findViewById(R.id.slider_layout);
        genreRv             = view.findViewById(R.id.genre_rv);
        countryRv           = view.findViewById(R.id.country_rv);
        genreLayout         = view.findViewById(R.id.genre_layout);
        countryLayout       = view.findViewById(R.id.country_layout);
        cViewPager          = view.findViewById(R.id.c_viewPager);

        if (!Constants.IS_GENRE_SHOW) {
            genreLayout.setVisibility(View.GONE);
        }
        if (!Constants.IS_COUNTRY_SHOW) {
            countryLayout.setVisibility(View.GONE);
        }

        //pageTitle.setText(getResources().getString(R.string.home));

        if (activity.isDark) {
           // pageTitle.setTextColor(activity.getResources().getColor(R.color.white));
            //searchBar.setCardBackgroundColor(activity.getResources().getColor(R.color.black_window_light));
            //menuIv.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_menu));
            //searchIv.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_search_white));
        }


        //indicator.setViewPager(viewPager);

        //----init timer slider--------------------
        timer = new Timer();


        //----btn click-------------
        btnClick();

        // --- genre recycler view ---------
        genreRv.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false));
        genreRv.setHasFixedSize(true);
        genreRv.setNestedScrollingEnabled(false);
        genreAdapter = new GenreAdapter(getActivity(), genreList, "genre", "home");
        genreRv.setAdapter(genreAdapter);

        // --- country recycler view ---------
        countryRv.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false));
        countryRv.setHasFixedSize(true);
        countryRv.setNestedScrollingEnabled(false);
        countryAdapter = new CountryAdapter(getActivity(), countryList);
        countryRv.setAdapter(countryAdapter);

        //----featured tv recycler view-----------------
        recyclerViewTv = view.findViewById(R.id.recyclerViewTv);
        recyclerViewTv.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewTv.setHasFixedSize(true);
        recyclerViewTv.setNestedScrollingEnabled(false);
        adapterTv = new LiveTvHomeAdapter(getContext(), listTv, "MainActivity");
        recyclerViewTv.setAdapter(adapterTv);

        //-----Radio recycler view-----//
        recyclerViewRadio = view.findViewById(R.id.recyclerViewRadio);
        recyclerViewRadio.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewRadio.setHasFixedSize(true);
        recyclerViewRadio.setNestedScrollingEnabled(false);
        adapterRadio = new FeaturedRadioAdapter(getContext(), listRadio );
        recyclerViewRadio.setAdapter(adapterRadio);

        //-----Event recycler view-----//
        recyclerViewEvent = view.findViewById(R.id.recyclerViewEvent);
        recyclerViewEvent.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewEvent.setHasFixedSize(true);
        recyclerViewEvent.setNestedScrollingEnabled(false);
        adapterEvent = new EventHomeAdapter(getContext(), listEvent, "MainActivity");
        recyclerViewEvent.setAdapter(adapterEvent);

        //----movie's recycler view-----------------
        recyclerViewMovie = view.findViewById(R.id.recyclerView);
        recyclerViewMovie.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewMovie.setHasFixedSize(true);
        recyclerViewMovie.setNestedScrollingEnabled(false);
        adapterMovie = new HomePageAdapter(getContext(), listMovie);
        recyclerViewMovie.setAdapter(adapterMovie);

        //----series's recycler view-----------------
        recyclerViewTvSeries = view.findViewById(R.id.recyclerViewTvSeries);
        recyclerViewTvSeries.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewTvSeries.setHasFixedSize(true);
        recyclerViewTvSeries.setNestedScrollingEnabled(false);
        adapterSeries = new HomePageAdapter(getActivity(), listSeries);
        recyclerViewTvSeries.setAdapter(adapterSeries);

        //----genre's recycler view--------------------
        recyclerViewGenre = view.findViewById(R.id.recyclerView_by_genre);
        recyclerViewGenre.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewGenre.setHasFixedSize(true);
        recyclerViewGenre.setNestedScrollingEnabled(false);
        genreHomeAdapter = new GenreHomeAdapter(getContext(), listGenre);
        recyclerViewGenre.setAdapter(genreHomeAdapter);


        shimmerFrameLayout.startShimmer();


        if (new NetworkInst(getContext()).isNetworkAvailable()) {

            if (Constants.IS_GENRE_SHOW) {
                getAllGenre();
            }
            if (Constants.IS_COUNTRY_SHOW) {
                getAllCountry();
            }
            getFeaturedTV();
            getFeaturedRadio();
            getEvent();
            getSlider(apiResources.getSlider());
            getLatestSeries();
            getLatestMovie();
            getDataByGenre();


        } else {
            tvNoItem.setText(getString(R.string.no_internet));
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            coordinatorLayout.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                recyclerViewMovie.removeAllViews();
                recyclerViewTv.removeAllViews();
                recyclerViewRadio.removeAllViews();
                recyclerViewEvent.removeAllViews();
                recyclerViewTvSeries.removeAllViews();
                recyclerViewGenre.removeAllViews();

                genreList.clear();
                countryList.clear();
                listMovie.clear();
                listSeries.clear();
                listSlider.clear();
                listTv.clear();
                listRadio.clear();
                listEvent.clear();
                listGenre.clear();


                if (new NetworkInst(getContext()).isNetworkAvailable()) {
                    if (Constants.IS_GENRE_SHOW) {
                        getAllGenre();
                    }
                    if (Constants.IS_COUNTRY_SHOW) {
                        getAllCountry();
                    }
                    getFeaturedTV();
                    getFeaturedRadio();
                    getSlider(apiResources.getSlider());
                    getEvent();
                    getLatestSeries();
                    getLatestMovie();
                    getDataByGenre();
                } else {
                    tvNoItem.setText(getString(R.string.no_internet));
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    coordinatorLayout.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.GONE);
                }
            }
        });


        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY < oldScrollY) { // up
                    //animateNavigation(false);
                    //animateSearchBar(false);
                }
                if (scrollY > oldScrollY) { // down
                   // animateNavigation(true);
                    //animateSearchBar(false);
                }
            }
        });

        getAdDetails(new ApiResources().getAdDetails());

    }


    private void loadAd() {
        if (ApiResources.adStatus.equals("1")) {

            if (ApiResources.adType.equals(Constants.ADMOB)) {
                BannerAds.ShowBannerAds(activity, adView);
                BannerAds.ShowBannerAds(activity, adView1);
            } else if (ApiResources.adType.equals(Constants.START_APP)) {
                BannerAds.showStartAppBanner(activity, adView);
                BannerAds.showStartAppBanner(activity, adView1);


            } else if(ApiResources.adType.equals(Constants.NETWORK_AUDIENCE)) {

            }

        }
    }

    private void btnClick() {

        btnMoreMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ItemMovieActivity.class);
                intent.putExtra("url", apiResources.getGet_movie());
                intent.putExtra("title", "Movies");
                getActivity().startActivity(intent);
            }
        });

        btnMoreTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), LiveTvActivity.class);
                getActivity().startActivity(intent);

            }
        });

        btnMoreSeries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ItemSeriesActivity.class);
                intent.putExtra("url", apiResources.getTvSeries());
                intent.putExtra("title", "TV Series");
                getActivity().startActivity(intent);
            }
        });

        btnMoreRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ItemRadioActivity.class);
                intent.putExtra("url", apiResources.getFeaturedRadio());
                intent.putExtra("title", "Radio");
                getActivity().startActivity(intent);
            }
        });

        btnMoreGenre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), GenreActivity.class));

            }
        });

        btnMoreCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CountryActivity.class));

            }
        });

        btnMoreEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), EventActivity.class));
            }
        });

       /* btnMoreEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ItemEventActivity.class);
                intent.putExtra("url", apiResources.getFeaturedRadio());
                intent.putExtra("title", "Event");
                getActivity().startActivity(intent);
            }
        });
*/
    }

    private boolean loadFragment(Fragment fragment){
        if (fragment!=null){
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container,fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void getAdDetails(String url) {
        JSONObject params = new JSONObject();
        try {
            params.put("user", "Mahfuz");
            params.put("pass", "fjaijf");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, params,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("admob");
                    ApiResources.adStatus = jsonObject.getString("status");
                    ApiResources.adMobBannerId = jsonObject.getString("admob_banner_ads_id");
                    ApiResources.adMobInterstitialId = jsonObject.getString("admob_interstitial_ads_id");
                    ApiResources.adMobPublisherId = jsonObject.getString("admob_publisher_id");

                    ApiResources.adType = Constants.ADMOB;

                    new GDPRChecker()
                            .withContext(activity)
                            .withPrivacyUrl(Config.TERMS_URL) // your privacy url
                            .withPublisherIds(ApiResources.adMobPublisherId) // your admob account Publisher id
                            .withTestMode("9424DF76F06983D1392E609FC074596C") // remove this on real project
                            .check();

                    loadAd();


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }

        } );

        Volley.newRequestQueue(getContext()).add(jsonObjectRequest);


    }

    private void getDataByGenre() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, new ApiResources().getGenreMovieURL(), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                for (int i = 0; i < response.length(); i++) {

                    try {

                        JSONObject jsonObject = response.getJSONObject(i);

                        GenreModel models = new GenreModel();

                        models.setName(jsonObject.getString("name"));
                        models.setId(jsonObject.getString("genre_id"));
                        JSONArray jsonArray = jsonObject.getJSONArray("videos");
                        //listGenreMovie.clear();
                        List<CommonModels> listGenreMovie = new ArrayList<>();
                        for (int j = 0; j < jsonArray.length(); j++) {

                            JSONObject movieObject = jsonArray.getJSONObject(j);

                            CommonModels commonModels = new CommonModels();

                            commonModels.setId(movieObject.getString("videos_id"));
                            commonModels.setTitle(movieObject.getString("title"));
                            commonModels.setIsPaid(movieObject.getString("is_paid"));

                            if (movieObject.getString("is_tvseries").equals("0")) {
                                commonModels.setVideoType("movie");
                            } else {
                                commonModels.setVideoType("tvseries");
                            }


                            commonModels.setReleaseDate(movieObject.getString("release"));
                            commonModels.setQuality(movieObject.getString("video_quality"));
                            commonModels.setImageUrl(movieObject.getString("thumbnail_url"));

                            listGenreMovie.add(commonModels);

                        }


                        models.setList(listGenreMovie);

                        listGenre.add(models);
                        genreHomeAdapter.notifyDataSetChanged();
//                        Log.e("LIST 2 SIZE ::", String.valueOf(listGenreMovie.size()));


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(jsonArrayRequest);


    }

    private void getSlider(String url) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    swipeRefreshLayout.setRefreshing(false);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);
                    coordinatorLayout.setVisibility(View.GONE);


                    if (response.getString("slider_type").equals("disable")) {
                        sliderLayout.setVisibility(View.GONE);
                    } else if (response.getString("slider_type").equals("movie")) {

                        JSONArray jsonArray = response.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            CommonModels models = new CommonModels();
                            models.setImageUrl(jsonObject.getString("poster_url"));
                            models.setTitle(jsonObject.getString("title"));
                            models.setVideoType("movie");
                            models.setId(jsonObject.getString("videos_id"));
                            models.setIsPaid(jsonObject.getString("is_paid"));
                            listSlider.add(models);
                        }


                    } else {
                        JSONArray jsonArray = response.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            CommonModels models = new CommonModels();
                            models.setImageUrl(jsonObject.getString("image_link"));
                            models.setTitle(jsonObject.getString("title"));
                            models.setVideoType("image");
                            //models.setIsPaid(jsonObject.getString("is_paid"));
                            listSlider.add(models);
                        }

                    }
                    cSliderAdapter = new CSliderAdapter(listSlider);
                    cViewPager.setAdapter(cSliderAdapter);
                    cSliderAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                swipeRefreshLayout.setRefreshing(false);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                coordinatorLayout.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.GONE);

            }
        });

        Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);


    }

    private void getLatestSeries() {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, apiResources.getLatestTvSeries(), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                swipeRefreshLayout.setRefreshing(false);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        CommonModels models = new CommonModels();
                        models.setImageUrl(jsonObject.getString("thumbnail_url"));
                        models.setTitle(jsonObject.getString("title"));
                        models.setVideoType("tvseries");
                        models.setReleaseDate(jsonObject.getString("release"));
                        models.setQuality(jsonObject.getString("video_quality"));
                        models.setId(jsonObject.getString("videos_id"));
                        models.setIsPaid(jsonObject.getString("is_paid"));
                        listSeries.add(models);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapterSeries.notifyDataSetChanged();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        singleton.addToRequestQueue(jsonArrayRequest);

    }

    private void getLatestMovie() {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, apiResources.getLatest_movie(), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                swipeRefreshLayout.setRefreshing(false);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        CommonModels models = new CommonModels();
                        models.setImageUrl(jsonObject.getString("thumbnail_url"));
                        models.setTitle(jsonObject.getString("title"));
                        models.setVideoType("movie");
                        models.setReleaseDate(jsonObject.getString("release"));
                        models.setQuality(jsonObject.getString("video_quality"));
                        models.setId(jsonObject.getString("videos_id"));
                        models.setIsPaid(jsonObject.getString("is_paid"));
                        listMovie.add(models);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapterMovie.notifyDataSetChanged();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        singleton.addToRequestQueue(jsonArrayRequest);

    }

    private void getFeaturedRadio(){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, apiResources.getFeaturedRadio(), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                swipeRefreshLayout.setRefreshing(false);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        CommonModels models = new CommonModels();
                        models.setImageUrl(jsonObject.getString("poster_url"));
                        models.setTitle(jsonObject.getString("radio_name"));
                        models.setVideoType("radio");
                        models.setId(jsonObject.getString("radio_id"));
                        models.setIsPaid(jsonObject.getString("is_paid"));
                        listRadio.add(models);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapterRadio.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        singleton.addToRequestQueue(jsonArrayRequest);
    }

    private void getEvent() {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, apiResources.getFeaturedEvent(), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                swipeRefreshLayout.setRefreshing(false);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                String savedEventList = PreferenceUtils.getEvents(getContext());
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        CommonModels models = new CommonModels();
                        models.setImageUrl(jsonObject.getString("poster_url"));
                        models.setTitle(jsonObject.getString("event_name"));
                        models.setVideoType("event");
                        models.setId(jsonObject.getString("event_id"));
                        models.setIsPaid(jsonObject.getString("is_paid"));
                        models.setPrice(jsonObject.getString("price"));


                        if (models.getIsPaid().equals("0") || savedEventList.contains(models.getId())){
                            listEvent.add(models);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapterEvent.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        singleton.addToRequestQueue(jsonArrayRequest);
    }

    private void getFeaturedTV() {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, apiResources.getGet_featured_tv(), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                swipeRefreshLayout.setRefreshing(false);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        CommonModels models = new CommonModels();
                        models.setImageUrl(jsonObject.getString("poster_url"));
                        models.setTitle(jsonObject.getString("tv_name"));
                        models.setVideoType("tv");
                        models.setId(jsonObject.getString("live_tv_id"));
                        models.setIsPaid(jsonObject.getString("is_paid"));
                        listTv.add(models);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapterTv.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        singleton.addToRequestQueue(jsonArrayRequest);

    }

    @Override
    public void onStart() {
        super.onStart();

        /*menuIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.openDrawer();
            }
        });*/



        shimmerFrameLayout.startShimmer();
    }

    @Override
    public void onPause() {
        super.onPause();
        shimmerFrameLayout.stopShimmer();
        timer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public class CSliderAdapter extends CardSliderAdapter<CommonModels> {


        public CSliderAdapter(@NotNull ArrayList<CommonModels> items) {
            super(items);
        }

        @Override
        public void bindView(final int i, @NotNull View view, @org.jetbrains.annotations.Nullable final CommonModels commonModels) {

            if (commonModels != null) {
                TextView textView = view.findViewById(R.id.textView);

                textView.setText(commonModels.getTitle());

                ImageView imageView = view.findViewById(R.id.imageview);

                Picasso.get().load(commonModels.getImageUrl()).into(imageView);
                View lyt_parent = view.findViewById(R.id.lyt_parent);

                lyt_parent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (commonModels.getVideoType().equals("movie")){
                            if (PreferenceUtils.isMandatoryLogin(getContext())){
                                if (PreferenceUtils.isLoggedIn(getContext())){
                                    Intent intent=new Intent(getContext(), DetailsActivity.class);
                                    intent.putExtra("vType",commonModels.getVideoType());
                                    intent.putExtra("id",commonModels.getId());

                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    getContext().startActivity(intent);
                                }else {
                                    getContext().startActivity(new Intent(getContext(), LoginActivity.class));
                                }
                            }else {
                                Intent intent=new Intent(getContext(), DetailsActivity.class);
                                intent.putExtra("vType",commonModels.getVideoType());
                                intent.putExtra("id",commonModels.getId());

                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                getContext().startActivity(intent);
                            }
                        }
                    }
                });
            }

        }

        @Override
        public int getItemContentLayout(int i) {
            return R.layout.item_slider;
        }

    }

    private void getAllGenre() {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, apiResources.getAllGenre(), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (String.valueOf(response).length() < 10) {
                    coordinatorLayout.setVisibility(View.VISIBLE);
                } else {
                    coordinatorLayout.setVisibility(View.GONE);
                }

                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        CommonModels models = new CommonModels();
                        models.setId(jsonObject.getString("genre_id"));
                        models.setTitle(jsonObject.getString("name"));
                        models.setImageUrl(jsonObject.getString("image_url"));
                        models.setPosterUrl(jsonObject.getString("image_url"));
                        genreList.add(models);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //Toast.makeText(activity, "size:" + genreList.size(), Toast.LENGTH_SHORT).show();
                genreAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipeRefreshLayout.setRefreshing(false);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                new ToastMsg(getActivity()).toastIconError(getString(R.string.fetch_error));

                coordinatorLayout.setVisibility(View.VISIBLE);
            }
        });
        Volley.newRequestQueue(getContext()).add(jsonArrayRequest);


    }

    private void getAllCountry() {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, apiResources.getAllCountry(), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);


                if (String.valueOf(response).length() < 10) {
                    coordinatorLayout.setVisibility(View.VISIBLE);
                } else {
                    coordinatorLayout.setVisibility(View.GONE);
                }

                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        CommonModels models = new CommonModels();
                        models.setTitle(jsonObject.getString("name"));
                        models.setId(jsonObject.getString("country_id"));
                        models.setImageUrl(jsonObject.getString("image_url"));
                        models.setImageUrl(jsonObject.getString("image_url"));
                        countryList.add(models);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                countryAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipeRefreshLayout.setRefreshing(false);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                new ToastMsg(getActivity()).toastIconError(getString(R.string.fetch_error));
                coordinatorLayout.setVisibility(View.VISIBLE);

            }
        });
        Volley.newRequestQueue(getContext()).add(jsonArrayRequest);

    }

    boolean isSearchBarHide = false;

    /*private void animateSearchBar(final boolean hide) {
        if (isSearchBarHide && hide || !isSearchBarHide && !hide) return;
        isSearchBarHide = hide;
        int moveY = hide ? -(2 * searchRootLayout.getHeight()) : 0;
        searchRootLayout.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }
*/


}
