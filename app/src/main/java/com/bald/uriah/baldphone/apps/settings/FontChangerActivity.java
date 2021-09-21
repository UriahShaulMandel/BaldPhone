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

package com.bald.uriah.baldphone.apps.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bald.uriah.baldphone.R;
import com.bald.uriah.baldphone.core.BaldActivity;

import static com.bald.uriah.baldphone.apps.settings.SettingsActivity.FONT_SIZES;

public class FontChangerActivity extends BaldActivity {
    private static final String TAG = FontChangerActivity.class.getSimpleName();
    private static final int[] STRING_RES = new int[]{R.string.tiny, R.string.small, R.string.medium, R.string.large, R.string.huge};
    private static final int[] SIZES_RES = new int[]{R.dimen.tiny, R.dimen.small, R.dimen.medium, R.dimen.large, R.dimen.huge};
    private LinearLayout example;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font);
        final SeekBar fontSeekBar = findViewById(R.id.font_seek_bar);
        example = findViewById(R.id.example);
        showExamples(this);

        try {
            final float font = Settings.System.getFloat(getContentResolver(), Settings.System.FONT_SCALE);
            for (int i = 0; i < FONT_SIZES.length; i++) {
                if (FONT_SIZES[i] == font) {
                    fontSeekBar.setProgress(i);
                    break;
                }
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            fontSeekBar.setProgress(4);
        }

        fontSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                final float SIZE = FONT_SIZES[progress];
                Settings.System.putFloat(getBaseContext().getContentResolver(),
                        Settings.System.FONT_SCALE, SIZE);
                final Resources.Theme theme = getTheme();
                final Configuration cNew = new Configuration(theme.getResources().getConfiguration());
                cNew.fontScale = SIZE;
                final Context newContext = FontChangerActivity.this.createConfigurationContext(cNew);
                showExamples(newContext);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    private void showExamples(Context newContext) {
        example.removeAllViews();
        TextView textView;
        for (int i = 0; i < STRING_RES.length; i++) {
            textView = (TextView) LayoutInflater.from(FontChangerActivity.this).inflate(R.layout.font_text_view, example, false);
            textView.setText(STRING_RES[i]);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newContext.getResources().getDimension(SIZES_RES[i]));
            example.addView(textView);
        }
    }

    @Override
    protected int requiredPermissions() {
        return PERMISSION_WRITE_SETTINGS;
    }
}
