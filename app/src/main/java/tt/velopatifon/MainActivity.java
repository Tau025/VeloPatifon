package tt.velopatifon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity {
    String LOG_TAG = "MainActivity";
    private TextView tvSpeed1, tvSpeed2, tvSpeed3, tvSpeed4, tvSpeed5, tvTargetSpeed, tvDiffNormal;
    private TextView tvSpeedDiff1, tvSpeedDiff2, tvSpeedDiff3, tvSpeedDiff4, tvSpeedDiff5;
    private EditText etTargetSpeed, etDiffNormal;
    private ToggleButton tbtnTracking, tbtnPlayer;
    LinearLayout speedViews, speedDiffViews, targetSpeedAndDiffNormal, rateButtons;

    private AsyncSpeedometer speedometer;
    private GPSTracker mGPS;
    private final static int STEP_IN_SECONDS = 10;
    private Location currPoint = new Location("MainActivity");
    public static Map<Calendar, Location> locationsStorage = new HashMap<>();
    private double targetSpeed, diffNormal, currentSpeed, speedDiff;
    static boolean isTracking = false, isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, "new instance created");
        tvSpeed1        = (TextView) findViewById(R.id.tvSpeed1);
        tvSpeed2        = (TextView) findViewById(R.id.tvSpeed2);
        tvSpeed3        = (TextView) findViewById(R.id.tvSpeed3);
        tvSpeed4        = (TextView) findViewById(R.id.tvSpeed4);
        tvSpeed5        = (TextView) findViewById(R.id.tvSpeed5);
        tvSpeedDiff1    = (TextView) findViewById(R.id.tvSpeedDiff1);
        tvSpeedDiff2    = (TextView) findViewById(R.id.tvSpeedDiff2);
        tvSpeedDiff3    = (TextView) findViewById(R.id.tvSpeedDiff3);
        tvSpeedDiff4    = (TextView) findViewById(R.id.tvSpeedDiff4);
        tvSpeedDiff5    = (TextView) findViewById(R.id.tvSpeedDiff5);
        tvTargetSpeed   = (TextView) findViewById(R.id.tvTargetSpeed);
        etTargetSpeed   = (EditText) findViewById(R.id.etTargetSpeed);
        tvDiffNormal    = (TextView) findViewById(R.id.tvDiffNormal);
        etDiffNormal    = (EditText) findViewById(R.id.etDiffNormal);
        tbtnTracking    = (ToggleButton) findViewById(R.id.tbtnTracking);
        tbtnPlayer      = (ToggleButton) findViewById(R.id.tbtnPlayer);
        speedViews      = (LinearLayout) findViewById(R.id.speedViews);
        speedDiffViews  = (LinearLayout) findViewById(R.id.speedDiffViews);
        targetSpeedAndDiffNormal = (LinearLayout) findViewById(R.id.targetSpeedAndDiffNormal);
        rateButtons     = (LinearLayout) findViewById(R.id.rateButtons);
    }

    public void onTrackingTBTNClick(View v) {
        if (!isTracking) {
            mGPS = new GPSTracker(this);
            if (!mGPS.isGPSEnabled) mGPS.showGPSOffAlert();
            else {
                if ("".equals(etTargetSpeed.getText().toString()) || "".equals(etDiffNormal.getText().toString()))
                    Toast.makeText(getApplicationContext(), "заполните нужную скорость\nи порог", Toast.LENGTH_SHORT).show();
                else {
                    groupSetText(speedViews, "0");
                    groupSetText(speedDiffViews, "0");
                    targetSpeed = Double.parseDouble(etTargetSpeed.getText().toString());
                    diffNormal = Double.parseDouble(etDiffNormal.getText().toString());
                    speedometer = new AsyncSpeedometer();
                    speedometer.execute();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(tbtnTracking.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    tbtnTracking.setChecked(true);
                    isTracking = true;
                }
            }
        } else {
            speedometer.cancel(true);
            tbtnTracking.setChecked(false);
            isTracking = false;
        }
    }

    //метод для самого общего случая необходимости обойти ViewGroup с индивидуальными действиями для разных детей и возможыми вложенными ViewGroup
    protected void groupSetText(ViewGroup root, String text) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View view = root.getChildAt(i);
            if (view instanceof Button) ((Button) view).setText(text);
            else if (view instanceof EditText) ((EditText) view).setText(text);
            else if (view instanceof TextView) ((TextView) view).setText(text);
            else if (view instanceof ViewGroup) groupSetText((ViewGroup) view, text);
        }
    }

    public void onPlayerTBTNClick(View v) {
        if (!isPlaying) {
            startService(new Intent(this, PlayService.class));
            isPlaying = true;
        } else {
            stopService(new Intent(this, PlayService.class));
            isPlaying = false;
        }
        tbtnPlayer.setChecked(isPlaying);
        groupSetEnabled(rateButtons, isPlaying);
    }

    protected void groupSetEnabled(ViewGroup root, boolean isEnabled) {
        for (int i = 0; i < root.getChildCount(); i++)
            root.getChildAt(i).setEnabled(isEnabled);
    }

    //проверка if (isPlaying) здесь как подстраховка от NullPointer. На самом деле, isEnabled всех кнопок этой группы и так false, когда isPlaying false
    public void onRate050BTNClick(View v) { if (isPlaying) PlayService.soundPool.setRate(PlayService.streamId, 0.5f); }
    public void onRate100BTNClick(View v) { if (isPlaying) PlayService.soundPool.setRate(PlayService.streamId, 1.0f); }
    public void onRate150BTNClick(View v) { if (isPlaying) PlayService.soundPool.setRate(PlayService.streamId, 1.5f); }
    public void onRate200BTNClick(View v) { if (isPlaying) PlayService.soundPool.setRate(PlayService.streamId, 2.0f); }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onDestroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, PlayService.class));
        if (speedometer != null) speedometer.cancel(true);
    }


    //AsyncSpeedometer------------------------------------------------------------------------------
    class AsyncSpeedometer extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            groupSetEnabled(targetSpeedAndDiffNormal, false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; ; i++) {
                Location location = mGPS.getLocation();
                if (mGPS.isGPSEnabled) {
                    Location prevPoint = new Location(currPoint);
                    currPoint.set(location);
                    locationsStorage.put(Calendar.getInstance(), currPoint);
                    if (i > 0) {
                        double distanceValue = CalculateDistance(prevPoint, currPoint);
                        currentSpeed = (distanceValue / STEP_IN_SECONDS) * 3.6;
                        currentSpeed = RoundResult(currentSpeed, 2);
                        speedDiff = currentSpeed - targetSpeed;
                        speedDiff = RoundResult(speedDiff, 2);
                    }
                    if (i >= 5) publishProgress(5);
                    else publishProgress(i);
                } else {
                    publishProgress(-1);
                }
                try { TimeUnit.SECONDS.sleep(STEP_IN_SECONDS); } catch (InterruptedException e) {/*NOP*/}
                if (isCancelled()) return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            switch (values[0]) {
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

            String msg;
            if (values[0] == -1) msg = "у Вас выключен GPS";
            else if (values[0] == 0) msg = "Поехали!";
            else if (currentSpeed < 5.0) {
                msg = "вы остановились";
                if (isPlaying) PlayService.soundPool.setRate(PlayService.streamId, 1.0f);
            } else if (speedDiff < -diffNormal) {
                msg = "очень медленно";
                if (isPlaying) PlayService.soundPool.setRate(PlayService.streamId, 0.5f);
            } else if (speedDiff > diffNormal) {
                msg = "очень быстро";
                if (isPlaying) PlayService.soundPool.setRate(PlayService.streamId, 1.5f);
            } else {
                msg = "едем нормально";
                if (isPlaying) PlayService.soundPool.setRate(PlayService.streamId, 1.0f);
            }
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Toast.makeText(getApplicationContext(), "вы не увидите это сообщение", Toast.LENGTH_SHORT).show();
            groupSetEnabled(targetSpeedAndDiffNormal, true);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(getApplicationContext(), "программа остановлена", Toast.LENGTH_SHORT).show();
            groupSetEnabled(targetSpeedAndDiffNormal, true);
        }

        //Computes the approximate distance between two locations in meters
        private double CalculateDistance(Location startPoint, Location endPoint) {
            float[] results = new float[1];
            Location.distanceBetween(startPoint.getLatitude(), startPoint.getLongitude(), endPoint.getLatitude(), endPoint.getLongitude(), results);
            return results[0];
        }

        private double RoundResult(double value, int decimalSigns) {
            int multiplier = (int) Math.pow(10.0, (double) decimalSigns);
            int numerator = (int) Math.round(value * multiplier);
            return (double) numerator / multiplier;
        }
    }
}
