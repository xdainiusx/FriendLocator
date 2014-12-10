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

public class Receiver extends ParsePushBroadcastReceiver {

    private static String ACTIVITY = "Receiver";

    @Override
    public void onPushOpen(Context context, Intent intent) {
        Log.e(ACTIVITY, "Clicked");

        Intent i = new Intent(context, MainActivity.class);
        String status = null;
        try {
            status = this.getStatus(i);
        } catch (Exception e) {
            Log.d(ACTIVITY, "No status inside JSON: " + e);
            status = "0";
        }
        if(status.equals("3")) {
            Log.d(ACTIVITY, "DECLINED!!!");
        }
        else {
            Log.d(ACTIVITY, "ACCEPTED!!!");
            i.putExtras(intent.getExtras());
            i.putExtra("invitor", this.getInvitor(intent));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(i);
        }

    }

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

    private String getStatus(Intent i){

        String json = i.getExtras().getString("com.parse.Data");
        String status = null;
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(json);
            status = jsonObj.getString("connectionStatus");
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