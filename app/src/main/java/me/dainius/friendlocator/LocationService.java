package me.dainius.friendlocator;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class LocationService extends Service implements LocationListener {

    private static String SERVICE = "LocationService";
    private static Context context;
    private static int CONNECTED_CLOSE_BY = 4;
    private Location lastKnownLocation;
    private Location currentLocation;
    private double distanceChanged;
    private static final double LOG_DISTANCE_CHANGE_TO_DB = 1.0;
    private static final double ALERT_DISTANCE_TO_FRIEND = 3.0;
    private final IBinder locationBinder = new LocalLocationBinder();
    String userEmail = null;
    String friendEmail = null;

    public LocationService() {
    }

//    protected void onHandleIntent(Intent intent) {
//        this.userEmail = ParseUser.getCurrentUser().getEmail();
//        this.friendEmail = intent.getStringExtra("FriendEmail");
//    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(SERVICE, "onBind");
        this.userEmail = ParseUser.getCurrentUser().getEmail();
        this.friendEmail = intent.getStringExtra("FriendEmail");
        return this.locationBinder;
    }

    public class LocalLocationBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    public void onProviderEnabled(String provider) {

    }

    public void onProviderDisabled(String provider) {

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public void onLocationChanged(Location location) {
        Log.d(SERVICE, "LocationService onLocationChanged()");
        if(isNetworkAvailable()) {
            this.currentLocation = location;
            String locationString = "Updated Location: " + this.currentLocation.getLatitude() + ", " + this.currentLocation.getLongitude();
            Log.d(SERVICE, locationString);
            if (this.lastKnownLocation != null) {
                this.distanceChanged = this.roundDistance(this.currentLocation.distanceTo(this.lastKnownLocation));
                Log.d(SERVICE, "Distance Changed: " + this.distanceChanged);
                if (this.distanceChanged >= LOG_DISTANCE_CHANGE_TO_DB) {
                    this.updateDatabase(this.currentLocation);
                }
            }
            this.lastKnownLocation = location;
            double distance = location.distanceTo(getFriendLocation(this.friendEmail));
            alertIfClose(distance);
        }

    }

    public void alertIfClose(double distance) {
        if(distance < ALERT_DISTANCE_TO_FRIEND) {
            Log.d(SERVICE, "Friend is close.");
            this.sendPushNotification(this.friendEmail, distance);
        }
        else {
            Log.d(SERVICE, "Friend is not closer than " + ALERT_DISTANCE_TO_FRIEND + " meters.");
        }
    }

    /**
     * sendPushNotification()
     * @param email
     */
    private void sendPushNotification(String email, double distance) {

        ParseQuery parseQuery = ParseInstallation.getQuery();
        parseQuery.whereEqualTo("userEmail", email);

        // Send push notification to query
        ParsePush push = new ParsePush();
        push.setQuery(parseQuery);

        // Setting message with user information
        String userName = ParseUser.getCurrentUser().getString("name");

        String message = userName + " is " + distance + " meters away.";

        JSONObject data = null;
        try {
            data = new JSONObject("{\"alert\": \"" + message + "\",\"badge\": \"1\",\"invitor\": \"" + this.userEmail +"\", \"connectionStatus\": \"" + CONNECTED_CLOSE_BY + "\"}");
        } catch (JSONException e) {
            Log.d(SERVICE, "JSON ERROR: "+e);

        }

        push.setData(data);

        push.sendInBackground();

        Log.d(SERVICE, "Push Notification sent.");
    }

    /**
     * getFriendLocation() - gets location of a friend
     * @return
     */
    private Location getFriendLocation(String email) {
        Location location = new Location(LocationManager.GPS_PROVIDER);

        ParseUser user = this.getUserByEmail(email);

        location.setLatitude((Double)user.get("latitude"));
        location.setLongitude((Double)user.get("longitude"));
        return location;
    }

    /**
     * getUserByEmail()
     * @param emailAddress
     * @return ParseUser
     */
    private ParseUser getUserByEmail(String emailAddress) {
        ParseUser user = null;
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", emailAddress);
        try {
            user = query.getFirst().fetchIfNeeded();
        } catch (ParseException e) {
            Log.d(SERVICE, e.getLocalizedMessage());
        }
        return user;
    }

    /**
     * updateDatabase() - update DB with new user location
     * @param location
     */
    private void updateDatabase(Location location) {
        if(isNetworkAvailable()) {
            ParseUser user = ParseUser.getCurrentUser();
            try {
                user.put("latitude", location.getLatitude());
                user.put("longitude", location.getLongitude());
                user.saveInBackground(new SaveCallback() {
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.d(SERVICE, "Error saving location! " + e.getLocalizedMessage());
                        } else {
                            Log.d(SERVICE, "Location saved successfully");
                        }
                    }
                });
            } catch (Exception e) {
                Log.d(SERVICE, "Error: " + e);
            }
        }
    }

    /**
     * getActivityContext() - gets activity context, good to use in inner classes
     * @return Context
     */
    public static Context getActivityContext() {
        return LocationService.context;
    }

    /**
     * isNetworkAvailable()
     * @return boolean
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivitymanager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivitymanager.getActiveNetworkInfo();
        return networkInfo !=null && networkInfo.isConnected();
    }

    /**
     * roundDistance() - rounds distance to ##.##
     * @param number
     * @return
     */
    public double roundDistance(double number) {
        double n = Math.round(number * 100);
        n = n/100;

        return n;
    }
}