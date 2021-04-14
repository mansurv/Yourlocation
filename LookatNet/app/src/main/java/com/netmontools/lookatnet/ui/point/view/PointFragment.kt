package com.netmontools.lookatnet.ui.point.view

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.netmontools.lookatnet.App
import com.netmontools.lookatnet.BuildConfig
import com.netmontools.lookatnet.R
import com.netmontools.lookatnet.ui.point.viemodel.PointViewModel
import com.netmontools.lookatnet.ui.point.worker.PointWorker
import com.netmontools.lookatnet.utils.LogSystem
import com.netmontools.lookatnet.utils.map.MapsActivity
import java.util.concurrent.ExecutionException

class PointFragment : Fragment() {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var currentBssid: String
    private lateinit var currentSsid: String
    private lateinit var allProv: MutableList<String>
    private var isSatelliteMode: Boolean = false
    private var isSchemeMode: Boolean = false
    private var isHybridMode: Boolean = false

    val sp = PreferenceManager.getDefaultSharedPreferences(App.instance)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    private val listener: LocationListener = object : LocationListener {
        private var locationWorkRequest: OneTimeWorkRequest? = null
        private var currentLatitude: Double = 0.0
        private var currentLongitude: Double = 0.0
        override fun onLocationChanged(location: Location) {
            currentLatitude = location.latitude
            currentLongitude = location.longitude
            val myData = Data.Builder()
                    .putString("bssid", currentBssid)
                    .putString("name", currentSsid)
                    .putDouble("latitude", currentLatitude)
                    .putDouble("longitude", currentLongitude)
                    .build()
            locationWorkRequest = OneTimeWorkRequest.Builder(PointWorker::class.java)
                    .setInputData(myData)
                    .addTag("myTag")
                    .build()
            val wm = WorkManager.getInstance(App.instance)
            wm.enqueue(locationWorkRequest!!)
            val future = wm.getWorkInfosByTag("myTag")
            try {
                val list = future.get()
                // start only if no such tasks present
                if (list == null || list.size == 0) {
                    // shedule the task
                    //wm.enqueue(locationWorkRequest);
                } else {
                    // this task has been previously scheduled
                    swipeRefreshLayout.isRefreshing = false
                }
            } catch (ee: ExecutionException) {
                ee.printStackTrace()
            } catch (ie: InterruptedException) {
                ie.printStackTrace()
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val root = inflater.inflate(R.layout.fragment_point, container, false)
        swipeRefreshLayout = root.findViewById(R.id.refresh_layout)
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        adapter = PointAdapter()
        recyclerView.adapter = adapter

        pointViewModel = ViewModelProvider.AndroidViewModelFactory(App.getInstance()).create(PointViewModel::class.java)
        pointViewModel.allPoints.observe(viewLifecycleOwner, Observer { points -> adapter.setPoints(points) })

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                position = viewHolder.adapterPosition
                deleteAccessPoint()
            }
        }).attachToRecyclerView(recyclerView)

        adapter.setOnItemClickListener { point ->
            val intent = Intent(activity, MapsActivity::class.java)
            intent.putExtra(MapsActivity.SATELLITE, isSatelliteMode)
            intent.putExtra(MapsActivity.SCHEME, isSchemeMode)
            intent.putExtra(MapsActivity.HYBRID, isHybridMode)
            intent.putExtra(MapsActivity.ID, point.id)
            intent.putExtra(MapsActivity.BSSID, point.bssid)
            intent.putExtra(MapsActivity.SSID, point.name)
            intent.putExtra(MapsActivity.LATITUDE, point.lat)
            intent.putExtra(MapsActivity.LONGITUDE, point.lon)
            startActivity(intent)
        }
        val locationManager = App.instance.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager != null) {
            allProv = locationManager.allProviders
            if (!(allProv).isEmpty()) {
                val sb = StringBuilder()
                for (i in (allProv ).indices) {
                    sb.append((allProv).get(i))
                    if (i != (allProv ).size - 1) sb.append(", ")
                }
                if (BuildConfig.USE_LOG) {
                    LogSystem.logInFile(TAG, "\r\n Available providers:\n  $sb")
                }
            }
        }
        val wifiManager = App.instance.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connManager = App.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        currentSsid = "My location"
        currentBssid = "0"
        if (connManager != null) {
            val wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            //val network = connManager.activeNetwork
            //val abc = connManager.reportNetworkConnectivity(network,true)
            try {
                if (wifi != null) {
                    if (wifi.isConnectedOrConnecting) {
                        if (wifi.isConnected) {
                            val wifiInfo = wifiManager.connectionInfo
                            if (wifiInfo != null) {
                                if (wifiInfo.ssid != null) {
                                    if (!wifiInfo.ssid.equals("")) {
                                        currentSsid = wifiInfo.ssid.replace("\"", " ").trim { it <= ' ' }
                                    }
                                }
                                if (wifiInfo.bssid != null) {
                                    currentBssid = wifiInfo.bssid
                                }
                            }
                        }
                    }
                }
            } catch (npe: NullPointerException) {
                npe.printStackTrace()
            }
        }
        swipeRefreshLayout.setOnRefreshListener(OnRefreshListener {
            // Execute code when refresh layout swiped
            if (getActivity()?.let { ActivityCompat.checkSelfPermission(it, android.Manifest.permission.ACCESS_FINE_LOCATION) } == PackageManager.PERMISSION_GRANTED) {
                var gps_enabled = false
                var network_enabled = false
                // Exceptions will be thrown if the provider is not permitted.
                try {
                    gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                } catch (ex: Exception) {
                }
                try {
                    network_enabled = locationManager
                            .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                } catch (ex: Exception) {
                }

                //if (gps_enabled) {
                //    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null)
                /**} else */if (network_enabled)
                    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, null)
            } else {
                if (allProv.isNotEmpty()) {
                    for (i in allProv.indices) {
                        if (allProv[i].equals("network", ignoreCase = true)) {
                            try {
                                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, null)
                                break
                            } catch (se: SecurityException) {
                                se.printStackTrace()
                            } catch (npe: NullPointerException) {
                                npe.printStackTrace()
                            }
                        }
                    }
                }
            }
        })
        return root
    }

    override fun onResume() {
        super.onResume()
        isSatelliteMode = sp.getBoolean("isSatellite_mode", false)
        isSchemeMode = sp.getBoolean("isScheme_mode", false)
        isHybridMode = sp.getBoolean("isHybrid_mode", false)
    }

    private fun deleteAccessPoint() {
        confirmDelete.instantiate().show(requireActivity().supportFragmentManager, "confirm delete")
    }

    class confirmDelete : DialogFragment() {
        override fun onCreateDialog(bundle: Bundle?): Dialog {
            val builder = AlertDialog.Builder(requireActivity())
            builder.setTitle(R.string.confirm_title)
            builder.setMessage(R.string.confirm_message)
            builder.setPositiveButton(R.string.button_delete
            ) { dialog, button ->
                pointViewModel.delete(adapter.getPointAt(position))
                Toast.makeText(activity, "Access point deleted", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton(R.string.button_cancel
            ) { dialog, button -> adapter.notifyDataSetChanged() }
            return builder.create()
        }

        companion object {
            fun instantiate(): DialogFragment {
                return confirmDelete()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_point, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_satellite_mode -> {
                if (item.isChecked) {
                    isSatelliteMode = false
                    item.setChecked(false)
                } else {
                    item.setChecked(true)
                    isSatelliteMode = true
                    isSchemeMode = false
                    isHybridMode = false
                }
                sp.edit().putBoolean("isSatellite_mode", isSatelliteMode).apply()
                true
            }
            R.id.action_scheme_mode -> {
                if (item.isChecked) {
                    isSchemeMode = false
                    item.setChecked(false)
                } else {
                    item.setChecked(true)
                    isSchemeMode = true
                    isSatelliteMode = false
                    isHybridMode = false
                }
                sp.edit().putBoolean("isScheme_mode", isSchemeMode).apply()
                true
            }
            R.id.action_hybrid_mode -> {
                if (item.isChecked) {
                    isHybridMode = false
                    item.setChecked(false)
                } else {
                    item.setChecked(true)
                    isHybridMode = true
                    isSatelliteMode = false
                    isSchemeMode = false
                }
                sp.edit().putBoolean("isHybrid_mode", isHybridMode).apply()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val TAG = "PointFragment"
        private lateinit var pointViewModel: PointViewModel
        private lateinit var adapter: PointAdapter
        private var position = 0
    }
}