package me.dainius.friendlocator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Set;

/**
 * Receiver - notifications for Parse
 */
public class Receiver extends ParsePushBroadcastReceiver {

    private static String ACTIVITY = "Receiver";

    /**
     * onPushOpen()
     * @param context
     * @param intent
     */
    @Override
    public void onPushOpen(Context context, Intent intent) {
        Log.e(ACTIVITY, "On PushOpen Clicked");

        Intent i = new Intent(context, MainActivity.class);
        Integer status = null;
        try {
            status = this.getStatus(intent);
        } catch (Exception e) {
            Log.d(ACTIVITY, "No status inside JSON: " + e);
            status = 0;
        }
        if(status == 3) {
            Log.d(ACTIVITY, "DECLINED!!!");
        }
        else {
            Log.d(ACTIVITY, "ACCEPTED!!!");
            i.putExtras(intent.getExtras());
            i.putExtra("invitor", this.getInvitor(intent));
            i.putExtra("pushReceived", false);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(i);
        }
    }

    /**
     * onPushReceive()
     * @param context
     * @param intent
     */
    public void onPushReceive(Context context, Intent intent) {
        Log.e(ACTIVITY, "On PushReceive Launched");

        Intent i = new Intent(context, MainActivity.class);
        Integer status = null;
        try {
            status = this.getStatus(intent);
        } catch (Exception e) {
            Log.d(ACTIVITY, "No status inside JSON: " + e);
            status = 0;
        }
        if(status == 3) {
            Log.d(ACTIVITY, "DECLINED!!!");
            i.putExtras(intent.getExtras());
            i.putExtra("invitor", this.getInvitor(intent));
            i.putExtra("pushReceived", false);
            i.putExtra("declined", true);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(i);
        }
        else if(status == 1) {
            Log.d(ACTIVITY, "PENDING!!!");
            i.putExtras(intent.getExtras());
            i.putExtra("invitor", this.getInvitor(intent));
            i.putExtra("pushReceived", false);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(i);
        }
        else if(status == 2) {
            Log.d(ACTIVITY, "CONNECTED!!!");
            i.putExtras(intent.getExtras());
            i.putExtra("invitor", this.getInvitor(intent));
            i.putExtra("pushReceived", true);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(i);
        }
    }

    /**
     * getInvitor()
     * @param i
     * @return
     */
    private String getInvitor(Intent i){

        String json = i.getExtras().getString("com.parse.Data");
        String email = null;
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(json);
            email = jsonObj.getString("invitor");
        } catch(JSONException e) {
            Log.d(ACTIVITY, "Error parsing: " + e);
            email = "";
        }

        Log.d(ACTIVITY, "Email received in json: " + email);

        return email;
    }

    /**
     * getStatus()
     * @param i
     * @return
     */
    private Integer getStatus(Intent i){

        String json = i.getExtras().getString("com.parse.Data");
        Integer status = null;
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(json);
            status = jsonObj.getInt("connectionStatus");
        } catch(JSONException e) {
            Log.d(ACTIVITY, "Error parsing: " + e);
            status = null;
        }

        Log.d(ACTIVITY, "Status received in json: " + status);

        return status;
    }

    /**
     * dumpIntent() - used for debugging
     * @param i
     */
    private static void dumpIntent(Intent i){
        Bundle bundle = i.getExtras();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            Log.e(ACTIVITY,"Dumping Intent start");
            while (it.hasNext()) {
                String key = it.next();
                Log.e(ACTIVITY,"[" + key + "=" + bundle.get(key)+"]");
            }
            Log.e(ACTIVITY,"Dumping Intent end");
        }
    }
}