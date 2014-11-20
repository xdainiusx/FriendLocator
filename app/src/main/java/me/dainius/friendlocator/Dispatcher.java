package me.dainius.friendlocator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.parse.ParseUser;


/**
 * Dispatcher - main application entry
 * Checks Login status and redirects to the appropriate Activity
 */
public class Dispatcher extends Activity {

    private static String DISPATCHER = "Dispatcher";

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
}
