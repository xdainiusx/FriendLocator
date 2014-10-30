package me.dainius.friendlocator;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class PasswordRecoveryActivity extends Activity {

    private EditText emailAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getClass().getName(), "onCreate() called");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_password_recovery);
    }

    public void onRecoverPasswordClick(View view) {
        Log.d(getClass().getName(), "onRecoverPasswordClick clicked");
    }

}
