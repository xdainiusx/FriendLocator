package me.dainius.friendlocator;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("ActiveConnection")
public class ActiveConnection extends ParseObject {

    /**
     * setInvitorEmail()
     * @param value
     */
    public void setInvitorEmail(String value) {
        put("invitorEmail", value);
    }

    /**
     * getInvitorEmail()
     * @return String
     */
    public String getInvitorEmail() {
        return getString("invitorEmail");
    }

    /**
     * setFriendEmail()
     * @param value
     */
    public void setFriendEmail(String value) {
        put("friendEmail", value);
    }

    /**
     * getFriendEmail()
     * @return String
     */
    public String getFriendEmail() {
        return getString("friendEmail");
    }

    /**
     * setStatus()
     * @param value
     */
    public void setStatus(int value) {
        put("status", value);
    }

    /**
     * getStatus() - get invitation status
     * 1 - pending
     * 2 - connected
     * 3 - declined
     * @return int status code
     */
    public int getStatus() {
        return getInt("status");
    }


}
