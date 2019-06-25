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

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.bald.uriah.baldphone.R;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PillsActivityTest extends BaseActivityTest {

    @Rule
    public ActivityTestRule<HomeScreenActivity> mActivityTestRule = new ActivityTestRule<>(HomeScreenActivity.class, true, false);

    @Test
    public void pillsActivityTest() {

        mActivityTestRule.launchActivity(new Intent());

        sleep();
        ViewInteraction firstPageAppIcon = onView(
                allOf(withId(R.id.bt_reminders),
                        childAtPosition(
                                allOf(withId(R.id.time_container),
                                        childAtPosition(
                                                withId(R.id.page1),
                                                8)),
                                0),
                        isDisplayed()));
        sleep();
        firstPageAppIcon.perform(longClick());

        ViewInteraction baldLinearLayoutButton = onView(
                allOf(withId(R.id.bt_add),
                        childAtPosition(
                                allOf(withId(R.id.options_bar),
                                        childAtPosition(
                                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                                1)),
                                0),
                        isDisplayed()));
        sleep();
        baldLinearLayoutButton.perform(longClick());

        ViewInteraction baldEditText = onView(
                allOf(withId(R.id.reminder_edit_name),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                0),
                        isDisplayed()));
        sleep();
        baldEditText.perform(replaceText("cshev"), closeSoftKeyboard());

        ViewInteraction baldButton = onView(
                allOf(withText("Afternoon"),
                        childAtPosition(
                                allOf(withId(R.id.multiple_selection),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        sleep();
        baldButton.perform(longClick());

        ViewInteraction appCompatCheckBox = onView(
                allOf(withId(R.id.sunday), withText("Sun"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        2),
                                0),
                        isDisplayed()));
        sleep();
        appCompatCheckBox.perform(longClick());

        ViewInteraction appCompatCheckBox2 = onView(
                allOf(withId(R.id.tuesday), withText("Tue"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        2),
                                2),
                        isDisplayed()));
        sleep();
        appCompatCheckBox2.perform(longClick());

        ViewInteraction baldImageButton2 = onView(
                allOf(withId(R.id.green),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3),
                                4),
                        isDisplayed()));
        sleep();
        baldImageButton2.perform(longClick());

        ViewInteraction baldButton2 = onView(
                allOf(withId(R.id.bt_submit), withText("submit"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                4),
                        isDisplayed()));
        sleep();
        baldButton2.perform(longClick());

        ViewInteraction baldLinearLayoutButton2 = onView(
                allOf(withId(R.id.bt_time_changer),
                        childAtPosition(
                                allOf(withId(R.id.options_bar),
                                        childAtPosition(
                                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        sleep();
        baldLinearLayoutButton2.perform(longClick());

        ViewInteraction appCompatImageView = onView(
                allOf(withId(R.id.up), withContentDescription("Down"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.chooser_minutes),
                                        0),
                                1),
                        isDisplayed()));
        sleep();
        appCompatImageView.perform(click());

        pressBack();

        ViewInteraction baldPictureTextButton = onView(
                allOf(withId(R.id.bt_delete),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.recycler_view),
                                        0),
                                4),
                        isDisplayed()));
        sleep();
        baldPictureTextButton.perform(longClick());

        ViewInteraction baldButton3 = onView(
                allOf(withId(R.id.dialog_box_true), withText("Yes"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.container),
                                        1),
                                0),
                        isDisplayed()));
        sleep();
        baldButton3.perform(longClick());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
