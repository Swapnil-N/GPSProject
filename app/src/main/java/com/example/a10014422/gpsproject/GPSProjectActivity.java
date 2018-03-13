package com.example.a10014422.gpsproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;

public class GPSProjectActivity extends AppCompatActivity implements LocationListener{

    LocationManager locationManager;
    static final int REQUEST_LOCATION = 1;
    double latitude = 0; //40.7430952;
    double longitude = 0; //-74.4045639;
    TextView latText;
    TextView longText;
    TextView addText;
    TextView disText;
    JSONObject jsonObject;
    Location pLocation;
    float distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpsproject);

        latText = (TextView) findViewById(R.id.id_latText);
        longText = (TextView) findViewById(R.id.id_longText);
        addText = (TextView)findViewById(R.id.id_addText);
        disText = (TextView) findViewById(R.id.id_disText);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getLocation();

    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);
        }
        else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
            onLocationChanged(location);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQUEST_LOCATION:
                getLocation();
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        /*    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                text2.setText(addresses.get(0).getAddressLine(0));
            } catch (Exception e) {
                Toast.makeText(GPSProjectActivity.this,"Unable to acquire address",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }*/
            AsyncThread thread = new AsyncThread();
            thread.execute();
        }

        latText.setText("Latitude: "+ latitude);
        longText.setText("Longitude: "+ longitude);
        if (pLocation != null)
            distance+=pLocation.distanceTo(location);


        double CD = convert(distance);
        double rounded = Math.round(CD*100.0)/100.0;
        disText.setText("Distance: "+ rounded+ " miles");
        pLocation = location;

    }

    private double convert(float in) {
        return in*0.000621371;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public class AsyncThread extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&key=AIzaSyClhP_UJs76prqFOID_wrNVojuGl1yYBYA");
                URLConnection urlConnection = url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String input = bufferedReader.readLine();
                String line = "";

                while (line != null){
                    line = bufferedReader.readLine();
                    input += line;
                }
                    
                jsonObject = new JSONObject(input);
                Log.d("qwer",jsonObject.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                String fullAddress = jsonObject.getJSONArray("results").getJSONObject(0).getString("formatted_address");
                addText.setText(fullAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}
