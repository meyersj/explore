package com.meyersj.tracker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.inputmethod.InputMethodManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;


public class Utils {

    //read properties from config file in assets
    private static Properties getProperties(Context context, String filename) {
        Properties properties = null;

        try {
            InputStream inputStream = context.getResources().getAssets().open(filename);
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
        }
        return properties;
    }

    public static String getHost(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String host = sharedPref.getString(Cons.HOST, "");
        if (host.isEmpty()) {
            Properties properties = getProperties(context, Cons.CONFIG_FILE);
            host = properties.getProperty(Cons.HOST);
            sharedPref.edit().putString(Cons.HOST, host).apply();
        }
        return host;
    }

    public static Integer getPort(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String port = sharedPref.getString(Cons.PORT, "");
        if (port.isEmpty()) {
            Properties properties = getProperties(context, Cons.CONFIG_FILE);
            port = properties.getProperty(Cons.PORT);
            sharedPref.edit().putString(Cons.PORT, port).apply();
        }
        return Integer.parseInt(port);
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

}
