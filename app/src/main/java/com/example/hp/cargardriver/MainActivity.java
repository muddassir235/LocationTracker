package com.example.hp.cargardriver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_ACCESS_FINE_LOCATION = 0;
    private static final int PERMISSION_ACCESS_COURSE_LOCATION = 1;

    Button startTrackingButton;
    EditText driverNameET;
    LinearLayout driverActivityStatusLayout;
    Switch driverActivityStatusSwitch;
    TextView driverActivityStatusTV;
    Button setNameButton;
    ImageButton editNameButton;

    boolean inTrip;
    boolean driverActive;

    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        requestLocationPermissons();


        inTrip = getTripState(getApplicationContext());
        driverActive = getDriverActivityState(getApplicationContext());

        if(driverActive){
            driverActivityStatusLayout.setBackgroundColor(Color.parseColor("#22CC33"));
            driverActivityStatusTV.setText("Active");
        }else{
            driverActivityStatusLayout.setBackgroundColor(Color.parseColor("#FF5555"));
            driverActivityStatusTV.setText("On Break");
        }

        driverActivityStatusSwitch.setChecked(driverActive);

        driverActivityStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    storeDriverActivityState(getApplicationContext(),true);
                    driverActivityStatusLayout.setBackgroundColor(Color.parseColor("#22CC33"));
                    driverActivityStatusTV.setText("Active");
                    driverActive = true;
                }else{
                    storeDriverActivityState(getApplicationContext(),false);
                    driverActivityStatusLayout.setBackgroundColor(Color.parseColor("#FF5555"));
                    driverActivityStatusTV.setText("On Break");
                    driverActive = false;
                }
            }
        });

        if(inTrip){
            driverNameET.setFocusable(false);
            driverNameET.setText(getNameFromSharedPreferences(getApplicationContext()));
            startTrackingButton.setText("End Trip");
        }

        name = getNameFromSharedPreferences(getApplicationContext());

        if(!name.equals("Name")&&!name.equals("")){
            driverNameET.setText(name);
            driverNameET.setFocusable(false);
            driverNameET.setClickable(false);
            setNameButton.setVisibility(View.GONE);
            editNameButton.setVisibility(View.VISIBLE);
            LocationService.AlarmReceiver.scheduleAlarms(getApplicationContext());
        }

        setNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(driverNameET.getText())) {
                    name = driverNameET.getText().toString();
                    if(LocationService.serviceIsRunning==null) {
                        LocationService.AlarmReceiver.scheduleAlarms(getApplicationContext());
                    }else if(LocationService.serviceIsRunning == false){
                        LocationService.AlarmReceiver.scheduleAlarms(getApplicationContext());
                    }
                    driverNameET.setFocusable(false);
                    driverNameET.setClickable(false);
                    putNameInSharedPreferences(getApplicationContext(),name);
                    setNameButton.setVisibility(View.GONE);
                    editNameButton.setVisibility(View.VISIBLE);
                }else {
                    driverNameET.setError("Please enter your full name");
                }

            }
        });

        editNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                driverNameET.setFocusableInTouchMode(true);
                driverNameET.setClickable(true);
                driverNameET.requestFocus();
                editNameButton.setVisibility(View.GONE);
                setNameButton.setVisibility(View.VISIBLE);

            }
        });


        startTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!inTrip) {
                    if (!TextUtils.isEmpty(driverNameET.getText())) {
                        if(driverActive) {
                            name = driverNameET.getText().toString();
                            putNameInSharedPreferences(MainActivity.this, name);
                            storeTripState(getApplicationContext(), true);
//                        EditText editText = (EditText) findViewById(R.id.driver_name_entry_edit_text);
//                        editText.setFocusable(false);
//                        editText.setClickable(false);
                            startTrackingButton.setText("End Trip");
                            //FirebaseDatabase.getInstance().getReference().child("Drivers").child(name).child("TrackingActive").setValue(true);
                            inTrip = true;
                        }else{
                            Toast.makeText(getApplicationContext(), "Change your status to active first",Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        driverNameET.setError("Please enter your name");
                    }
                }else{
                    storeTripState(getApplicationContext(),false);
//                    Calendar mcal = Calendar.getInstance();
//                    Intent intent = new Intent(getApplicationContext(), LocationService.class);
//                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1253, intent, 0);
//                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//                    alarmManager.cancel(pendingIntent);
//                    pendingIntent.cancel();
//                    stopService(new Intent(getApplicationContext(),LocationService.class));

                    startTrackingButton.setText("Start Trip");
                    inTrip = false;
//                    if(!name.equals("Name")) {
//                        FirebaseDatabase.getInstance().getReference().child("Drivers").child(name).child("TrackingActive").setValue(false);
//                    }
                }
            }
        });
    }

    void startTrackingService(String name){
        driverNameET.setText(name);
        setNameButton.setVisibility(View.GONE);
        editNameButton.setVisibility(View.VISIBLE);
        Intent alarmIntent = new Intent(getApplicationContext(), LocationService.AlarmReceiver.class);
        alarmIntent.putExtra("Name",name);

        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0,alarmIntent,0);

        AlarmManager am=(AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+3*1000, 3 * 1000,
                pi);
    }

    void bindViews(){
        startTrackingButton = (Button) findViewById(R.id.start_tracking);
        driverNameET = (EditText) findViewById(R.id.driver_name_entry_edit_text);
        driverActivityStatusLayout = (LinearLayout) findViewById(R.id.driver_activity_status_layout);
        driverActivityStatusSwitch = (Switch) findViewById(R.id.driver_activity_status_switch);
        driverActivityStatusTV = (TextView) findViewById(R.id.driver_activity_status_text_view);
        setNameButton = (Button) findViewById(R.id.set_name_button);
        editNameButton = (ImageButton) findViewById(R.id.edit_name_image_button);
    }

    static void putNameInSharedPreferences(Context context,String name){
        SharedPreferences namePrefs= PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor namePrefsEditor = namePrefs.edit();
        namePrefsEditor.putString("Name",name);
        namePrefsEditor.commit();
    }

    static void storeTripState(Context context,Boolean inTrip){
        SharedPreferences namePrefs= PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor namePrefsEditor = namePrefs.edit();
        namePrefsEditor.putBoolean("TripState",inTrip);
        namePrefsEditor.commit();
    }

    static boolean getTripState(Context context){
        SharedPreferences namePrefs= PreferenceManager.getDefaultSharedPreferences(context);
        return namePrefs.getBoolean("TripState",false);
    }

    static String getNameFromSharedPreferences(Context context){
        SharedPreferences namePrefs= PreferenceManager.getDefaultSharedPreferences(context);
        return namePrefs.getString("Name","Name");
    }

    static void storeDriverActivityState(Context context,Boolean active){
        SharedPreferences namePrefs= PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor namePrefsEditor = namePrefs.edit();
        namePrefsEditor.putBoolean("DriverActivityState",active);
        namePrefsEditor.commit();
    }

    static boolean getDriverActivityState(Context context){
        SharedPreferences namePrefs= PreferenceManager.getDefaultSharedPreferences(context);
        return namePrefs.getBoolean("DriverActivityState",false);
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
