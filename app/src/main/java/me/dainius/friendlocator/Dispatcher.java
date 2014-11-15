package me.dainius.friendlocator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.parse.ParseUser;


/**
 * Dispatcher - main application entry
 * Checks Login status and redirects to the appropriate Activity
 */
public class Dispatcher extends Activity {

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
        if (ParseUser.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, WelcomeActivity.class));
        }
    }

}
