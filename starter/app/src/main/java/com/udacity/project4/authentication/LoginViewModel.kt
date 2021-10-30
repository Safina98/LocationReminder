package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

class LoginViewModel : ViewModel() {
    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }
    //Check if the user logged in

    val authenticationState = FirebaseUserLiveData().map {
        if (it!=null){
                AuthenticationState.AUTHENTICATED
        }else  AuthenticationState.UNAUTHENTICATED
    }
}