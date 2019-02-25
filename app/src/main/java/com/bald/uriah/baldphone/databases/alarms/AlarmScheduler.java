/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.bald.uriah.baldphone.databases.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bald.uriah.baldphone.activities.HomeScreen;
import com.bald.uriah.baldphone.broadcast_receivers.AlarmReceiver;
import com.bald.uriah.baldphone.utils.D;
import com.bald.uriah.baldphone.utils.S;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import java.util.List;

/**
 *
 */
public class AlarmScheduler {
    private static final String TAG = AlarmScheduler.class.getSimpleName();
    public static final Object LOCK = new Object();
    public static final int SNOOZE_MILLIS = 5 * D.MINUTE;

    private AlarmScheduler() {
    }
    public static void cancelAlarm(int key, Context context) {
        synchronized (LOCK) {
            _cancelAlarm(key, context);
        }
    }

    // BaldDay -
    /*
          sunday = 1
          saturday = 64
          monday =2
          baldDay = 1<<israeliDay...
     */

    // JodaDay -
    /*
        sunday =7
        saturday =6
        monday =1
        jodaDay = day in cristian countries
     */

    private static int getBaldDay() {
        int today = DateTime.now().getDayOfWeek();
        if (today == 7)
            today = 0;
        today = 1 << today;
        return today;
    }

    private static int baldDayToJodaDay(int baldDay) {
        int day = 0;
        while (baldDay != 0) {
            day++;
            baldDay >>= 1;
        }
        day -= 1;

        if (day == 0)
            day = 7;
        return day;
    }


    public static long nextTimeAlarmWillWorkInMsFromNow(@NonNull Alarm alarm) {
        return nextTimeAlarmWillWorkInMs(alarm) - DateTime.now().getMillis();
    }

    static long nextTimeAlarmWillWorkInMs(@NonNull Alarm alarm) {
        final MutableDateTime mDateTime = MutableDateTime.now();
        {   //creating a date of today with the hours and minutes of the alarm
            mDateTime.setMillisOfSecond(0);
            mDateTime.setSecondOfMinute(0);
            mDateTime.setHourOfDay(alarm.getHour());
            mDateTime.setMinuteOfHour(alarm.getMinute());
        }

        final int baldDay = getBaldDay();

        {   //today or one time
            if ((alarm.getDays() & baldDay) == baldDay) {//today may have an alarm
                if ((alarm.getDays() == baldDay)) {
                    if (mDateTime.isBeforeNow())
                        mDateTime.addWeeks(1);  //next week if today's time already passed
                    return mDateTime.getMillis();
                } else {
                    if (mDateTime.isAfterNow())
                        return mDateTime.getMillis();
                }
            } else if (alarm.getDays() == -1) {
                if (mDateTime.isBeforeNow())
                    mDateTime.addDays(1);
                return mDateTime.getMillis();
            }
        }
        int selectedBaldDay = baldDay;

        {   //find next day
            for (int i = baldDay << 1; i != baldDay; i <<= 1) {
                if (i > D.Days.SATURDAY)
                    i = D.Days.SUNDAY;

                if ((alarm.getDays() & i) == i) {
                    selectedBaldDay = i;
                    break;
                }

            }
        }

        int day = baldDayToJodaDay(selectedBaldDay);
        mDateTime.setDayOfWeek(day);
        if (mDateTime.isBeforeNow()) {
            mDateTime.addWeeks(1);
        }
        return mDateTime.getMillis();
    }

    private static void _cancelAlarm(int key, Context context) {
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(getIntent(context, key));
    }
    public static void scheduleAlarm(@NonNull Alarm alarm, @NonNull Context context) throws IllegalArgumentException {
        synchronized (LOCK) {
            final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            final long nextTimeAlarmWillWorkInMs = nextTimeAlarmWillWorkInMs(alarm);
            alarmManager.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(
                            nextTimeAlarmWillWorkInMs,
                            PendingIntent.getActivity(context, 0, new Intent(context, HomeScreen.class), 0)//TODO??
                    ),
                    getIntent(context, alarm.getKey())
            );
        }
    }

    private static PendingIntent getIntent(Context context, int alarmKey) {
        Log.e(TAG, "getIntent: ");
        Intent intent = new Intent(context, AlarmReceiver.class).putExtra(Alarm.ALARM_KEY_VIA_INTENTS, alarmKey);
        return PendingIntent.getBroadcast(context, alarmKey, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void scheduleSnooze(@NonNull Alarm alarm, Context context) throws IllegalArgumentException {
        synchronized (LOCK) {
            final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(
                            DateTime.now().getMillis() + SNOOZE_MILLIS,
                            PendingIntent.getActivity(context, alarm.getKey(), new Intent(context, HomeScreen.class), 0)//TODO??
                    ),
                    getIntent(context, alarm.getKey())
            );
        }
    }

    public static void reStartAlarms(final Context context) {
        S.logImportant("reStartAlarms was called!");
        synchronized (LOCK) {
            S.logImportant("reStartAlarms was started!");
            final List<Alarm> alarmList = AlarmsDatabase.getInstance(context).alarmsDatabaseDao().getAllEnabled();
            for (Alarm alarm : alarmList) {
                AlarmScheduler.cancelAlarm(alarm.getKey(), context);
                AlarmScheduler.scheduleAlarm(alarm, context);
            }
            S.logImportant("reStartAlarms has finished!");

        }
    }
}



