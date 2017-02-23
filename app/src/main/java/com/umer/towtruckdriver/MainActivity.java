package com.umer.towtruckdriver;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;
import com.umer.towtruckdriver.Constants.Constants;
import com.umer.towtruckdriver.Constants.Url;
import com.umer.towtruckdriver.Firebase.MyFirebaseInstanceIDService;
import com.umer.towtruckdriver.MapService.DownloadTask;
import com.umer.towtruckdriver.Service.ExecuteTask;
import com.umer.towtruckdriver.interfaces.TaskListener;
import com.umer.towtruckdriver.activty.LoginActivity;
import com.umer.towtruckdriver.fragments.FragmentFeedback;
import com.umer.towtruckdriver.fragments.FragmentRating;
import com.umer.towtruckdriver.fragments.HistoryFragment;
import com.umer.towtruckdriver.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.umer.towtruckdriver.activty.LoginActivity.customerRequestInfo;


public class MainActivity extends AppCompatActivity implements TaskListener, AHBottomNavigation.OnTabSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, LocationListener, GoogleMap.OnMapLoadedCallback {

    private AHBottomNavigation bottomNavigation;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private Location location;
    private LocationRequest mLocationRequest;
    private Marker driverMarker, userMarker;
    private LatLng userLatLng;

    private Button driverStatus;
    private ImageView historyIcon;

    private int num = 0, locationStatus = 1;
    private int interval = 10000;

    private Handler mHandler;
    private boolean optionsSelected = false, isCustomerLocationEnable = false;
    private boolean userOrigin, userDestination = false;

    private RelativeLayout userRequestNotification, userRequestNotificationTimer, userContactDetails, userAddressDetails,
            driverReachedNotification, driverReachedDestinationNotification;

    private ProgressDialog progressDialog;

    private CountDownTimer countDownTimer;

    double userLat, userLong, userEndLat, userEndLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initControls();

    }

    private void initControls() {

        InitializeServices();

        mHandler = new Handler();

        buildAndConnectGoogleApiClient();

        userRequestNotification = (RelativeLayout) findViewById(R.id.userRequestNotification);
        userRequestNotificationTimer = (RelativeLayout) findViewById(R.id.userRequestNotificationTimer);
        userContactDetails = (RelativeLayout) findViewById(R.id.userContactDetails);
        userAddressDetails = (RelativeLayout) findViewById(R.id.userAddressDetails);
        driverReachedNotification = (RelativeLayout) findViewById(R.id.driverReachedNotification);
        driverReachedDestinationNotification = (RelativeLayout) findViewById(R.id.driverReachedDestinationNotification);
        userRequestNotification.setVisibility(View.GONE);
        userRequestNotificationTimer.setVisibility(View.GONE);
        userContactDetails.setVisibility(View.GONE);
        userAddressDetails.setVisibility(View.GONE);
        driverReachedNotification.setVisibility(View.GONE);
        driverReachedDestinationNotification.setVisibility(View.GONE);

        historyIcon = (ImageView) findViewById(R.id.historyIcon);
        driverStatus = (Button) findViewById(R.id.driverStatus);

        historyIcon.setOnClickListener(this);
        driverStatus.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("loading...");
        progressDialog.setCancelable(false);

    }

    private void InitializeServices() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            //When the broadcast received
            //We are sending the broadcast from GCMRegistrationIntentService
            @Override
            public void onReceive(Context context, Intent intent) {
                Utils.showLoger("Received BroadCast....");
                if (intent.getAction().equals(MyFirebaseInstanceIDService.REGISTRATION_SUCCESS)) {

                    String token = intent.getStringExtra("token");

                    Toast.makeText(getApplicationContext(), "Registration token:" + token, Toast.LENGTH_LONG).show();
                    sendTokenToServer(token);

                } else if (intent.getAction().equals(MyFirebaseInstanceIDService.REGISTRATION_ERROR)) {
                    Toast.makeText(getApplicationContext(), "GCM registration error!", Toast.LENGTH_LONG).show();
                } else {
                    if (intent.hasExtra("message")) {
                        String title = intent.getStringExtra("title");
                        String message = intent.getStringExtra("message");
                        String type = intent.getStringExtra("type");
                        Utils.showLoger("Received BroadCast  "+message+" and Type "+type);
                        if (type.equals("ServiceRequest")) {
                            RequestReceivedNotification();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_LONG).show();
                    }
                }
            }
        };

        if (!Utils.isNetworkAvailable(MainActivity.this)) {
            Toast.makeText(MainActivity.this, Constants.NETWORK_ERROR, Toast.LENGTH_SHORT).show();

        } else {
            startGcmRegService();

            String token = FirebaseInstanceId.getInstance().getToken();
            Log.v("GCM Token", token);
            if (token != null && !token.isEmpty()) {
                sendTokenToServer(token);
            }
        }

        startMapService();
        setUpBottomTabView();
    }

    private void startMapService() {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public synchronized void buildAndConnectGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }


    public void sendTokenToServer(final String token) {

        Map<String, String> params = new LinkedHashMap<>();
        params.put("did", Constants.driverId);
        params.put("gcm", token);
        params.put("d_type", "Android");

        num = 1;

        new ExecuteTask(this, params, Url.UpdateDriverGCM, this);

    }

    public void startGcmRegService() {
        //Checking play service is available or not
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        //if play service is not available
        if (ConnectionResult.SUCCESS != resultCode) {
            //If play service is supported but not installed
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                //Displaying message that play service is not installed
                Toast.makeText(getApplicationContext(), "Google Play Service is not install/enabled in this device!", Toast.LENGTH_LONG).show();
                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());
                //If play service is not supported
                //Displaying an error message
            } else {
                Toast.makeText(getApplicationContext(), "This device does not support for Google Play Service!", Toast.LENGTH_LONG).show();
            }
            //If play service is available
        } else {
            //Starting intent to register device
            Intent intent = new Intent(MainActivity.this, MyFirebaseInstanceIDService.class);
            startService(intent);
        }
    }

    @Override
    public void onSuccess(String result) {

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        JSONObject jsonObject;

        if (num == 1) {
            try {
                jsonObject = new JSONObject(result);

                if (jsonObject.getJSONArray("titles").get(0).equals("success")) {
                    Toast.makeText(this, "Driver Gcm Updated", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (num == 2) {
            try {
                jsonObject = new JSONObject(result);

                if (jsonObject.getString("Status").equals("Success")) {
                    Toast.makeText(this, "Driver Location Updated", Toast.LENGTH_SHORT).show();
                    handleNewLocation();
                } else {
                    Toast.makeText(this, "Driver Location Failed to Update", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (num == 3) {
            try {
                jsonObject = new JSONObject(result);

                if (jsonObject.getString("status").equals("success")) {
                    Toast.makeText(this, "Request Accepted", Toast.LENGTH_SHORT).show();
                    isCustomerLocationEnable = true;
                    UpdateCustomerLocationOnMap();
                    DriverBusy();
                    ShowUserDetail();
                    DrawRoute();
                    locationStatus = 0;
                } else {
                    Toast.makeText(this, "Request Failed to Accept", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (num == 4) {
            try {

                jsonObject = new JSONObject(result);
                if (jsonObject.getString("status").equals("success")) {
                    Toast.makeText(this, "Request Rejected", Toast.LENGTH_SHORT).show();
                    locationStatus = 0;

                    UpdateDriverLocation(locationStatus);

                    MakeDriverOffline();

                } else {
                    Toast.makeText(this, "Request Failed to Reject", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (num == 5) {
            try {
                jsonObject = new JSONObject(result);
                if (jsonObject.getString("status").equals("success")) {
                    Toast.makeText(this, "Request Complete", Toast.LENGTH_SHORT).show();
                    ResetSettings(jsonObject.getString("total"));
                } else {
                    Toast.makeText(this, "Request Failed to Complete", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (num == 6) {

            try {
                progressDialog.dismiss();
                jsonObject = new JSONObject(result);
                if (jsonObject.getString("Status").equals("Success")) {
                    Toast.makeText(this, "Start Ride", Toast.LENGTH_SHORT).show();
                    StartRide();
                } else {
                    driverReachedAtUserPlace();
                    Toast.makeText(this, "Request Failed to Complete", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (num == 7) {

            try {
                progressDialog.dismiss();
                jsonObject = new JSONObject(result);
                if (jsonObject.getString("Status").equals("Success")) {
                    Toast.makeText(this, "Rating Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to Rate", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onFailure(String result) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Toast.makeText(this, "Network Error Occurred.Try Again.", Toast.LENGTH_SHORT).show();
    }

    private void setUpBottomTabView() {

        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);

        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab1, R.drawable.home, R.color.buttonOrange);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab2, R.drawable.rating, R.color.buttonOrange);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab3, R.drawable.acount, R.color.buttonOrange);

// Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);

        //       bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FFFFFF"));

        bottomNavigation.setAccentColor(Color.parseColor("#F56914"));
        bottomNavigation.setInactiveColor(Color.parseColor("#747474"));


        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        bottomNavigation.setOnTabSelectedListener(this);
        bottomNavigation.setVisibility(View.VISIBLE);

    }

    @Override
    public boolean onTabSelected(int position, boolean wasSelected) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        switch (position) {
            case 0:
                while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    super.onBackPressed();
                }
                break;
            case 1:
                ft.replace(R.id.container, new FragmentRating());
                ft.addToBackStack("tab");
                ft.commit();
                break;
            case 2:
                ft.replace(R.id.container, new FragmentProfile());
                ft.addToBackStack("tab");
                ft.commit();
                break;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.LOCAL_LOCATION_INTERVAL);
        buildAndConnectGoogleApiClient();

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(MyFirebaseInstanceIDService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(MyFirebaseInstanceIDService.REGISTRATION_ERROR));

        registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter("really"));

    }

    @Override
    protected void onPause() {
        super.onPause();
        //      stopLocationUpdates();
        unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.setOnMapLoadedCallback(this);
    }

    @Override
    public void onMapLoaded() {

        if (map != null && customerRequestInfo != null) {

            if (customerRequestInfo.getRequestStatus().equals("Request Accepted")) {

                CustomerLocations();
                disableNavigation();
                isCustomerLocationEnable = true;
                UpdateCustomerLocationOnMap();
                ShowUserDetail();
                DrawRoute();
                locationStatus = 0;
                userOrigin = true;
                userDestination = false;
                DriverBusy();
                optionsSelected = true;
                startRepeatingTask();
            } else if (customerRequestInfo.getRequestStatus().equals("Request Completed")) {

            } else if (customerRequestInfo.getRequestStatus().equals("Request Started")) {

                CustomerLocations();
                disableNavigation();
                userOrigin = false;
                userDestination = true;
                locationStatus = 0;
                DriverBusy();
                optionsSelected = true;
                startRepeatingTask();
                ShowUserDetail();
                userContactDetails.setVisibility(View.GONE);
                StartRide();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            handleNewLocation();
            //map.setMyLocationEnabled(true);
        }
        startLocationUpdates();

    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void handleNewLocation() {

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        if(driverMarker != null){
            driverMarker.remove();
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.dala))
                .position(latLng)
                .anchor(0.5f,0.5f)
                .rotation(0f);

        map.clear();
        driverMarker = map.addMarker(markerOptions);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f));

        if(userOrigin){
            UpdateUserPosition(userLat,userLong);
            DrawRoute();
        }else if (userDestination){
            UpdateUserPosition(userEndLat,userEndLong);
            DrawRoute();
        }
        calculateDistance();
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.driverStatus:
                changeStatus();
                break;
            case R.id.historyIcon:
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(android.R.id.content,new HistoryFragment());
                ft.addToBackStack("fragment");
                ft.commit();
            //    bottomNavigation.setVisibility(View.GONE);
                break;
            case R.id.userRequestNotificationTimer:
                CallAcceptRequest();
                break;
            case R.id.driverReachedNotification:
                StartRide();
                break;
            case R.id.driverReachedDestinationNotification:
                EndRide();
                break;
            case R.id.btnCall:
                CallUser();
                break;
            case R.id.userContactDetails:
                ShowUserDescription();
                break;
        }
    }

    private void changeStatus(){

        progressDialog.show();

        if(driverStatus.getText().equals("Go Online")){
            driverStatus.setBackgroundResource(R.drawable.orange_rounded_button);
            //driverStatus.setBackground(getDrawable(R.drawable.orange_rounded_button));
            driverStatus.setTextColor(getResources().getColor(R.color.colorWhite));
            driverStatus.setText(getText(R.string.go_offline));
            optionsSelected = true;
            locationStatus = 1;
            UpdateDriverLocation(locationStatus);
            startRepeatingTask();

        }else if(driverStatus.getText().equals("Go Offline")){
            driverStatus.setBackgroundResource(R.drawable.white_rounded_button);
            //driverStatus.setBackground(getDrawable(R.drawable.white_rounded_button));
            driverStatus.setTextColor(getResources().getColor(R.color.buttonOrange));
            driverStatus.setText(getText(R.string.go_online));
            optionsSelected = false;
            stopRepeatingTask();
            locationStatus=0;
            UpdateDriverLocation(locationStatus);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    public void UpdateDriverLocation(int locStatus){

        Map<String,String> params = new LinkedHashMap<>();
        params.put("id", LoginActivity.driverInfo.getDriverId());
        params.put("lat", Double.toString(location.getLatitude()));
        params.put("lon",Double.toString(location.getLongitude()));
        params.put("avail",Integer.toString(locStatus));

        num = 2;

        new ExecuteTask(this,params, Url.driverLocation,this);
    }

    Runnable mStatusChecker = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                if(!optionsSelected){
                    return;
                }
                else{
                    UpdateDriverLocation(locationStatus);
                }

            }
            finally
            {
                mHandler.postDelayed(mStatusChecker, interval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {

        optionsSelected = false;
        mHandler.removeCallbacks(mStatusChecker);
    }

    private void RequestReceivedNotification(){
        Utils.showLoger("RequestReceivedNotification... ");
        if(getSupportFragmentManager().getBackStackEntryCount()>0){
            while(getSupportFragmentManager().getBackStackEntryCount()>0) {
                super.onBackPressed();
            }
        }

        bottomNavigation.setCurrentItem(0);

        CustomerLocations();
        UpdateDriverLocation(0);

        final TextView timer,username;
        ImageView userImage;

        bottomNavigation.setOnTabSelectedListener(null);
        bottomNavigation.setEnabled(false);
        bottomNavigation.setFocusable(false);
        bottomNavigation.setFocusableInTouchMode(false);

        userRequestNotification.setVisibility(View.VISIBLE);
        userRequestNotificationTimer.setVisibility(View.VISIBLE);
        userRequestNotificationTimer.setOnClickListener(this);

        username = (TextView)userRequestNotification.findViewById(R.id.customerName);
        userImage = (ImageView)userRequestNotification.findViewById(R.id.userPic);

        username.setText(customerRequestInfo.getName());

        if(customerRequestInfo.getDisplayPicture()!=null && !customerRequestInfo.getDisplayPicture().isEmpty()) {
            Picasso.with(MainActivity.this).load(customerRequestInfo.getDisplayPicture()).into(userImage);
        }else{
            Picasso.with(MainActivity.this).load(Url.NoDpUrl).into(userImage);
        }

        timer = (TextView)userRequestNotificationTimer.findViewById(R.id.timer);

        countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timer.setText(Long.toString(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                userRequestNotification.setVisibility(View.GONE);
                userRequestNotificationTimer.setVisibility(View.GONE);
                CallRejectRequest();
            }
        }.start();
    }


    private void CallRejectRequest() {

        isCustomerLocationEnable = false;
        bottomNavigation.setOnTabSelectedListener(this);
        bottomNavigation.setEnabled(true);

        if(userMarker!=null){
            userMarker.remove();
        }

        progressDialog.show();

        Map<String,String> params = new LinkedHashMap<>();
        params.put("request_id", customerRequestInfo.getRequestId());
        params.put("driver_id",LoginActivity.driverInfo.getDriverId());
        params.put("customer_lat", customerRequestInfo.getLatitude());
        params.put("customer_long", customerRequestInfo.getLongitude());
        params.put("driver_lat",Double.toString(location.getLatitude()));
        params.put("driver_long",Double.toString(location.getLongitude()));

        num = 4;

        new ExecuteTask(this,params,Url.RejectRequest,this);

    }

    private void CallAcceptRequest(){

        countDownTimer.cancel();
        userRequestNotification.setVisibility(View.GONE);
        userRequestNotificationTimer.setVisibility(View.GONE);

        progressDialog.show();

        Map<String,String> params = new LinkedHashMap<>();
        params.put("request_id", customerRequestInfo.getRequestId());
        params.put("driver_id",LoginActivity.driverInfo.getDriverId());

        num = 3;

        new ExecuteTask(this,params,Url.AcceptRequest,this);
    }

    private void UpdateCustomerLocationOnMap() {

 //       isCustomerLocationEnable = true;
        userOrigin = true;
        userDestination = false;

        userMarker = map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
        .position(new LatLng(userLat,userLong)));

        userLatLng = new LatLng(userLat,userLong);
    }

    private void ShowUserDetail(){

        final TextView address,username,phone,rating;
        ImageView userImage;
        RatingBar ratingBar;
        Button call;

 //       DrawRoute();

        userContactDetails.setVisibility(View.VISIBLE);
        userAddressDetails.setVisibility(View.VISIBLE);

        userContactDetails.setOnClickListener(this);

        address = (TextView)userAddressDetails.findViewById(R.id.userAddress);
        username = (TextView)userContactDetails.findViewById(R.id.userContactDetailsName);
        phone = (TextView)userContactDetails.findViewById(R.id.userPhone);
        rating = (TextView)userContactDetails.findViewById(R.id.userRatingNum);
        ratingBar = (RatingBar)userContactDetails.findViewById(R.id.userRating);
        userImage = (ImageView)userContactDetails.findViewById(R.id.userContactDetailsPic);
        call = (Button)userContactDetails.findViewById(R.id.btnCall);
        call.setOnClickListener(this);

        address.setText(customerRequestInfo.getStartAddress());
        username.setText(customerRequestInfo.getName());
        phone.setText(customerRequestInfo.getPhone());

        if(customerRequestInfo.getDisplayPicture() != null && !customerRequestInfo.getDisplayPicture().isEmpty()){
            Picasso.with(MainActivity.this).load(customerRequestInfo.getDisplayPicture()).into(userImage);
        }else{
            Picasso.with(MainActivity.this).load(Url.NoDpUrl).into(userImage);
            customerRequestInfo.setDisplayPicture(Url.NoDpUrl);
        }

        if(customerRequestInfo.getRating() != null && !customerRequestInfo.getRating().isEmpty()){
            rating.setText(customerRequestInfo.getRating());
            ratingBar.setRating(Float.parseFloat(customerRequestInfo.getRating()));
        }
        if(isCustomerLocationEnable) {
            calculateDistance();
        }
    }

    private void DrawRoute() {

        LatLng origin = new LatLng(location.getLatitude(),location.getLongitude());

        LatLng destination = new LatLng(userLatLng.latitude,userLatLng.longitude);

        DownloadTask downloadTask = new DownloadTask(map);

        String url = downloadTask.getDirectionsUrl(origin, destination);

        downloadTask.execute(url);
    }

    private void calculateDistance() {

        Location driver = location;

        if(isCustomerLocationEnable){

            if(userOrigin) {
                Location user = new Location("User Origin");
                user.setLatitude(userLat);
                user.setLongitude(userLong);

                float distance = driver.distanceTo(user);

                if (distance < 40) {
                    isCustomerLocationEnable = false;
                    driverReachedAtUserPlace();
                }
            }else if(userDestination){
                Location user = new Location("User Destination");
                user.setLatitude(userLatLng.latitude);
                user.setLongitude(userLatLng.longitude);

                float distance = driver.distanceTo(user);

                if (distance < 40) {
                    CustomerReachedAtDestination();
                    isCustomerLocationEnable = false;
                }
            }
        }else {
            if(userMarker !=null){
                userMarker.remove();
            }
        }
    }

    private void driverReachedAtUserPlace() {

        userContactDetails.setVisibility(View.GONE);
        driverReachedNotification.setVisibility(View.VISIBLE);

        final RelativeLayout data = (RelativeLayout)findViewById(R.id.driverReachedNotification);
        final float alpha = data.getAlpha();

        SeekBar seekBar = (SeekBar)driverReachedNotification.findViewById(R.id.driverReachedSwipe);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                float a = progress/100f;
                data.setAlpha(1 - a);

                if(progress>90){
                    driverReachedNotification.setVisibility(View.GONE);
                    StartRideApiCall();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(5);
                data.setAlpha(alpha);
            }
        });

    }

    private void StartRideApiCall(){

        progressDialog.show();

        Map<String,String> params = new LinkedHashMap<>();
        params.put("request_id", customerRequestInfo.getRequestId());
        params.put("customer_id", customerRequestInfo.getCustomerID());

        num = 6;

        new ExecuteTask(this,params,Url.StartService,this);
    }

    private void StartRide(){

        driverReachedNotification.setVisibility(View.GONE);

        if(userMarker !=null){
            userMarker.remove();
        }

        userMarker = map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
                .position(new LatLng(userEndLat,userEndLong)));

        UpdateUserPosition(userEndLat,userEndLong);

        DrawRoute();
        userOrigin = false;
        isCustomerLocationEnable = true;
        userDestination = true;
    }

    private void CustomerReachedAtDestination(){

        driverReachedNotification.setVisibility(View.GONE);
        driverReachedDestinationNotification.setVisibility(View.VISIBLE);

        final RelativeLayout data = (RelativeLayout)findViewById(R.id.driverReachedDestinationNotification);
        final float alpha = data.getAlpha();

        SeekBar seekBar = (SeekBar)driverReachedDestinationNotification.findViewById(R.id.driverReachedDestinationSwipe);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                float a = progress/100f;
                data.setAlpha(1 - a);

                if(progress>90){
                    driverReachedDestinationNotification.setVisibility(View.GONE);
                    EndRide();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(5);
                data.setAlpha(alpha);
            }
        });

    }

    private void MakeDriverOffline(){

        locationStatus = 0;
        UpdateDriverLocation(locationStatus);
        optionsSelected = false;
        stopRepeatingTask();

        final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {

                locationStatus = 1;
                optionsSelected = true;
                startRepeatingTask();
                service.shutdown();
            }
        }, 60, 60, TimeUnit.SECONDS);

    }

    private void EndRide(){

        driverReachedDestinationNotification.setVisibility(View.GONE);
        userAddressDetails.setVisibility(View.GONE);

        Map<String,String> params = new LinkedHashMap<>();
        params.put("rid", customerRequestInfo.getRequestId());
        params.put("distance", customerRequestInfo.getDistance());
        params.put("cid", customerRequestInfo.getCustomerID());

        num=5;

        new ExecuteTask(this,params,Url.CompleteRequest,this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CallUser();
                }
                return;
            }
        }
    }

    private void CallUser(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);

        }
        else{
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + customerRequestInfo.getPhone()));
            startActivity(intent);
        }
    }

    private void ResetSettings(String total){

        map.clear();

        locationStatus = 1;

        isCustomerLocationEnable = false;
        UpdateDriverLocation(locationStatus);

        driverStatus.setBackground(getResources().getDrawable(R.drawable.orange_rounded_button));
        driverStatus.setTextColor(getResources().getColor(R.color.colorWhite));
        driverStatus.setText(getText(R.string.go_offline));
        driverStatus.setEnabled(true);
        driverStatus.setClickable(true);
        optionsSelected = true;

        bottomNavigation.setEnabled(true);
        bottomNavigation.setFocusableInTouchMode(true);
        bottomNavigation.setFocusable(true);
        bottomNavigation.setOnTabSelectedListener(this);

        userOrigin = false;
        userDestination = false;

   //     ShowCustomerFeedbackNotification();

        Bundle b = new Bundle();
        b.putString("total",total);

        FragmentFeedback fragmentFeedback = new FragmentFeedback();
        fragmentFeedback.setArguments(b);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container,fragmentFeedback);
        ft.addToBackStack("tab");
        ft.commit();

    }

    private void DriverBusy(){

        driverStatus.setBackground(getResources().getDrawable(R.drawable.grey_rounded_button));
        driverStatus.setText("Busy");
        driverStatus.setTextColor(getResources().getColor(android.R.color.white));
        driverStatus.setOnClickListener(null);
        driverStatus.setEnabled(false);
    }

    private void ShowCustomerFeedbackNotification(){

       /* final Dialog rankDialog = new Dialog(MainActivity.this, R.style.FullHeightDialog);
        rankDialog.setContentView(R.layout.rating_feedback);
        rankDialog.setCancelable(false);
        rankDialog.show();

        final RatingBar ratingBar = (RatingBar)rankDialog.findViewById(R.id.dialog_ratingbar);

        TextView text = (TextView) rankDialog.findViewById(R.id.rank_dialog_text1);
        text.setText(LoginActivity.customerRequestInfo.getName());

        Button updateButton = (Button) rankDialog.findViewById(R.id.rank_dialog_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ratingBar.getRating()>0) {

                    rankDialog.dismiss();

                    Map<String, String> params = new LinkedHashMap<>();
                    params.put("rid", LoginActivity.customerRequestInfo.getRequestId());
                    params.put("cid", LoginActivity.customerRequestInfo.getCustomerID());
                    params.put("rating", Float.toString(ratingBar.getRating()));
                    params.put("feedback", "");

                    num = 7;
                    new ExecuteTask(MainActivity.this, params, Url.FeedBack, MainActivity.this);
                }
            }
        });*/
    }

    private void disableNavigation(){

        bottomNavigation.setEnabled(false);
        bottomNavigation.setFocusable(false);
        bottomNavigation.setFocusableInTouchMode(false);
        bottomNavigation.setOnTabSelectedListener(null);
    }

    private void UpdateUserPosition(double lat,double lon){

        userLatLng = new LatLng(lat,lon);

        userMarker = map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
                .position(new LatLng(lat,lon)));
    }

    private void CustomerLocations(){

        if(customerRequestInfo != null) {

            userLat = Double.parseDouble(customerRequestInfo.getLatitude());
            userLong = Double.parseDouble(customerRequestInfo.getLongitude());
            userEndLat = Double.parseDouble(customerRequestInfo.getEnd_latitude());
            userEndLong = Double.parseDouble(customerRequestInfo.getEnd_longitude());
        }
    }

    @Override
    protected void onDestroy() {
        UpdateDriverLocation(0);
        super.onDestroy();
    }

    private void ShowUserDescription(){

        String data[] = customerRequestInfo.getDescription().split(" ! ");

        if(data.length>1) {

            final NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(this);

            dialogBuilder
                    .withTitle("Description")
                    .withTitleColor(R.color.topBar)
                    .withDividerColor("#FFFFFF")
                    .withMessage(data[0] +"\n" + data[1] + "\n")
                    .withMessageColor(R.color.topBar)
                    .withDialogColor("#FFFFFF")
                    .withIcon(getResources().getDrawable(R.drawable.logo))
                    .withDuration(700)
                    .withEffect(Effectstype.SlideBottom)
                    .withButton1Text("OK")
                    .withButtonDrawable(R.drawable.blue_rounded_button)
                    .isCancelableOnTouchOutside(false)
                    .setButton1Click(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogBuilder.dismiss();
                        }
                    })
                    .show();
        }else{
            final NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(this);

            dialogBuilder
                    .withTitle("Description")
                    .withTitleColor(R.color.topBar)
                    .withDividerColor("#FFFFFF")
                    .withMessage(data[0] +"\n\n")
                    .withMessageColor(R.color.topBar)
                    .withDialogColor("#FFFFFF")
                    .withIcon(getResources().getDrawable(R.drawable.logo))
                    .withDuration(700)
                    .withEffect(Effectstype.SlideBottom)
                    .withButton1Text("OK")
                    .withButtonDrawable(R.drawable.blue_rounded_button)
                    .isCancelableOnTouchOutside(false)
                    .setButton1Click(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogBuilder.dismiss();
                        }
                    })
                    .show();
        }
    }

}
