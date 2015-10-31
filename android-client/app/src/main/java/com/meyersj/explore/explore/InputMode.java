package com.meyersj.explore.explore;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.widget.EditText;
import android.widget.ImageView;

import com.meyersj.explore.R;

/**
 * Created by jeff on 10/30/15.
 */
public class InputMode {

    public static final String MESSAGE = "message";
    public static final String REGISTER = "register";
    private Context context;
    private ImageView icon;
    private EditText editText;
    private String mode;


    public InputMode(Context context, ImageView icon, EditText editText) {
        this.context = context;
        this.icon = icon;
        this.editText = editText;
        mode = "";
    }

    public void activateMessage() {
        mode = MESSAGE;
        icon.setImageDrawable(context.getDrawable(R.drawable.ic_chat_white_24dp));
        editText.setHint("Enter a message");

    }

    public void activateRegister() {
        mode = REGISTER;
        icon.setImageDrawable(context.getDrawable(R.drawable.ic_location_city_white_24dp));
        editText.setHint("Enter a location name");
    }

    public String getMode() {
        return mode;
    }


}
