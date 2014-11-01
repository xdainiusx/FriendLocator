package me.dainius.friendlocator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

/**
 * Login Activity - Login Form
 */
public class LoginActivity extends Activity {

    private EditText emailAddress;
    private EditText password;

    /**
     * onCreate()
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getClass().getName(), "onCreate() called");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
    }

    /**
     * onSignInClick() - Sign In button clicked
     * @param view
     */
    public void onSignInClick(View view) {
        Log.d(getClass().getName(), "onSignInClick() clicked");
        this.emailAddress = (EditText) findViewById(R.id.emailAddress);
        this.password = (EditText) findViewById(R.id.password);

        Log.d(getClass().getName(), "Email: " + this.emailAddress + "Password: " + this.password);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * onPasswordRecoverClick() - Password Recover link clicked
     * @param view
     */
    public void onPasswordRecoverClick(View view) {
        Log.d(getClass().getName(), "onPasswordRecoverClick() clicked");
        Intent intent = new Intent(this, PasswordRecoveryActivity.class);
        startActivity(intent);

    }

}
