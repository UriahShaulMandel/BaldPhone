package com.bald.uriah.baldphone.utils;

import android.graphics.Color;
import android.util.SparseIntArray;

import androidx.annotation.ColorInt;

import java.util.Random;

public class ColorUtils {

    public static class RandomColorMaker {
        private final SparseIntArray hashsToColors = new SparseIntArray();
        private final Random random = new Random();
        private final boolean bright;

        public RandomColorMaker(int backgroundColor) {
            this.bright = !isColorBright(backgroundColor);
        }

        private static boolean isColorBright(@ColorInt int colorInt) {
            float[] hsv = new float[3];
            Color.RGBToHSV(Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt), hsv);
            return hsv[2] > 0.5f;
        }

        @ColorInt
        public int generateColor(int hash) {
            @ColorInt int color;
            color = hashsToColors.get(hash, -1);
            if (color == -1) {
                color = Color.HSVToColor(new float[]{random.nextFloat() * 360, 0.65f, bright ? 0.3f * random.nextFloat() : 0.9f - 0.3f * random.nextFloat()});
                hashsToColors.append(hash, color);
            }
            return color;
        }
    }
}
