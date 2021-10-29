package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.ReminderLocalRepositoryFake
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest(),KoinTest {


    private lateinit var repository: ReminderDataSource

    private lateinit var viewModel: RemindersListViewModel
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        // Stop the original app koin.
        stopKoin()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                        getApplicationContext(),
                        get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(getApplicationContext()) }
        }

        startKoin {
            modules(listOf(myModule))
        }

        repository = get()

        runBlocking {
            repository.deleteAllReminders()
        }
    }



    //    TODO: test the displayed data on the UI.

    //when new data added, the the data dispalyed in UI
    @Test
    fun addData_DisplayedInUi() = runBlocking{
        // GIVEN - add new task
        val reminder1 = ReminderDTO("Title","Desc","Location",0.0,0.0)
        repository.saveReminder(reminder1)
        //WHEN fragment launched
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        //Then data displayed in the ui
        onView(withText("Title")).check(ViewAssertions.matches(isDisplayed()))
        onView(withText("Desc")).check(ViewAssertions.matches(isDisplayed()))
        onView(withText("Location")).check(ViewAssertions.matches(isDisplayed()))

        Thread.sleep(2000)

    }

    //when the there is no data, then noDataTextView is displayed in UI
    @Test
    fun noData_NoDataDisplayedInUi(){
        // GIVEN - no data
        //WHEN fragment launched
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        //Then data displayed in the ui
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(isDisplayed()))
    }

//    TODO: test the navigation of the fragments.

    //
    @Test
    fun addReminderFABClicked_NavigateToSaveReminder(){
    //Given
    val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
    val navController = mock(NavController::class.java)
    scenario.onFragment {
        Navigation.setViewNavController(it.view!!, navController)
    }
    //WHEN add fab clicked
    onView(withId(R.id.addReminderFAB)).perform(click())

    //THEN navigate to savereminderfragment
    verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

//    TODO: add testing for the error messages.
}