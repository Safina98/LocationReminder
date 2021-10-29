package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Exception

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var localDataSource: RemindersLocalRepository

    @Before
    fun initiate(){
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        localDataSource = RemindersLocalRepository(database.reminderDao(),Dispatchers.Main)
    }
    @After
    fun close_DB()=database.close()
    //save a reminder then retrive the reminder by id
    @Test
    fun saveReminder_GetReminderById()= runBlocking{
        val reminder = ReminderDTO("Title","Desc","Location",0.0,0.0)
        //save reminder
        localDataSource.saveReminder(reminder)
        //retrieve reminder by id
        val loaded = localDataSource.getReminder(reminder.id)

        //THEN reminder retrieved
        assertThat(loaded.succeeded, `is`(true))
        loaded as Result.Success

        assertThat(loaded.data.id,`is`(reminder.id))
        assertThat(loaded.data.title,`is`(reminder.title))
        assertThat(loaded.data.description,`is`(reminder.description))
        assertThat(loaded.data.location,`is`(reminder.location))
        assertThat(loaded.data.latitude,`is`(reminder.latitude))
        assertThat(loaded.data.longitude,`is`(reminder.longitude))
    }

    //when id not found in the database,then getRemiderById return error message
    @Test
    fun gerReminderById_noDataFound()= runBlocking{
        //when reminder ID not found
        val loaded = localDataSource.getReminder("ID")

        //THEN getReminder return an error message
        assertThat(loaded.succeeded, `is`(false))
        loaded as Result.Error
        assertThat(loaded.message,`is`("Reminder not found!"))
    }

    //when saving multiple reminders,  getReminders should return all saved reminders
    @Test
    fun saveMultipleReminder_getAllReminder()= runBlocking{
        val reminder1 = ReminderDTO("Title1","Desc1","Location1",0.1,0.1)
        val reminder2 = ReminderDTO("Title2","Desc2","Location2",0.2,0.2)
        val reminder3 = ReminderDTO("Title3","Desc3","Location3",0.3,0.3)
        //save multiple remindes
        localDataSource.saveReminder(reminder1)
        localDataSource.saveReminder(reminder2)
        localDataSource.saveReminder(reminder3)
        //WHEN reminder getRemindres called
        val loaded =localDataSource.getReminders()
        //THEN all reminder retireved
        assertThat(loaded.succeeded, `is`(true))
        assertThat(loaded as Result.Success , notNullValue())
        assertThat(loaded.data.size,`is`(3))
        assertThat(loaded.data.contains(reminder1),`is`(true))
        assertThat(loaded.data.contains(reminder2),`is`(true))
        assertThat(loaded.data.contains(reminder3),`is`(true))
    }

    //when deleteAllReminderCalled, then all data from database deleted, hence getReminders retrun empty list
    @Test
    fun noData_ReturnError()= runBlocking{

        //WHEN deleteAllReminders called
        localDataSource.deleteAllReminders()
        //Then no data retrieved
        val loaded =localDataSource.getReminders()
        assertThat(loaded.succeeded,`is`(true))
        assertThat(loaded as Result.Success, notNullValue())
        assertThat(loaded.data, `is`(emptyList()))

    }




}