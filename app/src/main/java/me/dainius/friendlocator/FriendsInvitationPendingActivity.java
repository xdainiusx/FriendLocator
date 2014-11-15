package me.dainius.friendlocator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * FriendsInvitationPendingActivity
 */
public class FriendsInvitationPendingActivity extends Activity {

    private static String ACTIVITY = "FriendsInvitationPendingActivity";
    private static Context context;
    private ListView pendingInvitesListView;
    private ArrayList<String> pendingInvites = null;
    private TextView emailAddress;

    /**
     * onCreate()
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_friends_invitation_pending);

        Intent intent = getIntent();
        this.pendingInvites = intent.getStringArrayListExtra("pendingInvites");

        Log.d(ACTIVITY, "Inside pop up screen.");
        for(int i=0; i<this.pendingInvites.size(); i++) {
            Log.d(ACTIVITY, this.pendingInvites.get(i));
        }

        this.pendingInvitesListView = (ListView) findViewById(android.R.id.list);

        this.pendingInvitesListView.setAdapter(new InvitationPendingListViewAdapter(this, this.getStringArray(this.pendingInvites)));


        Button close_button = (Button) findViewById(R.id.close_button);
        close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    /**
     * getStringArray()
     * @param list
     * @return String[]
     */
    private String[] getStringArray(ArrayList<String> list) {
        String[] pendingInvites = new String[list.size()];
        for(int i=0; i<list.size(); i++) {
            pendingInvites[i] = list.get(i);
        }
        return pendingInvites;
    }

    public void onAcceptClick(View view) {
        Log.d(ACTIVITY, "onAcceptClick() clicked");
        TextView emailView = (TextView)findViewById(R.id.friendEmail);
        String email = emailView.getText().toString();
        Log.d(ACTIVITY, email);
    }

    public void onDeclineClick(View view) {
        Log.d(ACTIVITY, "onDeclineClick() clicked");
        TextView emailView = (TextView)findViewById(R.id.friendEmail);
        String email = emailView.getText().toString();
        Log.d(ACTIVITY, email);
    }

}
