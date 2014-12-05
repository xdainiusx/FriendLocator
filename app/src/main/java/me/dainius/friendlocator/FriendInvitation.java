package me.dainius.friendlocator;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * FriendInvitation class
 */
@ParseClassName("FriendInvitation")
public class FriendInvitation extends ParseObject {

    /**
     * getInviter()
     * @return String
     */
    public String getInviter() {
        return getString("inviter");
    }

    /**
     * setInviter()
     * @param value
     */
    public void setInviter(String value) {
        put("inviter", value);
    }

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
     * getFriend()
     * @return String
     */
    public String getFriend() {
        return getString("friend");
    }

    /**
     * setFriend()
     * @param value
     */
    public void setFriend(String value) {
        put("friend", value);
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
     * getQuery()
     * @return ParseQuery<FriendInvitation>
     */
    public static ParseQuery<FriendInvitation> getQuery() {
        return ParseQuery.getQuery(FriendInvitation.class);
    }
}