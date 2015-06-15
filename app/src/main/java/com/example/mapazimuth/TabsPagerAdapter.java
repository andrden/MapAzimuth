package com.example.mapazimuth;

import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentPagerAdapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class TabsPagerAdapter extends FragmentPagerAdapter {
    SupportMapFragment mapFragment;
    FirstPage firstPage = new FirstPage();

    Location location;

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    void makeUseOfNewLocation(boolean gps, Location location){
        firstPage.makeUseOfNewLocation(gps, location);
        this.location = location;
    }

        @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                // Top Rated fragment activity
                return firstPage;
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
                //map.getCameraPosition() -- ??
                if( location!=null ) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    LatLng latLngBearing = new LatLng(location.getLatitude(), location.getLongitude()-10);
                    map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    map.addMarker(new MarkerOptions().position(latLng).title("Me"));
                    map.addPolyline(new PolylineOptions().color(Color.BLUE).add(latLng, latLngBearing));
                }
            }
        }
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }

}