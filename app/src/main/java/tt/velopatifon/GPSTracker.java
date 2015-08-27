package tt.velopatifon;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by Tau on 16.08.2015.
 */
public class GPSTracker implements LocationListener {
    String LOG_TAG = "GPSTracker";
    private final Context mContext;
    public boolean isGPSEnabled = false;
    Location location;
    private static final long MIN_DISTANCE_BW_UPDATES = 0; // 0 meters
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 second
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    //Function to get the user's current location
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Log.d(LOG_TAG, "isGPSEnabled: " + String.valueOf(isGPSEnabled));

            if (isGPSEnabled) {
                // if GPS Enabled get lat/long using GPS Services
                location = null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_BW_UPDATES, this);
                if (locationManager != null)
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return location;
    }

    //Function to show settings alert dialog On pressing Settings button will
    public void showGPSOffAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Проблема")
                .setMessage("GPS выключен. Открыть меню настроек?")
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                mContext.startActivity(intent);
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

    public void onLocationChanged(Location location) { }
    public void onProviderDisabled(String provider) { }
    public void onProviderEnabled(String provider) { }
    public void onStatusChanged(String provider, int status, Bundle extras) { }
}
