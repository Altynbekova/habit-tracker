package com.example.habittracker.ui;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.habittracker.MainActivity;
import com.example.habittracker.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class HabitDetailTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

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

    @Test
    public void habitDetailTest() {
        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.recyclerViewHabits),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                2)));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction viewGroup = onView(
                allOf(withParent(allOf(withId(R.id.fragment_container),
                                withParent(withId(R.id.fragment_container)))),
                        isDisplayed()));
        viewGroup.check(matches(isDisplayed()));

        ViewInteraction linearLayout = onView(
                allOf(withId(R.id.segmentedButtonGroup),
                        withParent(withParent(withId(R.id.fragment_container))),
                        isDisplayed()));
        linearLayout.check(matches(isDisplayed()));

        ViewInteraction cardView = onView(
                allOf(withId(R.id.streakCard),
                        withParent(withParent(withId(R.id.fragment_container))),
                        isDisplayed()));
        cardView.check(matches(isDisplayed()));

        ViewInteraction textView = onView(
                allOf(withId(R.id.textDetailName), withText("Зарядка"),
                        withParent(withParent(withId(R.id.streakCard))),
                        isDisplayed()));
        textView.check(matches(isDisplayed()));

        ViewInteraction progressBar = onView(
                allOf(withId(R.id.streakProgress),
                        withParent(withParent(withId(R.id.streakCard))),
                        isDisplayed()));
        progressBar.check(matches(isDisplayed()));

        ViewInteraction linearLayout2 = onView(
                allOf(withId(R.id.descriptionSection),
                        withParent(withParent(IsInstanceOf.instanceOf(android.view.ViewGroup.class))),
                        isDisplayed()));
        linearLayout2.check(matches(isDisplayed()));

        ViewInteraction textView3 = onView(
                allOf(withText("О привычке"),
                        withParent(allOf(withId(R.id.descriptionSection),
                                withParent(IsInstanceOf.instanceOf(android.widget.ScrollView.class)))),
                        isDisplayed()));
        textView3.check(matches(withText("О привычке")));

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.textLongDescription), withText("Утренняя зарядка 15 минут"),
                        withParent(allOf(withId(R.id.descriptionSection),
                                withParent(IsInstanceOf.instanceOf(android.widget.ScrollView.class)))),
                        isDisplayed()));
        textView4.check(matches(withText("Утренняя зарядка 15 минут")));

        ViewInteraction cardView3 = onView(
                allOf(withId(R.id.notificationCard),
                        withParent(allOf(withId(R.id.descriptionSection),
                                withParent(IsInstanceOf.instanceOf(android.widget.ScrollView.class)))),
                        isDisplayed()));
        cardView3.check(matches(isDisplayed()));

        ViewInteraction switch_ = onView(
                allOf(withId(R.id.switchNotification), withText("ВКЛ"),
                        withParent(withParent(withId(R.id.notificationCard))),
                        isDisplayed()));
        switch_.check(matches(isDisplayed()));

        ViewInteraction view = onView(
                allOf(withId(android.R.id.navigationBarBackground),
                        withParent(IsInstanceOf.instanceOf(android.widget.FrameLayout.class)),
                        isDisplayed()));
        view.check(matches(isDisplayed()));
    }
}
