package com.example.mapazimuth;

import android.support.v4.app.FragmentPagerAdapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class TabsPagerAdapter extends FragmentPagerAdapter {
    SupportMapFragment mapFragment;

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                // Top Rated fragment activity
                return new TopRatedFragment();
            case 1:
                // Games fragment activity
                return new GamesFragment();
            case 2:
                // Movies fragment activity
                //return new MoviesFragment();
                //return new GamesFragment();
                if( mapFragment==null ){
                    //mapFragment= new MapFragment();
                    mapFragment = new SupportMapFragment();
                }
                setupMap();
                return mapFragment;
        }

        return null;
    }

    void setupMap() {
        if( mapFragment!=null ) {
            GoogleMap map = mapFragment.getMap();
            if (map != null) {
                map.addMarker(new MarkerOptions().position(new LatLng(30, 0)).title("Marker"));
            }
        }
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }

}