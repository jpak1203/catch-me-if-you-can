package com.example.catchmeifyoucan.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.os.*
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
import com.example.catchmeifyoucan.utils.PermissionsUtil.approveActivityRecognition
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.lang.String.*
import java.util.*
import javax.inject.Inject


class HomeFragment : BaseFragment(), OnMapReadyCallback {

    companion object {
        private val TAG = HomeFragment::class.java.simpleName
        const val ACTION_GEOFENCE_EVENT = "ACTION_GEOFENCE_EVENT"
        private const val DEFAULT_ZOOM = 20f
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
    private var runButtonMotionStarted = false
    private var recordButtonMotionStarted = false

    private val geoPendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private val run = RunsModel()
    private var seconds = 0

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
                    if (!it.value) {
                        Snackbar.make(
                            requireView(),
                            R.string.activity_recognition_required_error, Snackbar.LENGTH_INDEFINITE
                        ).setAction(android.R.string.ok) {
                            requestActivityRecognitionPermissions()
                        }.show()
                    }
                }
            }
            Timber.i(it.key + " ${it.value}")
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

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
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
                    requestActivityRecognitionPermissions()
                    true
                }
            }
        }

        binding.recordButton.setOnClickListener {
            recordButtonMotionStarted = if (recordButtonMotionStarted) {
                binding.motionLayout.setTransition(R.id.record_end, R.id.record_start).run {
                    stopTimer()
                    binding.motionLayout.transitionToStart()
                    getEndLocationLatLng()
                    false
                }
            } else {
                binding.motionLayout.setTransition(R.id.record_start, R.id.record_end).run {
                    binding.motionLayout.transitionToEnd()
                    object : CountDownTimer(3000, 1000) {

                        @SuppressLint("SetTextI18n")
                        override fun onTick(millisUntilFinished: Long) {
                            binding.countdownOverlay.visibility = View.VISIBLE
                            binding.countdownOverlay.text = ((millisUntilFinished+1000)/1000).toString()
                        }
                        override fun onFinish() {
                            binding.countdownOverlay.visibility = View.GONE
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

    private fun requestActivityRecognitionPermissions() {
        if (approveActivityRecognition(requireContext()))
            return

        requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION))
    }

    private fun startTimer() {
        mHandler = Handler(Looper.getMainLooper())
        mStatusChecker.run()
    }

    private fun stopTimer() {
        viewModel.setRunTime(seconds)
        seconds = 0
        mHandler.removeCallbacks(mStatusChecker)
    }

    private var mStatusChecker: Runnable = object : Runnable {
        override fun run() {
            try {
                val hours: Int = seconds / 3600
                val minutes: Int = seconds % 3600 / 60
                val secs: Int = seconds % 60

                val time: String = format(
                    Locale.getDefault(),
                    "%d:%02d:%02d", hours,
                    minutes, secs
                )
                binding.recordTime.text = time
                seconds++
            } finally {
                mHandler.postDelayed(this, 1000)
            }
        }
    }

    private fun createRunData() {
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
}