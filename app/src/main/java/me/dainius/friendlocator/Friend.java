package me.dainius.friendlocator;

/**
 * Friend Model
 */
public class Friend implements Comparable<Friend>{

    private String objectId;
    private String firstName;
    private String lastName;
    private String email;
    private String image;
    private String status; // pending, active, declined
    private Boolean tracking = false;
    private double lastLatitude;
    private double lastLongitude;


    /**
     * Initializer
     * @param firstName
     * @param lastName
     * @param email
     */
    public Friend(String id, String firstName, String lastName, String email) {
        this.objectId = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
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
    public Boolean isTrackingEnabled() {
        return this.tracking;
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
