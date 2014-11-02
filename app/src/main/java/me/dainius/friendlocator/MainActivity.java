package me.dainius.friendlocator;

import android.app.ActionBar;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

/**
 * Main Activity - user comes here after login
 */
public class MainActivity extends TabActivity {

    private ActionBar actionBar;

    /**
     * onCreate()
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        this.actionBar = getActionBar();

        //TabHost tabHost = getTabHost();
        TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);
        Resources res = getResources();

        // Friends Tab
        TabSpec friendsTab = tabHost.newTabSpec("Friends");
        friendsTab.setIndicator("", res.getDrawable(R.drawable.icon_friends_tab));
        Intent friendsIntent = new Intent(this, FriendsActivity.class);
        friendsTab.setContent(friendsIntent);

        // Map Tab
        TabSpec mapTab = tabHost.newTabSpec("Map");
        mapTab.setIndicator("", res.getDrawable(R.drawable.icon_map_tab));
        Intent mapIntent = new Intent(this, MapActivity.class);
        mapTab.setContent(mapIntent);

        // Settings Tab
        TabSpec settingsTab = tabHost.newTabSpec("Settings");
        settingsTab.setIndicator("", res.getDrawable(R.drawable.icon_settings_tab));
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        settingsTab.setContent(settingsIntent);

        // Adding all TabSpec to TabHost
        tabHost.addTab(friendsTab);
        tabHost.addTab(mapTab);
        tabHost.addTab(settingsTab);
    }
}