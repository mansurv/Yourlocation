package com.netmontools.lookatnet.ui.point.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.netmontools.lookatnet.App
import com.netmontools.lookatnet.BuildConfig
import com.netmontools.lookatnet.ui.point.model.DataModel
import com.netmontools.lookatnet.ui.point.model.DataModelDao
import com.netmontools.lookatnet.ui.remote.repository.FilesRepository
import com.netmontools.lookatnet.utils.LogSystem
import kotlinx.coroutines.*

class PointRepository(application: Application?) {
    private val pointDao: DataModelDao
    val all: LiveData<List<DataModel>>
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    suspend fun insert(point: DataModel?) = withContext<Unit>(ioDispatcher) {
        coroutineScope {
            launch { pointDao.insert(point) }
        }
    }

    suspend fun update(point: DataModel?) = withContext<Unit>(ioDispatcher) {
        coroutineScope {
            launch { pointDao.update(point) }
        }
    }

    suspend fun delete(point: DataModel?) = withContext<Unit>(ioDispatcher) {
        coroutineScope {
            launch { pointDao.delete(point) }
        }
    }

    init {
        val db = App.instance.database
        pointDao = db.dataModelDao()
        all = pointDao.all
    }
}