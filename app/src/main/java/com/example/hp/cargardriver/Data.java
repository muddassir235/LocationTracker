package com.example.hp.cargardriver;

/**
 * Created by hp on 8/22/2016.
 */
public class Data {
    public double lat;
    public double lng;
    public long time;
    public boolean inTrip;
    public boolean onBreak;

    public Data(){}

    public Data(double lat, double lng, long time, boolean inTrip, boolean onBreak) {
        this.lat = lat;
        this.lng = lng;
        this.time = time;
        this.inTrip = inTrip;
        this.onBreak = onBreak;
    }
}
