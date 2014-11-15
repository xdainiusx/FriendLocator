package me.dainius.friendlocator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.parse.ParseUser;

/**
 * Settings Activity
 */
public class SettingsActivity extends Activity {

    private static String ACTIVITY = "SettingsActivity";

    /**
     * onCreate()
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_settings);
    }

    public void onLogoutClick(View view) {
        Log.d(ACTIVITY, "onLogoutClick() clicked");
        ParseUser.logOut();
        Intent intent = new Intent(SettingsActivity.this, Dispatcher.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void onChangePasswordClick(View view) {
        Log.d(ACTIVITY, "onChangePasswordClick() clicked");
    }

    public void onUpdateAccountClick(View view) {
        Log.d(ACTIVITY, "onUpdateAccountClick() clicked");
    }

    /**
     * onHelpClick()
     * @param view
     */
    public void onHelpClick(View view) {
        Log.d(ACTIVITY, "onHelpClick() clicked");
    }
}