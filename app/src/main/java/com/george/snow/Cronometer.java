package com.george.snow;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by farmaker1 on 25/11/2017.
 */

public class Cronometer {

    Context context;
    private static final String TIME_ADDED = "time_added";
    private static final String START_TIME = "start_time";
    private static final String STOP_TIME = "stop_time";
    private long summary = 0;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;

    long startTime = 0;
    long stopTime = 0;

    public Cronometer(Context context) {
        this.context = context;
    }

    public void getStopTime() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long lon = sharedPreferences.getLong(STOP_TIME, 0);
        long lonstart = sharedPreferences.getLong(START_TIME, 0);

        startTime = lonstart;
        stopTime = lon;

        Log.e("StopTimeCronometer", Long.toString(lon));

        Long difference = stopTime - startTime;

        Log.e("Difference", Long.toString(difference));

        millisInTime(difference);
        checkTime(difference);
    }

    public void setStartTime() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long lon = sharedPreferences.getLong(START_TIME, 0);

        startTime = lon;
        String startValue = Long.toString(startTime);
        Log.e("StartTimeCronometer", startValue);

    }

    public void millisInTime(Long lon) {

        long timeElapsed = lon;
        int hours = (int) (timeElapsed / 3600000);
        int minutes = (int) (timeElapsed - hours * 3600000) / 60000;
        int seconds = (int) (timeElapsed - hours * 3600000 - minutes * 60000) / 1000;

        String timeVisible = String.valueOf(hours) + ":" + String.valueOf(minutes) + ":" + String.valueOf(seconds);
        Log.e("TIME", timeVisible);

    }

    //Method to check what time it is already written in SharedPreferences
    public void checkTime(Long lon) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean contains = sharedPreferences.contains(TIME_ADDED);
        long checkedLong = sharedPreferences.getLong(TIME_ADDED, 0);

        if (contains && checkedLong != 0) {
            summary = checkedLong + lon;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(TIME_ADDED, summary);
            editor.apply();
        } else if (contains && checkedLong == 0) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(TIME_ADDED, lon);
            editor.apply();
        }

        String incomi = Long.toString(sharedPreferences.getLong(TIME_ADDED, 0));
        Log.e("TimeTotal", incomi);
    }

    //Method to send total time onine to Firebase Database
    public void sendOnLine() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean contains = sharedPreferences.contains(TIME_ADDED);
        long checkedLong = sharedPreferences.getLong(TIME_ADDED, 0);

        if (contains && checkedLong != 0) {

            long timeElapsed = checkedLong;
            int hours = (int) (timeElapsed / 3600000);
            int minutes = (int) (timeElapsed - hours * 3600000) / 60000;
            int seconds = (int) (timeElapsed - hours * 3600000 - minutes * 60000) / 1000;

            String timeToSend = String.valueOf(hours) + ":" + String.valueOf(minutes) + ":" + String.valueOf(seconds);
            Log.e("timeToFirebase", timeToSend);


            //Instantiating the database..access point of the database reference
            mFirebaseDatabase = FirebaseDatabase.getInstance();

            //making the references
            mMessagesDatabaseReference = mFirebaseDatabase.getReference();

            mMessagesDatabaseReference.child("Timecollected").push().setValue(getTheDateTime() +","+timeToSend+","+ getDeviceInfo());

            //After sending the total time we set time to zero
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(TIME_ADDED, 0);
            editor.apply();

        } else {
            return;
        }

    }

    private String getTheDateTime() {
        DateFormat df = new SimpleDateFormat("d MMM");
        String date = df.format(Calendar.getInstance().getTime());
        return date;
    }

    private String getDeviceInfo(){
        return android.os.Build.DEVICE + "-" +android.os.Build.MODEL;
    }

}
