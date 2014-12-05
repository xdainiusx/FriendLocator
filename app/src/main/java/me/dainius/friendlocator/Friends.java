package me.dainius.friendlocator;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("Friends")
public class Friends extends ParseObject {

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser value) {
        put("user", value);
    }

    public ParseUser getUsersFriend() {
        return getParseUser("usersFriend");
    }

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

    public void setStatus(int value) {
        put("status", value);
    }

    public String getName() {
        return getString("name");
    }

    public String getEmail() {
        return getString("email");
    }

    public static ParseQuery<Friends> getQuery() {

        return ParseQuery.getQuery(Friends.class);
    }

}