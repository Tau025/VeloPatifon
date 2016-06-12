package com.devtau.velopatifon.activities;

/**
 * Created by Tau on 25.08.2015.
 */
import android.support.v4.app.FragmentActivity;


public class RouteActivity extends FragmentActivity {
//    private static final String LOG_TAG = "Route activity";
//    //private GoogleMap mMap; // Might be null if Google Play services APK is not available.
//    private Fragment mapFragment;
//    private AsyncTask updateTask;
//    RangeSeekBar<Long> seekBar;
//    Calendar rangeStart, rangeEnd;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.gps_activity_route);
//        setUpMapIfNeeded();
//
//        mapFragment = GPSHelper.getMapFragment();
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        ft.add(R.id.gps_activity_route, mapFragment);
//        ft.commit();
//
//        final Shift currentShift = MainActivity.currentShift;
//        ((MapFragment) mapFragment).showPath(LocationsStorage.getLocationsByShift(currentShift));
//        MainActivity.locationsStorage;
//
//        //add RangeSeekBar to map window
//        //начало отрезка = начало сегодняшнего дня, конец отрезка = сегодняшний момент времени
//        rangeStart = Calendar.getInstance();
//        rangeStart.set(Calendar.HOUR_OF_DAY, 0);
//        rangeStart.set(Calendar.MINUTE, 0);
//        rangeStart.set(Calendar.SECOND, 0);
//        rangeStart.set(Calendar.MILLISECOND, 0);
//        rangeEnd = Calendar.getInstance();
////        final Calendar rangeStartSupport = rangeStart;
////        final Calendar rangeEndSupport = rangeEnd;
//
//        seekBar = new RangeSeekBar<>(rangeStart.getTimeInMillis(), rangeEnd.getTimeInMillis(), RouteActivity.this);
//        seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Long>() {
//            @Override
//            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Long minValue, Long maxValue) {
//                // handle changed range values
//                //TODO добавить окошки для введенных дат
//                if (updateTask != null) updateTask.cancel(true);
//                rangeStart.setTimeInMillis(minValue);
//                rangeEnd.setTimeInMillis(maxValue);
//                String msg1 = String.format("%02d.%02d.%02d %02d:%02d", rangeStart.get(Calendar.DAY_OF_MONTH), rangeStart.get(Calendar.MONTH) + 1, rangeStart.get(Calendar.YEAR) % 100,
//                        rangeStart.get(Calendar.HOUR_OF_DAY), rangeStart.get(Calendar.MINUTE));
//                String msg2 = String.format("%02d.%02d.%02d %02d:%02d", rangeEnd.get(Calendar.DAY_OF_MONTH), rangeEnd.get(Calendar.MONTH) + 1, rangeEnd.get(Calendar.YEAR) % 100,
//                        rangeEnd.get(Calendar.HOUR_OF_DAY), rangeEnd.get(Calendar.MINUTE));
//                Toast.makeText(RouteActivity.this, "MIN = " + msg1 + ",\nMAX = " + msg2, Toast.LENGTH_LONG).show();
//                ((MapFragment) mapFragment).showPath(LocationsStorage.getLocationsByPeriod(rangeStart.getTime(), rangeEnd.getTime()));
//            }
//        });
//        ViewGroup layout = (ViewGroup) findViewById(R.id.routeLayout);
//        layout.addView(seekBar);
//
//        updateTask = new routeAsyncTask();
//        updateTask.execute();
//    }
//
//    private class routeAsyncTask extends AsyncTask{
//        @Override
//        protected Object doInBackground(Object[] params) {
//            while (true) {
//                try {
//                    if (isCancelled()) break;
//                    TimeUnit.SECONDS.sleep(4);
//                    publishProgress();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    break;
//                }
//            }
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(Object[] values) {
//            //super.onProgressUpdate(values);
//            Logger.d("Updating map");
//            try {
//                seekBar.setNormalizedMaxValue(Calendar.getInstance().getTimeInMillis());
//                ((MapFragment) mapFragment).showPath(LocationsStorage.getLocationsByPeriod(rangeStart.getTime(), Calendar.getInstance().getTime()));
//            } catch (Exception e) {
//                e.printStackTrace();
//                updateTask.cancel(true);
//            }
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (updateTask!=null) updateTask.cancel(true);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        setUpMapIfNeeded();
//    }
//
//    private void setUpMapIfNeeded() { }
//    private void setUpMap() { }
}

