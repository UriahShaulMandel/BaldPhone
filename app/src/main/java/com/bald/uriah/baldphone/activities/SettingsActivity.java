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

package com.bald.uriah.baldphone.activities;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.Ringtone;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;
import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.activities.alarms.AlarmScreen;
import com.bald.uriah.baldphone.activities.pills.PillTimeSetterActivity;
import com.bald.uriah.baldphone.utils.*;
import com.bald.uriah.baldphone.views.BaldTitleBar;
import com.bald.uriah.baldphone.views.ModularRecyclerView;
import com.bumptech.glide.Glide;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.ACTION_VIEW;

/**
 * Settings Activity,
 * {{@link SettingsItem}} are generated programmatically because of the huge differences that lies between different settings.
 */
public class SettingsActivity extends BaldActivity {
    public static final int REQUEST_SELECT_CUSTOM_APP = 88;
    public static final float[] FONT_SIZES = new float[]{0.8f, 0.9f, 1.0f, 1.1f, 1.3f, 1.5f, 1.7f};
    public static final String SAVEABLE_HISTORY_KEY = "SAVEABLE_HISTORY_KEY";
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private final List<SettingsItem>
            mainSettingsItemList = new ArrayList<>(),
            connectionSettingsList = new ArrayList<>(),
            accessibilitySettingsList = new ArrayList<>(),
            displaySettingsList = new ArrayList<>(),
            personalizationSettingsList = new ArrayList<>();
    private Ringtone ringtone;
    private Vibrator vibrator;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private RecyclerView recyclerView;
    private int section = -1;
    private BaldPrefsUtils baldPrefsUtils;
    private List<SettingsItem> settingsList = mainSettingsItemList;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkPermissions(this, requiredPermissions()))
            return;
        setContentView(R.layout.activity_settings);
        sharedPreferences = getSharedPreferences(D.BALD_PREFS, MODE_PRIVATE);
        baldPrefsUtils = BaldPrefsUtils.newInstance(this);
        editor = sharedPreferences.edit();

        vibrator = (sharedPreferences.getBoolean(BPrefs.VIBRATION_FEEDBACK_KEY, BPrefs.VIBRATION_FEEDBACK_DEFAULT_VALUE)) ? (Vibrator) getSystemService(VIBRATOR_SERVICE) : null;
        ((BaldTitleBar) findViewById(R.id.bald_title_bar)).getBt_back().setOnClickListener((v) -> {
            onBackPressed();
        });

        recyclerView = findViewById(R.id.child);

        final TypedValue typedValue = new TypedValue();
        final Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.bald_stroke_color, typedValue, true);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .drawable(R.drawable.settings_divider)
                        .build()
        );
        populateSettingsList();
        recyclerView.setAdapter(new SettingsRecyclerViewAdapter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (baldPrefsUtils.hasChanged(this))
            recreate();
    }

    private void populateSettingsList() {
        final SettingsItem back =
                new RunnableSettingsItem(R.string.back, ((v) -> goBack()), R.drawable.close_on_button);
        connectionSettingsList.add(back);
        accessibilitySettingsList.add(back);
        displaySettingsList.add(back);
        personalizationSettingsList.add(back);
        mainSettingsItemList.add(new RunnableSettingsItem(
                R.string.connection_settings,
                (v) -> newList(connectionSettingsList, 0),
                R.drawable.wifi_on_button
        ));
        mainSettingsItemList.add(new RunnableSettingsItem(
                R.string.accessibility_settings,
                (v) -> newList(accessibilitySettingsList, 1),
                R.drawable.accessibility_on_button
        ));
        mainSettingsItemList.add(new RunnableSettingsItem(
                R.string.display_settings,
                (v) -> newList(displaySettingsList, 2),
                R.drawable.screen_on_button
        ));
        mainSettingsItemList.add(new RunnableSettingsItem(
                R.string.personalization_settings,
                (v) -> newList(personalizationSettingsList, 3),
                R.drawable.brush_on_button
        ));

        mainSettingsItemList.add(
                new RunnableSettingsItem(R.string.set_home_screen,
                        v -> FakeLauncherActivity.resetPreferredLauncherAndOpenChooser(this)
                        , R.drawable.home_on_button)
        );

        mainSettingsItemList.add(new RunnableSettingsItem(R.string.advanced_options, v -> {
            try {
                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                BaldToast.from(this).setText(R.string.setting_does_not_exist).setType(BaldToast.TYPE_ERROR).show();

            }
        }, R.drawable.settings_on_button));

        final SettingsItem keyboard = new RunnableSettingsItem(R.string.set_keyboard, v -> startActivity(new Intent(this, KeyboardChangerActivity.class)), R.drawable.keyboard_on_button);
        accessibilitySettingsList.add(keyboard);
        personalizationSettingsList.add(keyboard);

        personalizationSettingsList.add(new RunnableSettingsItem(R.string.language_settings, v -> {
            try {
                startActivity(new Intent(Settings.ACTION_LOCALE_SETTINGS));
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                BaldToast.from(this).setText(R.string.setting_does_not_exist).setType(BaldToast.TYPE_ERROR).show();

            }
        }, R.drawable.translate_on_button));

        personalizationSettingsList.add(new RunnableSettingsItem(R.string.time_changer, v -> startActivity(new Intent(this, PillTimeSetterActivity.class)), R.drawable.pill));
        personalizationSettingsList.add(
                // !!dont change string without changing it too in onActivityResult!!
                new BDBSettingsItem(R.string.custom_app,
                        BDB.from(this)
                                .setTitle(R.string.custom_app)
                                .setSubText(R.string.custom_app_subtext)
                                .setDialogState(BDialog.DialogState.OPTION_OPTION_OK_CANCEL)
                                .setOptions(R.string.whatsapp, R.string.custom)
                                .setOptionsStartingIndex(() -> sharedPreferences.contains(BPrefs.CUSTOM_APP_KEY) ? 1 : 0)
                                .setPositiveButtonListener(params -> {
                                    if (params[0].equals(0)) {
                                        editor.remove(BPrefs.CUSTOM_APP_KEY).apply();
                                    } else
                                        startActivityForResult(new Intent(this, AppsActivity.class).putExtra(AppsActivity.EXTRA_MODE, AppsActivity.MODE_CHOOSE_ONE), REQUEST_SELECT_CUSTOM_APP);
                                    return true;
                                }), R.drawable.whatsapp_on_button

                )
        );
        accessibilitySettingsList.add(new RunnableSettingsItem(R.string.accessibility_level, v -> startActivity(new Intent(this, AccessibilityLevelChangerActivity.class)), R.drawable.accessibility_on_button));
        accessibilitySettingsList.add(
                new BDBSettingsItem(R.string.accidental_touches, BDB.from(this)
                        .setDialogState(BDialog.DialogState.OPTION_OPTION_OK_CANCEL)
                        .setTitle(R.string.accidental_touches)
                        .setSubText(R.string.accidental_touches_settings_subtext)
                        .setOptions(R.string.on, R.string.off)
                        .setPositiveButtonListener(params -> {
                            editor.putBoolean(BPrefs.USE_ACCIDENTAL_GUARD_KEY, params[0].equals(0)).apply();
                            this.recreate();
                            return true;
                        })
                        .setOptionsStartingIndex(() -> sharedPreferences.getBoolean(BPrefs.USE_ACCIDENTAL_GUARD_KEY, BPrefs.USE_ACCIDENTAL_GUARD_DEFAULT_VALUE) ? 0 : 1), R.drawable.blocked_on_button));
        accessibilitySettingsList.add(
                new BDBSettingsItem(R.string.strong_hand,
                        BDB.from(this)
                                .setDialogState(BDialog.DialogState.OPTION_OPTION_OK_CANCEL)
                                .setTitle(R.string.strong_hand)
                                .setSubText(R.string.strong_hand_subtext)
                                .setOptions(R.string.left_handed, R.string.right_handed)
                                .setPositiveButtonListener(params -> {
                                    editor.putBoolean(BPrefs.RIGHT_HANDED_KEY, params[0].equals(1)).apply();
                                    this.recreate();
                                    return true;
                                })
                                .setOptionsStartingIndex(() -> sharedPreferences.getBoolean(BPrefs.RIGHT_HANDED_KEY, BPrefs.RIGHT_HANDED_DEFAULT_VALUE) ? 1 : 0), R.drawable.hand_on_button
                )
        );
        final SettingsItem themeSettingsItem = new BDBSettingsItem(R.string.theme_settings,
                BDB.from(this)
                        .setDialogState(BDialog.DialogState.OPTION_OPTION_OK_CANCEL)
                        .setTitle(R.string.theme_settings)
                        .setSubText(R.string.theme_settings_subtext)
                        .setOptions(R.string.light, R.string.adaptive, R.string.dark)
                        .setPositiveButtonListener(params -> {
                            editor.putInt(BPrefs.THEME_KEY, (Integer) params[0]).apply();
                            this.recreate();
                            return true;
                        })
                        .setOptionsStartingIndex(() -> sharedPreferences.getInt(BPrefs.THEME_KEY, BPrefs.THEME_DEFAULT_VALUE)), R.drawable.brush_on_button

        );
        displaySettingsList.add(themeSettingsItem);
        personalizationSettingsList.add(themeSettingsItem);
        personalizationSettingsList.add(new BDBSettingsItem(R.string.notes_settings,
                BDB.from(this)
                        .setDialogState(BDialog.DialogState.OPTION_OPTION_OK_CANCEL)
                        .setTitle(R.string.notes_settings)
                        .setSubText(R.string.notes_settings_subtext)
                        .setOptions(R.string.yes, R.string.no)
                        .setPositiveButtonListener(params -> {
                            editor.putBoolean(BPrefs.NOTE_VISIBLE_KEY, (Integer) params[0] == 0).apply();
                            this.recreate();
                            return true;
                        })
                        .setOptionsStartingIndex(() -> sharedPreferences.getBoolean(BPrefs.NOTE_VISIBLE_KEY, BPrefs.NOTE_VISIBLE_DEFAULT_VALUE) ? 0 : 1),
                R.drawable.note_on_button
        ));
        SettingsItem fontSettingsItem = new RunnableSettingsItem(R.string.font, v -> {
            startActivity(new Intent(this, FontChangerActivity.class));
        }, R.drawable.font_on_button);
        personalizationSettingsList.add(fontSettingsItem);
        displaySettingsList.add(fontSettingsItem);
        accessibilitySettingsList.add(fontSettingsItem);

        setupBrightness();
        setupAlarmVolume();

        //connections
        connectionSettingsList.add(new RunnableSettingsItem(R.string.airplane_mode, v -> {
            try {
                startActivity(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS));
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                BaldToast.from(this).setText(R.string.setting_does_not_exist).setType(BaldToast.TYPE_ERROR).show();

            }
        }, R.drawable.airplane_mode_on_button));
        connectionSettingsList.add(new RunnableSettingsItem(R.string.wifi, v -> {
            try {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                BaldToast.from(this).setText(R.string.setting_does_not_exist).setType(BaldToast.TYPE_ERROR).show();

            }
        }, R.drawable.wifi_on_button));
        connectionSettingsList.add(new RunnableSettingsItem(R.string.bluetooth, v -> {
            try {
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                BaldToast.from(this).setText(R.string.setting_does_not_exist).setType(BaldToast.TYPE_ERROR).show();

            }
        }, R.drawable.bluetooth_on_button));

        NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter != null)
            connectionSettingsList.add(new RunnableSettingsItem(R.string.nfc, v -> {
                try {
                    startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                    BaldToast.from(this).setText(R.string.setting_does_not_exist).setType(BaldToast.TYPE_ERROR).show();

                }
            }, R.drawable.nfc_on_button));
        connectionSettingsList.add(new RunnableSettingsItem(R.string.location, v -> {
            try {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                BaldToast.from(this).setText(R.string.setting_does_not_exist).setType(BaldToast.TYPE_ERROR).show();

            }
        }, R.drawable.location_on_button));

        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutInflater.from(this).inflate(R.layout.credits_button, linearLayout, true);
        LayoutInflater.from(this).inflate(R.layout.open_source_licenses_button, linearLayout, true);
        linearLayout.findViewById(R.id.credits_button).setOnClickListener(v -> startActivity(new Intent(this, CreditsActivity.class)));
        linearLayout.findViewById(R.id.open_source_licenses_button).setOnClickListener(v -> startActivity(new Intent(ACTION_VIEW, Uri.parse("https://sites.google.com/view/baldphone-open-source-licenses/home"))));
        final ImageView pic = new ImageView(this);
        linearLayout.addView(pic);
        Glide.with(pic).load(R.drawable.me).into(pic);

        settingsList.add(
                new BDBSettingsItem(R.string.about,
                        BDB.from(this)
                                .setCancelable(true)
                                .setDialogState(BDialog.DialogState.OK)
                                .setTitle(R.string.about)
                                .setSubText(R.string.about_subtext)
                                .setPositiveButtonListener(params -> true)
                                .setExtraView(linearLayout)

                        , R.drawable.info_on_button)
        );
        settingsList.add(
                new RunnableSettingsItem(R.string.technical_information,
                        v -> startActivity(new Intent(this, TechnicalInfoActivity.class)),
                        R.drawable.tech_info_on_button)
        );
        settingsList.add(
                new RunnableSettingsItem(R.string.share_baldphone, v -> S.shareBaldPhone(this), R.drawable.share_on_background)
        );

        settingsList.add(
                new RunnableSettingsItem(R.string.feedback,
                        v -> startActivity(new Intent(this, FeedbackActivity.class)),
                        R.drawable.feedback_on_button)
        );
        settingsList.add(
                new BDBSettingsItem(R.string.crash_reports,
                        BDB.from(this)
                                .setDialogState(BDialog.DialogState.OPTION_OPTION_OK_CANCEL)
                                .setTitle(R.string.crash_reports)
                                .setSubText(R.string.crash_reports_subtext)
                                .setOptions(R.string.on, R.string.off)
                                .setPositiveButtonListener(params -> {
                                    editor.putBoolean(BPrefs.CRASH_REPORTS_KEY, params[0].equals(0)).apply();
                                    this.recreate();
                                    return true;
                                })
                                .setOptionsStartingIndex(() -> sharedPreferences.getBoolean(BPrefs.CRASH_REPORTS_KEY, BPrefs.CRASH_REPORTS_DEFAULT_VALUE) ? 0 : 1),
                        R.drawable.upload_on_button));
        settingsList.add(
                new RunnableSettingsItem(R.string.check_for_updates,
                        v -> UpdatingUtil.checkForUpdates(this, true),
                        R.drawable.updates_on_button)
        );
    }

    @Override
    public void onBackPressed() {
        if (!goBack())
            super.onBackPressed();
    }

    /**
     * @return true if succeeded
     */
    private boolean goBack() {
        if (section == -1)
            return false;
        if (vibrator != null)
            vibrator.vibrate(D.vibetime);

        section = -1;
        settingsList = mainSettingsItemList;
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
        return true;
    }

    private void newList(List<SettingsItem> newList, int section) {
        this.section = section;
        settingsList = newList;
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    private void setupAlarmVolume() {
        final SeekBar volumeSeekBar = (SeekBar) LayoutInflater.from(this).inflate(R.layout.volume_seek_bar, null, false);
        final SharedPreferences sharedPreferences = BPrefs.get(this);
        int volume = sharedPreferences.getInt(BPrefs.ALARM_VOLUME_KEY, BPrefs.ALARM_VOLUME_DEFAULT_VALUE);
        volumeSeekBar.setProgress(volume);
        final Handler handler = new Handler();
        final Runnable closeRingtone = () -> {
            final Ringtone r = ringtone;
            if (r == null)
                return;
            r.stop();
            ringtone = null;
        };

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                handler.removeCallbacks(closeRingtone);
                closeRingtone.run();

                sharedPreferences.edit().putInt(BPrefs.ALARM_VOLUME_KEY, progress).apply();
                ringtone = AlarmScreen.getRingtone(SettingsActivity.this);
                ringtone.play();
                handler.postDelayed(closeRingtone, 4 * D.SECOND);

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final SettingsItem alarmVolumeSettingsItem = new BDBSettingsItem(R.string.alarm_volume,
                BDB.from(this)
                        .setDialogState(BDialog.DialogState.OK)
                        .setTitle(R.string.alarm_volume)
                        .setSubText(R.string.alarm_volume_subtext)
                        .setPositiveButtonListener(params -> true)
                        .setExtraView(volumeSeekBar), R.drawable.clock_on_background

        );
        personalizationSettingsList.add(alarmVolumeSettingsItem);

    }

    private void setupBrightness() {
        final LinearLayout brightnessSeekBarHolder = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.brightness_seek_bar, null, false);
        final SeekBar brightnessSeekBar = brightnessSeekBarHolder.findViewById(R.id.brightness_seek_bar);
        final CheckBox checkBoxAutoBrightness = brightnessSeekBarHolder.findViewById(R.id.auto_brightness_check_box);
        final int[] brightness = new int[1];
        brightnessSeekBar.setKeyProgressIncrement(1);
        try {
            if (Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                brightness[0] = -1;
                checkBoxAutoBrightness.setChecked(true);
                brightnessSeekBar.setEnabled(false);

            } else {
                brightnessSeekBar.setProgress(Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress <= 20)
                    brightness[0] = 20;
                else
                    brightness[0] = progress;
                WindowManager.LayoutParams layoutpars = getWindow().getAttributes();
                layoutpars.screenBrightness = brightness[0] / 255f;
                getWindow().setAttributes(layoutpars);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness[0]);
                final WindowManager.LayoutParams params = getWindow().getAttributes();
                params.screenBrightness = brightness[0] / 255f;
                getWindow().setAttributes(params);
            }
        });

        checkBoxAutoBrightness.setOnLongClickListener((v) -> {
            checkBoxAutoBrightness.setChecked(!checkBoxAutoBrightness.isChecked());
            return true;
        });
        checkBoxAutoBrightness.setOnCheckedChangeListener((buttonView, isChecked) -> {
            brightnessSeekBar.setEnabled(!isChecked);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, (isChecked ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL));
            brightness[0] = 50;
        });

        final SettingsItem brightnessSettingsItem = new BDBSettingsItem(R.string.brightness,
                BDB.from(this)
                        .setDialogState(BDialog.DialogState.OK)
                        .setTitle(R.string.brightness)
                        .setSubText(R.string.brightness_subtext)
                        .setPositiveButtonListener(params -> true)
                        .setExtraView(brightnessSeekBarHolder), R.drawable.brightness_on_button
        );
        displaySettingsList.add(brightnessSettingsItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_CUSTOM_APP && resultCode == RESULT_OK && data != null && data.getComponent() != null) {
            editor.putString(BPrefs.CUSTOM_APP_KEY, data.getComponent().flattenToString()).apply();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVEABLE_HISTORY_KEY, section);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.section = savedInstanceState.getInt(SAVEABLE_HISTORY_KEY);
        if (section != -1)
            settingsList.get(section).onClick(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ringtone != null)
            ringtone.stop();
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_WRITE_SETTINGS;
    }

    public class SettingsRecyclerViewAdapter extends ModularRecyclerView.ModularAdapter<SettingsRecyclerViewAdapter.ViewHolder> {
        final LayoutInflater layoutInflater;

        SettingsRecyclerViewAdapter() {
            layoutInflater = (LayoutInflater) SettingsActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(layoutInflater.inflate(R.layout.settings_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            holder.update(settingsList.get(position));
        }

        @Override
        public int getItemCount() {
            return settingsList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ImageView setting_icon;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.tv_setting_name);
                setting_icon = itemView.findViewById(R.id.setting_icon);
            }

            public void update(SettingsItem settingsItem) {
                this.textView.setText(settingsItem.textResId);
                this.setting_icon.setImageResource(settingsItem.drawableResId);
                this.itemView.setOnClickListener(settingsItem);
            }
        }
    }

    //OOP
    public abstract class SettingsItem implements View.OnClickListener {
        public final @StringRes
        int textResId;
        public final @DrawableRes
        int drawableResId;

        protected SettingsItem(@StringRes int textResId, @DrawableRes int drawableResId) {
            this.textResId = textResId;
            this.drawableResId = drawableResId;
        }

    }

    public class BDBSettingsItem extends SettingsItem {
        private final BDB bdb;

        BDBSettingsItem(@StringRes int textResId, BDB bdb, @DrawableRes int drawableResId) {
            super(textResId, drawableResId);
            this.bdb = bdb;
        }

        @Override
        public void onClick(View v) {
            bdb.show();
        }
    }

    public class RunnableSettingsItem extends SettingsItem {
        private final View.OnClickListener onClickListener;

        RunnableSettingsItem(@StringRes int textResId, View.OnClickListener onClickListener, @DrawableRes int drawableResId) {
            super(textResId, drawableResId);
            this.onClickListener = onClickListener;
        }

        @Override
        public void onClick(View v) {
            onClickListener.onClick(v);
        }
    }
}