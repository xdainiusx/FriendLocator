package me.dainius.friendlocator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Register Activity - User registration screen
 */
public class RegisterActivity extends Activity {

    private static String ACTIVITY = "RegisterActivity";

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
        StringBuilder validationErrorMessage = new StringBuilder("");
        this.name = (EditText) findViewById(R.id.name);
        this.emailAddress = (EditText) findViewById(R.id.emailAddress);
        this.password = (EditText) findViewById(R.id.password);
        this.passwordVerify = (EditText) findViewById(R.id.passwordVerify);
        String name = this.name.getText().toString();
        String emailAddress = this.emailAddress.getText().toString();
        String password = this.password.getText().toString();
        String passwordVerify = this.passwordVerify.getText().toString();
        boolean formIsValid = true;

        if(name.length() == 0) {
            formIsValid = false;
            String errorMessage = "Name is empty.\n";
            validationErrorMessage.append(errorMessage);
        }
        if(emailAddress.length() == 0) {
            formIsValid = false;
            String errorMessage = "Email address is empty.\n";
            validationErrorMessage.append(errorMessage);
        }
        if(password.length() == 0) {
            formIsValid = false;
            String errorMessage = "Password is empty.\n";
            validationErrorMessage.append(errorMessage);
        }
        if(passwordVerify.length() == 0) {
            formIsValid = false;
            String errorMessage = "Password verification is empty.\n";
            validationErrorMessage.append(errorMessage);
        }
        if(!this.isEmailValid(emailAddress)) {
            formIsValid = false;
            String errorMessage = "Email address is incorrect.\n";
            validationErrorMessage.append(errorMessage);
        }
        if(!password.equals(passwordVerify)) {
            formIsValid = false;
            String errorMessage = "Password verification is incorrect.\n";
            validationErrorMessage.append(errorMessage);
        }

        if(formIsValid) {

            Log.d(ACTIVITY, "Name:                  " + name);
            Log.d(ACTIVITY, "Email:                 " + emailAddress);
            Log.d(ACTIVITY, "Password:              " + password);
            Log.d(ACTIVITY, "Password verification: " + passwordVerify);

            ParseUser user = new ParseUser();

            user.setUsername(emailAddress);
            user.setPassword(password);
            user.setEmail(emailAddress);
            user.put("name", name);
            user.put("isOnline", true);
//            user.put("latitude", 0.0);
//            user.put("longitude", 0.0);

            user.signUpInBackground(new SignUpCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        Intent intent = new Intent(RegisterActivity.this, Dispatcher.class);
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

}
