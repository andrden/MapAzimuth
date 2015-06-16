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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class TabsPagerAdapter extends FragmentPagerAdapter {
    SupportMapFragment mapFragment;
    FirstPage firstPage = new FirstPage();

    Location location;
    Marker oldMarker;
    Polyline oldPolyline;

    double azimuth;

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    void makeUseOfNewLocation(boolean gps, Location location){
        firstPage.makeUseOfNewLocation(gps, location);
        this.location = location;
    }

    void makeUserOfNewAzimuth(double azimuth, double roll){
        this.azimuth = azimuth;
        firstPage.makeUserOfNewAzimuth(azimuth);
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
                    double screenPerpAzimuth = azimuth + 90; // degrees
                    final double KM = 30;
                    double kmX = KM * Math.sin(Math.toRadians(screenPerpAzimuth)); // delta longitudinal
                    double kmY = KM * Math.cos(Math.toRadians(screenPerpAzimuth)); // delta latitude
                    double degreeLatitude = 111; // acually 110.5 .. 111.5 because earth is ellipsoid
                    double degreeLongitude = degreeLatitude * Math.cos(Math.toRadians(location.getLatitude()));
                    //LatLng latLngBearing = new LatLng(location.getLatitude(), location.getLongitude()-0.3);
                    LatLng latLngBearing = new LatLng(
                            location.getLatitude() + kmY / degreeLatitude,
                            location.getLongitude() + kmX / degreeLongitude);

                    if( oldMarker!=null ){
                        oldMarker.remove();
                    }
                    if( oldPolyline!=null ){
                        oldPolyline.remove();
                    }
                    oldMarker = map.addMarker(new MarkerOptions().position(latLng).title("Me"));
                    oldPolyline = map.addPolyline(new PolylineOptions().color(Color.BLUE).add(latLng, latLngBearing));

                    try {
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds(latLng, latLngBearing), 20));
                    }catch (IllegalStateException e){
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, map.getMaxZoomLevel()-4));
                    }
                }
            }
        }
    }

    LatLngBounds bounds(LatLng p1, LatLng p2){
        LatLng southWest = new LatLng(Math.min(p1.latitude, p2.latitude), Math.min(p1.longitude, p2.longitude));
        LatLng northEast = new LatLng(Math.max(p1.latitude, p2.latitude), Math.max(p1.longitude, p2.longitude));
        return new LatLngBounds(southWest, northEast);
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }

}