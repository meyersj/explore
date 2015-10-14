package com.meyersj.tracker;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by jeff on 10/14/15.
 */
public class Utils {

    //read properties from config file in assets
    public static Properties getProperties(Context context, String filename) {
        Properties properties = null;

        try {
            InputStream inputStream = context.getResources().getAssets().open(filename);
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
        }
        return properties;
    }

    // return hex string for array of bytes
    public static String getHexString(byte[] rawBytes) {
        String hexString = "";
        for(int i=0; i < rawBytes.length; i++) {
            hexString += String.format("%2s", Integer.toHexString(rawBytes[i] & 0xFF)).replace(' ', '0') + " ";
        }
        return hexString;
    }

}
