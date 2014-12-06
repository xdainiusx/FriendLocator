package me.dainius.friendlocator;

import android.app.Application;
import android.content.Context;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * App - Called before any Activity
 */
public class App extends Application {

    private static Context context;
    private static String ACTIVITY = "App";

    /**
     * Initializer
     */
    public App() {
        context = this;
    }

    /**
     * onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(FriendInvitation.class);
        ParseObject.registerSubclass(Friends.class);
        Parse.initialize(this, "---application id---", "---client key---");
    }

    /**
     * getAppContext()
     * @return Context
     */
    public static Context getAppContext() {
        return App.context;
    }
}