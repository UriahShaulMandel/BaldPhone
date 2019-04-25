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

package com.bald.uriah.baldphone.activities.pills;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.BaldActivity;
import com.bald.uriah.baldphone.databases.reminders.Reminder;
import com.bald.uriah.baldphone.databases.reminders.ReminderScheduler;
import com.bald.uriah.baldphone.databases.reminders.RemindersDatabase;
import com.bald.uriah.baldphone.utils.BPrefs;
import com.bald.uriah.baldphone.utils.BaldToast;
import com.bald.uriah.baldphone.utils.D;
import com.bald.uriah.baldphone.utils.S;

public class PillScreen extends BaldActivity {
    public static final int TIME_SCREEN_ON = D.MINUTE * 2;

    private static final String TAG = PillScreen.class.getSimpleName();

    private static final int TIME_DELAYED_SCHEDULE = 100;
    private static final AudioAttributes alarmAttributes =
            new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
    private final Runnable closeScreen = () -> {
        final Window window = getWindow();
        if (window != null)
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    };
    private TextView tv_textual_content, snooze, took;//todo delte
    private ImageView iv_pill;
    private Vibrator vibrator;
    private Ringtone ringtone;
    private Reminder reminder;
    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        S.logImportant("reminderScreen was called!");
        final Window window = getWindow();

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.reminder_screen);

        attachXml();
        if (getSharedPreferences(BPrefs.KEY, MODE_PRIVATE)
                .getBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE))
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        final Intent intent = getIntent();
        if (intent == null) throw new AssertionError();
        int key = intent.getIntExtra(Reminder.REMINDER_KEY_VIA_INTENTS, -1);
        if (key == -1) throw new AssertionError();
        reminder = RemindersDatabase.getInstance(this).remindersDatabaseDao().getById(key);
        if (reminder == null) {
            S.logImportant("reminder == null!, returning");
            return;
        }

        final String textual_content = reminder.getTextualContent();
        if (textual_content == null) tv_textual_content.setVisibility(View.GONE);
        else tv_textual_content.setText(textual_content);

        if (reminder.getBinaryContentType() == Reminder.BINARY_RGB) {
            final Drawable drawable = getDrawable(R.drawable.pill).mutate();
            drawable.setTint(Color.rgb(reminder.getBinaryContent()[0] & 0xFF, reminder.getBinaryContent()[1] & 0xFF, reminder.getBinaryContent()[2] & 0xFF));
            iv_pill.setImageDrawable(drawable);

        }

        took.setOnClickListener(v -> {
            if (vibrator != null)
                vibrator.vibrate(D.vibetime);
            finish();
        });
        took.setOnLongClickListener(v -> {
            if (vibrator != null)
                vibrator.vibrate(D.vibetime);
            finish();
            return true;
        });

        snooze.setOnClickListener((v) -> snooze());
        snooze.setOnLongClickListener((v) -> {
            snooze();
            return true;
        });

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        } catch (Exception e) {
            BaldToast.error(this);
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        makeBiggerAndSmaller(iv_pill);
        scheduleNextAlarm();
        handler = new Handler();
        handler.postDelayed(closeScreen, TIME_SCREEN_ON);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ringtone != null)
            ringtone.play();
    }

    @Override
    protected void onStop() {
        if (ringtone != null)
            ringtone.stop();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (ringtone != null)
            ringtone.stop();
        super.onDestroy();
    }

    private void attachXml() {
        tv_textual_content = findViewById(R.id.textual_content);
        took = findViewById(R.id.took);
        snooze = findViewById(R.id.snooze);
        iv_pill = findViewById(R.id.iv_pill);
    }

    private void makeBiggerAndSmaller(final View view) {
        final Animation enlarge = AnimationUtils.loadAnimation(this, R.anim.enlarge);
        final Animation ensmall = AnimationUtils.loadAnimation(this, R.anim.ensmall);
        enlarge.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(ensmall);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        ensmall.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(enlarge);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(enlarge);

    }

    private void snooze() {
        if (vibrator != null)
            vibrator.vibrate(D.vibetime);
        ReminderScheduler.scheduleSnooze(reminder, this);
        finish();
    }

    private void scheduleNextAlarm() {
        new Handler().postDelayed(() -> ReminderScheduler.scheduleReminder(reminder, this), TIME_DELAYED_SCHEDULE);
    }

    @Override
    public void onBackPressed() {
        snooze();
        super.onBackPressed();
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_NONE;
    }
}
