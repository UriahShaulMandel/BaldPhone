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

package com.bald.uriah.baldphone.databases.apps;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.ComponentName;

import com.bald.uriah.baldphone.adapters.AppsRecyclerViewAdapter;
import com.bald.uriah.baldphone.databases.home_screen_pins.HomeScreenPinHelper;
import com.bald.uriah.baldphone.views.HomeScreenAppView;
import com.bumptech.glide.Glide;

import java.util.Arrays;
import java.util.Objects;

/**
 * even though its not a representation of App and it represents an activity
 * this name fits the best
 * <p>
 * using this old java getters and setters because Room requires that.
 * see {@link Entity}
 */
@Entity
public class App implements AppsRecyclerViewAdapter.InAppsRecyclerView, HomeScreenPinHelper.HomeScreenPinnable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "flatten_component_name")
    private String flattenComponentName;

    @ColumnInfo(name = "icon")
    private byte[] icon;

    @ColumnInfo(name = "label")
    private String label;

    @ColumnInfo(name = "pinned")
    private boolean pinned;

    @Ignore
    @Override
    public int type() {
        return AppsRecyclerViewAdapter.TYPE_ITEM;
    }


    public String getFlattenComponentName() {
        return flattenComponentName;
    }

    public void setFlattenComponentName(String flattenComponentName) {
        this.flattenComponentName = flattenComponentName;
    }

    public byte[] getIcon() {
        return icon;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final App app = (App) o;
        return id == app.id &&
                pinned == app.pinned &&
                Objects.equals(flattenComponentName, app.flattenComponentName) &&
                Arrays.equals(icon, app.icon) &&
                Objects.equals(label, app.label);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, flattenComponentName, label, pinned);
        result = 31 * result + Arrays.hashCode(icon);
        return result;
    }

    @Ignore
    @Override
    public void applyToHomeScreenAppView(HomeScreenAppView homeScreenAppView) {
        homeScreenAppView.setText(getLabel());
        Glide.with(homeScreenAppView.iv_icon).load(getIcon()).into(homeScreenAppView.iv_icon);
        homeScreenAppView.setIntent(ComponentName.unflattenFromString(getFlattenComponentName()));
    }
}