package com.ti.luanbatista.projetomds;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap gmap;

    private boolean mLocationPermissionGranted;

    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private LatLng mDefaultLocation;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final float  DEFAULT_ZOOM = (float) 18;
    private static final boolean ENABLE_HIGH_ACCURACY = true;
    private static final int SPLASH_TIME_OUT = 3000;

    private ArrayList<MyLocation> listaBuracos;

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // Esse método será executado sempre que o timer acabar
                // E inicia a activity principal
                //Intent i = new Intent(SplashDevMedia.this, ActivityPrincipal.class);
                //startActivity(i);

                // Fecha esta activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "O buraco foi registrado", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                addMarker(mLastKnownLocation);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }


        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mDefaultLocation = new LatLng(-22.0069101, -47.8919187);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);



        listaBuracos = new ArrayList<>();
    }

    class MyLocation {
        private Location location;
        private int level = 1;

        public MyLocation(Location location) {
            this.location = location;
            this.level = 1;
        }

        public Location getLocation() {
            return location;
        }

        public int getLevel() {
            return level;
        }

        public int newLevel() {
            if(this.level < 3)
                return this.level = this.level + 1;

            return this.level;
        }
    }

    private void addMarker(Location location) {

        boolean addNewMarker = true;

        LatLng current = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());

        for (MyLocation myLocation : listaBuracos) {

            Location currentLocation = myLocation.location;

            if(currentLocation.getLongitude() == location.getLongitude() &&
                    currentLocation.getLatitude() == location.getLatitude()) {
                myLocation.newLevel();
                addNewMarker = false;
            }
        }

        if(!addNewMarker) {
            gmap.clear();

            for (MyLocation myLocation : listaBuracos) {

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(myLocation.location.getLatitude(), myLocation.location.getLongitude()))
                        .title("Aqui tem um buraco");

                if(myLocation.level == 1)
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.level_1));
                if(myLocation.level == 2)
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.level_2));
                if(myLocation.level == 3)
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.level_3));

                gmap.addMarker(markerOptions);
            }
        } else {

            MyLocation newLocation = new MyLocation(location);

            listaBuracos.add(newLocation);

            gmap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title("Aqui tem um buraco")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.level_1))
            );
        }


        gmap.moveCamera(CameraUpdateFactory.newLatLng(current));


    }

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"Voce tem acesso a sua localização", Toast.LENGTH_LONG).show();
            mLocationPermissionGranted = true;
        } else {
            // Show rationale and request permission.
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"Voce tem acesso ao armazenamento", Toast.LENGTH_LONG).show();
        } else {
            // Show rationale and request permission.
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;

        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        //
        //updateCurrentLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == 1) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this,"Voce autorizou", Toast.LENGTH_LONG).show();
                mLocationPermissionGranted = true;
            } else {
                // Permission was denied. Display an error message.
                Toast.makeText(this,"Permissão negada", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == 2) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this,"Voce autorizou", Toast.LENGTH_LONG).show();

            } else {
                // Permission was denied. Display an error message.
                Toast.makeText(this,"Permissão negada", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Intent intent;

        if (id == R.id.nav_camera) {
            // Handle the camera action
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_gallery) {
            intent = new Intent(this, SobreActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_slideshow) {
            intent = new Intent(this, DesenvolvedoresActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void updateLocationUI() {
        if (mapView == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                gmap.setMyLocationEnabled(true);
                gmap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                gmap.setMyLocationEnabled(false);
                gmap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();

                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation =  task.getResult();
                            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d("LUAN", "Current location is null. Using defaults.");
                            Log.e("LUAN", "Exception: %s", task.getException());
                            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            gmap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });


                LocationRequest request = new LocationRequest();
                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                request.setInterval(1);

                LocationCallback locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) {
                            return;
                        }
                        for (Location location : locationResult.getLocations()) {
                            Log.d("LUAN", "Localização disponivel" + location.getLatitude() +  " | " + location.getLongitude());

                            mLastKnownLocation =  location;
                            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        }
                    }

                    @Override
                    public void onLocationAvailability(LocationAvailability locationAvailability) {
                        Log.d("LUAN", "Localização disponivel");
                    }
                };

                Task<Void> locationMove = mFusedLocationProviderClient.requestLocationUpdates(request, locationCallback, null);

                locationMove.addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            Log.d("LUAN", "Callback ativo com sucesso");
                        } else {
                            Log.d("LUAN", "Callback nao esta ativo.");
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
   /*
    private void updateCurrentLocation() {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks(){

                    @Override
                    public void onConnected(Bundle arg0) {
                        // TODO Auto-generated method stub
                        LocationRequest request = new LocationRequest();
                        //int priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
                        //if (ENABLE_HIGH_ACCURACY) {
                        //    priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
                        //}

                        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;

                        request.setPriority(priority);

                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                locationClient, request, new LocationListener() {

                                    @Override
                                    public void onLocationChanged(Location location) {

                                        locationClient.disconnect();
                                    }

                                });
                    }

                    @Override
                    public void onConnectionSuspended(int arg0) {
                        // TODO Auto-generated method stub

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {

                    @Override
                    public void onConnectionFailed(ConnectionResult arg0) {
                        // TODO Auto-generated method stub

                    }
                })
                .build();
    }


    private void updateCurrentLocation() {

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                mLastKnownLocation = location;
                Log.d("LUAN", "Latidude: " + Double.toString(location.getLatitude()));
                Log.d("LUAN", "Longetude: " + Double.toString(location.getLongitude()));
                gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mLastKnownLocation.getLatitude(),
                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}

        };

        if ( Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }*/
}
