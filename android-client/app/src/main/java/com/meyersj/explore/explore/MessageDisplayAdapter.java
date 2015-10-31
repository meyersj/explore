package com.meyersj.explore.explore;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.meyersj.explore.R;

import java.util.ArrayList;

public class MessageDisplayAdapter extends ArrayAdapter<MessageDisplay> {

    private final String TAG = getClass().getCanonicalName();

    public MessageDisplayAdapter(Context context) {
        super(context, 0, new ArrayList<MessageDisplay>());
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        MessageDisplay messageDisplay = getItem(position);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.message_list_view, parent, false);
        }
        TextView dateText = (TextView) view.findViewById(R.id.date);
        TextView usernameText = (TextView) view.findViewById(R.id.username);
        TextView messageText = (TextView) view.findViewById(R.id.message);
        dateText.setText(messageDisplay.getTimestamp());
        usernameText.setText(messageDisplay.getUsername());
        messageText.setText(messageDisplay.getMessage());
        return view;
    }

}