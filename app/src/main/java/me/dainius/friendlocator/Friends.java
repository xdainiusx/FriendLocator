package me.dainius.friendlocator;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Friends table from the database
 */
@ParseClassName("Friends")
public class Friends extends ParseObject {

    /**
     * getUser()
     * @return ParseUser
     */
    public ParseUser getUser() {
        return getParseUser("user");
    }

    /**
     * setUser()
     * @param value
     */
    public void setUser(ParseUser value) {
        put("user", value);
    }

    /**
     * getUsersFriend()
     * @return ParseUser
     */
    public ParseUser getUsersFriend() {
        return getParseUser("usersFriend");
    }

    /**
     * setUsersFriend
     * @param value
     */
    public void setUsersFriend(ParseUser value) {
        put("usersFriend", value);
    }

    /**
     * getStatus() - get invitation status
     * 1 - pending
     * 2 - accepted
     * 3 - declined
     * @return int status code
     */
    public int getStatus() {
        return getInt("status");
    }

    /**
     * setStatus()
     * @param value
     */
    public void setStatus(int value) {
        put("status", value);
    }

    /**
     * getName()
     * @return String
     */
    public String getName() {
        return getString("name");
    }

    /**
     * getEmail()
     * @return String
     */
    public String getEmail() {
        return getString("email");
    }

    /**
     * getQuery()
     * @return ParseQuery<Friends>
     */
    public static ParseQuery<Friends> getQuery() {

        return ParseQuery.getQuery(Friends.class);
    }
}