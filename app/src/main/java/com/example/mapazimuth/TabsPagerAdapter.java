package com.example.mapazimuth;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentPagerAdapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class TabsPagerAdapter extends FragmentPagerAdapter {
    Context context;
    ViewPager viewPager;

    SupportMapFragment mapFragment = new SupportMapFragment();

    FirstPage firstPage = new FirstPage();
    CameraFragment cameraFragment;

    Location location;
    Marker oldMarker;
    Polyline oldPolyline;

    double azimuth;


    public TabsPagerAdapter(ViewPager viewPager, FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        this.viewPager = viewPager;
    }

    void makeUseOfNewLocation(boolean gps, Location location){
        firstPage.makeUseOfNewLocation(gps, location);
        this.location = location;
    }

    void makeUserOfNewAzimuth(double azimuth, String desc){
        this.azimuth = azimuth;
        firstPage.makeUserOfNewAzimuth(azimuth, desc);
        cameraFragment.makeUserOfNewAzimuth(azimuth, desc);
    }

    void setUpAndConfigureCamera(){
        if( cameraFragment!=null ){
            cameraFragment.setUpAndConfigureCamera();
        }
    }

        @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                // Top Rated fragment activity
                return firstPage;
            case 1:
                // Games fragment activity
                cameraFragment = new CameraFragment();
                cameraFragment.setContext(context, new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            viewPager.setCurrentItem(2); // switch from camera to map
                        }
                        return true;
                    }
                });
                return cameraFragment;
            case 2:
                setupMap();
                return mapFragment;
        }

        return null;
    }

    void setupMap(){
            if( mapFragment!=null ) {
                final GoogleMap map = mapFragment.getMap();
                //mapFragment.setHasOptionsMenu(true);
                //mapFragment.setMenuVisibility(true);
                if (map != null) {
                    map.setMapType(firstPage.getMapType());
                    //map.getUiSettings().setMapToolbarEnabled(true);
                    //map.getCameraPosition() -- ??
                    if( location!=null && azimuth>=0 ) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        double screenPerpAzimuth = azimuth; // degrees
                        final double KM = 30;
                        double kmX = KM * Math.sin(Math.toRadians(screenPerpAzimuth)); // delta longitudinal
                        double kmY = KM * Math.cos(Math.toRadians(screenPerpAzimuth)); // delta latitude
                        double degreeLatitude = 111; // acually 110.5 .. 111.5 because earth is ellipsoid
                        double degreeLongitude = degreeLatitude * Math.cos(Math.toRadians(location.getLatitude()));
                        //LatLng latLngBearing = new LatLng(location.getLatitude(), location.getLongitude()-0.3);
                        LatLng latLngBearing = new LatLng(
                                location.getLatitude() + kmY / degreeLatitude,
                                location.getLongitude() + kmX / degreeLongitude);
                        LatLng latLngBearingShort = new LatLng(
                                location.getLatitude() + kmY / degreeLatitude / 10,
                                location.getLongitude() + kmX / degreeLongitude / 10);

                        if( oldMarker!=null ){
                            oldMarker.remove();
                        }
                        if( oldPolyline!=null ){
                            oldPolyline.remove();
                        }
                        oldMarker = map.addMarker(new MarkerOptions().position(latLng).title("Me az="+Math.round(azimuth)));
                        oldPolyline = map.addPolyline(new PolylineOptions().color(Color.BLUE).add(latLng, latLngBearing));

                        try {
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds(latLng, latLngBearingShort), 20));
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