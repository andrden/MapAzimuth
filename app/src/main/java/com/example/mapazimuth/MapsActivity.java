package com.example.mapazimuth;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements ActionBar.TabListener {

    //private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    // Tab titles
    private String[] tabs = { "Tab1", "Tab2", "Map" };

    LocationManager locationManager;
    LocationListener locationListenerGps = new MyLocationListener(true);
    LocationListener locationListenerNetwork = new MyLocationListener(false);

    SensorManager sm;
    CompassCalc compassCalc = new CompassCalc(){
        @Override
        void azimuthChanged() {
            int intRoll = (int) roll.getAvg();
            int intPitch = (int) pitch.getAvg();

            // directly compute:
            double magAzimuth = 180  - Math.toDegrees(Math.atan2(geomagAvg[1].getAvg()/*gy*/, geomagAvg[2].getAvg()/*gz*/));


            String desc = "roll=" + intRoll + " pitch=" + intPitch;
            if( intRoll < -80 && Math.abs(intPitch) < 5 ) {
               desc += String.format(" gx=%.1f gy=%.1f gz=%.1f  a=%.1f",
                        geomagAvg[0].getAvg(), geomagAvg[1].getAvg(), geomagAvg[2].getAvg(), magAzimuth);
            }
            mAdapter.makeUserOfNewAzimuth(magAzimuth /*compassCalc.azimuth.getAvg()+90*/, desc);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_maps);
       // setUpMapIfNeeded();
// Initilization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), this);

        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        compassCalc.listenOn(sm);

        // Adding Tabs
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }

/**
 * on swiping the viewpager make respective tab selected
 * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        listenLocation(true);
    }

    private void listenLocation(boolean on) {
        boolean gpsIsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkIsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(gpsIsEnabled)
        {
            if( on ) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10F, locationListenerGps);
            }else{
                locationManager.removeUpdates(locationListenerGps);
            }
        }
        if(networkIsEnabled)
        {
            if( on ) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 10F, locationListenerNetwork);
            }else{
                locationManager.removeUpdates(locationListenerNetwork);
            }
        }

        // Register the listener with the Location Manager to receive location updates
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        //locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 0, 0, locationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        listenLocation(false);
        compassCalc.listenOff(sm);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
        if( mAdapter!=null ) {
            mAdapter.setupMap();
        }
        listenLocation(true);
        compassCalc.listenOn(sm);

        Log.w("VideoActivity", "onResume");
        try {

            mAdapter.setUpAndConfigureCamera();
        }catch (Throwable t){
            AlertDialog show = new AlertDialog.Builder(this)
                    .setTitle("Err1").setMessage(""+t).setPositiveButton("OK",null).create();
            show.show();
        }

    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
        if( mAdapter!=null ) {
            mAdapter.setupMap();
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    private class MyLocationListener implements LocationListener {
        boolean gps;

        public MyLocationListener(boolean gps) {
            this.gps = gps;
        }

        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            mAdapter.makeUseOfNewLocation(gps, location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    }
}
