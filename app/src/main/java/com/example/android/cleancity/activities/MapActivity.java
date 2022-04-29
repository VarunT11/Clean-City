package com.example.android.cleancity.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.cleancity.DustbinsListFragment;
import com.example.android.cleancity.MainViewModel;
import com.example.android.cleancity.R;
import com.example.android.cleancity.dustbin;
import com.example.android.cleancity.dustbinAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.SphericalUtil;

import static com.google.android.gms.maps.GoogleMap.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, OnMarkerClickListener {

    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private Boolean mLocationPermissionGranted = false;
    private static int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private GoogleMap mMap;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private CardView cardView;
    private Location currentLocation;
    private MaterialToolbar toolbar;
    private MainViewModel mainViewModel;

    private ArrayList<dustbin> dustbins;
    private static final String LIST_FRAGMENT_TAG="DustbinsListFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.android.cleancity.R.layout.activity_map);
        getLocationPermission();

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: Map is ready here");
        mMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, com.example.android.cleancity.R.raw.google_maps_style));

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }
        mMap.setOnMarkerClickListener((OnMarkerClickListener) this);
        mMap.setOnMapClickListener(latLng -> {
            Log.d(TAG, "onMapClick: Map clicked");
            cardView.setVisibility(View.GONE);
        });
        addingFirebaseData(mMap);
        getDeviceLocation();
    }

    public void addingFirebaseData(final GoogleMap googleMap) {
        dustbins = new ArrayList<>();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference reference = firebaseDatabase.getReference("dusbins");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                double latitude, longitude;
                long id, level, area;
                float rate;

                id = (Long) dataSnapshot.child("id").getValue();
                latitude = (Double) dataSnapshot.child("latitude").getValue();
                longitude = (Double) dataSnapshot.child("longitude").getValue();
                level = (Long) dataSnapshot.child("level").getValue();
                area = (Long) dataSnapshot.child("area").getValue();
                rate = Float.valueOf(dataSnapshot.child("rate").getValue().toString());

                dustbin Du = new dustbin(id, level, latitude, longitude, area, rate);
                dustbins.add(Du);
                mainViewModel.setDustbinsList(dustbins);

                markerAdder(googleMap, id, level, area, latitude, longitude);
//                Toast.makeText(MapActivity.this,""+Pos.toString(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                googleMap.clear();
                addingFirebaseData(googleMap);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        toolbar = findViewById(R.id.toolbarMain);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.menuShowPath){
                    showBestPath();
                } else if(item.getItemId() == R.id.menuDustbinList){
                    DustbinsListFragment listFragment = DustbinsListFragment.newInstance();
                    listFragment.show(getSupportFragmentManager(), LIST_FRAGMENT_TAG);
                }
                return false;
            }
        });
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the current device location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                Task<Location> location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: got the location");
                        currentLocation = (Location) task.getResult();
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        ;
                        float zoom = 15f;
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
                    } else {
                        Log.d(TAG, "onComplete: current location is null");
                        Toast.makeText(MapActivity.this, "Couldn't find the current location", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void markerAdder(GoogleMap googleMap, long id, long level, long area, double longitude, double latitude) {
        if (level == 0) {
            Drawable dustbin = getResources().getDrawable(com.example.android.cleancity.R.drawable.ic_dustbin_green);
            BitmapDescriptor markerIcon = getMarkerIconFromDrawable(dustbin);

            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(longitude, latitude))
                    .title("Dustbin: " + id)
                    .icon(markerIcon));
        } else if (level == 1) {
            Drawable dustbin = getResources().getDrawable(com.example.android.cleancity.R.drawable.ic_dustbin_yellow);
            BitmapDescriptor markerIcon = getMarkerIconFromDrawable(dustbin);

            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(longitude, latitude))
                    .title("Dustbin: " + id)
                    .icon(markerIcon));
        } else if (level > 1) {
            Drawable dustbin = getResources().getDrawable(com.example.android.cleancity.R.drawable.ic_dustbin_red);
            BitmapDescriptor markerIcon = getMarkerIconFromDrawable(dustbin);

            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(longitude, latitude))
                    .title("Dustbin: " + id)
                    .icon(markerIcon));
        }

    }

    private void initMap() {
        Log.d(TAG, "initMap: Initializing the map");
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(com.example.android.cleancity.R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
        cardView = findViewById(com.example.android.cleancity.R.id.card_view);
        Button newButton = findViewById(com.example.android.cleancity.R.id.make_toast);
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapActivity.this, "Your complaint has been reported!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: Getting location permissions");
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case 1234: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    //initialize our map
                    initMap();
                }
            }
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick: " + marker.getTitle());
        cardView.setVisibility(View.VISIBLE);

        final TextView percentage, area, id, rateCard;
        final double[] rate = new double[1];
        final RatingBar ratingBar = findViewById(com.example.android.cleancity.R.id.rating_card);
        rateCard = findViewById(com.example.android.cleancity.R.id.rate_card);
        percentage = findViewById(com.example.android.cleancity.R.id.percentage_card);
        area = findViewById(com.example.android.cleancity.R.id.area_card);
        id = findViewById(R.id.id_card);

        final String id_s = marker.getTitle().substring(9);

        for (int i = 0; i < dustbins.size(); i++) {
            dustbin d = dustbins.get(i);
            if (Long.toString(d.getId()).equals(id_s)) {
                long p = d.getLevel();
                if (p == 0) {
                    percentage.setTextSize(72);
                    percentage.setText("<50%");
                } else if (p == 1) {
                    percentage.setTextSize(72);
                    percentage.setText("50%");
                } else if (p == 2) {
                    percentage.setTextSize(72);
                    percentage.setText("75%");
                } else if (p == 3) {
                    percentage.setTextSize(65);
                    percentage.setText("100%");
                }
                area.setText(String.format(Locale.ENGLISH, "%d", d.getArea()));
                id.setText(String.format(Locale.ENGLISH, "%d", d.getId()));
                ratingBar.setRating(d.getRate());
                rateCard.setText(String.format(Locale.ENGLISH, "%f", d.getRate()));
            }
        }

        return false;
    }

    public void showBestPath() {
        ArrayList<dustbin> dustbinsList = new ArrayList<>();
        for (int i = 0; i < dustbins.size(); i++) {
            dustbin d = dustbins.get(i);
            if (d.getLevel() > 0) {
                dustbinsList.add(d);
            }
        }

        double[][] AdjMat = new double[dustbinsList.size() + 1][dustbinsList.size() + 1];

        double cLat = currentLocation.getLatitude();
        double cLon = currentLocation.getLongitude();

        for (int i = 0; i < dustbinsList.size(); i++) {
            dustbin d = dustbinsList.get(i);
            double distance = SphericalUtil.computeDistanceBetween(new LatLng(cLat, cLon), new LatLng(d.getLatitude(), d.getLongitude()));
            AdjMat[0][i + 1] = distance;
            AdjMat[i + 1][0] = distance;
        }

        for (int i = 0; i < dustbinsList.size(); i++) {
            dustbin d1 = dustbinsList.get(i);
            for (int j = 0; j < dustbinsList.size(); j++) {
                dustbin d2 = dustbinsList.get(j);
                double distance = SphericalUtil.computeDistanceBetween(new LatLng(d1.getLatitude(), d1.getLongitude()), new LatLng(d2.getLatitude(), d2.getLongitude()));
                AdjMat[i + 1][j + 1] = distance;
                AdjMat[j + 1][i + 1] = distance;
            }
        }

        ArrayList<Integer> pathList = getShortestPath(AdjMat);

        Log.d(TAG, "SHORTEST PATH");
        Log.d(TAG, "Starting Position - " + cLat + "," + cLon);

        String url = "https://www.google.com/maps/dir/?api=1&origin="+cLat+","+cLon+"&destination="+cLat+","+cLon+"&waypoints=";

        for (int i = 1; i < pathList.size(); i++) {
            dustbin d = dustbinsList.get(pathList.get(i) - 1);
            url+=d.getLatitude()+","+d.getLongitude()+"|";
            Log.d(TAG, "Dustbin "+d.getId()+" - "+d.getLatitude()+","+d.getLongitude());
        }

        Uri directionUri = Uri.parse(url);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, directionUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private ArrayList<Integer> getShortestPath(double[][] AdjMat) {
        int len = AdjMat.length - 1;
        int[] pArr = new int[len];
        for (int i = 1; i <= len; i++) {
            pArr[i - 1] = i;
        }

        shortestDistance = Integer.MAX_VALUE;
        shortestPath = new ArrayList<>();
        permute(pArr, AdjMat);
        return shortestPath;
    }

    private double shortestDistance;
    private ArrayList<Integer> shortestPath;

    private void permute(int[] arr, double[][] AdjMat) {
        permuteHelper(arr, AdjMat, 0);
    }

    private void permuteHelper(int[] arr, double[][] AdjMat, int index) {
        if (index >= arr.length - 1) { //If we are at the last element - nothing left to permute
            //System.out.println(Arrays.toString(arr));
            //Print the array
            double currDist = AdjMat[0][arr[0]];
            for (int i = 0; i < arr.length - 1; i++) {
                currDist += AdjMat[arr[i]][arr[i + 1]];
            }

            if (currDist < shortestDistance) {
                shortestDistance = currDist;
                shortestPath = new ArrayList<>();
                shortestPath.add(0);
                for (int j : arr) {
                    shortestPath.add(j);
                }
            }

            return;
        }

        for (int i = index; i < arr.length; i++) { //For each index in the sub array arr[index...end]

            //Swap the elements at indices index and i
            int t = arr[index];
            arr[index] = arr[i];
            arr[i] = t;

            //Recurse on the sub array arr[index+1...end]
            permuteHelper(arr, AdjMat, index + 1);

            //Swap the elements back
            t = arr[index];
            arr[index] = arr[i];
            arr[i] = t;
        }
    }
}
