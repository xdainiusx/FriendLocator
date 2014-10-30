package me.dainius.friendlocator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(getClass().getName(), "onCreate() called");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome);
    }

    public void onSignInClick(View view) {
        Log.d(getClass().getName(), "onSignInClick() clicked");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void onRegisterClick(View view) {
        Log.d(getClass().getName(), "onRegisterClick() clicked");
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
