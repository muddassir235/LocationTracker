package com.example.hp.cargardriver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_ACCESS_FINE_LOCATION = 0;
    private static final int PERMISSION_ACCESS_COURSE_LOCATION = 1;

    Button startTrackingButton;
    EditText driverNameET;

    boolean serviceStarted;

    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        requestLocationPermissons();
        // TODO: Get the status of the service once the app is started
        if(LocationService.serviceIsRunning!=null){
            serviceStarted = LocationService.serviceIsRunning;
            if(serviceStarted){
                driverNameET.setFocusable(false);
                driverNameET.setText(getNameFromSharedPreferences(getApplicationContext()));
                startTrackingButton.setText("Stop Tracking");
            }
        }else{
            serviceStarted = false;
            LocationService.serviceIsRunning = false;
        }

        name = getNameFromSharedPreferences(getApplicationContext());

        if(!name.equals("Name")){
            driverNameET.setText(name);
        }

        startTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!serviceStarted) {
                    if (!TextUtils.isEmpty(driverNameET.getText())) {
                        name = driverNameET.getText().toString();
                        putNameInSharedPreferences(MainActivity.this,name);
                        Intent alarmIntent = new Intent(getApplicationContext(), LocationService.AlarmReceiver.class);
                        alarmIntent.putExtra("Name",driverNameET.getText().toString());

                        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0,alarmIntent,0);

                        AlarmManager am=(AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

                        am.setRepeating(AlarmManager.RTC_WAKEUP, 10*1000, 3 * 1000,
                                pi);

                        EditText editText = (EditText) findViewById(R.id.driver_name_entry_edit_text);
                        editText.setFocusable(false);
                        editText.setClickable(false);
                        startTrackingButton.setText("Stop Tracking");
                        FirebaseDatabase.getInstance().getReference().child("Drivers").child(name).child("TrackingActive").setValue(true);
                        serviceStarted = true;
                        LocationService.serviceIsRunning = true;
                    } else {
                        driverNameET.setError("Please enter your name");
                    }
                }else{
                    Calendar mcal = Calendar.getInstance();
                    Intent intent = new Intent(getApplicationContext(), LocationService.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1253, intent, 0);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                    stopService(new Intent(getApplicationContext(),LocationService.class));

                    EditText editText = (EditText) findViewById(R.id.driver_name_entry_edit_text);
                    editText.setFocusableInTouchMode(true);
                    editText.setClickable(true);
                    editText.requestFocus();
                    startTrackingButton.setText("Start Tacking");
                    serviceStarted = false;
                    if(!name.equals("Name")) {
                        FirebaseDatabase.getInstance().getReference().child("Drivers").child(name).child("TrackingActive").setValue(false);
                    }
                    LocationService.serviceIsRunning = false;
                }
            }
        });
    }

    void bindViews(){
        startTrackingButton = (Button) findViewById(R.id.start_tracking);
        driverNameET = (EditText) findViewById(R.id.driver_name_entry_edit_text);
    }

    static void putNameInSharedPreferences(Context context,String name){
        SharedPreferences namePrefs= PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor namePrefsEditor = namePrefs.edit();
        namePrefsEditor.putString("Name",name);
        namePrefsEditor.commit();
    }

    static void storeServiceState(Context context,Boolean running){
        SharedPreferences namePrefs= PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor namePrefsEditor = namePrefs.edit();
        namePrefsEditor.putBoolean("ServiceState",running);
        namePrefsEditor.commit();
    }

    static void getServiceState(Context context){
        SharedPreferences namePrefs= PreferenceManager.getDefaultSharedPreferences(context);
        namePrefs.getBoolean("ServiceState",false);
    }

    static String getNameFromSharedPreferences(Context context){
        SharedPreferences namePrefs= PreferenceManager.getDefaultSharedPreferences(context);
        return namePrefs.getString("Name","Name");
    }

    //request loaction permissons
    void requestLocationPermissons(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_ACCESS_COURSE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COURSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case PERMISSION_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
