package me.dainius.friendlocator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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

/**
 * Map Activity
 */
public class MapActivity extends Activity {

    private static String ACTIVITY = "MapActivity";
    Context mapContext;
    GoogleMap googleMap;
    LocationManager locationManager;
    Location oldLocation;
    Location friendLocation = null;
    TextView distanceTextView;
    private String friendID = null;

    /**
     * onCreate()
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_map);
        createMapView();
        Bundle extras = this.getIntent().getExtras();
        if(extras != null) {
            this.friendID = extras.getString("FriendID");
            Log.d(ACTIVITY, "Friend ID: " + this.friendID);
        }

        if(this.friendID!=null) {
            /**
             * Alert if invite to connect pending
             */
            AlertDialog alert = new AlertDialog.Builder(MapActivity.this).create();
            //alert.setTitle("Connect with " + friend.getFirstName() + " " + friend.getLastName());
            alert.setTitle("Connection with " + this.friendID + " Gui Boratto is pending!");
            alert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(ACTIVITY, "Cancel pressed");
                }
            });
            alert.show();
        }


        this.distanceTextView = (TextView) findViewById(R.id.distanceTextView);

        this.friendLocation = this.getFriendLocation();

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
                    double distance = location.distanceTo(getFriendLocation());

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
            long minTime = 2 * 1000; // Minimum time interval for update in seconds map
            long minDistance = 1;    // Min distance in meters to update
            this.locationManager.requestLocationUpdates(provider, minTime, minDistance, locationListener);

        }
        else if (status == ConnectionResult.SERVICE_MISSING ||
                 status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ||
                 status == ConnectionResult.SERVICE_DISABLED) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 1);
            dialog.show();
        }
    }

    /**
     * getFriendLocation() - gets location of a friend
     * @return
     */
    private Location getFriendLocation() {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(37.906108);
        location.setLongitude(-122.510264);
        return location;
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

            MarkerOptions marker = new MarkerOptions()
                    .position(new LatLng(lat, lon))
                    .title("Joe Doe")
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.gps_icon))
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                    .draggable(true);

            this.googleMap.addMarker(marker);
        }
    }

    public void onConnectionCancelClick(View view) {
        Log.d(ACTIVITY, "onConnectionCancelClick() clicked");
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