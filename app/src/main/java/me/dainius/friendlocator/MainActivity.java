package me.dainius.friendlocator;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

/**
 * Main Activity - Main Activity after user login
 */
public class MainActivity extends Activity {

    ActionBar.Tab friendsTab, mapTab, settingsTab;
    Fragment friendsFragment = new FriendsFragment();
    Fragment mapFragment = new MapFragment();
    Fragment settingsFragment = new SettingsFragment();

    /**
     * onCreate()
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getActionBar();

        // Hide Actionbar Icon
        actionBar.setDisplayShowHomeEnabled(false);

        // Hide Actionbar Title
        actionBar.setDisplayShowTitleEnabled(false);

        // Create Actionbar Tabs
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        this.friendsTab = actionBar.newTab().setIcon(R.drawable.icon_friends_tab);
        this.friendsTab = actionBar.newTab().setText("Friends");
        this.mapTab = actionBar.newTab().setText("Map");
        this.settingsTab = actionBar.newTab().setText("Settings");


        this.friendsTab.setTabListener(new TabListener(friendsFragment));
        this.mapTab.setTabListener(new TabListener(mapFragment));
        this.settingsTab.setTabListener(new TabListener(settingsFragment));

        // Add tabs to actionbar
        actionBar.addTab(this.friendsTab);
        actionBar.addTab(this.mapTab);
        actionBar.addTab(this.settingsTab);
    }
}
