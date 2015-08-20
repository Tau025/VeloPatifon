package tt.velopatifon;

import android.app.Activity;
import android.content.Intent;
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
    private TextView speed1, speed2, speed3, speed4, speed5, speedDiff1, speedDiff2, speedDiff3, speedDiff4, speedDiff5, tvStep, tvTargetSpeed;
    private EditText etStep, etTargetSpeed;
    private Button btnStartTracking, btnStopTracking;
    private AsyncSpeedometer speedometer;
    private GPSTracker mGPS;
    private int step;
    private double currentPointLat, currentPointLong;
    private double currentSpeed, targetSpeed, speedDiff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speed1              = (TextView) findViewById(R.id.speed1);
        speed2              = (TextView) findViewById(R.id.speed2);
        speed3              = (TextView) findViewById(R.id.speed3);
        speed4              = (TextView) findViewById(R.id.speed4);
        speed5              = (TextView) findViewById(R.id.speed5);
        speedDiff1          = (TextView) findViewById(R.id.speedDiff1);
        speedDiff2          = (TextView) findViewById(R.id.speedDiff2);
        speedDiff3          = (TextView) findViewById(R.id.speedDiff3);
        speedDiff4          = (TextView) findViewById(R.id.speedDiff4);
        speedDiff5          = (TextView) findViewById(R.id.speedDiff5);
        tvStep              = (TextView) findViewById(R.id.tvStep);
        etStep              = (EditText) findViewById(R.id.etStep);
        tvTargetSpeed       = (TextView) findViewById(R.id.tvTargetSpeed);
        etTargetSpeed       = (EditText) findViewById(R.id.etTargetSpeed);
        btnStartTracking    = (Button)   findViewById(R.id.startTracking);
        btnStopTracking     = (Button)   findViewById(R.id.stopTracking);
        mGPS = new GPSTracker(this);
    }

    public void onStartBTNClick(View v){
        speed1.setText(""); speed2.setText(""); speed3.setText(""); speed4.setText(""); speed5.setText("");
        speedDiff1.setText(""); speedDiff2.setText(""); speedDiff3.setText(""); speedDiff4.setText(""); speedDiff5.setText("");
        if ("".equals(etTargetSpeed.getText().toString()))
            Toast.makeText(getApplicationContext(), "заполните нужную скорость", Toast.LENGTH_LONG).show();
        else {
            targetSpeed = Double.parseDouble(etTargetSpeed.getText().toString());
            speedometer = new AsyncSpeedometer();
            speedometer.execute();
        }
    }

    public void onStopBTNClick(View v){
        speedometer.cancel(true);
    }

    public void onPlayerBTNClick(View v){
        startActivity(new Intent("tt.musicapp.player"));
    }

    //Computes the approximate distance between two locations in meters
    public static double CalculateDistanceByPoints(double latitudeStartP, double longitudeStartP, double latitudeEndP, double longitudeEndP) {
        float[] results = new float[1];
        Location.distanceBetween(latitudeStartP, longitudeStartP, latitudeEndP, longitudeEndP, results);
        return results[0];
    }


    class AsyncSpeedometer extends AsyncTask<Void, Integer, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Поехали!", Toast.LENGTH_SHORT).show();
            step = 1;
            if (!etStep.getText().toString().equals("")) step = Integer.parseInt(etStep.getText().toString());

            tvStep.setEnabled(false);
            etStep.setEnabled(false);
            tvTargetSpeed.setEnabled(false);
            etTargetSpeed.setEnabled(false);
            btnStartTracking.setEnabled(false);
            btnStopTracking.setEnabled(true);
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
                        double distanceValue = CalculateDistanceByPoints(previousPointLat, previousPointLong, currentPointLat, currentPointLong);
                        currentSpeed = (distanceValue / step) * 3.6;//метров в секунду * 3,6 = км/ч
                        currentSpeed = RoundResult(currentSpeed, 2);
                        speedDiff = currentSpeed - targetSpeed;
                    }
                    if (i >= 5) publishProgress(5);
                    else publishProgress(i);

                    try{TimeUnit.SECONDS.sleep(step);}catch (InterruptedException e){/*NOP*/};
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
                    speed1.setText(String.valueOf(currentSpeed));
                    speedDiff1.setText(String.valueOf(speedDiff));
                    break;
                case 2:
                    speed2.setText(speed1.getText());
                    speed1.setText(String.valueOf(currentSpeed));
                    speedDiff2.setText(speedDiff1.getText());
                    speedDiff1.setText(String.valueOf(speedDiff));
                    break;
                case 3:
                    speed3.setText(speed2.getText());
                    speed2.setText(speed1.getText());
                    speed1.setText(String.valueOf(currentSpeed));
                    speedDiff3.setText(speedDiff2.getText());
                    speedDiff2.setText(speedDiff1.getText());
                    speedDiff1.setText(String.valueOf(speedDiff));
                    break;
                case 4:
                    speed4.setText(speed3.getText());
                    speed3.setText(speed2.getText());
                    speed2.setText(speed1.getText());
                    speed1.setText(String.valueOf(currentSpeed));
                    speedDiff4.setText(speedDiff3.getText());
                    speedDiff3.setText(speedDiff2.getText());
                    speedDiff2.setText(speedDiff1.getText());
                    speedDiff1.setText(String.valueOf(speedDiff));
                    break;
                case 5:
                    speed5.setText(speed4.getText());
                    speed4.setText(speed3.getText());
                    speed3.setText(speed2.getText());
                    speed2.setText(speed1.getText());
                    speed1.setText(String.valueOf(currentSpeed));
                    speedDiff5.setText(speedDiff4.getText());
                    speedDiff4.setText(speedDiff3.getText());
                    speedDiff3.setText(speedDiff2.getText());
                    speedDiff2.setText(speedDiff1.getText());
                    speedDiff1.setText(String.valueOf(speedDiff));
                    break;
            }

            String msg = "мне пока нечего сказать";
            if (currentSpeed < 5.0) msg = "наверное вы остановились";
            else if (-10 <= speedDiff && speedDiff < -5) msg = "медленнее на 5-10 км/ч";
            else if (-5  <= speedDiff && speedDiff < 0)  msg = "медленнее на 0-5 км/ч";
            else if (0   <= speedDiff && speedDiff < 5)  msg = "быстрее на 0-5 км/ч";
            else if (5   <= speedDiff && speedDiff < 10) msg = "быстрее на 5-10 км/ч";
            else if (10  <= speedDiff) msg = "очень быстро";
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Toast.makeText(getApplicationContext(), "вы не увидите это сообщение", Toast.LENGTH_SHORT).show();

            tvStep.setEnabled(true);
            etStep.setEnabled(true);
            tvTargetSpeed.setEnabled(true);
            etTargetSpeed.setEnabled(true);
            btnStartTracking.setEnabled(true);
            btnStopTracking.setEnabled(false);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(getApplicationContext(), "программа остановлена", Toast.LENGTH_SHORT).show();

            tvStep.setEnabled(true);
            etStep.setEnabled(true);
            tvTargetSpeed.setEnabled(true);
            etTargetSpeed.setEnabled(true);
            btnStartTracking.setEnabled(true);
            btnStopTracking.setEnabled(false);
        }

        private double RoundResult(double value, int decimalSigns) {
            int multiplier = (int) Math.pow(10.0, (double) decimalSigns);
            int numerator = (int) Math.round(value * multiplier);
            return (double) numerator / multiplier;
        }
    }
}
