package com.zamoo.live.nav_fragments;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
import com.zamoo.live.EventPaymentStripeActivity;
import com.zamoo.live.MainActivity;
import com.zamoo.live.PapalPaymentActivity;
import com.zamoo.live.R;
import com.zamoo.live.adapters.EventAdapter;
import com.zamoo.live.adapters.PaymentInfoAdapter;
import com.zamoo.live.models.Event;
import com.zamoo.live.network.RetrofitClient;
import com.zamoo.live.network.apis.EventApi;
import com.zamoo.live.network.apis.PaymentApi;
import com.zamoo.live.network.apis.SubscriptionApi;
import com.zamoo.live.network.model.ActiveStatus;
import com.zamoo.live.utils.ApiResources;
import com.zamoo.live.utils.BannerAds;
import com.zamoo.live.utils.Constants;
import com.zamoo.live.utils.NetworkInst;
import com.zamoo.live.utils.ToastMsg;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventFragment extends Fragment implements EventAdapter.OnItemClickListener   {
    private static final int PAYPAL_REQUEST_CODE = 100;

    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList = new ArrayList<>();

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
    private View view;
    private Event event;
    private String currency = "";
    private String exchangeRate = "";

    PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(ApiResources.PAYPAL_CLIENT_ID);


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        activity.setTitle(R.string.event);
        view =  inflater.inflate(R.layout.fragment_event, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // getting currency symble from shared preference
        SharedPreferences pref = getActivity().getSharedPreferences("paymentConfig", getActivity().MODE_PRIVATE);
        currency = pref.getString("currencySymbol", "\\u00a3");
        exchangeRate = pref.getString("exchangeRate", "");



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
        adapter = new EventAdapter(activity, eventList);
        recyclerView.setAdapter(adapter);
        adapter.setItemClickListener(this);

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
            getEventData();
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
                eventList.clear();
                recyclerView.removeAllViews();
                adapter.notifyDataSetChanged();
                if (new NetworkInst(activity).isNetworkAvailable()){
                    getEventData();
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

    private void getEventData() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        EventApi api = retrofit.create(EventApi.class);
        Call<List<Event>> call = api.getAllEvent(Config.API_KEY);
        call.enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                if (response.code() == 200) {
                    eventList.addAll(response.body());

                    if (eventList.size() == 0) {
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
            public void onFailure(Call<List<Event>> call, Throwable t) {

            }
        });
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



    @Override
    public void onItemClick(Event event) {
        event = event;
        openPaymentDialog(event);

    }

    private void openPaymentDialog(final Event event) {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_payment_dialog);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        //list
        List<String> infoList = new ArrayList<>();
        String[] infoArray = getResources().getStringArray(R.array.payment_info);
        for (int i = 0; i < infoArray.length; i++){
            infoList.add(infoArray[i]);
        }

        RecyclerView recyclerView = dialog.findViewById(R.id.payment_info_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        PaymentInfoAdapter adapter = new PaymentInfoAdapter(getContext(), infoList);
        recyclerView.setAdapter(adapter);


        Button paypal_btn = dialog.findViewById(R.id.paypal_btn);
        paypal_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processPayPalPayment(event);
            }
        });

        Button stripeBtn = dialog.findViewById(R.id.stripe_btn);
        stripeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), EventPaymentStripeActivity.class);
                intent.putExtra("event", event);
                intent.putExtra("currency", currency);
                startActivity(intent);
            }
        });
        dialog.setCancelable(true);
        dialog.show();
    }

    private void processPayPalPayment(Event event) {

        PayPalPayment payPalPayment = new PayPalPayment((new BigDecimal(String.valueOf(event.getPrice()))), ApiResources.CURRENCY,
                "Payment for Event: "+ event.getEventName() , PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(getActivity(), PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }


    @Override
    public void onDestroyView() {
        getActivity().stopService(new Intent(getActivity(), PayPalService.class));
        super.onDestroyView();
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == PAYPAL_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentDetails = confirmation.toJSONObject().toString(4);

                        completePayment(paymentDetails);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    new ToastMsg(getContext()).toastIconError("Cancel");
                }
            }

        }else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            new ToastMsg(getContext()).toastIconError("Invalid");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void completePayment(String paymentDetails) {

        try {
            JSONObject jsonObject = new JSONObject(paymentDetails);
            sendDataToServer(jsonObject.getJSONObject("response"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private void sendDataToServer(JSONObject response) {

        try {

            String payId = response.getString("id");
            final String state = response.getString("state");

            //if(state.equals())

            SharedPreferences sharedPreferences = getContext().getSharedPreferences(Constants.USER_DATA, getContext().MODE_PRIVATE);
            final String userId = sharedPreferences.getString(Constants.USER_ID, "");

            Retrofit retrofit = RetrofitClient.getRetrofitInstance();
            PaymentApi paymentApi = retrofit.create(PaymentApi.class);
            Call<ResponseBody> call = paymentApi.saveEventPayment(Config.API_KEY, event.getEventId(), userId, event.getPrice(),
                    payId, "Paypal");
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.code() == 200) {
                        new ToastMsg(getActivity()).toastIconSuccess(getResources().getString(R.string.payment_success));

                        updateActiveStatus(userId);

                        Intent intent = new Intent(getActivity(), PapalPaymentActivity.class);
                        intent.putExtra("state", state);
                        intent.putExtra("amount", event.getPrice());
                        startActivity(intent);

                        getActivity().finish();
                    } else {
                        new ToastMsg(getActivity()).toastIconError("Something went wrong.");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updateActiveStatus(String userId) {

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(com.zamoo.live.Config.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    ActiveStatus activeStatus = response.body();
                    saveActiveStatus(activeStatus);
                } else {
                    new ToastMsg(getActivity()).toastIconError("Payment info not save to the own server. something went wrong.");
                }
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                new ToastMsg(getActivity()).toastIconError(t.getMessage());
                t.printStackTrace();
            }
        });

    }

    private void saveActiveStatus(ActiveStatus activeStatus) {
        SharedPreferences.Editor editor = getContext().getSharedPreferences(Constants.SUBSCRIPTION_STATUS, Context.MODE_PRIVATE).edit();
        editor.putString(Constants.SUBSCRIPTION_STATUS, activeStatus.getStatus());
        editor.apply();
    }


}
