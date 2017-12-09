package com.george.snow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.Display;
import android.widget.Toast;


public class ConnectionChangeReceiver extends BroadcastReceiver {


    private static final String START_TIME = "start_time";
    private static final String STOP_TIME = "stop_time";

    Context context;

    public ConnectionChangeReceiver() {
    }

    public ConnectionChangeReceiver(Context context) {
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //Getting instance of Cronometer class
        Cronometer cronometerVariable = new Cronometer(context);
        MainActivity mainActivity = new MainActivity();
        SecondActivity secondActivity = new SecondActivity();

        //Checking internet availability
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        /*PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();*/

        if (isConnected) {
            Toast.makeText(context, "Active WiFi", Toast.LENGTH_LONG).show();
            long stopTime = System.currentTimeMillis();
            long startTime = System.currentTimeMillis();

            //Using SharedPreferences to save stop/start time
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            //Setting also start time in order to catch a bug where time was calculated if user closed screen when app was running
            editor.putLong(START_TIME, startTime);
            editor.putLong(STOP_TIME, stopTime);
            editor.apply();


            //Check if app is in foreground
            if (mainActivity.isActive == true || secondActivity.isActive == true) {
                cronometerVariable.getStopTime();

                cronometerVariable.sendOnLine();
            }

        } else {
            Toast.makeText(context, "NOT Active WiFi", Toast.LENGTH_LONG).show();
            long startTime = System.currentTimeMillis();
            //Using SharedPreferences to save start time

            if (isScreenOn(context) == false) {
                return;
            }
            if (mainActivity.isActive == true || secondActivity.isActive == true) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(START_TIME, startTime);
                editor.apply();
            }
        }
    }

    public boolean isScreenOn(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }
}
