/*
 * Copyright 2019 Uriah Shaul Mandel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bald.uriah.baldphone.databases.reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bald.uriah.baldphone.apps.homescreen.HomeScreenActivity;
import com.bald.uriah.baldphone.apps.pills.PillsActivity;
import com.bald.uriah.baldphone.broadcast_receivers.ReminderReceiver;
import com.bald.uriah.baldphone.core.BPrefs;
import com.bald.uriah.baldphone.utils.DateTimeUtils;
import com.bald.uriah.baldphone.utils.S;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import java.util.List;

/**
 *
 */
public class ReminderScheduler {
    private static final String TAG = ReminderScheduler.class.getSimpleName();
    public static final Object LOCK = new Object();
    public static final int SNOOZE_MILLIS = 5 * DateTimeUtils.MINUTE;

    /**
     * helper class should not be instantiate
     */
    private ReminderScheduler() {
    }

    //Helpers Doesn't need to be synchronized
    public static void cancelReminder(int key, Context context) {
        synchronized (LOCK) {
            _cancelReminder(key, context);
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

    private static long nextTimeReminderWillWorkInMs(@NonNull Reminder reminder, Context context) {
        final MutableDateTime mDateTime = MutableDateTime.now();

        {   //creating a date of today with the hours and minutes of the alarm
            mDateTime.setMillisOfSecond(0);
            mDateTime.setSecondOfMinute(0);

            mDateTime.setHourOfDay(BPrefs.getHour(reminder.getStartingTime(), context));
            mDateTime.setMinuteOfHour(BPrefs.getMinute(reminder.getStartingTime(), context));
        }

        final int baldDay = getBaldDay();
        final int days = reminder.getDays();
        {   //today or one time
            if ((days & baldDay) == baldDay) {//today may have an alarm
                if ((days == baldDay)) {
                    if (mDateTime.isBeforeNow())
                        mDateTime.addWeeks(1);  //next week if today's time already passed
                    return mDateTime.getMillis();
                } else {
                    if (mDateTime.isAfterNow())
                        return mDateTime.getMillis();
                }
            } else if (days == -1) {
                if (mDateTime.isBeforeNow())
                    mDateTime.addDays(1);
                return mDateTime.getMillis();
            }
        }

        int selectedBaldDay = baldDay;

        {   //find next day
            for (int i = baldDay << 1; i != baldDay; i <<= 1) {
                if (i > DateTimeUtils.Days.SATURDAY)
                    i = DateTimeUtils.Days.SUNDAY;

                if ((days & i) == i) {
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

    private static void _cancelReminder(int key, Context context) {
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(getIntent(context, key));
    }

    public static void scheduleReminder(@NonNull Reminder reminder, @NonNull Context context) throws IllegalArgumentException {
        synchronized (LOCK) {
            final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            final long nextTimeReminderWillWorkInMs = nextTimeReminderWillWorkInMs(reminder, context);
            alarmManager.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(
                            nextTimeReminderWillWorkInMs,
                            PendingIntent.getActivity(context, 0, new Intent(context, PillsActivity.class), 0)
                    ),
                    getIntent(context, reminder.getId())
            );
        }
    }

    private static PendingIntent getIntent(Context context, int alarmKey) {
        Log.e(TAG, "getIntent: ");
        Intent intent = new Intent(context, ReminderReceiver.class).putExtra(Reminder.REMINDER_KEY_VIA_INTENTS, alarmKey);
        return PendingIntent.getBroadcast(context, alarmKey, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void scheduleSnooze(@NonNull Reminder alarm, Context context) throws IllegalArgumentException {
        synchronized (LOCK) {
            final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(
                            DateTime.now().getMillis() + SNOOZE_MILLIS,
                            PendingIntent.getActivity(context, alarm.getId(), new Intent(context, HomeScreenActivity.class), 0)//TODO??
                    ),
                    getIntent(context, alarm.getId())
            );
        }
    }

    public static void reStartReminders(final Context context) {
        S.logImportant("reStartReminders was called!");
        synchronized (LOCK) {
            S.logImportant("reStartReminders was started!");
            final List<Reminder> alarmList =
                    RemindersDatabase.getInstance(context)
                            .remindersDatabaseDao().getAllReminders();
            for (Reminder alarm :
                    alarmList) {
                ReminderScheduler.cancelReminder(alarm.getId(), context);
                ReminderScheduler.scheduleReminder(alarm, context);
            }
            S.logImportant("reStartReminders has finished!");
        }
    }
}



