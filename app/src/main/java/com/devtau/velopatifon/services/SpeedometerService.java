package com.devtau.velopatifon.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.devtau.velopatifon.GPSTracker;
import com.devtau.velopatifon.util.Logger;
import com.devtau.velopatifon.util.Util;
import com.devtau.velopatifon.activities.MainActivity;
/**
 * Created by TAU on 12.06.2016.
 */
public class SpeedometerService extends IntentService {
    private Handler handler;
    public static final String TAG = "AsyncSpeedometerService";
    public static final String EXTRA_TARGET_SPEED = "targetSpeed";
    public static final String EXTRA_DIFF_NORMAL = "diffNormal";
    public static final String EXTRA_STEP = "extraStep";
    public static final String EXTRA_CURRENT_SPEED = "extraCurrentSpeed";
    public static final String EXTRA_SPEED_DIFF = "extraSpeedDiff";


    private final static int STEP_IN_SECONDS = 3;
    private double targetSpeed, diffNormal;
    private Location currPoint = new Location("MainActivity");
    private double currentSpeed, speedDiff;
    private Map<Calendar, Location> locationsStorage = new HashMap<>();
    private boolean isStopped;

    public SpeedometerService() {
        super(TAG);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();
        targetSpeed = intent.getDoubleExtra(EXTRA_TARGET_SPEED, 0);
        diffNormal = intent.getDoubleExtra(EXTRA_DIFF_NORMAL, 0);
        isStopped = false;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GPSTracker gpsTracker = new GPSTracker(this);
        if (intent != null) {
            for (int i = 0; !isStopped; i++) {
                Location location = gpsTracker.getLocation();
                int step;
                if (location != null) {
                    Location prevPoint = new Location(currPoint);
                    currPoint.set(location);
                    locationsStorage.put(Calendar.getInstance(), currPoint);
                    if (i > 0) {
                        double distanceValue = Util.calculateDistance(prevPoint, currPoint);
                        currentSpeed = (distanceValue / STEP_IN_SECONDS) * 3.6;
                        currentSpeed = Util.roundResult(currentSpeed, 2);
                        speedDiff = currentSpeed - targetSpeed;
                        speedDiff = Util.roundResult(speedDiff, 2);
                    }
                    if (i >= 5) {
                        step = 5;
                    } else {
                        step = i;
                    }
                } else {
                    step = -1;
                }
                publishProgress(step, currentSpeed, speedDiff);
                try {
                    TimeUnit.SECONDS.sleep(STEP_IN_SECONDS);
                } catch (InterruptedException e) {/*NOP*/}
            }
        }
    }

    private void publishProgress(int step, double currentSpeed, double speedDiff) {
        Intent responseIntent = new Intent();
        responseIntent.setAction(TAG);//тэг, используемый в IntentFilter приемника
        responseIntent.putExtra(EXTRA_STEP, step);
        responseIntent.putExtra(EXTRA_CURRENT_SPEED, currentSpeed);
        responseIntent.putExtra(EXTRA_SPEED_DIFF, speedDiff);
        sendBroadcast(responseIntent);


        String msg;
        if (step == -1) {
            msg = "Вас не видно";
        } else if (step == 0) {
            msg = "Поехали!";
        } else if (currentSpeed < 5.0) {
            msg = "вы остановились";
            if (MainActivity.isPlaying) {
                SoundPoolService.soundPool.setRate(SoundPoolService.streamId, 1.0f);
            }
        } else if (speedDiff < -diffNormal) {
            msg = "очень медленно";
            if (MainActivity.isPlaying) {
                SoundPoolService.soundPool.setRate(SoundPoolService.streamId, 0.5f);
            }
        } else if (speedDiff > diffNormal) {
            msg = "очень быстро";
            if (MainActivity.isPlaying) {
                SoundPoolService.soundPool.setRate(SoundPoolService.streamId, 1.5f);
            }
        } else {
            msg = "едем нормально";
            if (MainActivity.isPlaying) {
                SoundPoolService.soundPool.setRate(SoundPoolService.streamId, 1.0f);
            }
        }
        final String msgResult = msg;
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msgResult, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        Logger.d("SpeedometerService. onDestroy");
        isStopped = true;
    }


    public IBinder onUnBind(Intent arg0) {
        return null;
    }
    public void onPause() { }
    @Override
    public void onLowMemory() { }
}
