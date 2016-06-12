package com.devtau.velopatifon.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.devtau.velopatifon.MyBroadcastReceiver;
import com.devtau.velopatifon.R;
import com.devtau.velopatifon.services.SoundPoolService;
import com.devtau.velopatifon.services.SpeedometerService;
import com.devtau.velopatifon.util.Util;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener{
    private EditText etTargetSpeed, etDiffNormal;
    private ToggleButton btnTracking, tbtnPlayer;
    private LinearLayout speedViews, speedDiffViews, targetSpeedAndDiffNormal, rateButtons;

    private Intent speedometerIntent;
    private boolean isTracking = false;
    public static boolean isPlaying = false;
    private MyBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initControls();
        setBtnClickListeners();
        addBroadcastReceiver();
    }

    private void initControls() {
        etTargetSpeed = (EditText) findViewById(R.id.etTargetSpeed);
        etDiffNormal = (EditText) findViewById(R.id.etDiffNormal);

        btnTracking = (ToggleButton) findViewById(R.id.btnTracking);
        tbtnPlayer = (ToggleButton) findViewById(R.id.btnPlayer);

        speedViews = (LinearLayout) findViewById(R.id.speedViews);
        speedDiffViews = (LinearLayout) findViewById(R.id.speedDiffViews);
        targetSpeedAndDiffNormal = (LinearLayout) findViewById(R.id.targetSpeedAndDiffNormal);
        rateButtons = (LinearLayout) findViewById(R.id.rateButtons);
    }

    private void setBtnClickListeners() {
        btnTracking.setOnClickListener(this);
        tbtnPlayer.setOnClickListener(this);
        (findViewById(R.id.rate050)).setOnClickListener(this);
        (findViewById(R.id.rate100)).setOnClickListener(this);
        (findViewById(R.id.rate150)).setOnClickListener(this);
        (findViewById(R.id.rate200)).setOnClickListener(this);
    }

    private void addBroadcastReceiver() {
        receiver = new MyBroadcastReceiver();
        registerReceiver(receiver, new IntentFilter(SpeedometerService.TAG));
    }

    @Override
    public void onClick(View button) {
        float rate = 0;
        switch (button.getId()) {
            case R.id.rate050:
                rate = 0.5f;
                break;
            case R.id.rate100:
                rate = 1.0f;
                break;
            case R.id.rate150:
                rate = 1.5f;
                break;
            case R.id.rate200:
                rate = 2.0f;
                break;

            case R.id.btnTracking:
                changeTrackingStatus();
                break;

            case R.id.btnPlayer:
                if (!isPlaying) {
                    startService(new Intent(this, SoundPoolService.class));
                    isPlaying = true;
                } else {
                    stopService(new Intent(this, SoundPoolService.class));
                    isPlaying = false;
                }
                tbtnPlayer.setChecked(isPlaying);
                Util.groupSetEnabled(rateButtons, isPlaying);
                break;
        }
        if (isPlaying && rate != 0) {
            SoundPoolService.soundPool.setRate(SoundPoolService.streamId, rate);
        }
    }

    private void changeTrackingStatus() {
        if (!isTracking) {
            if ("".equals(etTargetSpeed.getText().toString()) || "".equals(etDiffNormal.getText().toString())) {
                Toast.makeText(getApplicationContext(), "заполните нужную скорость\nи порог", Toast.LENGTH_SHORT).show();
                btnTracking.setChecked(false);
            } else {
                //очистим поля вывода
                Util.groupSetText(speedViews, "___");
                Util.groupSetText(speedDiffViews, "___");

                //подготовим данные для запуска службы
                double targetSpeed = Double.parseDouble(etTargetSpeed.getText().toString());
                double diffNormal = Double.parseDouble(etDiffNormal.getText().toString());

                //соберем подготовленные данные вместе и запустим службу
                speedometerIntent = new Intent(this, SpeedometerService.class);
                speedometerIntent.putExtra(SpeedometerService.EXTRA_TARGET_SPEED, targetSpeed);
                speedometerIntent.putExtra(SpeedometerService.EXTRA_DIFF_NORMAL, diffNormal);
                startService(speedometerIntent);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(btnTracking.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                btnTracking.setChecked(true);
                isTracking = true;
            }
        } else {
            if (speedometerIntent != null) {
                stopService(speedometerIntent);
            }
            btnTracking.setChecked(false);
            isTracking = false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onDestroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, SoundPoolService.class));
        if (speedometerIntent != null) {
            stopService(speedometerIntent);
        }
        unregisterReceiver(receiver);
    }
}