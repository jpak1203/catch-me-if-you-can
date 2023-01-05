package com.example.catchmeifyoucan.ui.runs

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.SharedPreferences.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.activities.HomeActivity
import com.example.catchmeifyoucan.databinding.FragmentRunDetailsBinding
import com.example.catchmeifyoucan.geofence.GeofenceBroadcastReceiver
import com.example.catchmeifyoucan.ui.BaseFragment
import com.example.catchmeifyoucan.ui.home.HomeFragment.Companion.ACTION_GEOFENCE_EVENT
import com.example.catchmeifyoucan.ui.runs.RunsFragment.Companion.RUN_ID
import com.example.catchmeifyoucan.ui.runs.RunsFragment.Companion.RUN_STEPS
import com.example.catchmeifyoucan.ui.runs.RunsFragment.Companion.RUN_TIME
import com.example.catchmeifyoucan.ui.runs.RunsFragment.Companion.RUN_TIMESTAMP
import com.example.catchmeifyoucan.utils.FormatUtil
import com.example.catchmeifyoucan.utils.FormatUtil.getRunTime
import com.example.catchmeifyoucan.utils.PermissionsUtil
import com.example.catchmeifyoucan.utils.PermissionsUtil.approveForegroundAndBackgroundLocation
import com.example.catchmeifyoucan.utils.PermissionsUtil.runningQOrLater
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.android.support.AndroidSupportInjection
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class RunDetailsFragment : BaseFragment(), OnMapReadyCallback, OnSharedPreferenceChangeListener {

    companion object {
        private val TAG = RunDetailsFragment::class.java.simpleName
        private const val DEFAULT_ZOOM = 15f
        const val DEFAULT_GEOFENCE_RADIUS = 25f
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentRunDetailsBinding
    private lateinit var viewModel: RunsFragmentViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var cancellationSource: CancellationTokenSource
    private lateinit var map: GoogleMap
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var mHandler: Handler
    private lateinit var startGeofence: Geofence
    private lateinit var endGeofence: Geofence
    private lateinit var geoFencePref: SharedPreferences

    private var raceButtonMotionStarted = false


    private val geoPendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

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
                "ACCESS_BACKGROUND_LOCATION" -> {
                    if (it.value) {
                        checkLocationPermissions()
                    } else {
                        Snackbar.make(
                            requireView(),
                            R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                        ).setAction(android.R.string.ok) {
                            checkLocationPermissions()
                        }.show()
                    }
                }
            }
            it.value
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this, viewModelFactory)[RunsFragmentViewModel::class.java]
        binding = FragmentRunDetailsBinding
            .inflate(inflater, container, false)
            .apply { lifecycleOwner = viewLifecycleOwner }

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initView()
        subscribe()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        cancellationSource = CancellationTokenSource()
        geoFencePref = requireActivity().getSharedPreferences("TriggerdExitedId", Context.MODE_PRIVATE)
        geoFencePref.registerOnSharedPreferenceChangeListener(this)
        mHandler = Handler(Looper.getMainLooper())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val runTimestamp = FormatUtil.getDate(arguments?.getString(RUN_TIMESTAMP) ?: "")
        (requireActivity() as HomeActivity).showToolbar(runTimestamp)
    }

    private fun initView() {
        val runTime = getRunTime(arguments?.getInt(RUN_TIME) ?: 0)
        val runSteps = arguments?.getInt(RUN_STEPS)
        binding.runDetailsTime.text = runTime
        binding.runDetailsSteps.text = String.format(getString(R.string.step_count), runSteps)
        binding.raceButton.setOnClickListener {
            raceButtonMotionStarted = if (raceButtonMotionStarted) {
                binding.motionLayout.transitionToStart().run {
                    stopRace()
                    false
                }
            } else {
                binding.motionLayout.transitionToEnd().run {
                    checkLocationPermissions()
                    true
                }
            }
        }
    }

    private fun subscribe() {
        val runId = arguments?.getString(RUN_ID) ?: ""
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModel.getUserRun(uid, runId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe { run ->
                viewModel.setRunDetails(run)
                addStartEndMarkers(run)
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
    }

    private fun addStartEndMarkers(runDetails: RunsModel) {
        val start = LatLng(runDetails.start_lat, runDetails.start_lng)
        val end = LatLng(runDetails.end_lat, runDetails.end_lng)
        map.addMarker(
            MarkerOptions()
                .position(start)
                .icon(bitmapFromVector(requireContext(), R.drawable.ic_runs))
                .title("Start Location")
        )
        map.addMarker(
            MarkerOptions()
                .position(end)
                .icon(bitmapFromVector(requireContext(), R.drawable.ic_race))
                .title("End Location")
        )
    }

    private fun bitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (PermissionsUtil.approveForegroundLocation(requireContext())) {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true
            val locationResult = fusedLocationProviderClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationSource.token
            )
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful && task.result != null) {
                    val latLng = LatLng(task.result.latitude, task.result.longitude)
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng,
                            DEFAULT_ZOOM
                        ))
                }
            }
        } else {
            enableMyLocation()
        }
    }

    private fun requestForegroundAndBackgroundLocationPermissions() {
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (runningQOrLater) permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
        requestPermissionLauncher.launch(permissionsArray)
    }

    @SuppressLint("MissingPermission")
    private fun saveStartGeofence(runDetails: RunsModel) {
        startGeofence = Geofence.Builder()
            .setRequestId(runDetails.id+"_start")
            .setCircularRegion(
                runDetails.start_lat,
                runDetails.start_lng,
                DEFAULT_GEOFENCE_RADIUS
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(startGeofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geoPendingIntent).run {
            addOnCompleteListener {
                Timber.i("Added starting geofence")
            }
            addOnFailureListener {
                Timber.e("Failed to add starting geofence")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startFinishLineGeofence(runDetails: RunsModel) {
        endGeofence = Geofence.Builder()
            .setRequestId(runDetails.id+"_end")
            .setCircularRegion(
                runDetails.end_lat,
                runDetails.end_lng,
                DEFAULT_GEOFENCE_RADIUS
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(endGeofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geoPendingIntent).run {
            addOnCompleteListener {
                Timber.i("Added ending geofence")
            }
            addOnFailureListener {
                Timber.e("Failed to add ending geofence")
            }
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
                saveStartGeofence(viewModel.runDetails.value!!)
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

    override fun onDestroy() {
        super.onDestroy()
        geofencingClient.removeGeofences(geoPendingIntent)
    }

    private fun startRace() {
        geofencingClient.removeGeofences(geoPendingIntent).addOnCompleteListener {
            startFinishLineGeofence(viewModel.runDetails.value!!)
            object : CountDownTimer(3000, 1000) {

                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    binding.recordTime.text = ((millisUntilFinished + 1000) / 1000).toString()
                }

                override fun onFinish() {
                    startTimer()
                }
            }.start()
        }
    }

    private fun stopRace() {
        stopTimer()
        geofencingClient.removeGeofences(geoPendingIntent)
    }

    private fun endRace() {
        stopRace()
        val raceTime = viewModel.seconds
        val runTime = viewModel.runDetails.value!!.time

        val title = if (raceTime < runTime) getString(R.string.won_title)
            else if (raceTime == runTime) getString(R.string.tied_title)
            else getString(R.string.lost_title)

        binding.motionLayout.transitionToStart()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(String.format(getString(R.string.race_time_message), getRunTime(raceTime)))
            .setNegativeButton(getString(R.string.good_race)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()

        viewModel.seconds = 0
        binding.recordTime.text = getString(R.string.start_race_message)
    }

    private fun startTimer() {
        mHandler = Handler(Looper.getMainLooper())
        mStatusChecker.run()
    }

    private fun stopTimer() {
        mHandler.removeCallbacks(mStatusChecker)
        geofencingClient.removeGeofences(geoPendingIntent)
    }

    private var mStatusChecker: Runnable = object : Runnable {
        override fun run() {
            try {
                val hours: Int = viewModel.seconds / 3600
                val minutes: Int = viewModel.seconds % 3600 / 60
                val secs: Int = viewModel.seconds % 60

                val time: String = java.lang.String.format(
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (geoFencePref.getString("geoFenceId", null)!!.contains("_start")) startRace()
        else endRace()
    }
}