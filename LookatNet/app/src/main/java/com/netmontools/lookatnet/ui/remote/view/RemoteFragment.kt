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
import android.view.*
import android.widget.Toast
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.netmontools.lookatnet.App
import com.netmontools.lookatnet.R
import com.netmontools.lookatnet.ui.remote.viewmodel.RemoteViewModel
import com.netmontools.lookatnet.ui.remote.workers.RemoteWorker
import com.netmontools.lookatnet.utils.AddFragment
import javax.xml.transform.TransformerFactory.newInstance

class  RemoteFragment  : Fragment() {

    private lateinit var  recyclerView: RecyclerView
    private lateinit var remoteRefreshLayout: SwipeRefreshLayout
    private lateinit var fab: FloatingActionButton
    private var currentBssid: String? = null
    private var broadcastIP = 0
    private var subnetIP = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    fun updateUI() {
        remoteRefreshLayout.setOnRefreshListener(OnRefreshListener {
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

                val wm = WorkManager.getInstance(App.instance)
                wm.enqueue(scanWorkRequest)

                val startTime = System.currentTimeMillis()
                wm.getWorkInfoByIdLiveData(scanWorkRequest.id)
                        .observe(viewLifecycleOwner, Observer { workStatus ->
                            if (workStatus != null && workStatus.state.isFinished) {
                                val endTime = System.currentTimeMillis()
                                val hostsCount = workStatus.getOutputData().getInt("count", 0)
                                remoteRefreshLayout.setRefreshing(false)
                                var traceTime = endTime - startTime
                                var traceTimeRem: Long = 0
                                var s2 = getString(R.string.time_ms)
                                var s3: String? = null
                                if (traceTime > 1000 && traceTime < 60000) {
                                    traceTime = traceTime / 1000
                                    s2 = getString(R.string.time_s)
                                } else if (traceTime >= 60000) {
                                    traceTime = traceTime / 60000
                                    s2 = getString(R.string.time_min)
                                    traceTimeRem = traceTime % 60000
                                    if (traceTimeRem > 0) {
                                        s3 = getString(R.string.time_s)
                                    }
                                }
                                val s = getString(R.string.found_hosts)
                                val s1 = " "
                                if (traceTime < 60000) {
                                    val snackbar: Snackbar
                                    snackbar = Snackbar.make(recyclerView,
                                            s + s1 + (hostsCount) + s1 + getString(R.string.for_time) + s1 + traceTime + s1 + s2,
                                            BaseTransientBottomBar.LENGTH_LONG)
                                    snackbar.show()
                                } else if (traceTime > 60000 && traceTimeRem > 0) {
                                    val snackbar: Snackbar
                                    snackbar = Snackbar.make(recyclerView,
                                            s + s1 + (hostsCount) + s1 + getString(R.string.for_time) +
                                                    s1 + traceTime + s1 + s2 + s1 + traceTimeRem + s1 + s3,
                                            BaseTransientBottomBar.LENGTH_LONG)
                                    snackbar.show()
                                } else if (traceTime >= 60000 && traceTimeRem == 0L) {
                                    val snackbar: Snackbar
                                    snackbar = Snackbar.make(recyclerView,
                                            s + s1 + (hostsCount) + s1 + getString(R.string.for_time) +
                                                    s1 + traceTime + s1 + s2,
                                            BaseTransientBottomBar.LENGTH_LONG)
                                    snackbar.show()
                                }
                            }
                        })
            } else {
                remoteRefreshLayout.setRefreshing(false)
                val snackbar: Snackbar
                snackbar = Snackbar.make(recyclerView, R.string.cantrefresh, BaseTransientBottomBar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_setup) {
                            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                            startActivity(intent)
                        }
                snackbar.show()
            }
        })
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            val ipaddress = point?.addr
            val fragment = FilesFragment()
            val bundle = Bundle()
            bundle.putString("tag", ipaddress)
            fragment.setArguments(bundle)
            val fragmentManager: androidx.fragment.app.FragmentManager? = fragmentManager
            getParentFragmentManager()/*requireFragmentManager()*/.beginTransaction().replace(R.id.nav_host_fragment, fragment).commit()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val root = inflater.inflate(R.layout.fragment_remote, container, false)

        remoteRefreshLayout = root.findViewById(R.id.remote_refresh_layout)
        remoteRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light)
        recyclerView = root.findViewById(R.id.remote_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        adapter = RemoteAdapter()
        recyclerView.adapter = adapter

        remoteViewModel = ViewModelProvider.AndroidViewModelFactory(App.getInstance()).create(RemoteViewModel::class.java)
        remoteViewModel.allRemotes.observe(viewLifecycleOwner, Observer { points -> adapter.setHosts(points) })

        return root
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
                remoteViewModel.delete(adapter.getHostAt(position))
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
        private const val TAG = "RemoteFragment"
        private lateinit var adapter: RemoteAdapter
        private lateinit var remoteViewModel: RemoteViewModel
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_remote, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_new_smb_server -> {
                if (item.isChecked) {

                    item.setChecked(false)
                } else {
                    item.setChecked(true)

                }

                true
            }
            R.id.action_edit_smb_server -> {
                if (item.isChecked) {

                    item.setChecked(false)
                } else {
                    item.setChecked(true)

                }

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
