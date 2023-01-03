package com.example.catchmeifyoucan.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.activities.HomeActivity
import com.example.catchmeifyoucan.databinding.FragmentHomeBinding
import com.example.catchmeifyoucan.ui.BaseFragment
import com.example.catchmeifyoucan.utils.PermissionsUtil.approveForegroundLocation
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.lang.String.*
import java.util.*
import javax.inject.Inject


class HomeFragment : BaseFragment(), OnMapReadyCallback, SensorEventListener {

    companion object {
        private val TAG = HomeFragment::class.java.simpleName
        const val ACTION_GEOFENCE_EVENT = "ACTION_GEOFENCE_EVENT"
        private const val DEFAULT_ZOOM = 18f
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeFragmentViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var cancellationSource: CancellationTokenSource
    private lateinit var map: GoogleMap
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var mHandler: Handler

    private var startRecording = false
    private var runButtonMotionStarted = false
    private var recordButtonMotionStarted = false

//    private val geoPendingIntent: PendingIntent by lazy {
//        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
//        intent.action = ACTION_GEOFENCE_EVENT
//        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.all {
            when (it.key.replace("android.permission.", "")) {
                "ACCESS_FINE_LOCATION" -> {
                    if (it.value) {
                        enableMyLocation()
                    } else {
                        Snackbar.make(
                            requireView(),
                            R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                        ).setAction(android.R.string.ok) {
                            enableMyLocation()
                        }.show()
                    }
                }
                "ACTIVITY_RECOGNITION" -> {
                    if (it.value) {
                        setupStepCounterListener()
                    } else {
                        Snackbar.make(
                            requireView(),
                            R.string.activity_recognition_required_error, Snackbar.LENGTH_INDEFINITE
                        ).setAction(android.R.string.ok) {
                            requestActivityRecognitionPermissions()
                        }.show()
                    }
                }
            }
            it.value
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

        Timber.i("${requireActivity()}")
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
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }

    private fun initView() {
        binding.runButton.setOnClickListener {
            runButtonMotionStarted = if (runButtonMotionStarted) {
                binding.motionLayout.setTransition(R.id.run_end, R.id.run_start).run {
                    binding.motionLayout.transitionToStart()
                    false
                }
            } else {
                binding.motionLayout.setTransition(R.id.run_start, R.id.run_end).run {
                    binding.motionLayout.transitionToEnd()
                    requestActivityRecognitionPermissions()
                    true
                }
            }
        }

        binding.recordButton.setOnClickListener {
            recordButtonMotionStarted = if (recordButtonMotionStarted) {
                binding.motionLayout.setTransition(R.id.record_end, R.id.record_start).run {
                    startRecording = false
                    stopTimer()
                    viewModel.setRunLocationList()
                    binding.motionLayout.transitionToStart()
                    getEndLocationLatLng()
                    false
                }
            } else {
                binding.motionLayout.setTransition(R.id.record_start, R.id.record_end).run {
                    startRecording = true
                    binding.motionLayout.transitionToEnd()
                    object : CountDownTimer(3000, 1000) {

                        @SuppressLint("SetTextI18n")
                        override fun onTick(millisUntilFinished: Long) {
                            binding.countdownOverlay.visibility = View.VISIBLE
                            binding.countdownOverlay.text = ((millisUntilFinished+1000)/1000).toString()
                        }
                        override fun onFinish() {
                            binding.countdownOverlay.visibility = View.GONE
                            addTimestamp()
                            startTimer()
                        }
                    }.start()
                    getStartLocationLatLng()
                    true
                }
            }
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
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
                }
            }
            setupLocationChangeListener()
        } else {
            requestForegroundLocationPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getStartLocationLatLng() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            viewModel.setStartLatLng(LatLng(it.result.latitude, it.result.longitude))
        }
    }

    @SuppressLint("MissingPermission")
    private fun getEndLocationLatLng() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            viewModel.setEndLatLng(LatLng(it.result.latitude, it.result.longitude))
            createRunData()
        }
    }

//    private fun checkDeviceLocationSettings(resolve:Boolean = true) {
//        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, 1000)
//        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest.build())
//        val settingsClient = LocationServices.getSettingsClient(requireContext())
//        val locationSettingsResponseTask =
//            settingsClient.checkLocationSettings(builder.build())
//        locationSettingsResponseTask.addOnFailureListener { exception ->
//            if (exception is ResolvableApiException && resolve){
//                try {
//                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution.intentSender).build()
//                    registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
//                        if (result != null) {
//                            Timber.i("${result.resultCode}")
//                            checkDeviceLocationSettings(false)
//                        }
//                    }.launch(intentSenderRequest)
//                } catch (e: IntentSender.SendIntentException) {
//                    Timber.e("location settings resolution error: %s", e.cause)
//                }
//            } else {
//                Snackbar.make(
//                    requireView(),
//                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
//                ).setAction(android.R.string.ok) {
//                    checkDeviceLocationSettings()
//                }.show()
//            }
//        }
//        locationSettingsResponseTask.addOnCompleteListener {
//            if (it.isSuccessful) {
//                Timber.i("location settings response success")
//            }
//        }
//    }

//    private fun checkLocationPermissions() {
//        if (approveForegroundAndBackgroundLocation(requireContext())) {
//            checkDeviceLocationSettings()
//        } else {
//            requestForegroundAndBackgroundLocationPermissions()
//        }
//    }


    private fun requestForegroundLocationPermissions() {
        val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermissionLauncher.launch(permissionsArray)
    }

//    private fun requestForegroundAndBackgroundLocationPermissions() {
//        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
//        if (runningQOrLater) permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
//        requestPermissionLauncher.launch(permissionsArray)
//    }

    private fun requestActivityRecognitionPermissions() {
        requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION))
    }

    private fun addTimestamp() {
        val tsLong = System.currentTimeMillis() / 1000
        viewModel.setTimestamp(tsLong.toString())
    }


    private fun startTimer() {
        mHandler = Handler(Looper.getMainLooper())
        mStatusChecker.run()
    }

    private fun stopTimer() {
        viewModel.setRunTime()
        viewModel.seconds = 0
        mHandler.removeCallbacks(mStatusChecker)
    }

    private var mStatusChecker: Runnable = object : Runnable {
        override fun run() {
            try {
                val hours: Int = viewModel.seconds / 3600
                val minutes: Int = viewModel.seconds % 3600 / 60
                val secs: Int = viewModel.seconds % 60

                val time: String = format(
                    Locale.getDefault(),
                    "%d:%02d:%02d", hours,
                    minutes, secs
                )
                binding.recordTime.text = time
                viewModel.seconds++
            } finally {
                mHandler.postDelayed(this, 1000)
            }
        }
    }

    private fun createRunData() {
        clearLocationToRoute()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.nice_run))
            .setMessage(getString(R.string.save_run_message))
            .setCancelable(false)
            .setPositiveButton(R.string.save) { _, _ ->
                Timber.i("${viewModel.runData.value}")
                viewModel.saveRun()
            }
            .setNegativeButton(R.string.delete) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Steps Sensor
    private fun setupStepCounterListener() {
        val sensorManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return
        sensorManager.registerListener(
            this,
            stepCounterSensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        event.values.firstOrNull()?.let {
            if (viewModel.initialStepCount == -1) {
                viewModel.initialStepCount = it.toInt()
            }
            val currentNumberOfStepCount = it.toInt() - viewModel.initialStepCount
            Timber.i("Steps count: $currentNumberOfStepCount ")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // not used
    }

    // Tracking
    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.locations.forEach {
                if (startRecording) {
                    viewModel.locationList.value?.add(GeoLocation(it.latitude, it.longitude))
                }
                val latLng = LatLng(it.latitude, it.longitude)
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM)
                map.animateCamera(cameraUpdate)
            }
            if (startRecording) {
                addLocationToRoute(locationResult.locations)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupLocationChangeListener() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest.build(),
            locationCallback,
            Looper.getMainLooper()
        )
    }

    var polylineOptions = PolylineOptions()
    private fun addLocationToRoute(locations: List<Location>) {
        val originalLatLngList = polylineOptions.points
        val latLngList = locations.map {
            LatLng(it.latitude, it.longitude)
        }
        originalLatLngList.addAll(latLngList)
        map.addPolyline(polylineOptions)
    }

    private fun clearLocationToRoute() {
        map.clear()
    }
}