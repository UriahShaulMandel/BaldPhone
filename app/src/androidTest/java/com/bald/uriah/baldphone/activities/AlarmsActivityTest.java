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

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.runner.AndroidJUnit4;

import com.bald.uriah.baldphone.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AlarmsActivityTest extends BaseActivityTest {

    private static Matcher<View> childAtPosition(final Matcher<View> parentMatcher, final int position) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent) && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @Test
    public void alarmsActivityTest() {
        mActivityTestRule.launchActivity(new Intent());
        ViewInteraction firstPageAppIcon = onView(allOf(withId(R.id.bt_clock), childAtPosition(allOf(withId(R.id.time_container), childAtPosition(withId(R.id.page1), 8)), 4), isDisplayed()));
        firstPageAppIcon.perform(longClick());
        sleep();
        ViewInteraction baldLinearLayoutButton = onView(allOf(withId(R.id.bt_add_alarm), childAtPosition(allOf(withId(R.id.options_bar), childAtPosition(withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")), 2)), 0), isDisplayed()));
        baldLinearLayoutButton.perform(longClick());
        sleep();
        ViewInteraction baldEditText = onView(allOf(withId(R.id.alarm_edit_name), childAtPosition(childAtPosition(withClassName(is("android.widget.LinearLayout")), 1), 0), isDisplayed()));
        baldEditText.perform(replaceText("n"), closeSoftKeyboard());
        sleep();
        ViewInteraction appCompatImageView = onView(allOf(withId(R.id.up), childAtPosition(childAtPosition(withId(R.id.chooser_hours), 0), 1), isDisplayed()));
        appCompatImageView.perform(click());
        sleep();
        ViewInteraction appCompatCheckBox = onView(allOf(withId(R.id.monday), childAtPosition(childAtPosition(withClassName(is("android.widget.LinearLayout")), 2), 1), isDisplayed()));
        appCompatCheckBox.perform(longClick());
        sleep();
        ViewInteraction appCompatCheckBox2 = onView(allOf(withId(R.id.thursday), childAtPosition(childAtPosition(withClassName(is("android.widget.LinearLayout")), 2), 4), isDisplayed()));
        appCompatCheckBox2.perform(longClick());
        sleep();
        ViewInteraction appCompatRadioButton = onView(allOf(withId(R.id.rb_every_day), isDisplayed()));
        appCompatRadioButton.perform(longClick());
        sleep();
        ViewInteraction baldButton = onView(allOf(withId(R.id.bt_alarm_submit), childAtPosition(childAtPosition(withClassName(is("android.widget.LinearLayout")), 1), 3), isDisplayed()));
        baldButton.perform(longClick());
        sleep();
        ViewInteraction baldPictureTextButton = onView(allOf(withId(R.id.bt_delete), childAtPosition(childAtPosition(withId(R.id.child), 0), 5), isDisplayed()));
        baldPictureTextButton.perform(longClick());
        sleep();
        ViewInteraction baldButton2 = onView(allOf(withId(R.id.dialog_box_true), childAtPosition(childAtPosition(withId(R.id.container), 1), 0), isDisplayed()));
        baldButton2.perform(longClick());
        sleep();
        ViewInteraction baldLinearLayoutButton2 = onView(allOf(withId(R.id.bt_quickly_add_alarm), childAtPosition(allOf(withId(R.id.options_bar), childAtPosition(withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")), 2)), 1), isDisplayed()));
        baldLinearLayoutButton2.perform(longClick());
        sleep();
        ViewInteraction baldButton3 = onView(allOf(withId(R.id.bt_alarm_submit), childAtPosition(childAtPosition(withId(android.R.id.content), 0), 7), isDisplayed()));
        baldButton3.perform(longClick());
        sleep();
        ViewInteraction baldLinearLayoutButton3 = onView(allOf(withId(R.id.bt_cancel_all_alarms), isDisplayed()));
        baldLinearLayoutButton3.perform(longClick());
        sleep();
        ViewInteraction baldButton4 = onView(allOf(withId(R.id.dialog_box_true), childAtPosition(childAtPosition(withId(R.id.container), 1), 0), isDisplayed()));
        baldButton4.perform(longClick());

    }

    @Override
    protected Class<? extends Activity> activity() {
        return HomeScreenActivity.class;
    }
}
