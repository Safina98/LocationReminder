package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.content.Context
import android.provider.Settings.Global.getString
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import junit.framework.TestCase.assertNull

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)

class SaveReminderViewModelTest {
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var tasksRepository: FakeDataSource
    private lateinit var appContext:Application
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel(){
        stopKoin()
        tasksRepository = FakeDataSource()
        appContext =ApplicationProvider.getApplicationContext()
        saveReminderViewModel =SaveReminderViewModel( ApplicationProvider.getApplicationContext(),tasksRepository)

    }

    @Test
    fun setLanLng_liveDataValueUpdated() {
        // Given a fresh ViewModel
        // When set selected position is called
        saveReminderViewModel.setSelectedPosition(LatLng(37.819927, -122.478256),"Unregistered")

        // Then the mutable live data value updated
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(),`is`(37.819927))
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(),`is`(-122.478256))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(),`is`("Unregistered"))
    }

    @Test
    fun setPoi_LiveDataValueUpdated(){
        //given a poi
        val poi = PointOfInterest(LatLng(37.819927, -122.478256),"ID","Location")

        // When setSelectedPoi is called
        saveReminderViewModel.setSelectedPoi(poi)
        //Then mutable live data value updated
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue(),`is`(poi))
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), `is`(37.819927))
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), `is`(-122.478256))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`("Location"))
    }
    @Test
    fun clear_LiveDataIsNull(){
        //given poi
        val poi = PointOfInterest(LatLng(0.0, 0.0),"-","-")
        saveReminderViewModel.setSelectedPoi(poi)

        //when onClear Called
        saveReminderViewModel.onClear()

        //Then live data value updated to null
        assertNull(saveReminderViewModel.selectedPOI.getOrAwaitValue())
        assertNull(saveReminderViewModel.latitude.getOrAwaitValue())
        assertNull(saveReminderViewModel.longitude.getOrAwaitValue())
        assertNull(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue())
    }
    @Test
    fun checkNullTitle_validateEnteredDataReturnFalse(){
        //GIVEN ReminderDataItem with null title
        var reminder = ReminderDataItem(null,"desc","Location",0.0,0.0)
        //WHEN  validateEnteredData called
        val bool = saveReminderViewModel.validateEnteredData(reminder)
        //THEN bool is false and showSnackBarInt value updated
        assertThat(bool,`is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),`is`(R.string.err_enter_title))

    }
    @Test
    fun checkEmptyTitle_EnteredDataReturnFalse(){
        //GIVEN ReminderDataItem with empty string title
        var reminder = ReminderDataItem(String(),"desc","Location",0.0,0.0)
        //WHEN  validateEnteredData called
        val bool = saveReminderViewModel.validateEnteredData(reminder)
        //THEN bool is false and showSnackBarInt value updated
        assertThat(bool,`is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),`is`(R.string.err_enter_title))
    }
    @Test
    fun checkNullLocation_validateEnteredDataReturnFalse(){
        //GIVEN ReminderDataItem with null string location
        var reminder = ReminderDataItem("Reminder","desc",null,0.0,0.0)
        //WHEN  validateEnteredData called
        val bool = saveReminderViewModel.validateEnteredData(reminder)
        //THEN bool is false and showSnackBarInt value updated
        assertThat(bool,`is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),`is`(R.string.err_select_location))

    }
    @Test
    fun checkEmptyLocation_validateEnteredDataReturnFalse(){
        //GIVEN ReminderDataItem with empty string location
        var reminder = ReminderDataItem("Title","desc",String(),0.0,0.0)
        //WHEN validateEnteredData called
        val bool = saveReminderViewModel.validateEnteredData(reminder)

        assertThat(bool,`is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),`is`(R.string.err_select_location))
    }
    @Test
    fun checkvalidReminder_validateEnteredDataReturnTrue(){
        //GIVEN a ReminderDataItem
        var reminder = ReminderDataItem("Reminder","desc","Location",0.0,0.0)
        //WHEN validateEnteredDataCalled
        val bool = saveReminderViewModel.validateEnteredData(reminder)
        //THEN bool is true
        assertThat(bool,`is`(true))
    }
    @Test
    fun savingData_LoadingToastLivedataValueUpdated(){
        //given view model

        var reminder = ReminderDataItem("Reminder","desc","Location",0.0,0.0)
        //when saving reminder
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminder)
        //then show loading value is true
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(),`is`(true))
        // after load finnished then show loading value is false and showToastValueUpdated
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(),`is`(false))
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(),`is`(appContext.getString(R.string.reminder_saved)))
    }
}