package com.devtau.velopatifon.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import java.util.List;
import com.devtau.velopatifon.R;
import com.devtau.velopatifon.activities.MainActivity;

public class SoundPoolService extends Service {
    public static int streamId = 0, soundId = 0;
    public static SoundPool soundPool;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= 21) {
            soundPool = new SoundPool.Builder().build();
        } else {
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        if (streamId == 0) {
            soundId = soundPool.load(this, R.raw.heart_beat, 1);
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    streamId = soundPool.play(soundId, 1, 1, 1, -1, 1f);
                    soundPool.setLoop(streamId, -1);
                    MainActivity.isPlaying = true;
                }
            });
        }
        MainActivity.isPlaying = true;
        Toast.makeText(this, "Плеер создан", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Плеер запущен", Toast.LENGTH_SHORT).show();
        soundPool.resume(streamId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //сначала снимем лог всех запущенных Service
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(50);
        for (int i = 0; i < rs.size(); i++) {
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            Log.i("Service List", "Process " + rsi.process + " with component " + rsi.service.getClassName());
        }
        //после этого завершим текущий Service
        soundPool.release();
        Toast.makeText(this, "Плеер остановлен", Toast.LENGTH_SHORT).show();
        soundPool.stop(streamId);
        streamId = 0;
    }
}
