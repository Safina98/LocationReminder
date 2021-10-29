package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.authentication.FirebaseUserLiveData
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Config.OLDEST_SDK])
class RemindersListViewModelTest  {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    private lateinit var tasksViewModel: RemindersListViewModel
    private lateinit var tasksRepository: FakeDataSource
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    @Before
    fun setupViewModel(){
        stopKoin()
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        tasksRepository =FakeDataSource()
        val reminder1 = ReminderDTO("Title1","Desc1","loc1",0.0,0.0)
        val reminder2 = ReminderDTO("Title1","Desc2","loc2",0.0,0.0)

        tasksRepository.addReminder(reminder1)
        tasksRepository.addReminder(reminder2)
        tasksViewModel = RemindersListViewModel( ApplicationProvider.getApplicationContext(),tasksRepository)
    }


    @Test
    fun loadTask_checkShowLoadingValue(){
        //given view model
        //when loading reminder
        mainCoroutineRule.pauseDispatcher()
        tasksViewModel.loadReminders()
        //then show loading value is true
        assertThat(tasksViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        // after load finnished then show loading value is false
        assertThat(tasksViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    //workded
    @Test
    fun resultError_checkShouldReturnError(){
        //GIVEN
        tasksRepository.setReturnError(true)
        //WHEN load reminders called
        tasksViewModel.loadReminders()
        //THEN show no data value is true and showSnacBar value updated
        assertThat(tasksViewModel.showNoData.getOrAwaitValue(),`is`(true))
        assertThat(tasksViewModel.showSnackBar.getOrAwaitValue(),`is`("Error"))
    }

    @Test
    fun noData_checkShoeNoDataValue() = runBlockingTest{
        //GIVEN no reminders
        tasksRepository.deleteAllReminders()
        //WHEN load remiders
        tasksViewModel.loadReminders()
        //THEN show no data value is true and showSnacBar value updated
        assertThat(tasksViewModel.showNoData.getOrAwaitValue(),`is`(true))
    }



}