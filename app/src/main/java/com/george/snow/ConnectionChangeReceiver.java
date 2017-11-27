package com.george.snow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;


public class ConnectionChangeReceiver extends BroadcastReceiver {


    private static final String START_TIME = "start_time";
    private static final String STOP_TIME = "stop_time";

    Context context;
    public ConnectionChangeReceiver(){}

    public ConnectionChangeReceiver(Context context){
        this.context=context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //Getting instance of Cronometer class
        Cronometer cronometerVariable = new Cronometer(context);

        //Checking internet availability
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected){
            Toast.makeText(context, "Active WiFi" , Toast.LENGTH_LONG).show();
            long stopTime = System.currentTimeMillis();

            //Using SharedPreferences to save stop time
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(STOP_TIME,stopTime);
            editor.apply();

            cronometerVariable.getStopTime();

            cronometerVariable.sendOnLine();
        }else{
            Toast.makeText(context, "NOT Active WiFi", Toast.LENGTH_LONG).show();
            long startTime = System.currentTimeMillis();

            //Using SharedPreferences to save start time
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(START_TIME,startTime);
            editor.apply();

            cronometerVariable.setStartTime();
        }

    }
}
