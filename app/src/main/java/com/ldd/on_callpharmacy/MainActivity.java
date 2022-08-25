package com.ldd.on_callpharmacy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;
import com.parse.FindCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private RecyclerView pharmacyRV;
    private ProgressBar loadingPB;
    private ArrayList<Pharmacy> pharmacyArrayList;
    private PharmacyRVAdapter pharmacyRVAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private double longitude, latitude;
    private String locality;
    private TextView myLocality, error;

    private ResultReceiver resultReceiver;
    private static final int REQUEST_PERMISSION_CODE = 1;
    FusedLocationProviderClient fusedLocationProviderClient;
    Bitmap bmp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        /*ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network

        }else {
            error.setText("Not internet connection found!!\nEnable internet and refresh");
        }*/


        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                   getMyLocation();
//            getLocation();
        }else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    100);
        }

        // initializing our views.
        pharmacyRV = findViewById(R.id.idRVPharmacy);
        loadingPB = findViewById(R.id.idProgressBar);
        pharmacyArrayList = new ArrayList<>();
        swipeRefreshLayout = findViewById(R.id.refreshLayout);
        myLocality = findViewById(R.id.locality);
        error = findViewById(R.id.error);

        if (!internetIsConnected()){
            error.setText("No internet connection found!!\nCheck your connection and refresh again.");
            loadingPB.setVisibility(View.GONE);
        }


//        getLocation();
        preparePharmacyRV();

        getDataFromServer();

        swipe();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.length > 0) {
            if (requestCode == 100 && grantResults.length > 0 && (grantResults[0] + grantResults[1]
                    == PackageManager.PERMISSION_GRANTED)) {
//                getCurrentLocation();
//                getLocation();
                getMyLocation();
            } else {
                Toast.makeText(getApplicationContext(), "Permission denial!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getDataFromServer() {
        pharmacyArrayList.clear();

        // Configure Query with our query.
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Pharm");
//        query.addAscendingOrder("lat");
        query.whereEqualTo("locality", locality);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, com.parse.ParseException e) {
                // in done method checking if the error is null or not.
                if (e == null) {
                    // Adding objects into the Array
                    // if error is not null we are getting data from
                    // our object and passing it to our array list.
                    for (int i = 0; i < objects.size(); i++) {

                        // on below line we are extracting our
                        // data and adding it ot our array list
                        ParseFile logo = objects.get(i).getParseFile("image");
                        String name = objects.get(i).getString("name");
                        String longi = objects.get(i).getString("long");
                        String lati = objects.get(i).getString("lat");

                        double lat2 = Double.parseDouble(lati);
                        double long2 = Double.parseDouble(longi);


                        double distance = distance(latitude, lat2, longitude, long2);

                        LatLng myLocation = new LatLng(latitude, longitude);
                        LatLng pharmLocation = new LatLng(lat2, long2);
                        Utility.pharmLatLng = pharmLocation;
                        Utility.myLocation = myLocation;

                        double dis = SphericalUtil.computeDistanceBetween(myLocation, pharmLocation);
                        dis = dis / 1000;

                        // on below line we are adding data to our array list.
                        pharmacyArrayList.add(new Pharmacy(logo, name, dis, "open", myLocation, pharmLocation));

                        //sort by distance
                        Collections.sort(pharmacyArrayList, Pharmacy.sortbydistance);
                    }

                    // notifying adapter class on adding new data.
                    pharmacyRVAdapter.notifyDataSetChanged();
                    loadingPB.setVisibility(View.GONE);
                } else {
                    // handling error if we get any error.
                    Toast.makeText(MainActivity.this, "Fail to get data..", Toast.LENGTH_SHORT).show();
                    loadingPB.setVisibility(View.GONE);
                    error.setText("-Either your location is not turn on. Or\n-Internet is not available\n\nCheck the above and try again. Thanks");
                }
            }
        });
    }

    public boolean internetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }


    private void preparePharmacyRV() {
        pharmacyRV.setHasFixedSize(true);
        pharmacyRV.setLayoutManager(new LinearLayoutManager(this));

        // adding our array list to our recycler view adapter class.
        pharmacyRVAdapter = new PharmacyRVAdapter(this, pharmacyArrayList);

        // setting adapter to our recycler view.
        pharmacyRV.setAdapter(pharmacyRVAdapter);
    }

    @SuppressLint("MissingPermission")
    public void getLocation() {
//        progressBar.setVisibility(View.VISIBLE);
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {


            @Override
            public void onComplete(@NonNull Task<Location> task) {
                //initialising location
                Location location = task.getResult();
                if (location != null){
                    try {
                        //initialise geoCoder
                        Geocoder geocoder = new Geocoder(MainActivity.this,
                                Locale.getDefault());
                        //initialise address
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );
                        //set lat and long on textview
                        /*latlong.setText(String.format(
                                "Latitude: %s\nLongitude: %s", addresses.get(0).getLatitude(),
                                addresses.get(0).getLongitude()
                        ));*/
                        latitude = addresses.get(0).getLatitude();
                        longitude = addresses.get(0).getLongitude();

                        //set country name, locality, address
                        /*address.setText(String.format("Country: %s\nLocality: %s\nAddress: %s",
                                addresses.get(0).getCountryName(), addresses.get(0).getLocality(),
                                addresses.get(0).getAddressLine(0)));*/
                        locality = addresses.get(0).getLocality();
                        myLocality.setText(locality);

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Not found: Check your location settings "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Toast.makeText(getApplicationContext(), "Not found Check your location settings",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    public static double distance(double lat1, double lat2, double lon1, double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = (R * c * 1000) / 1000; // convert to kilo meters

        double height = 0.0;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    private void swipe(){
        // Refresh  the layout
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        // Your code goes here

                        getDataFromServer();
                        preparePharmacyRV();
//                        getLocation();
                        getMyLocation();

                        // This line is important as it explicitly
                        // refreshes only once
                        // If "true" it implicitly refreshes forever
                        swipeRefreshLayout.setRefreshing(false);
                        loadingPB.setVisibility(View.GONE);
                    }
                }
        );
    }

       @SuppressLint("MissingPermission")
   public void getMyLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE
        );
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null) {
                       /* latlong.setText(String.format("Latitude: %s\nLongitude: %s",
                                location.getLatitude(), location.getLongitude()));*/


                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        //initialise geoCoder
                        Geocoder geocoder = new Geocoder(MainActivity.this,
                                Locale.getDefault());

                        //initialise address
                        try {
                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(), location.getLongitude(), 1);

                            locality = addresses.get(0).getLocality();
                            myLocality.setText(locality);

                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Not found: Check your location settings "+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        LocationRequest locationRequest = new LocationRequest()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000)
                                .setFastestInterval(1000)
                                .setNumUpdates(1);

                        LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                Location location1 = locationResult.getLastLocation();
                                /*latlong.setText(String.format("Latitude: %s\nLongitude: %s",
                                        location1.getLatitude(), location1.getLongitude()));*/

                                latitude = location1.getLatitude();
                                longitude = location1.getLongitude();

                                //initialise geoCoder
                                Geocoder geocoder = new Geocoder(MainActivity.this,
                                        Locale.getDefault());

                                //initialise address
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(
                                            location1.getLatitude(), location1.getLongitude(), 1);

                                    locality = addresses.get(0).getLocality();
                                    myLocality.setText(locality);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Not found: Check your location settings "+e.getMessage(),Toast.LENGTH_SHORT).show();
                                }

                            }
                        };
                        //request location update
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                                locationCallback, Looper.myLooper());
                    }
                }
            });
       }else {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
   }

}