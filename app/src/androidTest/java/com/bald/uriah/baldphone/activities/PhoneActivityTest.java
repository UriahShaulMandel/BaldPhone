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
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PhoneActivityTest extends BaseActivityTest {
    @Rule
    public ActivityTestRule<HomeScreenActivity> mActivityTestRule = new ActivityTestRule<>(HomeScreenActivity.class, true, false);

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
    public void phoneActivityTest() {
        mActivityTestRule.launchActivity(new Intent());
        ViewInteraction firstPageAppIcon = onView(allOf(withId(R.id.bt_dialer), childAtPosition(allOf(withId(R.id.phone_container), childAtPosition(withId(R.id.page1), 2)), 2), isDisplayed()));
        sleep();
        firstPageAppIcon.perform(longClick());
        ViewInteraction baldButton = onView(allOf(withId(R.id.b_0), withText("0"), childAtPosition(allOf(withId(R.id.include), childAtPosition(withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")), 2)), 10), isDisplayed()));
        sleep();
        baldButton.perform(longClick());
        ViewInteraction baldButton2 = onView(allOf(withId(R.id.b_5), withText("5"), childAtPosition(allOf(withId(R.id.include), childAtPosition(withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")), 2)), 4), isDisplayed()));
        sleep();
        baldButton2.perform(longClick());
        ViewInteraction baldButton3 = onView(allOf(withId(R.id.b_4), withText("4"), childAtPosition(allOf(withId(R.id.include), childAtPosition(withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")), 2)), 3), isDisplayed()));
        sleep();
        baldButton3.perform(longClick());
        ViewInteraction baldLinearLayoutButton = onView(allOf(withId(R.id.empty_view), childAtPosition(allOf(withId(R.id.scrolling_helper), childAtPosition(withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")), 3)), 2), isDisplayed()));
        sleep();
        baldLinearLayoutButton.perform(longClick());
        ViewInteraction baldEditText = onView(allOf(withId(R.id.et_name), childAtPosition(childAtPosition(withId(android.R.id.content), 0), 1), isDisplayed()));
        sleep();
        baldEditText.perform(replaceText("v"), closeSoftKeyboard());
        ViewInteraction baldButton4 = onView(allOf(withId(R.id.save), childAtPosition(childAtPosition(withId(android.R.id.content), 0), 8), isDisplayed()));
        sleep();
        baldButton4.perform(longClick());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ViewInteraction baldLinearLayoutButton2 = onView(allOf(withId(R.id.bt_edit), childAtPosition(allOf(withId(R.id.options_bar), childAtPosition(withId(R.id.ll_info), 0)), 0), isDisplayed()));
        sleep();
        baldLinearLayoutButton2.perform(longClick());
        ViewInteraction baldEditText2 = onView(allOf(withId(R.id.et_mail), childAtPosition(childAtPosition(withId(android.R.id.content), 0), 7), isDisplayed()));
        sleep();
        baldEditText2.perform(replaceText("hh"), closeSoftKeyboard());
        ViewInteraction baldButton5 = onView(allOf(withId(R.id.save), childAtPosition(childAtPosition(withId(android.R.id.content), 0), 8), isDisplayed()));
        sleep();
        baldButton5.perform(longClick());
        sleep();
        ViewInteraction baldLinearLayoutButton3 = onView(allOf(withId(R.id.bt_delete), childAtPosition(allOf(withId(R.id.options_bar), childAtPosition(withId(R.id.ll_info), 0)), 2), isDisplayed()));
        sleep();
        baldLinearLayoutButton3.perform(longClick());
        ViewInteraction baldButton6 = onView(allOf(withId(R.id.dialog_box_true), childAtPosition(childAtPosition(withId(R.id.container), 1), 0), isDisplayed()));
        sleep();
        baldButton6.perform(longClick());
        pressBack();
    }
}
