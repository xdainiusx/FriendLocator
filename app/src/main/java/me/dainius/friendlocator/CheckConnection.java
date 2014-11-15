package me.dainius.friendlocator;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class CheckConnection {

    /**
     * checkInternetConnection()
     */
    public static boolean checkInternetConnection(Context context) {
        boolean status = false;
        ConnectivityManager check = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(check != null) {
            NetworkInfo[] info = check.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i <info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        Toast.makeText(context, "Connection to the server: Available", Toast.LENGTH_SHORT).show();
                        return status;
                    }
                }

        }
        else {
            Toast.makeText(context, "Connection to the server: Unavailable", Toast.LENGTH_SHORT).show();
        }
        return status;
    }
}
