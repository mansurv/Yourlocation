package com.netmontools.lookatnet.ui.remote.view

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.netmontools.lookatnet.App
import com.netmontools.lookatnet.BuildConfig
import com.netmontools.lookatnet.MainActivity
import com.netmontools.lookatnet.R
import com.netmontools.lookatnet.ui.remote.model.RemoteFolder
import com.netmontools.lookatnet.ui.remote.viewmodel.FilesViewModel
import com.netmontools.lookatnet.ui.remote.viewmodel.RemoteViewModel
import com.netmontools.lookatnet.ui.remote.workers.LoginWorker
import com.netmontools.lookatnet.ui.remote.workers.RemoteWorker
import com.netmontools.lookatnet.utils.LogSystem
import com.netmontools.lookatnet.utils.LoginFragment
import com.netmontools.lookatnet.utils.SimpleUtils
import java.io.File

class FilesFragment : Fragment() {
    private lateinit var  recyclerView: RecyclerView
    private lateinit var filesRefreshLayout: SwipeRefreshLayout
    private lateinit var fab: FloatingActionButton

    var address: String = ""
    private var currentBssid: String? = null
    private var subnetIP = 0
    private var broadcastIP = 0
    private var isSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setRetainInstance(true);
        setHasOptionsMenu(true);

        val bundle = arguments
        if (bundle != null) {
            address = bundle.getString("tag").toString()
        }
    }

    fun updateUI() {
        //filesRefreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            // Execute code when refresh layout swiped
            if (isNetworkConnected()) {
                val scanWorkRequest: OneTimeWorkRequest
                val myRemoteData = Data.Builder()
                        .putString("bssid", currentBssid)
                        .putInt("subnet", subnetIP)
                        .putInt("broadcast", broadcastIP)
                        .build()
                scanWorkRequest = OneTimeWorkRequest.Builder(RemoteWorker::class.java)
                        .setInputData(myRemoteData)
                        .addTag("myRemoteTag")
                        .build()

                var wm = WorkManager.getInstance(App.instance)
                wm.enqueue(scanWorkRequest)
                wm.getWorkInfoByIdLiveData(scanWorkRequest.id)
                        .observe(viewLifecycleOwner, Observer { workStatus ->
                            if (workStatus != null && workStatus.state.isFinished) {
                                filesRefreshLayout.setRefreshing(false)
                                filesViewModel.allPoints.observe(viewLifecycleOwner, Observer { points -> adapter.setPoints(points) })
                            }
                        })
            } else {
                filesRefreshLayout.setRefreshing(false)
                val snackbar: Snackbar
                snackbar = Snackbar.make(recyclerView, R.string.cantrefresh, BaseTransientBottomBar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_setup) {
                            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                            startActivity(intent)
                        }
                snackbar.show()
            }
        //)}
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab.setOnClickListener(View.OnClickListener {
            if (!isSelected) {
                updateUI()
                fab.setImageResource(R.drawable.ic_sync_disabled_24dp)

            }
        })

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                position = viewHolder.adapterPosition
                deleteRemoteHost()
            }
        }).attachToRecyclerView(recyclerView)

        adapter.setOnItemClickListener { point ->

            for (folder in App.remoteFolders) {
                if (folder.isChecked) {
                    if (point.isChecked) {
                        point.isChecked = false
                    } else {
                        point.isChecked = true
                    }
                    isSelected = true
                    adapter.notifyItemChanged(position)
                    break
                }
            }
            if (!isSelected) {
                if (!point.isFile) {
                    if (point.isHost) {
                        if (isNetworkConnected()) {
                            var loginWorkRequest: OneTimeWorkRequest
                            var myLoginData = Data.Builder()
                                    .putString("bssid", currentBssid)
                                    .putString("address", point.name)
                                    .putString("user", "")
                                    .putString("pass", "")
                                    .build()
                            loginWorkRequest = OneTimeWorkRequest.Builder(LoginWorker::class.java)
                                    .setInputData(myLoginData)
                                    .addTag("myLoginTag")
                                    .build()

                            var wm = WorkManager.getInstance(App.instance)
                            wm.enqueue(loginWorkRequest)
                            wm.getWorkInfoByIdLiveData(loginWorkRequest.id)
                                    .observe(viewLifecycleOwner, Observer { workStatus ->
                                        if (workStatus != null && workStatus.state.isFinished) {
                                            if (workStatus.state == WorkInfo.State.SUCCEEDED) {
                                                val user = workStatus.getOutputData().getString("user")
                                                val pass = workStatus.getOutputData().getString("pass")
                                                if (user?.isEmpty()!! || pass?.isEmpty()!!) {
                                                    val dialog = LoginFragment.newInstance(point.name.toString(), currentBssid.toString())
                                                    dialog.show(requireActivity().supportFragmentManager, LoginFragment.TAG)
                                                } else {
                                                    filesViewModel.update(point)
                                                }
                                            }
                                        } else if (workStatus.state == WorkInfo.State.FAILED) {
                                            val dialog = LoginFragment.newInstance(point.name.toString(), currentBssid.toString())
                                            dialog.show(requireActivity().supportFragmentManager, LoginFragment.TAG)
                                        }
                                    })
                        } else {
                            val snackbar: Snackbar
                            snackbar = Snackbar.make(recyclerView, R.string.cantrefresh, BaseTransientBottomBar.LENGTH_INDEFINITE)
                                    .setAction(R.string.action_setup) {
                                        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                                        startActivity(intent)
                                    }
                            snackbar.show()
                        }
                    } else {
                        filesViewModel.update(point)
                    }
                } else {
                    try {
                        if (BuildConfig.DEBUG && point.path == null) {
                            error("Assertion failed")
                        }
                        val file = File(point.path)
                        if (file.exists() && file.isFile) {
                            SimpleUtils.openFile(App.instance, file)
                        }
                    } catch (npe: NullPointerException) {
                        if (BuildConfig.USE_LOG) {
                            LogSystem.logInFile(TAG, "\r\n NullPointerException: " + npe.message)
                        }
                    }
                }
            }
        }

        adapter.setOnItemLongClickListener { point ->
            if (point.isChecked) {
                point.isChecked = false
            } else {
                point.isChecked = true
            }
            adapter.notifyItemChanged(position)
        }

        filesRefreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            // Execute code when refresh layout swiped
            updateUI()
        })
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val root = inflater.inflate(R.layout.fragment_files, container, false)
        fab = root.findViewById(R.id.remote_fab)

        filesRefreshLayout = root.findViewById(R.id.files_refresh_layout)
        filesRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light)

        recyclerView = root.findViewById(R.id.files_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        adapter = FilesAdapter()
        recyclerView.adapter = adapter

        //remoteViewModel = ViewModelProvider.AndroidViewModelFactory(App.getInstance()).create(RemoteViewModel::class.java)
        //remoteViewModel.allRemotes.observe(viewLifecycleOwner, Observer { points -> adapter.setPoints(points) })
        filesViewModel = ViewModelProvider.AndroidViewModelFactory(App.getInstance()).create(FilesViewModel::class.java)
        filesViewModel.allPoints.observe(viewLifecycleOwner, Observer { points -> adapter.setPoints(points) })

        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(requireContext())
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(
                true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                try {
                    var smbFile = App.remotePreviousPath
                    if (smbFile != null) {
                        if (!smbFile.equals(App.remoteRootPath, ignoreCase = true)) {
                            val fd = RemoteFolder()
                            smbFile = smbFile.trimEnd('/')
                            fd.name = smbFile.substring(smbFile.lastIndexOf('/') + 1)
                            fd.path = smbFile.substring(0, smbFile.lastIndexOf('/') + 1)
                            fd.size = 0L
                            fd.isHost = false
                            fd.image = App.folder_image
                            filesViewModel.update(fd)
                        } else {
                            val fd = RemoteFolder()
                            fd.path = smbFile
                            smbFile = smbFile.trimEnd('/')
                            fd.name = smbFile.substring(smbFile.lastIndexOf('/') + 1)
                            fd.size = 0L
                            fd.isHost = true
                            fd.bssid = currentBssid
                            fd.image = App.host_image
                            filesViewModel.update(fd)
                        }
                    } else {
                        if (isEnabled) {
                            isEnabled = false
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                } catch (npe: NullPointerException) {
                    npe.printStackTrace()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
                this,  // LifecycleOwner
                callback)
    }

    private fun deleteRemoteHost() {
        confirmDelete.instantiate().show(requireActivity().supportFragmentManager, "confirm delete")
    }

    class confirmDelete : DialogFragment() {
        override fun onCreateDialog(bundle: Bundle?): Dialog {
            val builder = AlertDialog.Builder(requireActivity())
            builder.setTitle(R.string.confirm_title)
            builder.setMessage(R.string.confirm_message)
            builder.setPositiveButton(R.string.button_delete
            ) { dialog, button ->
                //filesViewModel.delete(adapter.getPointAt(position))
                Toast.makeText(activity, "Remote host deleted", Toast.LENGTH_SHORT).show()
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

    companion object {
        private const val TAG = "FilesFragment"
        private lateinit var filesViewModel: FilesViewModel
        private lateinit var remoteViewModel: RemoteViewModel
        private lateinit var adapter: FilesAdapter
        private var position = 0
    }

    fun isNetworkConnected(): Boolean {
        val cm = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (cm != null) {
            //if (Build.VERSION.SDK_INT < 23) {
            val ni: NetworkInfo? = cm.activeNetworkInfo
            if (ni != null) {
                val wifiManager = App.instance.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val connManager = App.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                currentBssid = "0"
                val wifiInfo = wifiManager.connectionInfo
                val dhcpInfo = wifiManager.dhcpInfo
                if (wifiInfo != null) {
                    if (wifiInfo.bssid != null) {
                        currentBssid = wifiInfo.bssid
                    }
                }
                if (dhcpInfo != null) {
                    var netMask: Int
                    val serverIP: Int
                    serverIP = dhcpInfo.serverAddress
                    netMask = dhcpInfo.netmask
                    if (serverIP != 0 && netMask == 0) {
                        netMask = 16777215
                        subnetIP = dhcpInfo.ipAddress and netMask
                        broadcastIP = dhcpInfo.ipAddress and netMask or netMask.inv()
                    } else if (netMask != 0) {
                        subnetIP = dhcpInfo.ipAddress and dhcpInfo.netmask
                        broadcastIP = dhcpInfo.ipAddress and dhcpInfo.netmask or dhcpInfo.netmask.inv()
                    }
                }
                return ni.isConnected() && (ni.getType() === ConnectivityManager.TYPE_WIFI)
            }
            /*} else {
                val n: Network? = cm.activeNetwork
                if (n != null) {
                    val nc = cm.getNetworkCapabilities(n)
                    return nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                }
            }*/
        }
        return false
    }
}
