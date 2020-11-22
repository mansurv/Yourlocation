package com.netmontools.lookatnet.ui.remote.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.netmontools.lookatnet.App
import com.netmontools.lookatnet.AppDatabase
import com.netmontools.lookatnet.BuildConfig
import com.netmontools.lookatnet.ui.remote.model.RemoteModel
import com.netmontools.lookatnet.ui.remote.model.RemoteModelDao
import com.netmontools.lookatnet.utils.LogSystem
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbException
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.IOException
import java.net.*
import java.util.*

class RemoteCoroutinesWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    lateinit var db: AppDatabase
    lateinit var remoteDao: RemoteModelDao

    override val coroutineContext = Dispatchers.IO

    override suspend fun doWork(): Result = coroutineScope {

        val currentBssid = inputData.getString("bssid")
        val subnetIP = inputData.getInt("subnet", 0)
        val broadcastIP = inputData.getInt("broadcast", 0)
        val range = ntol(broadcastIP - subnetIP)
        val currentSubnetIP = String.format(Locale.US, "%d.%d.%d.%d", subnetIP and 0xff, subnetIP shr 8 and 0xff,
                subnetIP shr 16 and 0xff, subnetIP shr 24 and 0xff)
        var startIP: Int
        var host: String
        var count = 0
        var reachable = false
        db = App.getInstance().database
        remoteDao = db.remoteModelDao()
        val remote = RemoteModel()
        var s = ""
        val jobs = (1 until 255).map {
            async {
                try {
                    for (i in 1..range) {
                        startIP = lton(ntol(subnetIP) + i)
                        host = String.format(Locale.US, "%d.%d.%d.%d", startIP and 0xff, startIP shr 8 and 0xff,
                                startIP shr 16 and 0xff, startIP shr 24 and 0xff)
                        val address = InetAddress.getByName(host)
                        val hostname = address.hostName
                        reachable = address.isReachable(2000);
                        if (reachable) {
                            remote.name = hostname
                            remote.addr = host
                            remote.isPass = false
                            remote.bssid = currentBssid
                            App.hosts.add(remote)
                            remoteDao.insert(remote)
                            count++
                        }
                        /*if (!hostname.equals("") ) {
                            s = scanLan(startIP, host, hostname)
                            if (s != "") {
                                if (s.equals("0")) {
                                    remote.name = hostname
                                    remote.addr = host
                                    remote.isPass = false
                                    remote.bssid = currentBssid
                                    App.hosts.add(remote)
                                    remoteDao.insert(remote)
                                    count++
                                } else if (s.equals("1")) {
                                    remote.name = hostname
                                    remote.addr = host
                                    remote.isPass = true
                                    remote.bssid = currentBssid
                                    App.hosts.add(remote)
                                    remoteDao.insert(remote)
                                    count++
                                }
                            }
                        }*/
                    }
                } catch (ie: InterruptedException) {
                    ie.printStackTrace()
                }
            }
        }

        // awaitAll will throw an exception if a download fails, which CoroutineWorker will treat as a failure
        jobs.awaitAll()

        val outputData = Data.Builder()
                .putInt("count", count)
                .build()
        return@coroutineScope Result.success(outputData)
    }

    @Throws(InterruptedException::class)
    private fun scanLan(ip: Int, host: String, hostname: String): String {
        if (ip == 0) return ""
        var share: Array<String?>? = null

        try {
            val auth = NtlmPasswordAuthentication(null, null, null)
            var dir: SmbFile? = null
            dir = try {
                SmbFile("smb://$host/", auth)
            } catch (mue: MalformedURLException) {
                if (BuildConfig.USE_LOG) {
                    LogSystem.logInFile(TAG, """  scanLan(), NtlmPasswordAuthentication
  MalformedURLException: ${mue.message}""")
                }
                return ""
            }
            return try {
                if (dir != null) {
                    for (f in dir.listFiles()) {
                        share = dir.list()
                    }
                }
                if (share != null) {
                    "0"
                } else ""
            } catch (se: SmbException) {
                if (se.message!!.contains("Logon failure: unknown user name or bad password") ||
                        se.message.equals("0xc000009a", ignoreCase = true)) {
                    if (BuildConfig.USE_LOG) {
                        LogSystem.logInFile(TAG, """  scanLan(), NtlmPasswordAuthentication
  SmbException: ${se.message}
  host $host""")
                    }
                    "1"
                } else {
                    ""
                }
            }
        } catch (se: SocketException) {
            se.printStackTrace()
            if (BuildConfig.USE_LOG) {
                LogSystem.logInFile(TAG, """  scanLan(), global
  SocketException:  ${se.message}""")
            }
        } catch (uhe: UnknownHostException) {
            uhe.printStackTrace()
            if (BuildConfig.USE_LOG) {
                LogSystem.logInFile(TAG, """  scanLan(), global
  UnknownHostException:  ${uhe.message}""")
            }
        } catch (ioe: IOException) {
            ioe.printStackTrace()
            if (BuildConfig.USE_LOG) {
                LogSystem.logInFile(TAG, """  scanLan(), global
  IOException:  ${ioe.message}""")
            }
        }
        return ""
    }

    companion object {
        const val TAG = "RemoteCoroutinesWorker"
        fun lton(c: Int): Int {
            return (c shr 0 and 0xFF shl 24 or (c shr 8 and 0xFF shl 16)
                    or (c shr 16 and 0xFF shl 8) or (c shr 24 and 0xFF shl 0))
        }

        // Flips the bytes from BIG ENDIAN to LITTLE. For example 0x04030201 becomes 0x01020304.
        private fun ntol(c: Int): Int {
            return (c shr 24 and 0xFF shl 0 or (c shr 16 and 0xFF shl 8)
                    or (c shr 8 and 0xFF shl 16) or (c shr 0 and 0xFF shl 24))
        }
    }
}

