package me.dainius.friendlocator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.parse.ParseUser;


/**
 * Dispatcher - main application entry
 * Checks Login status and redirects to the appropriate Activity
 */
public class Dispatcher extends Activity {

    private static String DISPATCHER = "Dispatcher";
    private static Context context;

    /**
     * Initializer
     */
    public Dispatcher() {}

    /**
     * onCreate()
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DISPATCHER, "Dispatcher will dispatch the Activity");
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (ParseUser.getCurrentUser() != null) {
            ParseUser user = ParseUser.getCurrentUser();
            user.put("isOnline", true);
            user.saveInBackground();
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, WelcomeActivity.class));
        }
    }

    public void onResume() {
        super.onResume();
        if(!isNetworkAvailable()) {
            Log.d(DISPATCHER, "Network is unavailable!");
        }
    }

    /**
     * getActivityContext() - gets activity context, good to use in inner classes
     * @return Context
     */
    public static Context getActivityContext() {
        return Dispatcher.context;
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
}
