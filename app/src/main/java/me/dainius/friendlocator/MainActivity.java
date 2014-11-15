package me.dainius.friendlocator;

import android.app.ActionBar;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

/**
 * Main Activity - user comes here after login
 */
public class MainActivity extends TabActivity {

    private static String ACTIVITY = "MainActivity";
    private ActionBar actionBar;
    private int defaultTab = -1;
    private String friendID = null;

    /**
     * onCreate()
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        Bundle extras = this.getIntent().getExtras();
        if(extras != null) {
            this.defaultTab = extras.getInt("defaultTab");
            Log.d(ACTIVITY, "Default tab: " + this.defaultTab);

            this.friendID = extras.getString("FriendID");
            Log.d(ACTIVITY, "Friend ID: " + this.friendID);
        }

        this.goToActivity("Friends", null);
        if(this.friendID!=null){
            this.goToActivity("Map", this.friendID);
        }
        else{
            this.goToActivity("Map", null);
        }
        this.goToActivity("Settings", null);
    }

    public void onResume() {
        super.onResume();
    }

    /**
     * gotToActivity()
     * @param activityName
     * @param friendID - if no ID passed -1
     */
    public void goToActivity(String activityName, String friendID) {

        TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);

        if(activityName.equals("Friends")) {
            // Friends Tab
            TabSpec friendsTab = tabHost.newTabSpec("Friends");
            // Friends Tab Indicator View
            View friendsTabIndicator = LayoutInflater.from(this).inflate(R.layout.bottom_tab, getTabWidget(), false);
            ((TextView) friendsTabIndicator.findViewById(R.id.title)).setText("Friends");
            ((ImageView) friendsTabIndicator.findViewById(R.id.image)).setImageResource(R.drawable.icon_friends_tab);
            // Add view to friends tab
            friendsTab.setIndicator(friendsTabIndicator);
            Intent friendsIntent = new Intent(this, FriendsActivity.class);
            friendsTab.setContent(friendsIntent);
            tabHost.addTab(friendsTab);
        }
        else if(activityName.equals("Map")) {

            // Map Tab
            TabSpec mapTab = tabHost.newTabSpec("Map");
            // Map Tab Indicator View
            View mapTabIndicator = LayoutInflater.from(this).inflate(R.layout.bottom_tab, getTabWidget(), false);
            ((TextView) mapTabIndicator.findViewById(R.id.title)).setText("Map");
            ((ImageView) mapTabIndicator.findViewById(R.id.image)).setImageResource(R.drawable.icon_map_tab);
            // Add view to map tab
            mapTab.setIndicator(mapTabIndicator);
            Intent mapIntent = new Intent(this, MapActivity.class);
            if(friendID!=null){
                mapIntent.putExtra("FriendID", friendID);
            }
            mapTab.setContent(mapIntent);
            tabHost.addTab(mapTab);
        }
        else if(activityName.equals("Settings")) {

            // Settings Tab
            TabSpec settingsTab = tabHost.newTabSpec("Settings");
            // Settings Tab Indicator View
            View settingsTabIndicator = LayoutInflater.from(this).inflate(R.layout.bottom_tab, getTabWidget(), false);
            ((TextView) settingsTabIndicator.findViewById(R.id.title)).setText("Settings");
            ((ImageView) settingsTabIndicator.findViewById(R.id.image)).setImageResource(R.drawable.icon_settings_tab);
            // Add view to settings tab
            settingsTab.setIndicator(settingsTabIndicator);
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            settingsTab.setContent(settingsIntent);
            tabHost.addTab(settingsTab);
        }

        if(friendID!=null){
            tabHost.setCurrentTab(1);
        }
    }
}