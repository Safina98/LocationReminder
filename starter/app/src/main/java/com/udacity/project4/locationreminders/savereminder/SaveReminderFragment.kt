package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

const val GEOFENCE_RADIUS_IN_METERS = 100f
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient


    //pending intent
    private val geofencePendingIntent :PendingIntent by lazy{
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    }


    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)
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

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
           // addNewGeofence(reminderDataItem)
            if(_viewModel.validateEnteredData(reminderDataItem)){
                //addNewGeofence(reminderDataItem)
                checkDeviceLocationSettingsAndStartGeofence(reminderDataItem)
            }
        }
    }
    private fun checkDeviceLocationSettingsAndStartGeofence(reminderDataItem: ReminderDataItem,resolve:Boolean = true) {
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
                    exception.startResolutionForResult(requireActivity(),
                            REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("TAG", "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                        this.view!!,
                        R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence(reminderDataItem)
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
               addNewGeofence(reminderDataItem)
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun addNewGeofence(reminderDataItem: ReminderDataItem){
        //build geofence object
        val geofence = Geofence.Builder()
                .setRequestId(reminderDataItem.id)
                .setCircularRegion(
                        reminderDataItem.latitude!!,
                        reminderDataItem.longitude!!,
                        GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

        //build geofence request to trigger geofence
        val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                    addOnCompleteListener {
                        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                            addOnSuccessListener {
                                //Toast.makeText(context, "Geofence Added", Toast.LENGTH_SHORT).show()
                                Log.e("Add Geofence", geofence.requestId)
                                _viewModel.saveReminder(reminderDataItem)

                            }
                            addOnFailureListener {
                                Toast.makeText(context, R.string.geofences_not_added,
                                        Toast.LENGTH_SHORT).show()
                                if ((it.message != null)) {
                                    Log.w("GEOFENCE_F", it.toString())
                                }
                            }
                        }
                    }
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

