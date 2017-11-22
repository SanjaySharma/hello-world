package com.google.developer.taskmaker;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

/**
 * Created by Sanjay on 23-09-2017.
 */
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);

    private MainActivity mActivity = null ;

    Instrumentation.ActivityMonitor monitor = getInstrumentation().addMonitor(AddTaskActivity.class.getName(),null,false);

    @org.junit.Before
    public void setUp() throws Exception {
        mActivity = mActivityTestRule.getActivity();
    }

    @Test
    public void testLaunchofAddTaskActivityOnFloatingButtonClick()
    {
        assertNotNull(mActivity.findViewById(R.id.fab));

        onView(withId(R.id.fab)).perform(click());

        Activity addTaskActivity = getInstrumentation().waitForMonitorWithTimeout(monitor,5000);

        assertNotNull(addTaskActivity);
    }
    @org.junit.After
    public void tearDown() throws Exception {
        mActivity = null;
    }

}

