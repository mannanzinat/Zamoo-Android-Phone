package com.zamoo.live.nav_fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zamoo.live.MainActivity;
import com.zamoo.live.R;
import com.zamoo.live.fragments.HomeFragment;
import com.zamoo.live.fragments.LiveTvFragment;
import com.zamoo.live.fragments.MoviesFragment;
import com.zamoo.live.fragments.TvSeriesFragment;

import static android.content.Context.MODE_PRIVATE;

public class MainHomeFragment extends Fragment {

    private MainActivity activity;
    private BottomNavigationView bottomNavigation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        activity = (MainActivity) getActivity();

        return inflater.inflate(R.layout.fragment_main_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


         bottomNavigation = view.findViewById(R.id.navigation_view);
         bottomNavigation.setSelectedItemId(R.id.nav_home);

        SharedPreferences sharedPreferences = activity.getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            bottomNavigation.setItemBackgroundResource(R.color.dark_theme_color);
        } else {
            bottomNavigation.setItemBackgroundResource(R.color.colorPrimary);

        }

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        loadFragment(new HomeFragment());
                        return true;
                    case R.id.nav_movie:
                        loadFragment(new MoviesFragment());
                        return true;
                    case R.id.nav_tv:
                        loadFragment(new LiveTvFragment());
                        return true;
                    case R.id.nav_tv_series:
                        loadFragment(new TvSeriesFragment());
                        return true;
                    case R.id.nav_event:
                        loadFragment(new EventFragment());
                        return true;
                }
                return false;
            }
        });



        loadFragment(new HomeFragment());

    }


    //----load fragment----------------------
    private boolean loadFragment(Fragment fragment) {

        if (fragment != null) {

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

            return true;
        }
        return false;

    }


    /*boolean isNavigationHide = false;

    private void animateNavigation(final boolean hide) {
        if (isNavigationHide && hide || !isNavigationHide && !hide) return;
        isNavigationHide = hide;
        int moveY = hide ? (2 * spaceNavigationView.getHeight()) : 0;
        spaceNavigationView.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }*/

}