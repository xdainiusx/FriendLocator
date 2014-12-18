package me.dainius.friendlocator;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class LocationService extends Service implements LocationListener {

    private final IBinder locationBinder = new LocalLocationBinder();

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
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

    }
}
