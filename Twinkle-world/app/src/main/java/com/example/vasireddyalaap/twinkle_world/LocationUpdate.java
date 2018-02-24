package com.example.vasireddyalaap.twinkle_world;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.security.AccessController.getContext;

public class LocationUpdate extends AppCompatActivity implements LocationListener {

    private static final String TAG = "xyz";
    private LocationManager location;

    private Location mCurrentLocation;

    private double latitude;
    private double longitude;
    private long timestamp;
    private String address_lat_long;
    private String new_add;



    private long  previous_timestamp;
    private long present_timestamp = 0;

    TextView Prev_TimeStamp;
    TextView Pres_TimeStamp;
    TextView Timestamp;
    TextView Address;
    TextView Time;


    Map<String,Long> hashmap = new HashMap();


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";

    private Boolean mRequestingLocationUpdates;

    public LocationUpdate() throws IOException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_update);
        location = (LocationManager) getSystemService(LOCATION_SERVICE);




        Prev_TimeStamp = (TextView) findViewById(R.id.Prev_TS_Value);
        Pres_TimeStamp = (TextView) findViewById(R.id.Pres_TS_Value);
        Timestamp = (TextView) findViewById(R.id.Timestamp_Value);
        Address = (TextView) findViewById(R.id.Address_Value);
        Time = (TextView) findViewById(R.id.Time_Value);

        mRequestingLocationUpdates = false;

        //updateValuesFromBundle(savedInstanceState);



    }

    protected void onResume() {
        super.onResume();
        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, this);
            }
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Permissions")
                        .setMessage("Sowmith Pentaparthy")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(LocationUpdate.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, this);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.i(TAG,"Nithin");
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        timestamp = location.getTime();

        List<android.location.Address> addresses = null;
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1);

        } catch (IOException e) {
            e.printStackTrace();
        }
        Address address = addresses.get(0);
        ArrayList<String> addressFragments = new ArrayList<>();
        for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
            addressFragments.add(address.getAddressLine(i));
        }
        SharedPreferences sp = getSharedPreferences("FILE_NAME", MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();

        edit.putLong("i", 0);

        if(sp.getLong("i",0) == 0){
            present_timestamp = timestamp;
            address_lat_long = addressFragments.get(0).toString();
            edit.putLong("Prev_time", present_timestamp);
            edit.putLong("Current_time", timestamp);
            edit.putLong("i",1);
            edit.apply();
            try
            {
                FileInputStream fileInputStream = new FileInputStream("myFile");
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                hashmap = (HashMap)objectInputStream.readObject();
            }
            catch(ClassNotFoundException | IOException | ClassCastException e) {
                e.printStackTrace();
            }
        }

        if(sp.getLong("i",0) > 0) {
            if((!(addressFragments.get(0).toString()).equals(address_lat_long))){
                edit.putLong("Prev_time", present_timestamp);
                edit.putLong("Current_time", timestamp);
                edit.apply();
            }
        }


        // save data into share SharePreference


        // get data from share SharePreference
        previous_timestamp = sp.getLong("Prev_time",0);
        present_timestamp = sp.getLong("Current_time",0);

        // reading hash map from file






        if((!((addressFragments.get(0).toString()).equals(address_lat_long))) && (((present_timestamp - previous_timestamp)) >= 20000)) {
            new_add = address_lat_long;
            address_lat_long = addressFragments.get(0).toString();
            Address.setText("new address" + address_lat_long);
            Long diff = present_timestamp - previous_timestamp;
            Long temp;

            if(hashmap.containsKey(new_add)){
               temp = hashmap.get(new_add);
                diff = temp + diff;
            }

                hashmap.put(new_add, diff);
            try
            {
                FileOutputStream fos = this.openFileOutput("myFile", Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(hashmap);
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Write a message to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("DAY2");
            diff = diff/(1000*60);
            float f = (float) diff;
            myRef.child(new_add).setValue(f + " min");

            Prev_TimeStamp.setText("Previous Time " + previous_timestamp);
            Pres_TimeStamp.setText(" " + timestamp);
            Timestamp.setText("" + (present_timestamp - previous_timestamp));
        }

        else {
            Time.setText(""+(!(addressFragments.get(0).toString()).equals(address_lat_long)));
            address_lat_long = addressFragments.get(0).toString();
            Prev_TimeStamp.setText("Previous Time " + previous_timestamp);
            Pres_TimeStamp.setText(" " + timestamp);
            Timestamp.setText("" + (present_timestamp - previous_timestamp  ));
            Address.setText("old address " + address_lat_long);
        }



    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }
            latitude = mCurrentLocation.getLatitude();
            longitude = mCurrentLocation.getLongitude();
            Pres_TimeStamp.setText(""+latitude);
            Prev_TimeStamp.setText(""+longitude);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
