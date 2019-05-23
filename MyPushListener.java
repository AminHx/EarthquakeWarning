package com.aminhx.earthquakeapp;


import android.app.Service;
import android.content.Intent;
import android.os.PowerManager;

import org.json.JSONException;
import org.json.JSONObject;

import co.ronash.pushe.PusheListenerService;

public class MyPushListener extends PusheListenerService {

    public static double lat;
    public static double lon;
    public static float speed;
    public static String ma;


    public void onMessageReceived(JSONObject customContent, JSONObject pushMessage) {


        PowerManager pm = (PowerManager) getSystemService(Service.POWER_SERVICE);
        boolean screenOn = pm.isScreenOn();

        if(!screenOn) {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,"MyLock");
            wl.acquire(10000);
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock");
            wl_cpu.acquire(10000);
        }

        try {
            String cord;
            cord = customContent.getString("cord");
            String[] output = cord.split("-");

            lat = Double.valueOf(output[0]);
            lon = Double.valueOf(output[1]);
            speed = Float.valueOf(customContent.getString("speed"));
            ma = customContent.getString("ma");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        android.util.Log.i("Pushe","Custom json Message: " + customContent.toString()); //print json to logCat

        Intent i = new Intent();
        i.setClass(this, MapsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

    }}