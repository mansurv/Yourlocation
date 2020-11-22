package com.netmontools.lookatnet.ui.remote.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.netmontools.lookatnet.App
import com.netmontools.lookatnet.AppDatabase
import com.netmontools.lookatnet.ui.remote.model.RemoteFolder
import com.netmontools.lookatnet.ui.remote.model.RemoteModel
import com.netmontools.lookatnet.ui.remote.model.RemoteModelDao
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbException
import jcifs.smb.SmbFile
import kotlinx.coroutines.*
import java.net.MalformedURLException

class FilesRepository(application: Application?) {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
    var allPoints: LiveData<List<RemoteFolder>>? = null
    private var liveData: MutableLiveData<List<RemoteFolder>>? = null
    lateinit var context: Context

    suspend fun update(point: RemoteFolder?) = withContext(ioDispatcher) {
        coroutineScope {
            launch { SmbAccess(point)}
        }
        PostUpdate()
    }

    fun getAll(): LiveData<List<RemoteFolder>>? {
        return allPoints
    }

   suspend fun PostUpdate()  = withContext(mainDispatcher) {
       liveData!!.value = App.remoteFolders
       allPoints = liveData
   }

    fun SmbAccess(point: RemoteFolder?): Array<String>? {
        var url: String
        if (point?.isHost!!) {
            val db: AppDatabase
            db = App.getInstance().database
            val remoteDao: RemoteModelDao = db.remoteModelDao()
            val remote: RemoteModel
            remote = remoteDao.getByBssidAndAddress(point.name, point.addr)
            user = remote.login.toString()
            pass = remote.pass.toString()
            url = point.path.toString()
            App.remoteRootPath = url
        } else {
            url = point.path + point.name + "/"
            App.remotePreviousPath = point.path
        }

        val auth = NtlmPasswordAuthentication(null, user, pass)
        var dir: SmbFile? = null
        try {
            dir = SmbFile(url, auth)
            for (f in dir.listFiles()) {
                share = dir.list()
            }
            App.remoteFolders.clear()
            for (i in 0..((share?.size)?.minus(1))!!) {
                val fd: RemoteFolder = RemoteFolder()
                try {
                    dir = SmbFile(url + share!!.get(i), auth)
                    if (dir.isDirectory) {
                        fd.isFile = false
                        fd.image = App.folder_image
                        fd.name = share!!.get(i)
                        fd.path = url
                        App.remoteFolders.add(fd)
                    } else {
                        continue
                    }
                } catch (se: SmbException) {
                    se.printStackTrace()
                }
            }
            for (i in 0..((share?.size)?.minus(1))!!) {
                val fd: RemoteFolder = RemoteFolder()
                try {
                    dir = SmbFile(url + share!!.get(i), auth)
                    if (dir.isDirectory) {
                        continue
                    } else {
                        fd.isFile = true
                        fd.size = dir.length()
                        fd.image = App.file_image
                        fd.name = share!!.get(i)
                        fd.path = url
                        App.remoteFolders.add(fd)
                    }
                } catch (se: SmbException) {
                    se.printStackTrace()
                }
            }
        } catch (mue: MalformedURLException) {
            if (App.share == null) App.share = arrayOfNulls(1)
            App.share.set(0, "Error: startSmb MalformedURLException. " + mue.message)
            return App.share
        } catch (se: SmbException) {
            if (App.share == null) App.share = arrayOfNulls(1)
            App.share[0] = "Error: startSmb SmbException. " + se.message
            return App.share
        }

        return App.share
    }

    companion object {
        lateinit var user: String
        lateinit var pass: String

        //var hosts = ArrayList<RemoteModel>()
        var share: Array<String>? = null
        private const val TAG = "FilesRepository"
        private lateinit var liveData: MutableLiveData<List<RemoteFolder>>
    }

    init {
        if (application != null) {
            context = application
        }

        liveData = MutableLiveData()
        try {
            liveData!!.setValue(App.remoteFolders)
        } catch (npe: NullPointerException) {
            npe.printStackTrace()
        }
        allPoints = liveData
    }
}
