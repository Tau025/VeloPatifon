package com.devtau.velopatifon;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import com.devtau.velopatifon.util.Logger;

/**
 * Created by Tau on 16.08.2015.
 */
public class GPSTracker implements LocationListener {
    private final Context context;
    private static final long MIN_DISTANCE_BW_UPDATES = 0; // 0 meters
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 second


    public GPSTracker(Context context) {
        this.context = context.getApplicationContext();
        getLocation();
    }

    //возвращает текущее положение пользователя
    public Location getLocation() {
        Location location = null;
        LocationManager locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        boolean isGPSEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Logger.d("isGPSEnabled: " + String.valueOf(isGPSEnabled));

        if (isGPSEnabled) {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_BW_UPDATES, this);
            location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else {
            showGPSOffAlert(context);
        }
        return location;
    }

    //показывает диалог-сообщение о том, что GPS-датчик выключен и предлагает открыть настройки
    private void showGPSOffAlert(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Проблема")
                .setMessage("GPS выключен. Открыть меню настроек?")
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                context.startActivity(intent);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                .show();
    }


    @Override
    public void onLocationChanged(Location location) { }

    @Override
    public void onProviderDisabled(String provider) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }
}
