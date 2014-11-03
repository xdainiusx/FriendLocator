package me.dainius.friendlocator;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

/**
 * Register Activity - User registration screen
 */
public class RegisterActivity extends Activity {

    private EditText name;
    private EditText emailAddress;
    private EditText password;
    private EditText passwordVerify;

    /**
     * onCreate()
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getClass().getName(), "onCreate() called");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);
    }

    /**
     * onRegisterClick() - Registration Form submit
     * @param view
     */
    public void onRegisterClick(View view) {
        Log.d(getClass().getName(), "onRegisterClick() clicked");
        this.name = (EditText) findViewById(R.id.name);
        this.emailAddress = (EditText) findViewById(R.id.emailAddress);
        this.password = (EditText) findViewById(R.id.password);
        this.passwordVerify = (EditText) findViewById(R.id.passwordVerify);

        Log.d(getClass().getName(), "Name: " + this.name);
        Log.d(getClass().getName(), "Email: " + this.emailAddress);
        Log.d(getClass().getName(), "Password: " + this.password);
    }

}
