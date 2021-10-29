package com.udacity.project4.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.udacity.project4.utils.SingleLiveEvent

/**
 * Base class for View Models to declare the common LiveData objects in one place
 */
abstract class BaseViewModel(app: Application) : AndroidViewModel(app) {

    val navigationCommand: SingleLiveEvent<NavigationCommand> = SingleLiveEvent()
    val showErrorMessage: SingleLiveEvent<String> = SingleLiveEvent() //tested in reminderlistViewmodel
    val showSnackBar: SingleLiveEvent<String> = SingleLiveEvent()//tested in reminderlistViewmodel
    val showSnackBarInt: SingleLiveEvent<Int> = SingleLiveEvent()//tested in reminderlistViewmodel
    val showToast: SingleLiveEvent<String> = SingleLiveEvent() //tested in savereminderviewmodel
    val showLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()//tested in reminderlistViewmodel &savereminderViewModel
    val showNoData: MutableLiveData<Boolean> = MutableLiveData() //tested in reminderlistViewmodel

}