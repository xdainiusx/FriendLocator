package me.dainius.friendlocator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Map Activity
 */
public class MapActivity extends Activity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        LocationListener,
        GooglePlayServicesClient.OnConnectionFailedListener{

    private final long MIN_TIME = 0 * 1000;
    private final long MIN_DISTANCE = 1;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 1;
    private static final int MILLISECONDS_PER_SECOND = 100;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private static final float SMALLEST_DISPLACEMENT_IN_METERS = 1f;

    private static String ACTIVITY = "MapActivity";
    private static String MARKER_KEY = "ABCDEFGHIJKLMN";
    private static double DEFAULT_DISTANCE = 10000.0;
    private static int PENDING = 1;
    private static int CONNECTED = 2;
    private static int DECLINED = 3;
    Context mapContext;
    GoogleMap googleMap;
    public LocationManager locationManager;
    Location oldLocation;
    Location friendLocation = null;
    TextView distanceTextView;
    private String userEmail = null;
    private String friendEmail = null;
    private String friendName = null;
    private String invitorEmail = null;
    private Boolean pushReceived = null;
    private boolean cancelledConnection = false;
    private ParseUser accountUser;
    public Location location;

    public Location lastKnownLocation;
    LocationRequest locationRequest;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;
    LocationClient locationClient;
    boolean updatesRequested;
    private Location currentLocation;

    /**
     * onCreate()
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(ACTIVITY, "onCreate()");
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_map);

        this.locationRequest = LocationRequest.create();
        this.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        this.locationRequest.setInterval(UPDATE_INTERVAL);
        this.locationRequest.setFastestInterval(FASTEST_INTERVAL);
        this.locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT_IN_METERS);
        this.sharedPreferences = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        this.sharedPreferencesEditor = this.sharedPreferences.edit();
        this.locationClient = new LocationClient(getApplicationContext(), this, this);
        this.updatesRequested = false;
        this.locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        this.lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        this.accountUser = ParseUser.getCurrentUser();
        this.userEmail = this.accountUser.getEmail();
        this.createMapView();
        Bundle extras = this.getIntent().getExtras();
        if(extras != null) {
            this.friendEmail = extras.getString("FriendEmail");
            this.friendName = extras.getString("FriendName");
            this.pushReceived = extras.getBoolean("pushReceived");
            Log.d(ACTIVITY, "Friend Email: " + this.friendEmail);
            Log.d(ACTIVITY, "Friend Name: " + this.friendName);

            String invitor = extras.getString("invitor");
            Log.d(ACTIVITY, "Invitor Email: " + invitor);
            Log.d(ACTIVITY, "Push Received: " + this.pushReceived);

            if(invitor!=null && this.pushReceived !=null) {
                this.invitorEmail = invitor;
                if(this.pushReceived) {
                    Log.d(ACTIVITY, "Push Received!");
                }
                else {
                    this.showAlert(this.invitorEmail);
                }
            }
        }
    }

    /**
     * onResume()
     */
    protected void onResume() {
        super.onResume();
        Log.d(ACTIVITY, "onResume()");
        this.mapConnectionView();
    }

    /**
     * onResume()
     */
    protected void onPause() {
        super.onPause();
        Log.d(ACTIVITY, "onPause()");
        this.sharedPreferencesEditor.putBoolean("KEY_MAP_UPDATES_ON", this.updatesRequested);
        this.sharedPreferencesEditor.commit();
    }

    /**
     * onStart()
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(ACTIVITY, "onStart");
        this.locationClient.connect();
    }

    /**
     * onStatusChanged()
     * @param provider
     * @param status
     * @param extras
     */
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    /**
     * onProviderEnabled()
     * @param provider
     */
    public void onProviderEnabled(String provider) { }

    /**
     * onProviderDisabled
     * @param provider
     */
    public void onProviderDisabled(String provider) {
        Toast.makeText(MapActivity.this, "GPS disabled: " + provider, Toast.LENGTH_SHORT).show();
    }

    /**
     * onLocationChanged()
     * @param location
     */
    public void onLocationChanged(Location location) {

        Log.d(ACTIVITY, "onLocationChanged()");
        if(isNetworkAvailable()) {
            this.currentLocation = location;
            double distance;
            try {
                distance = this.currentLocation.distanceTo(getUserLocation(friendEmail));
            } catch (Exception e) {
                Log.d(ACTIVITY, "Error getting location: " + e);
                distance = -1.0;
            }
            Log.d(ACTIVITY, "DISTANCE TO FRIEND: " + roundDistance(distance));
            int zoomLevel = getZoom(distance);

            CameraPosition cameraPosition =
                    new CameraPosition.Builder().target(
                            new LatLng(this.currentLocation.getLatitude(), this.currentLocation.getLongitude())
                    ).zoom(zoomLevel).build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            googleMap.getUiSettings().setCompassEnabled(true);
            String locationString = "Updated Location: " + this.currentLocation.getLatitude() + ", " + this.currentLocation.getLongitude();
            Log.d(ACTIVITY, locationString);

            setDistance(distance);
        }

    }

    /**
     * mapConnectionView()
     */
    private void mapConnectionView() {
        if(cancelledConnection) {
            Log.d(ACTIVITY, "Connection was cancelled!");
        }
        else {
            if (this.invitorEmail != null && this.pushReceived == false) {
                Log.d(ACTIVITY, "Connected to friend!!!");
            } else if (this.friendEmail != null && this.alreadyConnected(this.userEmail, this.friendEmail)) {
                Log.d(ACTIVITY, "Already connected to friend! Do not show alert.");
            } else if (this.friendEmail != null && this.friendName != null) {
                this.connectFriends(this.friendEmail);
                /**
                 * Alert if invite to connect pending
                 */
                AlertDialog alert = new AlertDialog.Builder(MapActivity.this).create();
                alert.setTitle("Connection with " + this.friendName + " is pending!");
                alert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(ACTIVITY, "Cancel pressed");
                        deleteConnection(friendEmail);
                        cancelledConnection = true;
                        Intent mainIntent = new Intent(MapActivity.this, MainActivity.class);
                        mainIntent.putExtra("defaultTab", 1);
                        startActivity(mainIntent);
                    }
                });
                alert.setCanceledOnTouchOutside(false);
                alert.show();
            }
            else if (this.friendEmail != null) {
                this.connectFriends(this.friendEmail);
                /**
                 * Alert if invite to connect pending
                 */
                AlertDialog alert = new AlertDialog.Builder(MapActivity.this).create();
                alert.setTitle("Connection with " + this.friendEmail + " is pending!");
                alert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(ACTIVITY, "Cancel pressed");
                        deleteConnection(friendEmail);
                        cancelledConnection = true;
                        Intent mainIntent = new Intent(MapActivity.this, MainActivity.class);
                        mainIntent.putExtra("defaultTab", 1);
                        startActivity(mainIntent);
                    }
                });
                alert.show();

            }
            else if (this.invitorEmail != null && this.pushReceived == true) {
                Log.d(ACTIVITY, "Approved connection. Mutually connecting!!!");
                this.friendEmail = this.invitorEmail;
                this.connectFriends(this.invitorEmail);
            }
            else {
                Log.d(ACTIVITY, "Friend Email is not found!");

            }
            cancelledConnection = false;
        }


        this.distanceTextView = (TextView) findViewById(R.id.distanceTextView);

        if(this.friendEmail!=null) {
            this.friendLocation = this.getUserLocation(this.friendEmail);
            Log.d(ACTIVITY, "FRIEND'S LOCATION: " + this.friendLocation);
        }

        if(this.activeOrPendingConnection()) {
            Log.d(ACTIVITY, "Active or Pending connection!");
        }

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        Log.d(ACTIVITY, "STATUS: " + status);

        if (status == ConnectionResult.SUCCESS) {
            Log.d(ACTIVITY, "Google Play Services are available");
            this.googleMap.setMyLocationEnabled(true);

            if(location==null) {
                location = this.getUserLocation(this.userEmail);
                Log.d(ACTIVITY, "Location not found!");
            }

            if(this.friendLocation!=null) {
                if(location==null) {
                    String message = "GPS is not working properly!\nPlease check your network connection!";
                    toastIt(message);

                }
                this.addMarker(this.friendLocation);

                double distance = location.distanceTo(this.friendLocation);
                Log.d(ACTIVITY, "DISTANCE TO FRIEND: " + this.roundDistance(distance));
                int zoomLevel = this.getZoom(distance);
                CameraPosition cameraPosition =
                        new CameraPosition.Builder().target(
                                new LatLng(location.getLatitude(), location.getLongitude())
                        ).zoom(zoomLevel).build();

                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                googleMap.getUiSettings().setCompassEnabled(true);
                this.setDistance(distance);
            }
            else {
                Log.d(ACTIVITY, "Friend location unknown!");
                int zoomLevel = this.getZoom(DEFAULT_DISTANCE);
                CameraPosition cameraPosition =
                        new CameraPosition.Builder().target(
                                new LatLng(location.getLatitude(), location.getLongitude())
                        ).zoom(zoomLevel).build();

                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                googleMap.getUiSettings().setCompassEnabled(true);
            }
        }
        else if (status == ConnectionResult.SERVICE_MISSING ||
                status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ||
                status == ConnectionResult.SERVICE_DISABLED) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 1);
            dialog.show();
        }
    }

    /**
     * setDistance()
     * @param distance
     */
    private void setDistance(double distance) {
        if(distance >= 0) {
            double roundedDistance = roundDistance(distance);
            String distanceLabel = null;
            if (roundedDistance > 1000) {
                distanceLabel = roundDistance(roundedDistance / 1000) + " km";
            } else {
                distanceLabel = roundedDistance + " meters";
            }

            Log.d(ACTIVITY, "DISTANCE TO FRIEND: " + distanceLabel);

            distanceTextView.setText(distanceLabel);
        }
        else {
            distanceTextView.setText("No connection to friend!");
        }
    }

    /**
     * executeConnection()
     * @param email
     */
    private void executeConnection(String email) {
        Log.d(ACTIVITY, "Email received " + email);
        this.friendEmail = email;

        ActiveConnection activeConnection = null;
        ArrayList<String> emailList = new ArrayList<String>();
        ArrayList<String> foundUsers = new ArrayList<String>();
        ParseQuery<ActiveConnection> query = ParseQuery.getQuery("ActiveConnection");
        query.whereEqualTo("invitorEmail", email);
        query.whereEqualTo("friendEmail", this.accountUser.getEmail());

        try {
            Log.d(ACTIVITY, "Will try to find users to connect.");

            activeConnection = query.getFirst();
            if(activeConnection!=null) {
                activeConnection.setStatus(CONNECTED);
                activeConnection.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.d(ACTIVITY, "Active connection saved to ActiveConnection table.");
                        } else {
                            Log.d(ACTIVITY, "Error saving Active connection: " + e);
                        }
                    }
                });
                Log.d(ACTIVITY, "Status saved in the background.");
            }
        } catch (ParseException e) {
            Log.d(ACTIVITY, e.getLocalizedMessage());
        }
    }

    /**
     * alreadyConnected()
     * @param userEmail
     * @param friendEmail
     * @return boolean
     */
    private boolean alreadyConnected(String userEmail, String friendEmail) {

        if(this.areFriendsInConnection(userEmail, friendEmail, CONNECTED)) {
            return true;
        }
        else if(this.areFriendsInConnection(friendEmail, userEmail, CONNECTED)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * deleteConnection()
     * @param email
     */
    private void deleteConnection(String email) {
        Log.d(ACTIVITY, "Email received " + email);
        this.friendEmail = email;

        ActiveConnection activeConnection = null;
        ArrayList<String> emailList = new ArrayList<String>();
        ArrayList<String> foundUsers = new ArrayList<String>();
        ParseQuery<ActiveConnection> query = ParseQuery.getQuery("ActiveConnection");
        query.whereEqualTo("invitorEmail", email);
        query.whereEqualTo("friendEmail", this.accountUser.getEmail());

        try {
            Log.d(ACTIVITY, "Will try to delete object.");

            activeConnection = query.getFirst();
            if(activeConnection!=null) {
                activeConnection.deleteInBackground();
                Log.d(ACTIVITY, "Object deleted.");
            }
        } catch (ParseException e) {
            Log.d(ACTIVITY, e.getLocalizedMessage());
        }

        // Delete the other way
        query.whereEqualTo("invitorEmail", this.accountUser.getEmail());
        query.whereEqualTo("friendEmail", email);

        try {
            Log.d(ACTIVITY, "Will try to delete object.");

            activeConnection = query.getFirst();
            if(activeConnection!=null) {
                activeConnection.deleteInBackground();
                Log.d(ACTIVITY, "Object deleted.");
            }
        } catch (ParseException e) {
            Log.d(ACTIVITY, e.getLocalizedMessage());
        }
    }

    /**
     * connectFriends()
     * @param email
     */
    private void connectFriends(String email) {
        Log.d(ACTIVITY, "Connecting friends...");

        if(email!=null && this.pushReceived == false) {
            this.sendPushNotification(email);
        }

        boolean connected = this.areFriendsInConnection(this.userEmail, this.friendEmail, CONNECTED);
        boolean pending = this.areFriendsInConnection(this.userEmail, this.friendEmail, PENDING);

        if(this.friendLocation!=null) {
            try {
                Log.d(ACTIVITY, "Friends Location LAT: " + this.friendLocation.getLatitude());
                Log.d(ACTIVITY, "Friends Location LON: " + this.friendLocation.getLongitude());
            } catch (Exception e) {
                Log.d(ACTIVITY, "ERROR: " + e);
            }
        }

        if(connected) {
            Log.d(ACTIVITY, "Friends in active connection!");
        }
        else {
            // reverse users
            connected = this.areFriendsInConnection(this.friendEmail, this.userEmail, CONNECTED);
            if(connected) {
                Log.d(ACTIVITY, "Reverse friends in active connection!");
            }
        }
        if(pending) {
            Log.d(ACTIVITY, "friends in pending connection!");
        }
        else {
            // reverse users
            pending = this.areFriendsInConnection(this.friendEmail, this.userEmail, PENDING);
            if(pending) {
                Log.d(ACTIVITY, "Reverse friends in pending connection!");
            }
        }

        if(!connected && !pending) {
            ActiveConnection activeConnection = new ActiveConnection();
            activeConnection.setInvitorEmail(this.userEmail);
            activeConnection.setFriendEmail(this.friendEmail);
            activeConnection.setStatus(PENDING);

            ParseACL connectionAcl = new ParseACL();
            connectionAcl.setPublicReadAccess(true);
            connectionAcl.setPublicWriteAccess(true);
            activeConnection.setACL(connectionAcl);

            activeConnection.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d(ACTIVITY, "ActiveConnection saved to ActiveConnection table.");
                    } else {
                        Log.d(ACTIVITY, "Error saving activeConnection to ActiveConnection table.");
                    }
                }
            });
        }
    }

    /**
     * sendPushNotification()
     * @param email
     */
    private void sendPushNotification(String email) {

        ParseQuery parseQuery = ParseInstallation.getQuery();
        parseQuery.whereEqualTo("userEmail", email);

        // Send push notification to query
        ParsePush push = new ParsePush();
        push.setQuery(parseQuery);

        // Setting message with user information
        String userName = this.accountUser.getString("name");

        String message = userName + " would like to connect with you.";

        JSONObject data = null;
        try {
            data = new JSONObject("{\"alert\": \"" + message + "\",\"badge\": \"1\",\"invitor\": \"" + this.userEmail +"\", \"connectionStatus\": \"" + PENDING + "\"}");
        } catch (JSONException e) {
            Log.d(ACTIVITY, "JSON ERROR: "+e);

        }

        push.setData(data);

        push.sendInBackground();

        Log.d(ACTIVITY, "Push Notification sent.");
    }

    /**
     * sendPushNotificationReply()
     * @param email
     * @param status
     */
    private void sendPushNotificationReply(String email, int status) {

        ParseQuery parseQuery = ParseInstallation.getQuery();
        parseQuery.whereEqualTo("userEmail", email);

        // Send push notification to query
        ParsePush push = new ParsePush();
        push.setQuery(parseQuery);

        // Setting message with user information
        String userName = this.accountUser.getString("name");

        String message = userName + " accepted invitation.";

        JSONObject data = null;
        try {
            data = new JSONObject("{\"alert\": \"" + message + "\",\"invitor\": \"" + this.userEmail +"\", \"connectionStatus\": \"" + status + "\"}");
        } catch (JSONException e) {
            Log.d(ACTIVITY, "JSON ERROR: "+e);

        }

        push.setData(data);

        push.sendInBackground();
        Log.d(ACTIVITY, "Push Notification reply sent.");
    }

    /**
     * activeOrPendingConnection()
     * @return boolean
     */
    private boolean activeOrPendingConnection() {
        this.userEmail = this.accountUser.getEmail();
        Log.d(ACTIVITY, "This user email is " + this.userEmail);
        boolean connected = this.areFriendsInConnection(this.userEmail, null, CONNECTED);
        boolean pending = this.areFriendsInConnection(this.userEmail, null, PENDING);
        if(connected) {
            Log.d(ACTIVITY, "Friends in active connection!");
        }
        else {
            // reverse
            connected = this.areFriendsInConnection(null, userEmail, CONNECTED);
            if(connected) {
                Log.d(ACTIVITY, "Reverse friends in active connection!");
            }
        }

        if(pending) {
            Log.d(ACTIVITY, "Friends in pending connection!");
        }
        else {
            // reverse
            pending = this.areFriendsInConnection(null, userEmail, PENDING);
            if(pending) {
                Log.d(ACTIVITY, "Reverse friends in pending connection!");
            }
        }

        if(connected || pending) {
            return true;
        }
        return false;
    }

    /**
     * areFriendsInConnection()
     * @param userEmail
     * @param friendEmail
     * @param status
     * @return boolean
     */
    private boolean areFriendsInConnection(String userEmail, String friendEmail, int status) {

        List<ActiveConnection> activeConnectionList = null;
        ArrayList<String> emailList = new ArrayList<String>();
        ArrayList<String> foundUsers = new ArrayList<String>();
        ParseQuery<ActiveConnection> query = ParseQuery.getQuery("ActiveConnection");
        if(userEmail!=null) {
            query.whereEqualTo("invitorEmail", userEmail);
        }
        if(friendEmail!=null) {
            query.whereEqualTo("friendEmail", friendEmail);
        }
        query.whereEqualTo("status", status);
        boolean connectionPresent = false;

        try {
            Log.d(ACTIVITY, "Will try to find users.");

            activeConnectionList = query.find();
            Log.d(ACTIVITY, "SIZE: "+ activeConnectionList.size());
            for (ActiveConnection ac : activeConnectionList) {
                try {
                    String emailParse = (String)ac.get("invitorEmail");
                    String friendEmailParse = (String)ac.get("friendEmail");
                    Log.d(ACTIVITY, "Invitor User Email:   " + emailParse);
                    Log.d(ACTIVITY, "Friend Email:         " + friendEmailParse);
                    emailList.add(emailParse);
                } catch (Exception e) {
                    Log.d(ACTIVITY, "User parse error! " + e.getLocalizedMessage());
                }
            }
        } catch (ParseException e) {
            Log.d(ACTIVITY, e.getLocalizedMessage());
        }

        if(emailList!=null && emailList.size() > 0) {
            Log.d(ACTIVITY, "Email list size: " + emailList.size());
            connectionPresent = true;
        }
        return connectionPresent;
    }

    /**
     * getUserLocation() - gets location of a friend
     * @return
     */
    private Location getUserLocation(String email) {
        Log.d(ACTIVITY, "getUserLocation() for " + email);
        Location location = new Location(LocationManager.GPS_PROVIDER);

        ParseUser user = this.getUserByEmail(email);

        location.setLatitude((Double)user.get("latitude"));
        location.setLongitude((Double)user.get("longitude"));

        Log.d(ACTIVITY, "USER's EMAIL   : " + email);
        Log.d(ACTIVITY, "USER's LOCATION: " + location);

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
            Log.d(ACTIVITY, e.getLocalizedMessage());
        }
        return user;
    }

    /**
     * getZoom() - gets zoom level depending on the distance between 2 objects
     * @param distance
     * @return
     */
    private int getZoom(double distance) {
        int zoom = 0;
        if(distance < 10){
            zoom = 22;
        }
        else if(distance < 15 && distance >= 10) {
            zoom = 21;
        }
        else if(distance < 25 && distance >= 15) {
            zoom = 20;
        }
        else if(distance < 50 && distance >= 25) {
            zoom = 19;
        }
        else if(distance < 125 && distance >= 50) {
            zoom = 18;
        }
        else if(distance < 200 && distance >= 125) {
            zoom = 17;
        }
        else if(distance < 400 && distance >= 200) {
            zoom = 16;
        }
        else if(distance < 750 && distance >=400) {
            zoom = 15;
        }
        else if(distance < 1500 && distance >= 750) {
            zoom = 14;
        }
        else if(distance < 2000 && distance >= 1500) {
            zoom = 13;
        }
        else if(distance < 5000 && distance >= 2000) {
            zoom = 12;
        }
        else if(distance < 10000 && distance >= 5000) {
            zoom = 11;
        }
        else if(distance < 20000 && distance >= 10000) {
            zoom = 10;
        }
        else if(distance < 50000 && distance >= 20000) {
            zoom = 9;
        }
        else if(distance < 100000 && distance >= 50000) {
            zoom = 8;
        }
        else if(distance < 150000 && distance >= 100000) {
            zoom = 7;
        }
        else if(distance < 250000 && distance >= 150000) {
            zoom = 6;
        }
        else if(distance < 500000 && distance >= 250000) {
            zoom = 5;
        }
        else if(distance < 1000000 && distance >= 500000) {
            zoom = 4;
        }
        else if(distance < 3000000 && distance >= 1000000) {
            zoom = 3;
        }
        else if(distance < 10000000 && distance >= 3000000) {
            zoom = 2;
        }
        else {
            zoom = 1;
        }
        return zoom;
    }

    /**
     * Initialize the map
     */
    private void createMapView(){

        Log.d(ACTIVITY, "Will create Google Map");

        try {
            if(null == this.googleMap){
                Log.d(ACTIVITY, "MAP IS NULL");
                this.googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();
                this.googleMap.getUiSettings().setZoomControlsEnabled(false);

                /**
                 * Show error if failed to initialize the map
                 */
                if(null == this.googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map", Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.d(ACTIVITY, "Google Map was created!");
                }
            }
        } catch (NullPointerException exception){
            Log.e(ACTIVITY, exception.toString());
        }
    }

    /**
     * Adds a marker to the map
     */
    private void addMarker(Location location){
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        /**
         * Map should be initialized before this step
         */
        if(null != this.googleMap){

            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
            Canvas canvas = new Canvas(bmp);

            Drawable d = getResources().getDrawable(R.drawable.gps_icon_red);
            d.setBounds(0, 0, bmp.getWidth(), bmp.getHeight());
            d.draw(canvas);

            this.googleMap.clear();

            MarkerOptions marker = new MarkerOptions()
                    .position(new LatLng(lat, lon))
                    .title(this.friendName)
                    .snippet(MARKER_KEY)
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                    .draggable(true);

            this.googleMap.addMarker(marker);
        }
    }

    /**
     * onConnectionCancelClick()
     * @param view
     */
    public void onConnectionCancelClick(View view) {
        Log.d(ACTIVITY, "onConnectionCancelClick() clicked");

        if(this.friendEmail!=null) {

            String cancelFriendName = null;
            try {
                cancelFriendName = (String) this.getUserByEmail(this.friendEmail).get("name");
            } catch (Exception e) {
                Log.d(ACTIVITY, "Could not get friend user");
            }

            /**
             * Alert on friend click
             */
            AlertDialog alert = new AlertDialog.Builder(MapActivity.this).create();
            if(cancelFriendName!=null) {
                alert.setTitle("Cancel connection with " + cancelFriendName + "?");
            }
            else {
                alert.setTitle("Cancel connection?");
            }

            alert.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(ACTIVITY, "Yes pressed");
                    deleteConnection(friendEmail);
                    cancelledConnection = true;
                    Intent mainIntent = new Intent(MapActivity.this, MainActivity.class);
                    mainIntent.putExtra("defaultTab", 1);
                    startActivity(mainIntent);
                }
            });
            alert.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(ACTIVITY, "No pressed");

                }
            });
            alert.show();

        }
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

    /**
     * getCoordinateArray()
     * @return double [] - [0]=latitude, [1]=longitude
     */
    public double[] getUserCoordinateArray() {
        double[] coordinates = new double[2];
        try {
            coordinates[1] = this.accountUser.getDouble("latitude");
            coordinates[0] = this.accountUser.getDouble("longitude");
        } catch (Exception e) {
            coordinates[1] = 0.0;
            coordinates[0] = 0.0;
        }
        return coordinates;
    }

    /**
     * capitalizeString()
     * @param str
     * @return String
     */
    public String capitalizeString(String str) {
        String newString = str.substring(0, 1).toUpperCase() + str.substring(1);
        return newString;
    }

    /**
     * toastIt() - toast used for form verification
     * @param message
     */
    public void toastIt(String message) {
        Log.d(ACTIVITY, message);
        Toast t = Toast.makeText(getApplicationContext(), this.capitalizeString(message), Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
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
     * showAlert()
     * @param invitorEmail
     */
    private void showAlert(final String invitorEmail) {

        ParseUser invitorUser = this.getUserByEmail(invitorEmail);

        /**
         * Alert on friend click
         */
        AlertDialog alert = new AlertDialog.Builder(MapActivity.this).create();
        alert.setTitle("Connect with " + invitorUser.get("name") + "?");

        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(ACTIVITY, "Yes pressed");
                executeConnection(invitorEmail);
                sendPushNotificationReply(invitorEmail, CONNECTED);
                mapConnectionView();
            }
        });
        alert.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(ACTIVITY, "No pressed");
                deleteConnection(invitorEmail);
                sendPushNotificationReply(invitorEmail, DECLINED);
            }
        });
        alert.show();
    }


    /**
     * onConnected()
     * @param dataBundle
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        Log.d(ACTIVITY, "onConnected");
        if(this.updatesRequested) {
            this.locationClient.requestLocationUpdates(this.locationRequest, this);
        }
        this.currentLocation = getLocation();
        this.startPeriodicUpdates();
    }

    /**
     * onDisconnected()
     */
    @Override
    public void onDisconnected() {

        Log.d(ACTIVITY, "onDisconnected");
        this.stopPeriodicUpdates();
    }

    /**
     * onConnectionFailed
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(ACTIVITY, "onConnectionFailed");
    }

    /**
     * startPeriodicUpdates() - start location updates
     */
    private void startPeriodicUpdates() {
        this.locationClient.requestLocationUpdates(locationRequest, this);
    }

    /**
     * stopPeriodicUpdates() - stop location updates
     */
    private void stopPeriodicUpdates() {
        this.locationClient.removeLocationUpdates(this);
    }

    /**
     * getLocation()
     * @return Location
     */
    private Location getLocation() {
        if (this.servicesConnected()) {
            return this.locationClient.getLastLocation();
        } else {
            return null;
        }
    }

    /**
     * servicesConnected()
     * @return boolean
     */
    private boolean servicesConnected() {
        Log.d(ACTIVITY, "servicesConnected");
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (status == ConnectionResult.SUCCESS) {
            return true;
        }
        else if (status == ConnectionResult.SERVICE_MISSING ||
                status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ||
                status == ConnectionResult.SERVICE_DISABLED) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 1);
            dialog.show();
        }
        return false;
    }

}