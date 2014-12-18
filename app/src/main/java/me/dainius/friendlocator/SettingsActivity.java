package me.dainius.friendlocator;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Settings Activity
 */
public class SettingsActivity extends Activity {

    private static String ACTIVITY = "SettingsActivity";
    private Dialog passwordChangeDialog;
    private Dialog updateAccountDialog;
    private StringBuilder validationErrorMessage;
    private boolean formIsValid;

    /**
     * onCreate()
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_settings);
    }

    /**
     * onLogoutClick()
     * @param view
     */
    public void onLogoutClick(View view) {
        Log.d(ACTIVITY, "onLogoutClick() clicked");
        ParseUser user = ParseUser.getCurrentUser();
        user.put("isOnline", false);
        user.saveInBackground();
        ParseUser.logOut();
        Intent intent = new Intent(SettingsActivity.this, Dispatcher.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * onChangePasswordClick()
     * @param view
     */
    public void onChangePasswordClick(View view) {
        Log.d(ACTIVITY, "onChangePasswordClick() clicked");
        this.callChangePasswordDialog();
    }

    /**
     * onUpdateAccountClick()
     * @param view
     */
    public void onUpdateAccountClick(View view) {
        Log.d(ACTIVITY, "onUpdateAccountClick() clicked");
        this.callUpdateAccountDialog();
    }

    /**
     * callChangePasswordDialog()
     */
    private void callChangePasswordDialog() {

        this.passwordChangeDialog = new Dialog(this);
        this.passwordChangeDialog.setContentView(R.layout.dialog_change_password);
        this.passwordChangeDialog.setCancelable(false);
        this.passwordChangeDialog.setTitle("Update Password");
        Button submit = (Button) this.passwordChangeDialog.findViewById(R.id.submitButton);
        Button cancel = (Button) this.passwordChangeDialog.findViewById(R.id.cancelButton);

        this.passwordChangeDialog.show();

        submit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v){
                formIsValid = true;
                validationErrorMessage = new StringBuilder("");

                final EditText passwordField = (EditText) passwordChangeDialog.findViewById(R.id.password);
                final EditText passwordVerifyField = (EditText) passwordChangeDialog.findViewById(R.id.passwordVerify);

                String password = passwordField.getText().toString();
                String passwordVerify = passwordVerifyField.getText().toString();

                Log.d(ACTIVITY, "Submit Clicked");
                Log.d(ACTIVITY, "Password: " + password.toString());
                Log.d(ACTIVITY, "Password Verify: " + passwordVerify.toString());

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

                if(!password.equals(passwordVerify)) {
                    formIsValid = false;
                    String errorMessage = "Password verification is incorrect.\n";
                    validationErrorMessage.append(errorMessage);
                }

                if(formIsValid) {
                    Log.d(ACTIVITY, "Form is valid");
                    ParseUser user = ParseUser.getCurrentUser();
                    user.setPassword(password);
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                passwordChangeDialog.dismiss();
                                toastIt("Password was updated.");
                            } else {
                                Log.d(ACTIVITY, "Error updating password: " + e.getLocalizedMessage());
                                toastIt(e.getLocalizedMessage());
                            }
                        }
                    });
                }
                else {
                    toastIt(validationErrorMessage.toString());
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v){
                Log.d(ACTIVITY, "Cancel clicked");
                passwordChangeDialog.dismiss();
            }
        });
    }

    /**
     * callUpdateAccountDialog()
     */
    private void callUpdateAccountDialog() {

        this.updateAccountDialog = new Dialog(this);
        this.updateAccountDialog.setContentView(R.layout.dialog_update_account);
        this.updateAccountDialog.setCancelable(false);
        this.updateAccountDialog.setTitle("Update Account");
        Button submit = (Button) this.updateAccountDialog.findViewById(R.id.submitButton);
        Button cancel = (Button) this.updateAccountDialog.findViewById(R.id.cancelButton);

        this.updateAccountDialog.show();

        submit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v){
                formIsValid = true;
                validationErrorMessage = new StringBuilder("");

                final EditText nameField = (EditText) updateAccountDialog.findViewById(R.id.name);

                String name = nameField.getText().toString();

                Log.d(ACTIVITY, "Submit Clicked");
                Log.d(ACTIVITY, "New Name: " + name);

                if(name.length() == 0) {
                    formIsValid = false;
                    String errorMessage = "Name is empty.\n";
                    validationErrorMessage.append(errorMessage);
                }

                if(formIsValid) {
                    Log.d(ACTIVITY, "Form is valid");
                    ParseUser user = ParseUser.getCurrentUser();
                    user.put("name", name);
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                updateAccountDialog.dismiss();
                                toastIt("Name was updated.");
                            } else {
                                Log.d(ACTIVITY, "Error updating name: " + e.getLocalizedMessage());
                                toastIt(e.getLocalizedMessage());
                            }
                        }
                    });
                }
                else {
                    toastIt(validationErrorMessage.toString());
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v){
                Log.d(ACTIVITY, "Cancel clicked");
                updateAccountDialog.dismiss();
            }
        });
    }

    /**
     * onHelpClick()
     * @param view
     */
    public void onHelpClick(View view) {
        Log.d(ACTIVITY, "onHelpClick() clicked");
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
     * @param message
     */
    private void toastIt(String message) {
        Log.d(ACTIVITY, message);
        Toast t = Toast.makeText(getApplicationContext(), this.capitalizeString(message), Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }
}