package com.example.mapazimuth;

/**
 * Created by denny on 6/19/15.
 */
public class SecondAvg {
    private double prevSecAvg=0;
    private long prevSec = 0;

    private int count=0;
    private double sum=0;

    boolean add(double val){
        boolean avgChanged = false;
        long nowSec = System.currentTimeMillis() / 1000;
        if( nowSec!=prevSec ){
            if( count>0 ){
                prevSecAvg = sum / count;
                sum = count = 0;
                avgChanged = true;
            }
            prevSec = nowSec;
        }
        count ++;
        sum += val;
        return avgChanged;
    }

    double getAvg(){
        return prevSecAvg;
    }
}
