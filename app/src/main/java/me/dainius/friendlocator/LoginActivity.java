package me.dainius.friendlocator;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class LoginActivity extends Activity {

    private EditText emailAddress;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getClass().getName(), "onCreate() called");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
    }

    public void onSignInClick(View view) {
        Log.d(getClass().getName(), "onSignInClick() clicked");
        this.emailAddress = (EditText) findViewById(R.id.emailAddress);
        this.password = (EditText) findViewById(R.id.password);

        Log.d(getClass().getName(), "Email: " + this.emailAddress + "Password: " + this.password);
    }

}
