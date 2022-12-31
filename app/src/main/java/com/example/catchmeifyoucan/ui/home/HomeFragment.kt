package com.example.catchmeifyoucan.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.activities.HomeActivity
import com.example.catchmeifyoucan.databinding.FragmentHomeBinding
import com.example.catchmeifyoucan.geofence.GeofenceBroadcastReceiver
import com.example.catchmeifyoucan.ui.BaseFragment
import com.example.catchmeifyoucan.ui.runs.RunsModel
import com.example.catchmeifyoucan.utils.PermissionsUtil.approveForegroundAndBackgroundLocation
import com.example.catchmeifyoucan.utils.PermissionsUtil.approveForegroundLocation
import com.example.catchmeifyoucan.utils.PermissionsUtil.runningQOrLater
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import javax.inject.Inject

class HomeFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        private val TAG = HomeFragment::class.java.simpleName
        const val ACTION_GEOFENCE_EVENT = "ACTION_GEOFENCE_EVENT"
        private const val DEFAULT_ZOOM = 15f
        const val DEFAULT_GEOFENCE_RADIUS = 5f
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeFragmentViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var cancellationSource: CancellationTokenSource
    private lateinit var map: GoogleMap
    private lateinit var geofencingClient: GeofencingClient
    private var runButtonMotionStarted = false
    private var recordButtonMotionStarted = false

    private val geoPendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private val run = RunsModel(time = 0.00)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val result = permissions.entries.all {
            it.value
        }
        if (result) {
            enableMyLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeFragmentViewModel::class.java]
        binding.apply {
            homeFragmentViewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initView()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        cancellationSource = CancellationTokenSource()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as HomeActivity).unlockNavigationDrawer()
    }

    private fun initView() {
        (requireActivity() as HomeActivity).hideToolbar()
        (requireActivity() as HomeActivity).setSupportActionBar(binding.toolbar)
        val drawerLayout = (requireActivity() as HomeActivity).findViewById<DrawerLayout>(R.id.drawer_layout)
        val drawerToggle =
            ActionBarDrawerToggle(requireActivity(),
                drawerLayout, binding.toolbar,
                R.string.content_nav_open, R.string.content_nav_closed)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        (requireActivity() as HomeActivity).title = getString(R.string.app_name)
        (requireActivity() as HomeActivity).setUserEmail()

        binding.runButton.setOnClickListener {
            runButtonMotionStarted = if (runButtonMotionStarted) {
                binding.motionLayout.setTransition(R.id.run_end, R.id.run_start).run {
                    binding.motionLayout.transitionToStart()
                    false
                }
            } else {
                binding.motionLayout.setTransition(R.id.run_start, R.id.run_end).run {
                    binding.motionLayout.transitionToEnd()
                    checkLocationPermissions()
                    true
                }
            }
        }

        binding.recordButton.setOnClickListener {
            recordButtonMotionStarted = if (recordButtonMotionStarted) {
                binding.motionLayout.setTransition(R.id.record_end, R.id.record_start).run {
                    binding.motionLayout.transitionToStart()
                    false
                }
            } else {
                binding.motionLayout.setTransition(R.id.record_start, R.id.record_end).run {
                    binding.motionLayout.transitionToEnd()
                    true
                }
            }
//            saveGeofenceForLocationReminder(run)
        }

        binding.raceButton.setOnClickListener {
            findNavController().navigate(R.id.run_history_fragment)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (approveForegroundLocation(requireContext())) {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true
            val locationResult = fusedLocationProviderClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationSource.token
            )
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful && task.result != null) {
                    val latLng = LatLng(task.result.latitude, task.result.longitude)
                    run.start_lat = task.result.latitude
                    run.start_lng = task.result.longitude
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
                }
            }
        } else {
            requestForegroundLocationPermissions()
        }
    }

    private fun checkDeviceLocationSettings(resolve:Boolean = true) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, 1000)
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest.build())
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                        if (result != null) {
                            Timber.i("${result.resultCode}")
                            checkDeviceLocationSettings(false)
                        }
                    }.launch(intentSenderRequest)
                } catch (e: IntentSender.SendIntentException) {
                    Timber.e("location settings resolution error: %s", e.cause)
                }
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Timber.i("location settings response success")
            }
        }
    }

    private fun checkLocationPermissions() {
        if (approveForegroundAndBackgroundLocation(requireContext())) {
            checkDeviceLocationSettings()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }


    private fun requestForegroundLocationPermissions() {
        if (approveForegroundAndBackgroundLocation(requireContext()))
            return
        val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        requestPermissionLauncher.launch(permissionsArray)
    }

    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (approveForegroundAndBackgroundLocation(requireContext()))
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (runningQOrLater) permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION

        requestPermissionLauncher.launch(permissionsArray)
    }

    @SuppressLint("MissingPermission")
    private fun saveGeofenceForLocationReminder(run: RunsModel) {
        val geofence = Geofence.Builder()
            .setRequestId(run.id)
            .setCircularRegion(
                run.start_lat!!,
                run.start_lng!!,
                DEFAULT_GEOFENCE_RADIUS
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        if (approveForegroundLocation(requireContext())) {
            geofencingClient.addGeofences(geofencingRequest, geoPendingIntent)
        }
    }
}