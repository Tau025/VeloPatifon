package tt.velopatifon;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.concurrent.TimeUnit;
import android.os.AsyncTask;


public class MainActivity extends Activity {
    private TextView tvSpeed1, tvSpeed2, tvSpeed3, tvSpeed4, tvSpeed5, tvSpeedDiff1, tvSpeedDiff2, tvSpeedDiff3, tvSpeedDiff4, tvSpeedDiff5, tvTargetSpeed, tvDiffNormal;
    private EditText etTargetSpeed, etDiffNormal;
    private Button btnStartTracking, btnStopTracking, btnPlayer;
    private AsyncSpeedometer tvSpeedometer;
    private GPSTracker mGPS;
    private final static int STEP_IN_SECONDS = 5;
    private double currentPointLat, currentPointLong;
    private double targetSpeed, diffNormal, currentSpeed, SpeedDiff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSpeed1            = (TextView) findViewById(R.id.tvSpeed1);
        tvSpeed2            = (TextView) findViewById(R.id.tvSpeed2);
        tvSpeed3            = (TextView) findViewById(R.id.tvSpeed3);
        tvSpeed4            = (TextView) findViewById(R.id.tvSpeed4);
        tvSpeed5            = (TextView) findViewById(R.id.tvSpeed5);
        tvSpeedDiff1        = (TextView) findViewById(R.id.tvSpeedDiff1);
        tvSpeedDiff2        = (TextView) findViewById(R.id.tvSpeedDiff2);
        tvSpeedDiff3        = (TextView) findViewById(R.id.tvSpeedDiff3);
        tvSpeedDiff4        = (TextView) findViewById(R.id.tvSpeedDiff4);
        tvSpeedDiff5        = (TextView) findViewById(R.id.tvSpeedDiff5);
        tvTargetSpeed       = (TextView) findViewById(R.id.tvTargetSpeed);
        etTargetSpeed       = (EditText) findViewById(R.id.etTargetSpeed);
        tvDiffNormal        = (TextView) findViewById(R.id.tvDiffNormal);
        etDiffNormal        = (EditText) findViewById(R.id.etDiffNormal);
        btnStartTracking    = (Button)   findViewById(R.id.btnStartTracking);
        btnStopTracking     = (Button)   findViewById(R.id.btnStopTracking);
        btnPlayer           = (Button)   findViewById(R.id.btnPlayer);
        mGPS = new GPSTracker(this);
    }

    public void onStartBTNClick(View v){
        tvSpeed1.setText(""); tvSpeed2.setText(""); tvSpeed3.setText(""); tvSpeed4.setText(""); tvSpeed5.setText("");
        tvSpeedDiff1.setText(""); tvSpeedDiff2.setText(""); tvSpeedDiff3.setText(""); tvSpeedDiff4.setText(""); tvSpeedDiff5.setText("");
        if ("".equals(etTargetSpeed.getText().toString()) || "".equals(etDiffNormal.getText().toString()))
            Toast.makeText(getApplicationContext(), "заполните нужную скорость и отклонение", Toast.LENGTH_LONG).show();
        else {
            targetSpeed = Double.parseDouble(etTargetSpeed.getText().toString());
            diffNormal  = Double.parseDouble(etDiffNormal.getText().toString());
            tvSpeedometer = new AsyncSpeedometer();
            tvSpeedometer.execute();
        }
    }

    public void onStopBTNClick(View v){
        tvSpeedometer.cancel(true);
    }

    public void onPlayerBTNClick(View v){
        Toast.makeText(getApplicationContext(), "ищу плеер с открытым АПИ или небольшой встроенный с регулировкой скорости", Toast.LENGTH_LONG).show();
//        startActivity(new Intent("tt.musicapp.velopatifon"));
    }

    //Computes the approximate distance between two locations in meters
    public static double CalculateDistanceByLatLng(double latitudeStartP, double longitudeStartP, double latitudeEndP, double longitudeEndP) {
        float[] results = new float[1];
        Location.distanceBetween(latitudeStartP, longitudeStartP, latitudeEndP, longitudeEndP, results);
        return results[0];
    }


    class AsyncSpeedometer extends AsyncTask<Void, Integer, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvDiffNormal.setEnabled(false);
            etDiffNormal.setEnabled(false);
            tvTargetSpeed.setEnabled(false);
            etTargetSpeed.setEnabled(false);
            btnStartTracking.setEnabled(false);
            btnStopTracking.setEnabled(true);
            btnPlayer.setEnabled(true);
            Toast.makeText(getApplicationContext(), "Поехали!", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mGPS.isGPSEnabled){
                for (int i = 0; ; i++) {
                    mGPS.getLocation();
                    double previousPointLat = currentPointLat;
                    double previousPointLong = currentPointLong;
                    currentPointLat = mGPS.getLatitude();
                    currentPointLong = mGPS.getLongitude();
                    if (i > 0) {
                        double distanceValue = CalculateDistanceByLatLng(previousPointLat, previousPointLong, currentPointLat, currentPointLong);
                        currentSpeed = (distanceValue / STEP_IN_SECONDS) * 3.6;
                        currentSpeed = RoundResult(currentSpeed, 2);
                        SpeedDiff = currentSpeed - targetSpeed;
                    }
                    if (i >= 5) publishProgress(5);
                    else publishProgress(i);

                    try{TimeUnit.SECONDS.sleep(STEP_IN_SECONDS);}catch (InterruptedException e){/*NOP*/}
                    if (isCancelled()) return null;
                }
            }
            else{
                Toast.makeText(getApplicationContext(), "GPS выключен!\nВключите его перед использованием!", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            switch (values[0]){
                case 0: break;
                case 1:
                    tvSpeed1.setText(String.valueOf(currentSpeed));
                    tvSpeedDiff1.setText(String.valueOf(SpeedDiff));
                    break;
                case 2:
                    tvSpeed2.setText(tvSpeed1.getText());
                    tvSpeed1.setText(String.valueOf(currentSpeed));
                    tvSpeedDiff2.setText(tvSpeedDiff1.getText());
                    tvSpeedDiff1.setText(String.valueOf(SpeedDiff));
                    break;
                case 3:
                    tvSpeed3.setText(tvSpeed2.getText());
                    tvSpeed2.setText(tvSpeed1.getText());
                    tvSpeed1.setText(String.valueOf(currentSpeed));
                    tvSpeedDiff3.setText(tvSpeedDiff2.getText());
                    tvSpeedDiff2.setText(tvSpeedDiff1.getText());
                    tvSpeedDiff1.setText(String.valueOf(SpeedDiff));
                    break;
                case 4:
                    tvSpeed4.setText(tvSpeed3.getText());
                    tvSpeed3.setText(tvSpeed2.getText());
                    tvSpeed2.setText(tvSpeed1.getText());
                    tvSpeed1.setText(String.valueOf(currentSpeed));
                    tvSpeedDiff4.setText(tvSpeedDiff3.getText());
                    tvSpeedDiff3.setText(tvSpeedDiff2.getText());
                    tvSpeedDiff2.setText(tvSpeedDiff1.getText());
                    tvSpeedDiff1.setText(String.valueOf(SpeedDiff));
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
                    tvSpeedDiff1.setText(String.valueOf(SpeedDiff));
                    break;
            }

            String msg = "мне пока нечего сказать";
            if (currentSpeed < 5.0) msg = "наверное вы остановились";
            else if (SpeedDiff < -diffNormal)  msg = "медленнее на 0-" + String.valueOf(diffNormal) + " км/ч";//триггер уменьшения скорости музыки
            else if (SpeedDiff > diffNormal)   msg = "быстрее на 0-"   + String.valueOf(diffNormal) + " км/ч";//триггер увеличения скорости музыки
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Toast.makeText(getApplicationContext(), "вы не увидите это сообщение", Toast.LENGTH_SHORT).show();

            tvDiffNormal.setEnabled(true);
            etDiffNormal.setEnabled(true);
            tvTargetSpeed.setEnabled(true);
            etTargetSpeed.setEnabled(true);
            btnStartTracking.setEnabled(true);
            btnStopTracking.setEnabled(false);
            btnPlayer.setEnabled(false);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(getApplicationContext(), "программа остановлена", Toast.LENGTH_SHORT).show();

            tvDiffNormal.setEnabled(true);
            etDiffNormal.setEnabled(true);
            tvTargetSpeed.setEnabled(true);
            etTargetSpeed.setEnabled(true);
            btnStartTracking.setEnabled(true);
            btnStopTracking.setEnabled(false);
            btnPlayer.setEnabled(false);
        }

        private double RoundResult(double value, int decimalSigns) {
            int multiplier = (int) Math.pow(10.0, (double) decimalSigns);
            int numerator = (int) Math.round(value * multiplier);
            return (double) numerator / multiplier;
        }
    }
}
