package me.dainius.friendlocator;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("FriendInvitation")
public class FriendInvitation extends ParseObject {

    public String getInviter() {
        return getString("inviter");
    }

    public void setInviter(String value) {
        put("inviter", value);
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser value) {
        put("user", value);
    }

    public String getFriend() {
        return getString("friend");
    }

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

    public void setStatus(int value) {
        put("status", value);
    }

    public static ParseQuery<FriendInvitation> getQuery() {
        return ParseQuery.getQuery(FriendInvitation.class);
    }
}