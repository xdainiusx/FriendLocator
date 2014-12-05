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
    private String firstName;
    private String lastName;
    private String email;
    private String image;
    private String status; // pending, active, declined
    private boolean tracking = false;
    private boolean isOnline = false;
    private double lastLatitude;
    private double lastLongitude;


    /**
     * Initializer
     * @param firstName
     * @param lastName
     * @param email
     */
    public Friend(String id, String firstName, String lastName, String email) {
        if(objectId==null)
            this.objectId = "";
        else
            this.objectId = id;
        if(firstName==null)
            this.firstName = "";
        else
            this.firstName = firstName;
        if(lastName==null)
            this.lastName = "";
        else
            this.lastName = lastName;
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
                this.firstName = this.friendUser.getString("name");
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
     * GETTERS
     */

    /**
     * getId() - getter
     * @return int id
     */
    public String getId() {
        return this.objectId;
    }

    /**
     * getFirstName() - getter
     * @return String firstName
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * getLastName() - getter
     * @return String lastName
     */
    public String getLastName() {
        return this.lastName;
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
    public String getStatus() {
        return this.status;
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
     * SETTERS
     */

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
        return this.firstName.compareTo(compareFriend.firstName);
    }

}
