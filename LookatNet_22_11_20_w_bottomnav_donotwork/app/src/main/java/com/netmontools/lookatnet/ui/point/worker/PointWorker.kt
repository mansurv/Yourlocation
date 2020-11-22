package com.netmontools.lookatnet.ui.point.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.netmontools.lookatnet.App
import com.netmontools.lookatnet.ui.point.model.DataModel

class PointWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val currentBssid = inputData.getString("bssid")
        val currentSsid = inputData.getString("name")
        val currentLatitude = inputData.getDouble("latitude", 0.0)
        val currentLongitude = inputData.getDouble("longitude", 0.0)

        val db = App.instance.database
        val pointDao = db.dataModelDao()

        val point = DataModel()
        point.bssid = currentBssid.toString()
        point.name = currentSsid.toString()
        point.lat = currentLatitude
        point.lon = currentLongitude
        pointDao.insert(point)

        return Result.success()
    }

    companion object {
        const val TAG = "PointWorker"
    }
}