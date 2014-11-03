package me.dainius.friendlocator;

import android.app.ActionBar;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
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

        //TabHost tabHost = getTabHost();
        TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);

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

        // Map Tab
        TabSpec mapTab = tabHost.newTabSpec("Map");
        // Map Tab Indicator View
        View mapTabIndicator = LayoutInflater.from(this).inflate(R.layout.bottom_tab, getTabWidget(), false);
        ((TextView) mapTabIndicator.findViewById(R.id.title)).setText("Map");
        ((ImageView) mapTabIndicator.findViewById(R.id.image)).setImageResource(R.drawable.icon_map_tab);
        // Add view to map tab
        mapTab.setIndicator(mapTabIndicator);
        Intent mapIntent = new Intent(this, MapActivity.class);
        mapTab.setContent(mapIntent);

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

        // Adding all TabSpec to TabHost
        tabHost.addTab(friendsTab);
        tabHost.addTab(mapTab);
        tabHost.addTab(settingsTab);
    }
}