package com.evazu;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.security.spec.MGF1ParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;

    private LocationCallback locationCallback;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 99;
    public static final int DEFAULT_ZOOM = 18;

    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;

    private View mapView;
    private boolean mSlideState = false;
    private ImageView navigationButton, locationBtn;
    private TextView locationSearch;
    private DrawerLayout mDrawerLayout;

    private ImageView profilePic;
    private TextView profileName;

    private View nav_header;
    private TextView profileIncompleteTV, codeTV;

    private RelativeLayout rideLayout;

    private NavigationDrawerExpandableListAdapter navigationDrawerExpandableListAdapter;
    private ExpandableListView listView;

    private List<String> _headerList;
    private HashMap<String, List<String>> _childList = new HashMap<>();
    private HashMap<String, Drawable> _icons = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();


        if(mFirebaseUser == null) {

            startNewActivity(LoginActivity.class);
        }
        setContentView(R.layout.activity_maps);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        nav_header = findViewById(R.id.nav_header_main);
        profilePic = nav_header.findViewById(R.id.profilePic);
        profileName = nav_header.findViewById(R.id.profileName);
        listView = findViewById(R.id.navigationListView);

        View view = getLayoutInflater().inflate(R.layout.navigation_bottom, listView, false);
        LinearLayout footerLayout = view.findViewById(R.id.parent);


        listView.addFooterView(footerLayout);

        prepareData();

        navigationDrawerExpandableListAdapter = new NavigationDrawerExpandableListAdapter(this, _headerList, _childList, _icons);

        listView.setAdapter(navigationDrawerExpandableListAdapter);

        listView.setOnGroupClickListener((ExpandableListView parent, View v, int groupPosition, long id) -> {

            switch (groupPosition) {
                case 1:
                    activityStart(Payment.class);
                    break;
                case 5:
                    activityStart(BecomePartner.class);
                    break;
            }

            if(groupPosition != (_headerList.size() - 1)) {
                new Handler().postDelayed(() -> mDrawerLayout.closeDrawer(Gravity.START), 1000);
            }


            return false;
        });

        listView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            if(groupPosition == (_headerList.size() - 1)) {
                switch (childPosition) {
                    case 0:
                        activityStart(FAQ.class);
                        break;
                    case 1:
                        activityStart(Troubleshoot.class);
                }
            }
            new Handler().postDelayed(() -> mDrawerLayout.closeDrawer(Gravity.START), 1000);

            return false;
        });

        TextView terms = footerLayout.findViewById(R.id.terms_of_service);
        terms.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, TermsAndConditions.class);
            intent.putExtra("from_class", "maps_terms");
            startActivity(intent);
            Animatoo.animateSlideLeft(this);
        });

        setProfile();
        navigationDrawer();

        navigationButton = findViewById(R.id.navigationButton);
        locationBtn = findViewById(R.id.locationBtn);
        locationSearch = findViewById(R.id.search_location);
        profileIncompleteTV = findViewById(R.id.profileIncompleteTV);
        codeTV = findViewById(R.id.codeTV);
        rideLayout = findViewById(R.id.rideLayout);
        //mSearchBtn = findViewById(R.id.searchBtn);

        locationSearch.setSelected(true);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawer(GravityCompat.START);

        mDrawerLayout.addDrawerListener(new ActionBarDrawerToggle(this,
                mDrawerLayout,
                0,
                0){
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                mSlideState=false;//is Closed
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mSlideState=true;//is Opened
            }});

        navigationButton.setOnClickListener(v -> {
            if(mSlideState) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                Log.i("Navigation","Close:"+mSlideState);
            }
            else {
                mDrawerLayout.openDrawer(GravityCompat.START);
                Log.i("Navigation","Open:"+mSlideState);
            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();



        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    //The last location in the list is the newest
                    Location location = locationList.get(locationList.size() - 1);
                    Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                    mLastLocation = location;
                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }

                    //Place current location marker
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.position(latLng);
//                    markerOptions.title("Current Position");
//                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//                    mCurrLocationMarker = mMap.addMarker(markerOptions);

                    //move map camera
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

                    CircleOptions circleOptions = new CircleOptions()
                            .center(latLng)
                            .radius(1000)
                            .strokeWidth(2)
                            .strokeColor(Color.BLUE)
                            .fillColor(Color.parseColor("#500084d3"));
                    mMap.addCircle(circleOptions);

                Log.i("Location", "Latitude:"+location.getLatitude());
                Log.i("Location","Longitude:"+location.getLongitude());
            }
            }

        };

//        mSearchBtn.setOnClickListener(v -> {
//            onMapSearch(v);
//        });

        locationBtn.setOnClickListener(v -> {
            getCurrentLocation();
        });

        rideLayout.setOnClickListener(v -> {
            activityStart(QRCodeActivity.class);
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

            if (!success) {
                Log.e("TAG", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("TAG", "Can't find style. Error: ", e);
        }
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            buildGoogleApiClient();
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
            mMap.setPadding(20,0,0,90);

        }
        else {
            checkLocationPermission();
        }
    }

    private void prepareData() {
        _headerList = Arrays.asList(getResources().getStringArray(R.array.navigation_menu));
        List<String> helpList = Arrays.asList(getResources().getStringArray(R.array.help_child));
        List<String> emptyList = Arrays.asList(new String[]{});

        _childList.put(_headerList.get(0), emptyList);
        _childList.put(_headerList.get(1), emptyList);
        _childList.put(_headerList.get(2), emptyList);
        _childList.put(_headerList.get(3), emptyList);
        _childList.put(_headerList.get(4), emptyList);
        _childList.put(_headerList.get(5), emptyList);
        _childList.put(_headerList.get(6), helpList);

        _icons.put(_headerList.get(0), getResources().getDrawable(R.drawable.how_to_ride_icon));
        _icons.put(_headerList.get(1), getResources().getDrawable(R.drawable.payments));
        _icons.put(_headerList.get(2), getResources().getDrawable(R.drawable.top_rides));
        _icons.put(_headerList.get(3), getResources().getDrawable(R.drawable.ride_history));
        _icons.put(_headerList.get(4), getResources().getDrawable(R.drawable.pay_per_ride));
        _icons.put(_headerList.get(5), getResources().getDrawable(R.drawable.become_partner));
        _icons.put(_headerList.get(6), getResources().getDrawable(R.drawable.help));
    }

    private void setProfile() {
        Picasso.get().load(mFirebaseUser.getPhotoUrl()).into(profilePic);
        profileName.setText(mFirebaseUser.getDisplayName());

        new EvazuDatabase().getValue("aadhaar_card", result -> {
            if(result == null) {
                profileIncompleteTV.setVisibility(View.VISIBLE);
                profileIncompleteTV.setOnClickListener(v -> {
                    Intent intent = new Intent(MapsActivity.this, AadhaarCard.class);
                    intent.putExtra("prevClass", "Maps");
                    startActivity(intent);
                    Animatoo.animateSlideLeft(this);
                });
            }
        });

    }

    public void onMapSearch(View view) {

        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title(location));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

            CircleOptions circleOptions = new CircleOptions()
                    .center(latLng)
                    .radius(1000)
                    .strokeWidth(2)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.parseColor("#500084d3"));
            mMap.addCircle(circleOptions);
        }

        locationSearch.clearFocus();
    }

    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        client.connect();
    }

    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location location = null;

        if(network_enabled){

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if(location != null){
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                MarkerOptions markerOptions = new MarkerOptions();
//                markerOptions.position(latLng);
//                markerOptions.title("Current Position");
//                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//                mCurrLocationMarker = mMap.addMarker(markerOptions);

                //move map camera
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
            }

            if (mapView != null &&
                    mapView.findViewById(Integer.parseInt("1")) != null) {
                // Get the button view
                View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                // and next place it, on bottom right (as Google Maps app)
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                        locationButton.getLayoutParams();
                // position on right bottom
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                layoutParams.setMargins(0, 0, 150, 100);
            }

        }

        try {

            Geocoder geocoder = new Geocoder(MapsActivity.this);
            List<Address> fromLocation = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            Address address = fromLocation.get(0);
            Log.i("Address", address.getAddressLine(0));
//            if(address.toString() != null)
//                Toast.makeText(MapsActivity.this, address.getAddressLine(0), Toast.LENGTH_LONG).show();
//            else
//                Toast.makeText(MapsActivity.this, "Couldn't get Address", Toast.LENGTH_LONG).show();
            locationSearch.setText(address.getAddressLine(0));
        }
        catch (Exception e) {

        }
    }

    private boolean checkLocationPermission() {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
            else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
            return false;
        }
        else
            return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if(client == null) {
                            buildGoogleApiClient();
                        }
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        mMap.getUiSettings().setCompassEnabled(false);
                        mMap.setMyLocationEnabled(true);
                        getCurrentLocation();
                    }
                }
                else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();

        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onPause() {
        if(mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }

        super.onPause();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
//        switch (menuItem.getItemId()) {
//            case R.id.logout:
//                mAuth.signOut();
//                startNewActivity(LoginActivity.class);
//                break;
//            default: Toast.makeText(MapsActivity.this, "Navigation Item Clicked", Toast.LENGTH_LONG).show();
//        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    private void navigationDrawer() {
        nav_header.setOnClickListener(v -> {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MapsActivity.this, Profile.class);
                startActivity(intent);
                Animatoo.animateSlideLeft(MapsActivity.this);
            }, 500);
        });
    }

    private void startNewActivity(Class intentClass) {
        Intent intent;
        intent = new Intent(MapsActivity.this, intentClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        Animatoo.animateSlideRight(MapsActivity.this);
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Do you really want to exit?")
                .setNegativeButton("YES", (dialog, which) -> MapsActivity.super.onBackPressed())
                .setPositiveButton("NO", (dialog, which) -> dialog.cancel())
                .setNeutralButton("RATE US", (dialog, which) -> {
                    //final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                    final String appPackageName = "com.mxtech.videoplayer.ad";
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        Animatoo.animateSlideLeft(this);
    }

    private void activityStart(Class newClass) {
        Intent intent = new Intent(this, newClass);
        startActivity(intent);
        Animatoo.animateSlideLeft(this);
    }
}