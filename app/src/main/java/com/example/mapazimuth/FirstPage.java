package com.example.mapazimuth;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class FirstPage extends Fragment {

    String locationStr = "loc??";
    TextView txtLocation, txtAzimuth;
    TextView txtArea;
    String locationsAll = "locationsAll\n";

    LinkedHashMap<String,Integer> mapTypes = new LinkedHashMap<String,Integer>(){{
        put("Map", GoogleMap.MAP_TYPE_NORMAL);
        put("Hybrid", GoogleMap.MAP_TYPE_HYBRID);
        put("Satellite", GoogleMap.MAP_TYPE_SATELLITE);
    }};
    Spinner dropdownMapType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_first, container, false);
        txtAzimuth = (TextView)rootView.findViewById(R.id.txtAzimuth);
        txtLocation = (TextView)rootView.findViewById(R.id.txtLocation);
        txtLocation.setText(locationStr);
        txtArea = (TextView)rootView.findViewById(R.id.txtArea);
        txtArea.setText(locationsAll);

        dropdownMapType = (Spinner)rootView.findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,
                new ArrayList<>(mapTypes.keySet()));
        dropdownMapType.setAdapter(adapter);
        dropdownMapType.setSelection(0);

        return rootView;
    }

    int getMapType(){
        return mapTypes.get(dropdownMapType.getSelectedItem());
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

    void makeUserOfNewAzimuth(double azimuth, String desc){
        txtAzimuth.setText("Azimuth = "+(int)azimuth+ "  "+desc);
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