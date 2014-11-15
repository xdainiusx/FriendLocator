package me.dainius.friendlocator;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * InvitationPendingListViewAdapter
 */
public class InvitationPendingListViewAdapter extends ArrayAdapter<String> {

    private static String ADAPTER = "InvitationPendingListViewAdapter";
    private final Context context;
    private final String[] pendingInvites;

    /**
     * Initializer
     * @param context
     * @param pendingInvites
     */
    public InvitationPendingListViewAdapter(Context context, String[] pendingInvites) {
        super(context, R.layout.activity_friends_invitation_pending_item, pendingInvites);
        this.context = context;
        this.pendingInvites = pendingInvites;
    }

    /**
     * getView
     * @param position
     * @param convertView
     * @param parent
     * @return View row
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.activity_friends_invitation_pending_item, parent, false);

        TextView textViewFriendEmail = (TextView) rowView.findViewById(R.id.friendEmail);
        textViewFriendEmail.setText(this.pendingInvites[position]);

        String email = this.pendingInvites[position];

        Log.d(ADAPTER, "Email: " + email);

        return rowView;
    }

    /**
     * getItem
     * @param position
     * @return Friend[] object
     */
    public String getItem(int position){
        return this.pendingInvites[position];
    }
}