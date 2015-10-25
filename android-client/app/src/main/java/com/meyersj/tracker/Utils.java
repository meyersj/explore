package com.meyersj.tracker;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.inputmethod.InputMethodManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.UUID;

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
            hexString += String.format("%2s", Integer.toHexString(rawBytes[i] & 0xFF)).replace(' ', '0');
        }
        return hexString;
    }

    public static String getDeviceID(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static Socket openSocket(Context context) throws IOException {
        Properties properties = Utils.getProperties(context, "config.properties");
        String host = properties.getProperty("Host");
        Integer port = Integer.valueOf(properties.getProperty("Port"));
        return new Socket(host, port);
    }

}
