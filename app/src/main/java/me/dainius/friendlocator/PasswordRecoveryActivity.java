package me.dainius.friendlocator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

/**
 * Password Recovery Activity
 */
public class PasswordRecoveryActivity extends Activity {

    private static String ACTIVITY = "PasswordRecoveryActivity";
    private EditText emailAddress;

    /**
     * onCreate()
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getClass().getName(), "onCreate() called");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_password_recovery);
    }

    /**
     * onRecoverPasswordClick() - Recover Password button clicked
     * @param view
     */
    public void onRecoverPasswordClick(View view) {
        Log.d(getClass().getName(), "onRecoverPasswordClick clicked");
        StringBuilder validationErrorMessage = new StringBuilder("");
        this.emailAddress = (EditText) findViewById(R.id.emailAddress);
        final String emailAddress = this.emailAddress.getText().toString();
        boolean formIsValid = true;

        if(emailAddress.length() == 0) {
            formIsValid = false;
            String errorMessage = "Email address is empty.\n";
            validationErrorMessage.append(errorMessage);
        }
        if(!this.isEmailValid(emailAddress)) {
            formIsValid = false;
            String errorMessage = "Email address is incorrect.\n";
            validationErrorMessage.append(errorMessage);
        }

        if(formIsValid) {
            if(this.isNetworkAvailable()) {
                ParseUser.requestPasswordResetInBackground(emailAddress, new RequestPasswordResetCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            String message = "Recovery email was sent to " + emailAddress + "\n";
                            toastIt(message);
                            Intent intent = new Intent(PasswordRecoveryActivity.this, Dispatcher.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Log.d(ACTIVITY, "Exception: " + e);
                            toastIt(e.getLocalizedMessage());
                        }
                    }
                });
            }
            else {
                this.toastIt("Network is unavailable! \nMake sure you are connected to the internet.");
            }
        }
        else {
            this.toastIt(validationErrorMessage.toString());
        }
    }

    /**
     * capitalizeString()
     * @param str
     * @return String
     */
    public String capitalizeString(String str) {
        String newString = str.substring(0, 1).toUpperCase() + str.substring(1);
        return newString;
    }

    /**
     * toastIt() - toast used for form verification
     * @param errorMessage
     */
    private void toastIt(String errorMessage) {
        Log.d(ACTIVITY, errorMessage);
        Toast t = Toast.makeText(getApplicationContext(), this.capitalizeString(errorMessage), Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }

    /**
     * isEmailValid() - checks if email address is valid
     * @param email
     * @return
     */
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
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
