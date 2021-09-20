package com.bald.uriah.baldphone.utils;

import androidx.annotation.StringRes;

import com.bald.uriah.baldphone.R;

public class DateTimeUtils {

    public static final int
            MILLISECOND = 1;
    public static final int SECOND = 1000 * MILLISECOND;
    public static final int MINUTE = 60 * SECOND;
    public static final int HOUR = 60 * MINUTE;
    public static final int DAY = 24 * HOUR;

    @StringRes
    public static int balddayToStringId(int baldDay) {
        switch (baldDay) {
            case 0:
                throw new RuntimeException("0 is not defined in a baldday int");
            case -1:
                throw new RuntimeException("-1 doesn't have a String id");
            case Days.SUNDAY:
                return R.string.sunday;
            case Days.MONDAY:
                return R.string.monday;
            case Days.TUESDAY:
                return R.string.tuesday;
            case Days.WEDNESDAY:
                return R.string.wednesday;
            case Days.THURSDAY:
                return R.string.thursday;
            case Days.FRIDAY:
                return R.string.friday;
            case Days.SATURDAY:
                return R.string.saturday;

        }
        throw new RuntimeException(baldDay + " is not defined in a specific baldday int");
    }

    public static class Days {
        public static final int SUNDAY = 0b1;
        public static final int MONDAY = 0b10;
        public static final int TUESDAY = 0b100;
        public static final int WEDNESDAY = 0b1000;
        public static final int THURSDAY = 0b10000;
        public static final int FRIDAY = 0b100000;
        public static final int SATURDAY = 0b1000000;
        public static final int ALL = SUNDAY | MONDAY | TUESDAY | WEDNESDAY | THURSDAY | FRIDAY | SATURDAY;
        public static final int[] ARRAY_ALL = new int[]{SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY};
    }
}
