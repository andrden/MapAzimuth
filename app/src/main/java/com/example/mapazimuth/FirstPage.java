package com.example.mapazimuth;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FirstPage extends Fragment {

    String locationStr = "loc??";
    TextView txtLocation;
    TextView txtArea;
    String locationsAll = "locationsAll\n";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_first, container, false);
        txtLocation = (TextView)rootView.findViewById(R.id.txtLocation);
        txtLocation.setText(locationStr);
        txtArea = (TextView)rootView.findViewById(R.id.txtArea);
        txtArea.setText(locationsAll);
        return rootView;
    }

    void makeUseOfNewLocation(boolean gps, Location location){
        locationStr(gps, location);
        locationsAll += locationStr + "\n";
        if( txtLocation!=null ){
            txtLocation.setText(locationStr);
        }
        if( txtArea!=null ){
            txtArea.setText(locationsAll);
        }
    }

    private void locationStr(boolean gps, Location location) {
        StringBuilder s = new StringBuilder();
        s.append((gps?"gps":"net")+"[");
        s.append(location.getProvider());
        s.append(String.format(" %.6f,%.6f", location.getLatitude(), location.getLongitude()));
        if (location.hasAccuracy()) s.append(String.format(" acc=%.0f", location.getAccuracy()));
        else s.append(" acc=???");
        if (location.getTime() == 0) {
            s.append(" t=?!?");
        }
        locationStr = s.toString();
    }

}