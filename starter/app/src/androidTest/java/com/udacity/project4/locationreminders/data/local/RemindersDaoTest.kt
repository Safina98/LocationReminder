package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initiate_DB(){
        database = Room.inMemoryDatabaseBuilder(
                getApplicationContext(),
                RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

    }


    @After
    fun close_DB() = database.close()

    //insert a data then get the inserted data by id
    @Test
    fun insert_getByID()= runBlockingTest{

        val reminder = ReminderDTO("Title","Desc","Location",0.0,0.0)
        //Save reminder
        database.reminderDao().saveReminder(reminder)
        //Retrive reminder
        val loaded = database.reminderDao().getReminderById(reminder.id)
        //THEN reminder retrieved
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id,`is`(reminder.id))
        assertThat(loaded.title,`is`(reminder.title))
        assertThat(loaded.description,`is`(reminder.description))
        assertThat(loaded.location,`is`(reminder.location))
        assertThat(loaded.latitude,`is`(reminder.latitude))
        assertThat(loaded.longitude,`is`(reminder.longitude))
    }

    //when id not found in the database,then getRemiderById return null
    @Test
    fun retriveId_dataNotFound()= runBlockingTest{
        // load reminder when no id found
        val loaded  = database.reminderDao().getReminderById("testid")
        //THEN return null value
        assertThat(loaded, nullValue())
    }

    // when inserting multiple reminders,  getReminders should return all saved reminders
    @Test
    fun saveMultipleReminder_getAllReminder()= runBlockingTest{

        val reminder1 = ReminderDTO("Title1","Desc1","Location1",0.1,0.1)
        val reminder2 = ReminderDTO("Title2","Desc2","Location2",0.2,0.2)
        val reminder3 = ReminderDTO("Title3","Desc3","Location3",0.3,0.3)

       //Save multiple reminder
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        //WHEN getReminders called
        val loaded = database.reminderDao().getReminders()
        //THEN all reminders retrieved
        assertThat(loaded.size,`is`(3))
        assertThat(loaded.contains(reminder1),`is`(true))
        assertThat(loaded.contains(reminder2),`is`(true))
        assertThat(loaded.contains(reminder3),`is`(true))
    }

    //when deleteAllReminderCalled, then all data from database deleted, hence getReminders retrun empty list
    @Test
    fun  deleteAllReminders_getAllReminderEmtpy()= runBlockingTest{
        //given
        val reminder = ReminderDTO("Title","Desc","Location",0.0,0.0)
        database.reminderDao().saveReminder(reminder)
        //WHEN deleteAllReminderCalled
        database.reminderDao().deleteAllReminders()
        //THEN database is empty
        val loaded = database.reminderDao().getReminders()
        assertThat(loaded, `is`(emptyList()))
    }




//    TODO: Add testing implementation to the RemindersDao.kt

}