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

package com.bald.uriah.baldphone.utils;

import android.content.Context;
import android.content.res.Resources;

import com.bald.uriah.baldphone.R;


public class VoiceRecognitionValues {
    //only single instance is required per app
    private static VoiceRecognitionValues INSTANCE;

    //package private
    final String[] call;
    final String[] open;

    private VoiceRecognitionValues(Context context) {
        final Resources resources = context.getResources();
        call = resources.getStringArray(R.array.words_for_call);
        open = resources.getStringArray(R.array.words_for_open);
    }

    public static synchronized VoiceRecognitionValues getInstance(Context context) {
        if (INSTANCE == null)
            INSTANCE = new VoiceRecognitionValues(context);
        return INSTANCE;
    }

    public static synchronized void removeInstance() {
        INSTANCE = null;
    }
}
