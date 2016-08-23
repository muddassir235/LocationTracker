package com.example.hp.cargardriver;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by gul on 8/8/16.
 */
public class LocationService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static Boolean serviceIsRunning;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private LocationRequest mLocationRequest;

    private static final String TAG = "Driver Service Log";
    private String name;

    public LocationService() {
        super("Thetacab");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        serviceIsRunning = true;
        name = intent.getStringExtra("Name");

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

//        Notification notification = new NotificationCompat.Builder(this)
//                .setContentTitle("nkDroid Music Player")
//                .setTicker("nkDroid Music Player")
//                .setContentText("nkDroid Music")
//                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_delete))
//                .setOngoing(true).build();

        mGoogleApiClient.connect();

    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved (Intent rootIntent){
        Log.e(TAG," Tracking stopped, task removed has been called.");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v(TAG," inside onConnected");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Log.v("Location: "," "+mLastLocation.getLatitude()+" "+mLastLocation.getLongitude());
        } else {
            Log.e("Location: ", " Location was null");
        }
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v("GoogleApiClient: ", "Connection Suspended. Trying to reconnect");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location!=null){
            Log.v("CurrentLoc: ", MainActivity.getNameFromSharedPreferences(getApplicationContext())+" " +
                            location.getLatitude()+" "+
                            location.getLongitude()+" "+
                            location.getTime()+" "+
                    MainActivity.getTripState(getApplicationContext())+" "+
                    MainActivity.getDriverActivityState(getApplicationContext()));

            Data data = new Data(
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getTime(),
                    MainActivity.getTripState(getApplicationContext()),
                    MainActivity.getDriverActivityState(getApplicationContext())
            );
            FirebaseDatabase.getInstance().getReference().
                    child("Drivers").
                    child(MainActivity.getNameFromSharedPreferences(getApplicationContext())).
                    child("data").push().
                    setValue(data);
        }else{
            Log.v("CurrentLoc: ",MainActivity.getNameFromSharedPreferences(getApplicationContext())+" "+
                    "null"+" "+"null"+" "+
                    System.currentTimeMillis()+" "+
                    MainActivity.getTripState(getApplicationContext())+" "+
                    MainActivity.getDriverActivityState(getApplicationContext()));
            Data data = new Data(
                    -1.0,
                    -1.0,
                    System.currentTimeMillis(),
                    MainActivity.getTripState(getApplicationContext()),
                    MainActivity.getDriverActivityState(getApplicationContext())
            );
            FirebaseDatabase.getInstance().getReference().
                    child("Drivers").
                    child(MainActivity.getNameFromSharedPreferences(getApplicationContext())).
                    child("data").push().
                    setValue(data);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v("GoogleApiClient: ", "Connection Failed. Trying to reconnect");
        mGoogleApiClient.connect();
    }

    public static class AlarmReceiver extends BroadcastReceiver {

        static AlarmManager mgr;

        @Override
        public void onReceive(Context context, Intent intent) {
            scheduleAlarms(context);
        }

        static void scheduleAlarms(Context ctxt) {
            if(mgr==null) {
                mgr = (AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE);
            }
            Intent i=new Intent(ctxt, LocationService.class);
            i.putExtra("Name",MainActivity.getNameFromSharedPreferences(ctxt.getApplicationContext()));
            PendingIntent pi=PendingIntent.getService(ctxt, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

            mgr.setRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + 3000, 3000, pi);
        }

        static void cancelAlarm(Context context){
            if(mgr==null) {
                mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            }
            context.stopService(new Intent(context,LocationService.class));
            Intent i=new Intent(context, LocationService.class);
            i.putExtra("Name",MainActivity.getNameFromSharedPreferences(context.getApplicationContext()));
            PendingIntent pi=PendingIntent.getService(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

            mgr.cancel(pi);
        }
    }

}