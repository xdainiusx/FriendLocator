package me.dainius.friendlocator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Friends Activity
 */
public class FriendsActivity extends Activity {

    private static boolean DEBUG = false;
    private static Context context;
    private static String ACTIVITY = "FriendsActivity";
    private static int PENDING = 1;
    private static int ACCEPTED = 2;
    private static int DECLINED = 3;
    private ListView friendsListView;
    private Friend[] friends;
    private Friend friendClicked = null;
    private ParseUser friendUser = null;
    private ArrayList<String> inviterFriends = null;

    /**
     * onCreate()
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FriendsActivity.context = getApplicationContext();
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_friends);


        this.inviterFriends = this.getPendingFriendInvitations();
        View invitationsTab = findViewById(R.id.invitationsTab);
        if(this.inviterFriends.size()>0) {
            Log.d(ACTIVITY, "Found invitations: " + this.inviterFriends.size());
            invitationsTab.setVisibility(View.VISIBLE);
        }
        else {
            Log.d(ACTIVITY, "No found invitations!");
        }



        this.friendsListView = (ListView) findViewById(android.R.id.list);

        //this.friends = this.generateFriends();
        this.friends = this.generateParseFriends();
        Arrays.sort(this.friends);

        this.friendsListView.setAdapter(new ListViewAdapter(this, this.friends));

        this.friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {

                Friend friend = (Friend) adapter.getItemAtPosition(position);

                setFriendClicked(friend);

                Log.d(ACTIVITY, friend.getFirstName() + " " + friend.getLastName() + " clicked");

                /**
                 * Alert on friend click
                 */
                AlertDialog alert = new AlertDialog.Builder(FriendsActivity.this).create();
                alert.setTitle("Connect with " + friend.getFirstName() + " " + friend.getLastName());
                Log.d(ACTIVITY, "Friends Email: " + friend.getEmail());
                Log.d(ACTIVITY, "Friends Location: " + friend.getCoordinateArray()[0] + ", " + friend.getCoordinateArray()[1]);
                alert.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(ACTIVITY, "Yes pressed");
                        Intent mainIntent = new Intent(FriendsActivity.this, MainActivity.class);
                        mainIntent.putExtra("defaultTab", 2);
                        if(getFriendClicked() != null) {
                            mainIntent.putExtra("FriendID", getFriendClicked().getId());
                        }
                        startActivity(mainIntent);
                    }
                });
                alert.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(ACTIVITY, "No pressed");
                    }
                });
                alert.show();

            }
        });
    }

    /**
     * onStart()
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * onInvitationsClick()
     * @param view
     */
    public void onInvitationsClick(View view) {
        Log.d(ACTIVITY, "onInvitationsClick() clicked");
        this.inviterFriends = this.getPendingFriendInvitations();
        if(this.inviterFriends.size()>0) {
            Log.d(ACTIVITY, "Found invitations: " + this.inviterFriends.size());
            for (int i = 0; i < this.inviterFriends.size(); i++) {
                Log.d(ACTIVITY, this.inviterFriends.get(i));
            }

            Intent intent = new Intent(this, FriendsInvitationPendingActivity.class);
            intent.putStringArrayListExtra("pendingInvites", this.inviterFriends);
            startActivity(intent);
        }
        else {
            Log.d(ACTIVITY, "No found invitations!");
        }
    }

    /**
     * onAddFriendClick()
     * @param view
     */
    public void onAddFriendClick(View view) {
        Log.d(ACTIVITY, "onAddFriendClick() clicked");

        /**
         * Alert on friend click
         */
        final AlertDialog alert = new AlertDialog.Builder(FriendsActivity.this).create();
        alert.setTitle("Enter email address below to invite your friend");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setSingleLine();
        alert.setView(input);
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Invite", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Log.d(ACTIVITY, "Invite pressed");
                Editable email = input.getText();
                String emailAddress = email.toString();
                Log.d(ACTIVITY, "Email address entered: " + emailAddress);

                boolean valid = isEmailValid(emailAddress);

                boolean canCloseDialog = (valid == true);

                if (canCloseDialog) {
                    invite(emailAddress);
                } else {
                    String errorMessage = "Email is invalid! Please try again.";
                    toastIt(errorMessage);
                }
            }
        });
        alert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            Log.d(ACTIVITY, "Cancel pressed");
            }
        });
        alert.show();
    }

    /**
     * getPendingFriendInvitations() - get objects of inviter friends
     * @return List<ParseObject>
     */
    private ArrayList<String> getPendingFriendInvitations() {
        List<ParseObject> userList = null;
        ArrayList<String> foundUsers = new ArrayList<String>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("FriendInvitation");
        query.whereEqualTo("friend", ParseUser.getCurrentUser().getEmail());
        query.whereEqualTo("status", PENDING);
        query.selectKeys(Arrays.asList("inviter"));

        try {
            Log.d(ACTIVITY, "Will try to find users.");
            userList = query.find();
            for (ParseObject o : userList) {
                foundUsers.add(o.get("inviter").toString());
            }
        } catch (ParseException e) {
            foundUsers = null;
            Log.d(ACTIVITY, e.getLocalizedMessage());
        }
        return foundUsers;
    }

    /**
     * isUserAlreadyInvited() - checks if user already invited
     * @param email
     * @return boolean
     */
    private boolean isUserAlreadyInvited(String email) {
        boolean invited = false;
        List<ParseObject> emailList = null;
        ArrayList<String> foundEmails = new ArrayList<String>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("FriendInvitation");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.selectKeys(Arrays.asList("friend"));

        try {
            emailList = query.find();
            for (ParseObject o : emailList) {
                foundEmails.add(o.get("friend").toString());
            }
        } catch (ParseException e) {
            toastIt(e.getLocalizedMessage());
        }
        if(foundEmails!=null){
            if(foundEmails.contains(email)){
                invited = true;
                Log.d(ACTIVITY, "Email in the list: "+email);
            }
            else {
                Log.d(ACTIVITY, "No emails found!");
            }
        }
        else {
            invited = false;
            Log.d(ACTIVITY, "Email not in the list");
        }
        return invited;
    }

    /**
     * isUserRegistered() - checks if user is registered with Friend Locator
     * @param emailAddress
     * @return
     */
    private boolean isUserRegistered(String emailAddress) {
        boolean registered = false;
        this.friendUser = this.getUserByEmail(emailAddress);
        if(friendUser!=null){
            registered = true;
        }
        return registered;
    }

    /**
     * getUserByEmail() - Assigns user to this.user so we don't do the same query again
     * @param emailAddress
     * @return ParseUser
     */
    private ParseUser getUserByEmail(String emailAddress) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", emailAddress);
        try {
            this.friendUser = query.getFirst();
        } catch (ParseException e) {
            this.friendUser = null;
            Log.d(ACTIVITY, e.getLocalizedMessage());
        }
        return this.friendUser;
    }

    /**
     * invite() - invite the friend
     * @param friendEmail
     */
    private void invite(String friendEmail) {

        if(!this.isUserRegistered(friendEmail)){
            String message = "User with the email address " + friendEmail + " is not registered with Friend Locator!";
            toastIt(message);
        }
        else if(isUserAlreadyInvited(friendEmail)) {
            String message = "User with the email address " + friendEmail + " was already invited.";
            toastIt(message);
        }
        else {
            this.saveToFriends(this.friendUser, FriendsActivity.PENDING);

            this.saveToFriendInvitation(friendEmail);

        }
    }

    /**
     * saveToFriends() - save to Friends table
     */
    public void saveToFriends(ParseUser friendUser, int status) {
        Friends friends = new Friends();
        friends.setUser(ParseUser.getCurrentUser());
        friends.setUsersFriend(friendUser);
        friends.setStatus(status);

        ParseACL friendAcl = new ParseACL();
        friendAcl.setPublicReadAccess(true);
        friends.setACL(friendAcl);

        friends.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(ACTIVITY, "Friend saved to Friends table.");
                } else {
                    Log.d(ACTIVITY, "Error saving friend to Friends table.");
                }
            }
        });
    }

    /**
     * saveToFriendInvitation() - save to FriendsInvitation table
     * @param email
     */
    public void saveToFriendInvitation(String email) {
        final String friendEmail = email;
        FriendInvitation friendInvitation = new FriendInvitation();
        friendInvitation.setFriend(email);
        friendInvitation.setUser(ParseUser.getCurrentUser());
        friendInvitation.setInviter(ParseUser.getCurrentUser().getEmail());
        friendInvitation.setStatus(FriendsActivity.PENDING);

        ParseACL acl = new ParseACL();
        acl.setPublicReadAccess(true);
        acl.setPublicWriteAccess(true);
        friendInvitation.setACL(acl);

        friendInvitation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                //finish();
                if (e == null) {
                    String message = "Invitation was sent to " + friendEmail + ".";
                    toastIt(message);
                } else {
                    Log.d(ACTIVITY, "Exception: " + e);
                    toastIt(e.getLocalizedMessage());
                }
            }
        });
    }

    /**
     * deleteFromFriendsInvitations()
     * @param user
     * @param friendEmail
     */
    public void deleteFromFriendsInvitations(ParseUser user, String friendEmail) {
        FriendInvitation friendInvitation = null;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("FriendInvitation");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.whereEqualTo("friend", friendEmail);
        try {
            friendInvitation = (FriendInvitation)query.getFirst();
        } catch (ParseException e) {
            friendInvitation = null;
            Log.d(ACTIVITY, e.getLocalizedMessage());
        }
        friendInvitation.deleteInBackground();
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
        Toast t = Toast.makeText(getApplicationContext(), this.capitalizeString(message), Toast.LENGTH_LONG);
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
     * getActivityContext() - gets activity context, good to use in inner classes
     * @return
     */
    public static Context getActivityContext() {
        return FriendsActivity.context;
    }

    /**
     * getFriendClicked() - getter
     * @return
     */
    public Friend getFriendClicked() {
        return this.friendClicked;
    }

    /**
     * setFriendClicked() - setter
     * @param friend
     */
    public void setFriendClicked(Friend friend) {
        this.friendClicked = friend;
    }

    /**
     * Generates friend object's array for testing
     * @return Friend[] objects array
     */
    public Friend[] generateFriends() {

        Friend f1 = new Friend("1", "Ten", "Walls", "tenwalls@gmail.com");
        Friend f2 = new Friend("2", "James", "Atkin", "jamesatkin@gmail.com");
        Friend f3 = new Friend("3", "John", "Carter", "johncarter@gmail.com");
        Friend f4 = new Friend("4", "Carl", "Cox", "carlcox@gmail.com");
        Friend f5 = new Friend("5", "Pete", "Tong", "petetong@gmail.com");
        Friend f6 = new Friend("6", "Mike", "Edwards", "mikeedwards@gmail.com");
        Friend f7 = new Friend("7", "Annie", "Mac", "anniemac@gmail.com");
        Friend f8 = new Friend("8", "Thomas", "Yorke", "thomasyorke@gmail.com");
        Friend f9 = new Friend("9", "Liam", "Howlett", "liamhowlett@gmail.com");
        Friend f10 = new Friend("10", "Keith", "Flint", "keithflint@gmail.com");
        Friend f11 = new Friend("11", "Robert", "de Naja", "robertdenaja@gmail.com");
        Friend f12 = new Friend("12", "Gui", "Boratto", "guiboratto@gmail.com");

        Friend[] friends = new Friend[]{f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12};
        return friends;
    }

    public Friend[] generateParseFriends() {

        final Friend[] friend = null;
        Friends friends;
        ParseQuery<Friends> query = ParseQuery.getQuery("Friends");
        query.whereEqualTo("user", ParseUser.getCurrentUser());

        query.findInBackground(new FindCallback<Friends>() {
            public void done(List<Friends> friends, ParseException e) {
                //Log.d(ACTIVITY, "FRIENDS: "+friends.get(0).getEmail());
            }
        });



        Friend f1 = new Friend("12", "Gui", "Boratto", "guiboratto@gmail.com");
        Friend f2 = new Friend("7", "Annie", "Mac", "anniemac@gmail.com");

        Friend[] fr = new Friend[]{f1, f2};
        return fr;
    }

    /**
     * getFriends()
     * @return Friend[]
     */
    public Friend[] getFriends() {
        Friend[] friends = new Friend[2];

        return friends;
    }

}
