package com.udacity.project4.locationreminders.data.local

import androidx.lifecycle.MutableLiveData
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.runBlocking
import java.util.LinkedHashMap

class ReminderLocalRepositoryFake():ReminderDataSource {
    var remindersServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    private val observableReminders = MutableLiveData<Result<List<ReminderDTO>>>()

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    suspend fun refreshReminder() {
        observableReminders.value = getReminders()
    }

    // TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        if (shouldReturnError) {
            return Result.Error("Test exception",0)
        }
        return Result.Success(remindersServiceData.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersServiceData[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError){
            return Result.Error("Error",0)
        }
        remindersServiceData[id]?.let {
            return Result.Success(it)
        }
        return Result.Error("Reminder not found!",0)

    }

    override suspend fun deleteAllReminders() {
        remindersServiceData.clear()
        refreshReminder()
    }
    fun addReminder(vararg reminders: ReminderDTO){
        for (reminder in reminders){
            remindersServiceData[reminder.id] = reminder
        }
        runBlocking { refreshReminder() }
    }

}