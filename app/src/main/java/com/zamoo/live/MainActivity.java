package com.zamoo.live;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jem.rubberpicker.RubberRangePicker;
import com.zamoo.live.adapters.NavigationAdapter;
import com.zamoo.live.fragments.LiveTvFragment;
import com.zamoo.live.fragments.MoviesFragment;
import com.zamoo.live.fragments.TvSeriesFragment;
import com.zamoo.live.models.NavigationModel;
import com.zamoo.live.nav_fragments.CountryFragment;
import com.zamoo.live.nav_fragments.EventFragment;
import com.zamoo.live.nav_fragments.FavoriteFragment;
import com.zamoo.live.nav_fragments.GenreFragment;
import com.zamoo.live.nav_fragments.MainHomeFragment;
import com.zamoo.live.nav_fragments.RadioFragment;
import com.zamoo.live.utils.PreferenceUtils;
import com.zamoo.live.utils.Constants;
import com.zamoo.live.utils.NetworkInst;
import com.zamoo.live.utils.SpacingItemDecoration;
import com.zamoo.live.utils.ToastMsg;
import com.zamoo.live.utils.Tools;
import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.StartAppSDK;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Serializable {

    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private LinearLayout navHeaderLayout;

    private RecyclerView recyclerView;
    private NavigationAdapter mAdapter;
    private List<NavigationModel> list =new ArrayList<>();
    private NavigationView navigationView;
    private String[] navItemImage;

    private String[] navItemName2;
    private String[] navItemImage2;
    private boolean status=false;

    private FirebaseAnalytics mFirebaseAnalytics;
    public boolean isDark;
    private String navMenuStyle;

    private Switch themeSwitch;
    private final int PERMISSION_REQUEST_CODE = 100;
    private String searchType;
    private boolean [] selectedtype = new boolean[3]; // 0 for movie, 1 for series, 2 for live tv

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        SharedPreferences appConfigSF = getSharedPreferences(Constants.APP_CONFIG, MODE_PRIVATE);
        isDark = sharedPreferences.getBoolean("dark", false);
        
        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);


        // To resolve cast button visibility problem. Check Cast State when app is open.
        CastContext castContext = CastContext.getSharedInstance(this);
        castContext.getCastState();

        StartAppSDK.init(this, "210936089", false);
        StartAppAd.disableSplash();

        navMenuStyle = appConfigSF.getString(Constants.NAV_MENU_STYLE, "");

        //---analytics-----------
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "main_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        //----dark mode----------

        if (sharedPreferences.getBoolean("firstTime", true)) {
            showTermServicesDialog();
        }

        // checking storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkStoragePermission()) {
                createDownloadDir();
            } else {
                requestPermission();
            }
        } else {
            createDownloadDir();
        }


        //----init---------------------------
        navigationView = findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        navHeaderLayout = findViewById(R.id.nav_head_layout);
        themeSwitch = findViewById(R.id.theme_switch);

        if (isDark) {
            themeSwitch.setChecked(true);
        }else {
            themeSwitch.setChecked(false);
        }

        //----navDrawer------------------------
        navigationView.setNavigationItemSelectedListener(this);
        if (!isDark) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            navHeaderLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else {
            navHeaderLayout.setBackgroundColor(getResources().getColor(R.color.nav_head_bg));
        }


        //----fetch array------------
        String[] navItemName = getResources().getStringArray(R.array.nav_item_name);
        navItemImage=getResources().getStringArray(R.array.nav_item_image);

        navItemImage2=getResources().getStringArray(R.array.nav_item_image_2);
        navItemName2=getResources().getStringArray(R.array.nav_item_name_2);



        //----navigation view items---------------------
        recyclerView = findViewById(R.id.recyclerView);
        if (navMenuStyle.equals("grid")) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            recyclerView.addItemDecoration(new SpacingItemDecoration(2, Tools.dpToPx(this, 15), true));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            //recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        }
        recyclerView.setHasFixedSize(true);


        SharedPreferences prefs = getSharedPreferences(Constants.USER_DATA, MODE_PRIVATE);
        status = prefs.getBoolean(Constants.USER_LOGIN_STATUS,false);

        if (status){
            for (int i = 0; i< navItemName.length; i++){
                NavigationModel models =new NavigationModel(navItemImage[i], navItemName[i]);
                list.add(models);
            }
        }else {
            for (int i=0;i< navItemName2.length;i++){
                NavigationModel models =new NavigationModel(navItemImage2[i],navItemName2[i]);
                list.add(models);
            }
        }


        //set data and list adapter
        mAdapter = new NavigationAdapter(this, list, navMenuStyle);
        recyclerView.setAdapter(mAdapter);

        final NavigationAdapter.OriginalViewHolder[] viewHolder = {null};

        mAdapter.setOnItemClickListener(new NavigationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, NavigationModel obj, int position, NavigationAdapter.OriginalViewHolder holder) {

                //----------------------action for click items nav---------------------

                if (position==0){
                    loadFragment(new MainHomeFragment());
                }
                else if (position==1){
                    loadFragment(new MoviesFragment());
                }
                else if (position==2){
                    loadFragment(new TvSeriesFragment());
                }
                else if (position==3){
                    loadFragment(new LiveTvFragment());
                }
                else if (position == 4){
                    loadFragment(new RadioFragment());
                }
                else if (position == 5){
                    loadFragment(new GenreFragment());
                }
                else if (position==6){
                    loadFragment(new CountryFragment());
                }
                else if (position==7){
                    loadFragment(new EventFragment());

                }

                else {


                    if (status){

                        if (position==8){
                            Intent intent=new Intent(MainActivity.this,ProfileActivity.class);
                            startActivity(intent);
                        }
                        else if (position==9){
                            loadFragment(new FavoriteFragment());
                        }
                        else if (position==10){
                            Intent intent=new Intent(MainActivity.this, SubscriptionActivity.class);
                            startActivity(intent);
                        }
                        else if (position==11){
                            Intent intent=new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                        }
                        else if (position==12){

                            new AlertDialog.Builder(MainActivity.this).setMessage("Are you sure to logout ?")
                                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                            if (user != null){
                                                FirebaseAuth.getInstance().signOut();
                                            }
                                            SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_DATA, MODE_PRIVATE).edit();
                                            editor.putString(Constants.USER_NAME, null);
                                            editor.putString(Constants.USER_EMAIL, null);
                                            editor.putString(Constants.USER_ID, null);
                                            editor.putBoolean(Constants.USER_LOGIN_STATUS,false);
                                            editor.apply();

                                            PreferenceUtils.clearSubscriptionSavedData(MainActivity.this);

                                            Intent intent = new Intent(MainActivity.this,FirebaseSignUpActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    }).create().show();
                        }

                    }else {
                        if (position==8){
                            Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else if (position==9){
                            Intent intent=new Intent(MainActivity.this,DownloadActivity.class);
                            startActivity(intent);
                        }
                        else if (position==10){
                            Intent intent=new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                        }

                    }

                }

                //----behaviour of bg nav items-----------------
                if (!obj.getTitle().equals("Settings") && !obj.getTitle().equals("Login") && !obj.getTitle().equals("Sign Out")){

                    if (isDark){
                        mAdapter.chanColor(viewHolder[0],position, R.color.nav_bg);
                    }else {
                        mAdapter.chanColor(viewHolder[0],position, R.color.white);
                    }


                    if (navMenuStyle.equals("grid")) {
                        holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        holder.name.setTextColor(getResources().getColor(R.color.white));
                    } else {
                        holder.selectedLayout.setBackground(getResources().getDrawable(R.drawable.round_grey_transparent));
                        holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }

                    viewHolder[0] =holder;
                }


                mDrawerLayout.closeDrawers();
            }
        });

        //----external method call--------------
        loadFragment(new MainHomeFragment());

        themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
                    editor.putBoolean("dark",true);
                    editor.apply();

                }else {
                    SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
                    editor.putBoolean("dark",false);
                    editor.apply();
                }

                mDrawerLayout.closeDrawers();
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                finish();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)  searchItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
                /*if (mSearchTerm == null && newFilter == null) {
                    return true;
                }
                if (mSearchTerm != null && mSearchTerm.equals(newFilter)) {
                    return true;
                }
                mSearchTerm = newFilter;
                mSearchQueryChanged = true;
                searchText(newText); //handle this*/
                return true;
            }
        });

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer();
                return true;

            case R.id.action_search:

                final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {

                        Intent intent=new Intent(MainActivity.this,SearchActivity.class);
                        intent.putExtra("q",s);
                        startActivity(intent);

                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });

                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private boolean loadFragment(Fragment fragment){

        if (fragment!=null){

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container,fragment)
                    .commit();

            return true;
        }
        return false;

    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawers();
        }else {

            new AlertDialog.Builder(MainActivity.this).setMessage("Do you want to exit ?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            finish();
                            System.exit(0);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create().show();

        }
    }


    //----nav menu item click---------------
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // set item as selected to persist highlight
        menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();
        return true;
    }

    private void showTermServicesDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_term_of_services);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        Button declineBt = dialog.findViewById(R.id.bt_decline);
        Button acceptBt = dialog.findViewById(R.id.bt_accept);

        if (isDark) {
            declineBt.setBackground(getResources().getDrawable(R.drawable.btn_rounded_grey_outline));
            acceptBt.setBackground(getResources().getDrawable(R.drawable.btn_rounded_dark));
        }

        ((ImageButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        acceptBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
                editor.putBoolean("firstTime",false);
                editor.apply();
                dialog.dismiss();
            }
        });

        declineBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }



    // ------------------ checking storage permission ------------
    private boolean checkStoragePermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");

                    // creating the download directory named oxoo
                    createDownloadDir();

                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    // creating download folder
    public void createDownloadDir() {

        if (Constants.SECURED_DOWNLOAD) {
            Constants.DOWNLOAD_DIR = this.getExternalCacheDir().toString() + File.separator;
        }

        File file = new File(Constants.DOWNLOAD_DIR,
                getResources().getString(R.string.app_name));
        if (!file.exists()) {
            Log.d("exist file:", "no");
            file.mkdirs();
        } else {
            Log.d("exist file:", "yes");
        }
    }

    public void showSearchDialog() {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View view1 = LayoutInflater.from(this).inflate(R.layout.layout_search_filter, null);
        builder.setView(view1);

        final EditText searchEt = view1.findViewById(R.id.search_et);
        final TextView minTv = view1.findViewById(R.id.min_tv);
        final TextView maxTv = view1.findViewById(R.id.max_tv);
        final Button searchBt = view1.findViewById(R.id.search_bt);
        final Button movieBt = view1.findViewById(R.id.movie_bt);
        final Button tvSeriesBt = view1.findViewById(R.id.tv_series_bt);
        final Button liveTvBt = view1.findViewById(R.id.live_tv_bt);
        final ImageView closeIv = view1.findViewById(R.id.close_iv);


        RubberRangePicker rangePicker = view1.findViewById(R.id.rangeSeekbar);
        rangePicker.setCurrentEndValue(2020);

        if (isDark) {
            searchEt.setBackground(getResources().getDrawable(R.drawable.edit_text_round_bd_dark));
        } else {
            closeIv.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN));
        }

        final android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();

        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog.dismiss();

            }
        });
        
        searchBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                searchType = "";

                if (selectedtype[0]) searchType+="movie";
                if (selectedtype[1]) searchType+="tvseries";
                if (selectedtype[2]) searchType+="tv";

                String range = minTv.getText().toString()+maxTv.getText().toString();
                String title = searchEt.getText().toString();

                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(searchType)) {
                    new ToastMsg(MainActivity.this).toastIconError(getResources()
                            .getString(R.string.searcError_message));
                    return;
                } else if (!new NetworkInst(MainActivity.this).isNetworkAvailable()) {
                    new ToastMsg(MainActivity.this).toastIconError(getResources()
                            .getString(R.string.no_internet));
                    return;
                }
                searchBt.setEnabled(false);


                /*new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ViewAnimation.fadeOut(lyt_progress);
                    }
                }, LOADING_DURATION);
*/
                Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                searchIntent.putExtra("range", range);
                searchIntent.putExtra("q", title);
                searchIntent.putExtra("type", searchType);
                startActivity(searchIntent);
                //searchBt.setEnabled(true);
                alertDialog.cancel();



            }
        });

        movieBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                toggleChange(movieBt);

            }
        });

        tvSeriesBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                toggleChange(tvSeriesBt);

            }
        });

        liveTvBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                toggleChange(liveTvBt);

            }
        });

        rangePicker.setOnRubberRangePickerChangeListener(new RubberRangePicker.OnRubberRangePickerChangeListener() {
            @Override
            public void onProgressChanged(@NotNull RubberRangePicker rubberRangePicker, int i, int i1, boolean b) {
                minTv.setText(i+"");
                maxTv.setText(i1+"");
            }

            @Override
            public void onStartTrackingTouch(@NotNull RubberRangePicker rubberRangePicker, boolean b) {

            }

            @Override
            public void onStopTrackingTouch(@NotNull RubberRangePicker rubberRangePicker, boolean b) {

            }
        });



    }

    private void toggleChange(Button b) {

        if (b.isSelected()) {
            b.setTextColor(getResources().getColor(R.color.grey_40));

            if (b.getText().equals(getResources().getString(R.string.movie))) {
                selectedtype[0] = false;
            } else if (b.getText().equals(getResources().getString(R.string.tv_series))) {
                selectedtype[1] = false;
            } else if (b.getText().equals(getResources().getString(R.string.live_tv))) {
                selectedtype[2] = false;
            }

        } else {
            b.setTextColor(Color.WHITE);
            if (b.getText().equals(getResources().getString(R.string.movie))) {
                selectedtype[0] = true;
            } else if (b.getText().equals(getResources().getString(R.string.tv_series))) {
                selectedtype[1] = true;
            } else if (b.getText().equals(getResources().getString(R.string.live_tv))) {
                selectedtype[2] = true;
            }
        }
        b.setSelected(!b.isSelected());
    }
}
