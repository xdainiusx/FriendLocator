package me.dainius.friendlocator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * InvitationPendingListViewAdapter
 */
public class InvitationPendingListViewAdapter extends ArrayAdapter<String> {

    private static String ADAPTER = "InvitationPendingListViewAdapter";
    private final FriendsInvitationPendingActivity context;
    private final String[] pendingInvites;

    /**
     * Initializer
     * @param context
     * @param pendingInvites
     */
    public InvitationPendingListViewAdapter(FriendsInvitationPendingActivity context, String[] pendingInvites) {
        super(context, R.layout.activity_friends_invitation_pending_item, pendingInvites);
        this.context = context;
        this.pendingInvites = pendingInvites;
        this.setNotifyOnChange(true);
    }

    /**
     * RowViewHolder - inner class
     */
    protected static class RowViewHolder {
        public TextView email;
        public Button acceptButton;
        public Button declineButton;
    }

    /**
     * getView()
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

        RowViewHolder holder = new RowViewHolder();
        holder.email = (TextView) rowView.findViewById(R.id.friendEmail);
        holder.acceptButton = (Button) rowView.findViewById(R.id.buttonAccept);
        holder.declineButton = (Button) rowView.findViewById(R.id.buttonDecline);
        holder.acceptButton.setOnClickListener(this.context.acceptClickListener);  // listener in the parent
        holder.declineButton.setOnClickListener(this.context.declineClickListener);// listener in the parent
        rowView.setTag(holder);

        return rowView;
    }

    /**
     * getItem()
     * @param position
     * @return String object
     */
    public String getItem(int position){
        return this.pendingInvites[position];
    }

}