package com.devtau.velopatifon.util;

import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
/**
 * Created by TAU on 12.06.2016.
 */
public class Util {
    //метод для самого общего случая необходимости обойти ViewGroup с индивидуальными действиями
    //для разных детей и возможыми вложенными ViewGroup
    public static void groupSetText(ViewGroup root, String text) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                groupSetText((ViewGroup) child, text);
            } else if (child instanceof Button) {
                ((Button) child).setText(text);
            } else if (child instanceof EditText) {
                ((EditText) child).setText(text);
            } else if (child instanceof TextView) {
                ((TextView) child).setText(text);
            }
        }
    }

    public static void groupSetEnabled(ViewGroup root, boolean isEnabled) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                groupSetEnabled((ViewGroup) child, isEnabled);
            } else {
                child.setEnabled(isEnabled);
            }
        }
    }

    //вычисляет приблизительное расстояние между двумя точками в метрах
    public static double calculateDistance(Location startPoint, Location endPoint) {
        float[] results = new float[1];
        Location.distanceBetween(startPoint.getLatitude(), startPoint.getLongitude(),
                endPoint.getLatitude(), endPoint.getLongitude(), results);
        return results[0];
    }

    public static double roundResult(double value, int decimalSigns) {
        int multiplier = (int) Math.pow(10.0, (double) decimalSigns);
        int numerator = (int) Math.round(value * multiplier);
        return (double) numerator / multiplier;
    }
}
