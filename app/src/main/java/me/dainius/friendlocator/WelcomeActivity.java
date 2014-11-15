package me.dainius.friendlocator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

/**
 * WelcomeActivity - first activity
 */
public class WelcomeActivity extends Activity {

    private static Context context;

    /**
     * onCreate()
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(getClass().getName(), "onCreate() called");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome);
    }

    /**
     * onSignInClick() - Sign in button click
     * @param view
     */
    public void onSignInClick(View view) {
        Log.d(getClass().getName(), "onSignInClick() clicked");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * onRegisterClick() - Register link click
     * @param view
     */
    public void onRegisterClick(View view) {
        Log.d(getClass().getName(), "onRegisterClick() clicked");
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * getActivityContext() - gets activity context, good to use in inner classes
     * @return
     */
    public static Context getActivityContext() {
        return WelcomeActivity.context;
    }

}
