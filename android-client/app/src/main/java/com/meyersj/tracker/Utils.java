package com.meyersj.tracker;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.io.InputStream;
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

}
