package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.Geofence.NEVER_EXPIRE
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.base.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.*
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

const val GEOFENCE_RADIUS_IN_METERS = 800f


private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
//private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
//private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private val TAG = SaveReminderFragment::class.java.simpleName
    private val resultLauncher =registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            checkDeviceLocationSettingsAndStartGeofence()
        }

    }




    //pending intent
    private val geofencePendingIntent :PendingIntent by lazy{
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        //Geofencing client
        geofencingClient = LocationServices.getGeofencingClient(requireContext())


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value
            val reminderDataItem = ReminderDataItem(title,description,location,latitude,longitude)
            _viewModel.setReminderDataItem(reminderDataItem)

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
           // addNewGeofence(reminderDataItem)
            if(_viewModel.validateEnteredData(reminderDataItem)){
                //addNewGeofence(reminderDataItem)
                    if (isPermissionGranted()) {
                        checkDeviceLocationSettingsAndStartGeofence()
                    }else{requestForegroundAndBackgroundLocationPermissions()
                    }

            }
        }
    }
    fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask =
                settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    resultLauncher.launch(intentSenderRequest)

                   // exception.startResolutionForResult(requireActivity(), REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("TAG", "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                        this.requireView(),
                        R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
               addNewGeofence()
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun addNewGeofence(){
        //build geofence object
        val geofence = Geofence.Builder()
                .setRequestId(_viewModel.reminderDataItem.value?.id)
                .setCircularRegion(
                    _viewModel.reminderDataItem.value!!.latitude!!,
                    _viewModel.reminderDataItem.value!!.longitude!!,
                        GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

        //build geofence request to trigger geofence
        val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                _viewModel.saveReminder()
                Snackbar.make(
                    binding.root,
                    R.string.geofences_added, Snackbar.LENGTH_SHORT
                ).show()

            }
            addOnFailureListener {
                Toast.makeText(context, R.string.geofences_not_added, Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isEmpty()||
            grantResults[0]==PackageManager.PERMISSION_DENIED||
            (requestCode== REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[1]==PackageManager.PERMISSION_DENIED)){
            Snackbar.make(this.requireView(),
            R.string.permission_denied_explanation,
            Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.settings){
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", requireContext().packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                }.show()
        }else{
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }



    override fun onDestroy() {
                super.onDestroy()
                //make sure to clear the view model after destroy, as it's a single view model.
                _viewModel.onClear()
            }
            companion object {
            internal const val ACTION_GEOFENCE_EVENT =
                    "SaveReminderFragment.action.ACTION_GEOFENCE_EVENT"
        }
}

