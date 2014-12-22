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

import com.parse.DeleteCallback;
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
    private ParseUser accountUser;

    /**
     * onCreate()
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FriendsActivity.context = getApplicationContext();
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_friends);

        this.accountUser = ParseUser.getCurrentUser();
        // Invitations
        this.inviterFriends = this.getPendingFriendInvitations();
        View invitationsTab = findViewById(R.id.invitationsTab);
        if(this.inviterFriends.size()>0) {
            Log.d(ACTIVITY, "Found invitations: " + this.inviterFriends.size());
            invitationsTab.setVisibility(View.VISIBLE);
        }
        else {
            Log.d(ACTIVITY, "No found invitations!");
            invitationsTab.setVisibility(View.GONE);
        }
    }

    /**
     * onStart()
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * onResume()
     */
    protected void onResume() {
        super.onResume();
        Log.d(ACTIVITY, "onResume()");
        // Check if we still have a user
        if(this.accountUser==null){
            this.accountUser = ParseUser.getCurrentUser();
        }
        // Invitations
        this.inviterFriends = this.getPendingFriendInvitations();
        if(this.inviterFriends.size()==0) {
            View invitationsTab = findViewById(R.id.invitationsTab);
            invitationsTab.setVisibility(View.GONE);
        }
        else {
            View invitationsTab = findViewById(R.id.invitationsTab);
            invitationsTab.setVisibility(View.VISIBLE);
        }

        // Moved to the private function
        this.friendsListView();
    }

    /**
     * friendsListView() - move it to onResume()
     */
    private void friendsListView() {
        this.friendsListView = (ListView) findViewById(android.R.id.list);

        this.friends = this.generateParseFriends();

        View noFriendsView = findViewById(R.id.noFriends);
        ListView listView = (ListView)findViewById(android.R.id.list);

        if(this.friends.length == 0) {
            Log.d(ACTIVITY, "No Friends Found!");

            listView.setVisibility(View.GONE);
            noFriendsView.setVisibility(View.VISIBLE);
        }
        else {
            noFriendsView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            Arrays.sort(this.friends);

            this.friendsListView.setAdapter(new ListViewAdapter(this, this.friends));

            this.friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {

                    Friend friend = (Friend) adapter.getItemAtPosition(position);

                    setFriendClicked(friend);

                    Log.d(ACTIVITY, friend.getName() + " clicked");

                    if (friendIsOnline(friend)) {
                        alertToConnect(friend);
                    } else {
                        alertFriendNotOnline(friend);
                    }

                }
            });

            this.friendsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
                    Friend friend = (Friend) adapter.getItemAtPosition(position);
                    Log.d(ACTIVITY, friend.getName() + " long clicked");
                    alertDeleteFriend(friend, view);
                    return true;
                }
            });
        }
    }

    /**
     * alertDeleteFriend()
     * @param friend
     */
    private void alertDeleteFriend(Friend friend, View view) {
        final Friend localFriend = friend;
        final View localView = view;
        /**
         * Alert on friend long click
         */
        AlertDialog alert = new AlertDialog.Builder(FriendsActivity.this).create();
        alert.setTitle("Delete friend? " + friend.getName());
        Log.d(ACTIVITY, "Friends Email: " + friend.getEmail());
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(ACTIVITY, "Yes pressed");
                View parent = (View)localView;
                parent.setVisibility(View.GONE);
                deleteFriend(getAccountUser(), localFriend.getEmail());
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

    /**
     * alertToConnect()
     * @param friend
     */
    private void alertToConnect(Friend friend) {
        /**
         * Alert on friend click
         */
        AlertDialog alert = new AlertDialog.Builder(FriendsActivity.this).create();
        alert.setTitle("Connect with " + friend.getName());
        Log.d(ACTIVITY, "Friends Email: " + friend.getEmail());
        Log.d(ACTIVITY, "Friends Location: " + friend.getCoordinateArray()[0] + ", " + friend.getCoordinateArray()[1]);
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(ACTIVITY, "Yes pressed");
                Intent mainIntent = new Intent(FriendsActivity.this, MainActivity.class);
                mainIntent.putExtra("defaultTab", 2);
                if (getFriendClicked() != null) {
                    mainIntent.putExtra("FriendEmail", getFriendClicked().getEmail());
                    mainIntent.putExtra("FriendName", getFriendClicked().getName());
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

    /**
     * alertFriendNotOnline()
     * @param friend
     */
    private void alertFriendNotOnline(Friend friend) {
        /**
         * Alert on friend click
         */
        AlertDialog alert = new AlertDialog.Builder(FriendsActivity.this).create();
        alert.setTitle(friend.getName() + " is not currently online.");
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(ACTIVITY, "OK pressed");
            }
        });
        alert.show();
    }

    public ParseUser getAccountUser() {
        return this.accountUser;
    }

    /**
     * friendIsOnline()
     * @param friend
     * @return Boolean
     */
    private Boolean friendIsOnline(Friend friend) {
        Boolean isOnline = false;
        try {
            ParseUser user = this.getUserByEmail(friend.getEmail());
            isOnline = (Boolean)user.get("isOnline");
        } catch (Exception e) {
            Log.d(ACTIVITY, "Error retrieving the user: " + e);
        }

        return isOnline;
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
        query.whereEqualTo("friend", this.getAccountUser().getEmail());
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
        FriendInvitation friendInvitation = null;
        ParseQuery<FriendInvitation> query = ParseQuery.getQuery("FriendInvitation");
        query.whereEqualTo("user", this.getAccountUser());
        query.whereEqualTo("friend", email);

        try {
            friendInvitation = query.getFirst();
        } catch (ParseException e) {
            Log.d(ACTIVITY, email + " is still not invited!");
        }
        if(friendInvitation!=null) {
            invited = true;
        }
        return invited;
    }

    /**
     * userAlreadyInvitedMe()
     * @param email
     * @return boolean
     */
    private boolean userAlreadyInvitedMe(String email) {
        boolean invited = false;
        FriendInvitation friendInvitation = null;
        ParseQuery<FriendInvitation> query = ParseQuery.getQuery("FriendInvitation");
        query.whereEqualTo("inviter", email);
        query.whereEqualTo("user", this.getAccountUser());

        try {
            friendInvitation = query.getFirst();
        } catch (ParseException e) {
            Log.d(ACTIVITY, email + " is still not invited by me!");
        }
        if(friendInvitation!=null) {
            invited = true;
        }
        return invited;
    }

    /**
     * isUserMe()
     * @param email
     * @return boolean
     */
    private boolean isUserMe(String email) {
        if(this.getAccountUser().getEmail().equals(email)) {
            return true;
        }
        return false;
    }

    /**
     * isUserRegistered() - checks if user is registered with Friend Locator
     * @param emailAddress
     * @return boolean
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
     * getUserByEmail()
     * @param emailAddress
     * @return ParseUser
     */
    private ParseUser getUserByEmail(String emailAddress) {
        ParseUser user;
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", emailAddress);
        try {
            user = query.getFirst();
        } catch (ParseException e) {
            user = null;
            Log.d(ACTIVITY, e.getLocalizedMessage());
        }
        return user;
    }

    /**
     * invite() - invite the friend
     * @param friendEmail
     */
    private void invite(String friendEmail) {

        Log.d(ACTIVITY, "EMAIL: " + friendEmail);
        if(this.isUserMe(friendEmail)) {
            String message = friendEmail + " is your own email!";
            toastIt(message);
        }
        else if(!this.isUserRegistered(friendEmail)){
            String message = "User with the email address " + friendEmail + " is not registered with Friend Locator!";
            toastIt(message);
        }
        else if(this.isUserAlreadyInvited(friendEmail)) {
            String message = "User with the email address " + friendEmail + " was already invited.";
            toastIt(message);
        }
        else if(this.userAlreadyInvitedMe(friendEmail)) {
            String message = "User with the email address " + friendEmail + " already invited you.";
            message += "\n";
            message += "Please go to Pending Invitations and accept your friend's invitation!";
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
        friends.setUser(this.getAccountUser());
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
        friendInvitation.setUser(this.getAccountUser());
        friendInvitation.setInviter(this.getAccountUser().getEmail());
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
     * deleteFriend()
     * @param user
     * @param friendEmail
     */
    public void deleteFriend(ParseUser user, String friendEmail) {
        Friends friends = null;
        ParseUser friend = this.getUserByEmail(friendEmail);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Friends");
        query.whereEqualTo("user", user);
        query.whereEqualTo("usersFriend", friend);
        try {
            friends = (Friends)query.getFirst();
        } catch (ParseException e) {
            friends = null;
            Log.d(ACTIVITY, e.getLocalizedMessage());
        }
        if(friends!=null) {
            friends.deleteInBackground(new DeleteCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d(ACTIVITY, "Friend1 deleted successfully.");
                    } else {
                        Log.d(ACTIVITY, "Failed to delete the friend1!");
                    }
                }
            });
        }
        else {
            Log.d(ACTIVITY, "Error finding the friend");
        }

        // Now delete the other way
        Friends friends2 = null;
        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Friends");
        query2.whereEqualTo("user", friend);
        query2.whereEqualTo("usersFriend", user);
        try {
            friends2 = (Friends)query2.getFirst();
        } catch (ParseException e) {
            friends2 = null;
            Log.d(ACTIVITY, e.getLocalizedMessage());
        }
        if(friends2!=null) {
            friends2.deleteInBackground(new DeleteCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d(ACTIVITY, "Friend2 deleted successfully.");
                    } else {
                        Log.d(ACTIVITY, "Failed to delete the friend2!");
                    }
                }
            });
        }
        else {
            Log.d(ACTIVITY, "Error finding the friend");
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
     * @return Context
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

        Friend f1 = new Friend("1", "Ten Walls", "tenwalls@gmail.com");
        Friend f2 = new Friend("2", "James Atkin", "jamesatkin@gmail.com");
        Friend f3 = new Friend("3", "John Carter", "johncarter@gmail.com");
        Friend f4 = new Friend("4", "Carl Cox", "carlcox@gmail.com");
        Friend f5 = new Friend("5", "Pete Tong", "petetong@gmail.com");
        Friend f6 = new Friend("6", "Mike Edwards", "mikeedwards@gmail.com");
        Friend f7 = new Friend("7", "Annie Mac", "anniemac@gmail.com");
        Friend f8 = new Friend("8", "Thomas Yorke", "thomasyorke@gmail.com");
        Friend f9 = new Friend("9", "Liam Howlett", "liamhowlett@gmail.com");
        Friend f10 = new Friend("10", "Keith Flint", "keithflint@gmail.com");
        Friend f11 = new Friend("11", "Robert de Naja", "robertdenaja@gmail.com");
        Friend f12 = new Friend("12", "Gui Boratto", "guiboratto@gmail.com");

        Friend[] friends = new Friend[]{f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12};
        return friends;
    }


    /**
     * generateParseFriends() - gets Friends list from the database
     * @return Friend[]
     */
    public Friend[] generateParseFriends() {

        Friend[] friendArray = null;
        List<Friends> friendList = null;
        Friends friends;
        ParseQuery<Friends> query = ParseQuery.getQuery("Friends");
        query.whereEqualTo("user", this.getAccountUser());
        query.selectKeys(Arrays.asList("usersFriend"));

        try {
            Log.d(ACTIVITY, "Will try to find friends.");
            ArrayList<Friend> friendsArrayList = new ArrayList<Friend>();
            friendList = query.find();
            for (Friends f : friendList) {
                ParseUser user = ((ParseUser)f.get("usersFriend")).fetchIfNeeded();

                try {
                    String id = user.getObjectId();
                    String email = user.getEmail();
                    String name = (String)user.get("name");
                    boolean online = (boolean)user.getBoolean("isOnline");
                    Log.d(ACTIVITY, "Friend ID:     =====>" + id);
                    Log.d(ACTIVITY, "Friend Email:  =====>" + email);
                    Log.d(ACTIVITY, "Friend Name:   =====>" + name);
                    Log.d(ACTIVITY, "Friend isOnline:  ==>" + online);
                    Friend friend = new Friend(id, name, email);
                    friend.setIsOnline(online);
                    friendsArrayList.add(friend);
                } catch (Exception e) {
                    Log.d(ACTIVITY, "User parse error! " + e.getLocalizedMessage());
                }
            }
            if(friendsArrayList.size()>0) {
                friendArray = friendsArrayList.toArray(new Friend[friendsArrayList.size()]);
            }
        } catch (ParseException e) {
            Log.d(ACTIVITY, e.getLocalizedMessage());
        }

        if(friendArray!=null && friendArray.length > 0) {
            for(int i=0; i< friendArray.length; i++) {
                Log.d(ACTIVITY, friendArray[i].toString());
            }
        }
        else {
            friendArray = new Friend[0];
        }
        return friendArray;
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