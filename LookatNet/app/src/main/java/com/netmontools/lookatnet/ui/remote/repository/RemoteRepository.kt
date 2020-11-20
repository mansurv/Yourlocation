package com.netmontools.lookatnet.ui.remote.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.netmontools.lookatnet.App
import com.netmontools.lookatnet.App.share
import com.netmontools.lookatnet.BuildConfig
import com.netmontools.lookatnet.ui.remote.model.RemoteModel
import com.netmontools.lookatnet.ui.remote.model.RemoteModelDao
import com.netmontools.lookatnet.utils.LogSystem
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbException
import jcifs.smb.SmbFile
import kotlinx.coroutines.*
import java.net.MalformedURLException

class RemoteRepository(application: Application?) {
    private val remoteDao: RemoteModelDao
    val all: LiveData<List<RemoteModel>>
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    suspend fun insert(point: RemoteModel?) = withContext<Unit>(ioDispatcher) {
        coroutineScope {
            launch { remoteDao.insert(point) }
        }
    }

    suspend fun update(point: RemoteModel?) = withContext<Unit>(ioDispatcher) {
        coroutineScope {
            launch { remoteDao.update(point) }
        }
    }

    suspend fun delete(point: RemoteModel?) = withContext<Unit>(ioDispatcher) {
        coroutineScope {
            launch { remoteDao.delete(point) }
        }
    }

    //private static MutableLiveData<List<RemoteModel>> liveData;
    init {
        val db = App.instance.database
        remoteDao = db.remoteModelDao()
        all = remoteDao.all

        /*liveData = new MutableLiveData<>();
        try {
            liveData.setValue(App.hosts);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        allRemotes = liveData;*/
    }
}