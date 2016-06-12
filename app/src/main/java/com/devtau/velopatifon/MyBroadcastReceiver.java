package com.devtau.velopatifon;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.devtau.velopatifon.services.SpeedometerService;
import com.devtau.velopatifon.util.Logger;
/**
 * Created by TAU on 12.06.2016.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {
    private TextView tvSpeed1, tvSpeed2, tvSpeed3, tvSpeed4, tvSpeed5;
    private TextView tvSpeedDiff1, tvSpeedDiff2, tvSpeedDiff3, tvSpeedDiff4, tvSpeedDiff5;

    @Override
    public void onReceive(Context context, Intent intent) {
        //прочитаем данные из полученного широковещательного сообщения
        int step = intent.getIntExtra(SpeedometerService.EXTRA_STEP, 0);
        double currentSpeed = intent.getDoubleExtra(SpeedometerService.EXTRA_CURRENT_SPEED, 0);
        double speedDiff = intent.getDoubleExtra(SpeedometerService.EXTRA_SPEED_DIFF, 0);

        //подготовим ссылки на вью и опубликуем полученные данные в них
        initControls((AppCompatActivity) context);
        publishDataIntoViews(step, currentSpeed, speedDiff);
    }

    private void initControls(Activity activity) {
        tvSpeed1 = (TextView) activity.findViewById(R.id.tvSpeed1);
        tvSpeed2 = (TextView) activity.findViewById(R.id.tvSpeed2);
        tvSpeed3 = (TextView) activity.findViewById(R.id.tvSpeed3);
        tvSpeed4 = (TextView) activity.findViewById(R.id.tvSpeed4);
        tvSpeed5 = (TextView) activity.findViewById(R.id.tvSpeed5);

        tvSpeedDiff1 = (TextView) activity.findViewById(R.id.tvSpeedDiff1);
        tvSpeedDiff2 = (TextView) activity.findViewById(R.id.tvSpeedDiff2);
        tvSpeedDiff3 = (TextView) activity.findViewById(R.id.tvSpeedDiff3);
        tvSpeedDiff4 = (TextView) activity.findViewById(R.id.tvSpeedDiff4);
        tvSpeedDiff5 = (TextView) activity.findViewById(R.id.tvSpeedDiff5);
    }

    private void publishDataIntoViews(int step, double currentSpeed, double speedDiff) {
        Logger.d("step: " + String.valueOf(step) + " currentSpeed: " + String.valueOf(currentSpeed) +
                " speedDiff: " + String.valueOf(speedDiff));

        switch (step) {
            case -1:
                break;
            case 0:
                break;
            case 1:
                tvSpeed1.setText(String.valueOf(currentSpeed));
                tvSpeedDiff1.setText(String.valueOf(speedDiff));
                break;
            case 2:
                tvSpeed2.setText(tvSpeed1.getText());
                tvSpeed1.setText(String.valueOf(currentSpeed));
                tvSpeedDiff2.setText(tvSpeedDiff1.getText());
                tvSpeedDiff1.setText(String.valueOf(speedDiff));
                break;
            case 3:
                tvSpeed3.setText(tvSpeed2.getText());
                tvSpeed2.setText(tvSpeed1.getText());
                tvSpeed1.setText(String.valueOf(currentSpeed));
                tvSpeedDiff3.setText(tvSpeedDiff2.getText());
                tvSpeedDiff2.setText(tvSpeedDiff1.getText());
                tvSpeedDiff1.setText(String.valueOf(speedDiff));
                break;
            case 4:
                tvSpeed4.setText(tvSpeed3.getText());
                tvSpeed3.setText(tvSpeed2.getText());
                tvSpeed2.setText(tvSpeed1.getText());
                tvSpeed1.setText(String.valueOf(currentSpeed));
                tvSpeedDiff4.setText(tvSpeedDiff3.getText());
                tvSpeedDiff3.setText(tvSpeedDiff2.getText());
                tvSpeedDiff2.setText(tvSpeedDiff1.getText());
                tvSpeedDiff1.setText(String.valueOf(speedDiff));
                break;
            case 5:
                tvSpeed5.setText(tvSpeed4.getText());
                tvSpeed4.setText(tvSpeed3.getText());
                tvSpeed3.setText(tvSpeed2.getText());
                tvSpeed2.setText(tvSpeed1.getText());
                tvSpeed1.setText(String.valueOf(currentSpeed));
                tvSpeedDiff5.setText(tvSpeedDiff4.getText());
                tvSpeedDiff4.setText(tvSpeedDiff3.getText());
                tvSpeedDiff3.setText(tvSpeedDiff2.getText());
                tvSpeedDiff2.setText(tvSpeedDiff1.getText());
                tvSpeedDiff1.setText(String.valueOf(speedDiff));
                break;
        }
    }
}

