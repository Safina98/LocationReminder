package com.udacity.project4

import android.app.Activity
import android.app.Application
import android.os.Parcel
import android.os.Parcelable
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest() :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()


    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                        appContext,
                        get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                        appContext,
                        get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }


//    TODO: add End to End testing to the app
    @Test
    fun addNewReminder_ReminderShowed()= runBlocking{
    val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
    dataBindingIdlingResource.monitorActivity(activityScenario)
    //add fab click, then navigate to save Reminder Fragment
    onView(withId(R.id.addReminderFAB)).perform(click())

    //Save fab click
    onView(withId(R.id.saveReminder)).perform(click())
    //then snackbar shows
    onView(withText(R.string.err_enter_title)).check(matches(isDisplayed()))
    Thread.sleep(2000)
    //type title and description
    onView(withId(R.id.reminderTitle)).perform(typeText("Title"))
    onView(withId(R.id.reminderDescription)).perform(typeText("Description"), closeSoftKeyboard())

    //click save fab
    onView(withId(R.id.saveReminder)).perform(click())
    //then snackbar shows
    onView(withText(R.string.err_select_location)).check(matches(isDisplayed()))
    Thread.sleep(2000)

    onView(withId(R.id.selectLocation)).perform(click())
    onView(withId(R.id.map)).perform(longClick())
    onView(withId(R.id.btnSaveSelectedLocation)).perform(click())


    onView(withId(R.id.saveReminder)).perform(click())

    onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(getActivity(activityScenario)?.window?.decorView)))).check(matches(isDisplayed()))
    onView(withText("Title")).check(matches(isDisplayed()))
    onView(withText("Description")).check(matches(isDisplayed()))
    onView(withText("Unregistered")).check(matches(isDisplayed()))
    activityScenario.close()
}
    @Test
    fun testBackButton(){
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        //add fab click, to navigate to save Reminder Fragment
        onView(withId(R.id.addReminderFAB)).perform(click())
        // click select location to navigate to select location fragmnet
        onView(withId(R.id.selectLocation)).perform(click())

        //press back button
        Espresso.pressBack()
        //check if navigated back to saveReminder fragment
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))

        //fill title and description and select a location
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.btnSaveSelectedLocation)).perform(click())

        //Press back button
        Espresso.pressBack()

        //check if navigate back to ReminderListFragment
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
        activityScenario.close()
    }



}
