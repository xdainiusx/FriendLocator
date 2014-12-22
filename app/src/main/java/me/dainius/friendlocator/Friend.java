package me.dainius.friendlocator;

import android.util.Log;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Friend Model
 */
public class Friend implements Comparable<Friend>{

    private static String MODEL = "Friend";
    private ParseUser friendUser;
    private String objectId = null;
    private String name;
    private String email;
    private String image;
    private int status; // pending, active, declined
    private boolean tracking = false;
    private boolean isOnline = false;
    private double lastLatitude;
    private double lastLongitude;

    /**
     * Initializer
     * @param name
     * @param email
     */
    public Friend(String id, String name, String email) {
        if(objectId==null)
            this.objectId = "";
        else
            this.objectId = id;
        if(name==null)
            this.name = "";
        else
            this.name = name;
        if(email==null)
            this.email = "";
        else
            this.email = email;

    }

    /**
     * Initializer
     * @param email
     */
    public Friend(String email) {
        this.email = email;
        if(this.objectId==null){
            this.friendUser = this.getUserByEmail(email);
            if(this.friendUser!=null) {
                this.objectId = this.friendUser.getObjectId();
                this.name = this.friendUser.getString("name");
                this.email = this.friendUser.getEmail();
                this.isOnline = this.friendUser.getBoolean("isOnline");
                this.lastLatitude = this.friendUser.getDouble("latitude");
                this.lastLongitude = this.friendUser.getDouble("longitude");
            }
        }
    }

    /**
     * getUserByEmail() - Assigns user to this.user so we don't do the same query again
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
            Log.d(MODEL, e.getLocalizedMessage());
        }
        return user;
    }

    /**
     * getId() - getter
     * @return int id
     */
    public String getId() {
        return this.objectId;
    }

    /**
     * getName() - getter
     * @return String name
     */
    public String getName() {
        return this.name;
    }

    /**
     * getEmail() - getter
     * @return String email
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * getStatus() - getter
     * @return
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * setStatus()
     * @param status
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * isTrackingEnabled() - getter
     * @return
     */
    public boolean isTrackingEnabled() {
        return this.tracking;
    }

    /**
     * isOnline() - getter
     * @return
     */
    public boolean isOnline() {
        return this.isOnline;
    }

    /**
     *  setIsOnline()
     * @param status
     */
    public void setIsOnline(boolean status) {

        this.isOnline = status;
    }

    /**
     * getCoordinateArray()
     * @return double [] - [0]=latitude, [1]=longitude
     */
    public double[] getCoordinateArray() {
            double[] coordinates = new double[2];
            coordinates[1] = this.lastLatitude;
            coordinates[0] = this.lastLongitude;
            return coordinates;
    }

    /**
     * enableTracking() - enable friend tracking
     */
    public void enableTracking() {
        this.tracking = true;
    }

    /**
     * disableTracking() - disable friend tracking
     */
    public void disableTracking() {
        this.tracking = false;
    }

    /**
     * setCoordinates() - set friend's coordinates
     * @param latitude
     * @param longitude
     */
    public void setCoordinates(double latitude, double longitude) {
        this.lastLatitude = latitude;
        this.lastLongitude = longitude;
    }

    /**
     * compareTo() - user for sorting of Friend objects by first name
     * @param compareFriend
     * @return int
     */
    public int compareTo(Friend compareFriend) {
        return this.name.compareTo(compareFriend.name);
    }

    /**
     * toString()
     * @return String
     */
    public String toString() {
        return this.getName() + ": " + this.getEmail();
    }

}
