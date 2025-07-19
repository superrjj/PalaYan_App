package com.example.palayan.Helper.AppHelper;

import android.content.Context;
import android.provider.Settings;

public class DeviceUtils {
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
