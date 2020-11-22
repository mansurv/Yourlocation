 package com.netmontools.lookatnet

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.KeyEvent
import android.view.Menu
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.netmontools.lookatnet.utils.LogSystem
import com.netmontools.lookatnet.utils.SimpleUtils
import com.netmontools.lookatnet.utils.StorageHelper
import com.netmontools.lookatnet.utils.StorageHelper.MountDevice
import java.io.File
import java.io.FileFilter
import java.util.*
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    private lateinit var mAppBarConfiguration: AppBarConfiguration
    //private lateinit var progressBar: ProgressBar
    //private lateinit var drawer: DrawerLayout
    //lateinit var fab: FloatingActionButton
    //var hostsCount = 0
    //var startTime: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration.Builder(
                R.id.nav_local, R.id.nav_remote, R.id.nav_places, R.id.nav_log)
                .build()
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        ///NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(navView, navController)

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkStoragePermission()
            checkLocationPermission()
            checkStatePermission()
        }
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        if (savedInstanceState == null) {
            SimpleUtils.renameTarget(getApplicationContext().getFilesDir().toString() + "/log.dat", "log1.dat")
            if (Build.VERSION.SDK_INT >= 21) {
                var remPathSize = 0
                var extPathSize = 0
                var temp: String
                var storages: ArrayList<MountDevice>
                storages = StorageHelper.getInstance().externalMountedDevices
                if (storages.size != 0) {
                    extPathSize = storages.size
                    sp.edit().putInt("ext_size", extPathSize).apply()
                    extSDPath = arrayOfNulls(extPathSize)
                    for (i in 0 until extPathSize) {
                        temp = storages[i].path
                        extSDPath[i] = temp
                        temp = temp.substring(temp.lastIndexOf("/") + 1)
                        if (temp.equals(Integer.toString(i), ignoreCase = true)) temp = "sdcard$temp"
                        sp.edit().putString("storage" + (i + 1), temp).apply()
                        sp.edit().putString("path_idx" + (i + 1), extSDPath[i]).apply()
                    }
                }
                if (StorageHelper.getInstance().removableMountedDevices.also { storages = it }.size != 0) {
                    remPathSize = storages.size
                    sp.edit().putInt("rem_size", remPathSize).apply()
                    remSDPath = arrayOfNulls(remPathSize)
                    for (i in 0 until remPathSize) {
                        temp = storages[i].path
                        remSDPath[i] = temp
                        temp = temp.substring(temp.lastIndexOf("/") + 1)
                        if (temp.equals(Integer.toString(i), ignoreCase = true)) temp = "extsd" + Integer.toString(i + extPathSize)
                        sp.edit().putString("storage" + (i + 1 + extPathSize), temp).apply()
                        sp.edit().putString("path_idx" + (i + 1 + extPathSize), remSDPath[i]).apply()
                    }
                }
                if (BuildConfig.USE_LOG) {
                    LogSystem.logInFile(TAG, "\r\n  extPathSize = $extPathSize\r\n  remPathSize = $remPathSize")
                }
            }
            if (BuildConfig.USE_LOG) {
                var numCpuCores = sp.getInt("cpu_cores", 0)
                if (numCpuCores == 0) {
                    numCpuCores = numCores
                    sp.edit().putString("model", Build.MODEL).apply()
                    sp.edit().putInt("version", Build.VERSION.SDK_INT).apply()
                    sp.edit().putString("proc", Build.HARDWARE).apply()
                    sp.edit().putInt("cpu_cores", numCpuCores).apply()
                }
                LogSystem.logInFile(TAG, """
                  Device: ${sp.getString("model", "")}
                  SDK version: ${sp.getInt("version", 0)}
                  Processor: ${sp.getString("proc", "")}
                  Cpu cores: $numCpuCores""")
            }
            /*if (telephonyManager != null) {
                //if (BuildConfig.USE_LOG) {
                    LogSystem.logInFile(TAG, """
                     Network type: ${getNetworkType(this)}
                     Phone type: ${telephonyManager!!.phoneType}""")
                //}
            }*/
            if (checkPlayServices()) {
                //if (BuildConfig.USE_LOG) {
                    LogSystem.logInFile(TAG, "\r\n Google Play Service availiable ")
               // }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun checkStatePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), REQUEST_STATE_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }
            REQUEST_STATE_PERMISSION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
         val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        return (NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp())
    }

    private fun checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show()
            } else {
                //if (BuildConfig.USE_LOG) {
                    LogSystem.logInFile(TAG, "\r\n This device is not supported")
                //}
            }
            return false
        }
        return true
    }

    private val numCores: Int
        get() {
            class CpuFilter : FileFilter {
                override fun accept(pathname: File): Boolean {
                    return if (Pattern.matches("cpu[0-9]", pathname.name)) {
                        true
                    } else false
                }
            }
            return try {
                val dir = File("/sys/devices/system/cpu/")
                val files = dir.listFiles(CpuFilter())
                files.size
            } catch (e: Exception) {
                e.printStackTrace()
                1
            }
        }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //что-то делаем: завершаем Activity, открываем другую и т.д.
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_STORAGE_PERMISSION = 101
        private const val REQUEST_LOCATION_PERMISSION = 102
        private const val REQUEST_STATE_PERMISSION = 103
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000

        lateinit var extSDPath: Array<String?>
        lateinit var remSDPath: Array<String?>
        var isCancellable = false
        var telephonyManager: TelephonyManager? = null
        var locationManager: LocationManager? = null
        var database: AppDatabase? = null
        fun getNetworkType(context: Context?): String {
            if (telephonyManager != null) {
                //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                //val networkType = if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //return
                //}

                val networkType = telephonyManager!!.networkType
                when (networkType) {
                    TelephonyManager.NETWORK_TYPE_GSM -> return "GSM"
                    TelephonyManager.NETWORK_TYPE_1xRTT -> return "1xRTT (2G)"
                    TelephonyManager.NETWORK_TYPE_CDMA -> return "CDMA (2G)"
                    TelephonyManager.NETWORK_TYPE_EDGE -> return "EDGE (2.75G)"
                    TelephonyManager.NETWORK_TYPE_EVDO_0 -> return "EVDO rev. 0 (3G)"
                    TelephonyManager.NETWORK_TYPE_EVDO_A -> return "EVDO rev. A (3G)"
                    TelephonyManager.NETWORK_TYPE_EVDO_B -> return "EVDO rev. B (3G)"
                    TelephonyManager.NETWORK_TYPE_GPRS -> return "GPRS (2.5G)"
                    TelephonyManager.NETWORK_TYPE_HSDPA -> return "HSDPA (3G+)"
                    TelephonyManager.NETWORK_TYPE_HSPA -> return "HSPA (3G+)"
                    TelephonyManager.NETWORK_TYPE_EHRPD -> return "HSPA+ (3G++)"
                    TelephonyManager.NETWORK_TYPE_HSPAP -> return "HSPA+ (3G++)"
                    TelephonyManager.NETWORK_TYPE_TD_SCDMA -> return "TD_SCDMA (3G++)"
                    TelephonyManager.NETWORK_TYPE_HSUPA -> return "HSUPA (3G+)"
                    TelephonyManager.NETWORK_TYPE_IDEN -> return "iDen (2G)"
                    TelephonyManager.NETWORK_TYPE_LTE -> return "LTE (4G)"
                    TelephonyManager.NETWORK_TYPE_UMTS -> return "UMTS (3G)"
                    TelephonyManager.NETWORK_TYPE_UNKNOWN -> return "Unknown"
                }
                return "New type of network"
            }
            return "Do not access to telephony service"
        }
    }
}