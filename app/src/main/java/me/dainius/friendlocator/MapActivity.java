package me.dainius.friendlocator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
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
public class MapActivity extends Activity {

    private static String ACTIVITY = "MapActivity";
    private static String MARKER_KEY = "ABCDEFGHIJKLMN";
    private static double DEFAULT_DISTANCE = 10000.0;
    private static int PENDING = 1;
    private static int CONNECTED = 2;
    private static int DECLINED = 3;
    Context mapContext;
    GoogleMap googleMap;
    LocationManager locationManager;
    Location oldLocation;
    Location friendLocation = null;
    TextView distanceTextView;
    private String userEmail = null;
    private String friendEmail = null;
    private String friendName = null;
    private String invitorEmail = null;
    private Boolean pushReceived = null;
    private boolean cancelledConnection = false;



    LocationService locationService;
    boolean isBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            LocationService.LocalLocationBinder binder = (LocationService.LocalLocationBinder) service;
            locationService = binder.getService();
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }

    };

    /**
     * onCreate()
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_map);
        this.userEmail = ParseUser.getCurrentUser().getEmail();
        createMapView();
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
        this.runLocationService();
    }

    /**
     * runLocationService()
     */
    private void runLocationService() {
        Log.d(ACTIVITY, "Running location service in the background");
        Intent intent = new Intent(this, LocationService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
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
            } else if (this.friendEmail != null) {
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
            this.friendLocation = this.getFriendLocation(this.friendEmail);
        }

        if(this.activeOrPendingConnection()) {
            Log.d(ACTIVITY, "Active or Pending connection!");
        }

        //addMarker();

        // Retrieve LocationManager from system services.
        this.locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Log.d(ACTIVITY, "LAST KNOWN LOCATION: " + lastKnownLocation.toString());

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        Log.d(ACTIVITY, "STATUS: " + status);

        if (status == ConnectionResult.SUCCESS) {
            Log.d(ACTIVITY, "Google Play Services are available");
            this.googleMap.setMyLocationEnabled(true);

            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the provider - using GPS only
            String provider = this.locationManager.getBestProvider(criteria, true);

            Log.d(ACTIVITY, "Provider used: " + provider);

            // Getting Current Location
            Location location = this.locationManager.getLastKnownLocation(provider);

            Log.d(ACTIVITY, "CURRENT LOCATION: " + location);

            if(this.friendLocation!=null) {
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

                /**
                 * Location listener - listens for location updates
                 */
                LocationListener locationListener = new LocationListener() {

                    public void onStatusChanged(String provider, int status, Bundle extras) { }

                    public void onProviderEnabled(String provider) {}

                    public void onProviderDisabled(String provider) {
                        Toast.makeText(MapActivity.this,
                                "GPS disabled: " + provider, Toast.LENGTH_SHORT).show();
                    }

                    public void onLocationChanged(Location location) {

                        Log.d(ACTIVITY, "Location changed");
                        double distance = location.distanceTo(getFriendLocation(friendEmail));

                        int zoomLevel = getZoom(distance);

                        CameraPosition cameraPosition =
                                new CameraPosition.Builder().target(
                                        new LatLng(location.getLatitude(), location.getLongitude())
                                ).zoom(zoomLevel).build();

                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        googleMap.getUiSettings().setCompassEnabled(true);

                        double roundedDistance = roundDistance(distance);
                        String distanceLabel = null;
                        if(roundedDistance>1000){
                            distanceLabel = roundDistance(roundedDistance/1000) + " km";
                        }
                        else {
                            distanceLabel = roundedDistance + " meters";
                        }

                        Log.d(ACTIVITY, "DISTANCE TO FRIEND: " + distanceLabel);

                        distanceTextView.setText(distanceLabel);

                    }
                };
                long minTime = 0 * 1000; // Minimum time interval for update in seconds map
                long minDistance = 1;    // Min distance in meters to update
                this.locationManager.requestLocationUpdates(provider, minTime, minDistance, locationListener);
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
        query.whereEqualTo("friendEmail", ParseUser.getCurrentUser().getEmail());

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
        query.whereEqualTo("friendEmail", ParseUser.getCurrentUser().getEmail());

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
        query.whereEqualTo("invitorEmail", ParseUser.getCurrentUser().getEmail());
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
        String userName = ParseUser.getCurrentUser().getString("name");

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
        String userName = ParseUser.getCurrentUser().getString("name");

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
        this.userEmail = ParseUser.getCurrentUser().getEmail();
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

//            MarkerOptions oldMarker = new MarkerOptions();
//            oldMarker.getSnippet();
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
}