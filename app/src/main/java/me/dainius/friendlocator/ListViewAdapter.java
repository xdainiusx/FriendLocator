package me.dainius.friendlocator;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * ListViewAdapter
 */
public class ListViewAdapter extends ArrayAdapter<Friend> {

    private static String ADAPTER = "ListViewAdapter";
    private final Context context;
    private final Friend[] friends;

    /**
     * Initializer
     * @param context
     * @param friends
     */
    public ListViewAdapter(Context context, Friend[] friends) {
        super(context, R.layout.activity_friends_item, friends);
        this.context = context;
        this.friends = friends;
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

        View rowView = inflater.inflate(R.layout.activity_friends_item, parent, false);

        ImageView imageViewPhoto = (ImageView) rowView.findViewById(R.id.photo);
        if(friends[position]!=null)
            imageViewPhoto.setImageResource(R.drawable.friend_photo);

        TextView textViewFriendName = (TextView) rowView.findViewById(R.id.friendName);
        if(friends[position]!=null)
            textViewFriendName.setText(friends[position].getName());
        TextView textViewFriendEmail = (TextView) rowView.findViewById(R.id.friendEmail);
        ImageView imageViewStatus = (ImageView) rowView.findViewById(R.id.status);
        if(friends[position]!=null) {
            Friend f = friends[position];
            textViewFriendEmail.setText(f.getEmail());
            if(f.isOnline()) {
                imageViewStatus.setImageResource(R.drawable.gps_green);
            }
            else {
                imageViewStatus.setImageResource(R.drawable.gps_red);
            }
        }

        Friend friend = friends[position];

        Log.d(ADAPTER, "FRIEND: " + friend);

        return rowView;
    }

    /**
     * getItem
     * @param position
     * @return Friend[] object
     */
    public Friend getItem(int position){
        Log.d(ADAPTER, "Item at position: " + position);
        return this.friends[position];
    }
}