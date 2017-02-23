package com.umer.towtruckdriver.Constants;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.List;
import java.util.Locale;

/**
 * Created by Mansoor Ali on 10/5/2016.
 */
public class Constants {

    public static final String SHARED_PREFERENCES_PROFILE="SharedPreferencesProfile";
    public static final String DEBUG_KEY = "DEBUG_KEY";
    public static final int SERVER_RESPONSE_CODE_SUCCESS = 1;
    public static final int SERVER_RESPONSE_CODE_ERROR = 0;
    public static final int SERVER_RESPONSE_CODE_ALREADY_EXIST = 2;
    public static final int SERVER_RESPONSE_CODE_TIME_OUT = 100;
    public static final int SERVER_RESPONSE_CODE_NO_DATA = 101;
    public static final String ENTER_EMAIL="Enter Email";
    public static final String ENTER_PASSWORD="Enter Password";
    public static final String LOGGED_IN="loggedIn";
    public static final String NETWORK_ERROR="No internet connection...";

    public static final long LOCAL_LOCATION_INTERVAL = 5*1000;

    public static String driverId;

    public static boolean isNwConnected(Context context) {
        if (context == null) {
            return true;
        }
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
        if (nwInfo != null && nwInfo.isConnected()){
            return true;
        }
        return false;
    }

    public static String getAddress(double lati,double longi,Context context)
    {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String address = "",city = "",state = "",zip = "",country = "",knownName="";
        try
        {
            List<Address> addresses  = geocoder.getFromLocation(lati, longi, 1);
            address = addresses.get(0).getAddressLine(0);
            knownName = addresses.get(0).getFeatureName();
            city = addresses.get(0).getLocality();
            state = addresses.get(0).getAdminArea();
            zip = addresses.get(0).getPostalCode();
            country = addresses.get(0).getCountryName();
        }
        catch (Exception e)
        {

        }
        return address+" "+city;
    }

}
