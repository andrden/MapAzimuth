package com.example.mapazimuth;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by denny on 6/16/15.
 */
public abstract class CompassCalc implements SensorEventListener {
    float[] inR = new float[16];
    float[] I = new float[16];
    float[] gravity = new float[3];
    float[] orientVals = new float[3];

    SecondAvg azimuth = new SecondAvg();
    SecondAvg pitch = new SecondAvg();
    SecondAvg roll = new SecondAvg();

    SecondAvg[] geomagAvg = {new SecondAvg(), new SecondAvg(), new SecondAvg()};

    abstract void azimuthChanged();

    void listenOn(SensorManager sm) {
        // Register this class as a listener for the accelerometer sensor
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
// ...and the orientation sensor
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    void listenOff(SensorManager sm){
        sm.unregisterListener(this);
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        // If the sensor data is unreliable return
        if (sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
            return;

        float[] geomag = new float[3];

        // Gets the value of the sensor that has been changed
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                gravity = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomag = sensorEvent.values.clone();
                geomagAvg[0].add(geomag[0]);
                geomagAvg[1].add(geomag[1]);
                geomagAvg[2].add(geomag[2]);
                break;
        }

        // If gravity and geomag have values then find rotation matrix
        if (gravity != null && geomag != null) {

            // checks that the rotation matrix is found
            boolean success = SensorManager.getRotationMatrix(inR, I,
                    gravity, geomag);
            if (success) {
                SensorManager.getOrientation(inR, orientVals);
                boolean avgChanged = azimuth.add( Math.toDegrees(orientVals[0]) );
                pitch.add( Math.toDegrees(orientVals[1]) );
                roll.add(Math.toDegrees(orientVals[2]));
                if( avgChanged ) {
                    azimuthChanged();
                }
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
