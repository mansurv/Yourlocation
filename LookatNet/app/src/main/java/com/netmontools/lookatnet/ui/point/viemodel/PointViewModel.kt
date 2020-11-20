package com.netmontools.lookatnet.ui.point.viemodel

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.netmontools.lookatnet.ui.point.repository.PointRepository
import com.netmontools.lookatnet.ui.point.model.DataModel
import kotlinx.coroutines.launch

class PointViewModel(application: Application) : AndroidViewModel(application) {
    private val dataModel: DataModel? = null
    val repository: PointRepository
    val allPoints: LiveData<List<DataModel>>

    fun insert(dataModel: DataModel?) {
        viewModelScope.launch {repository.insert(dataModel)}
    }

    fun update(dataModel: DataModel?) {
        viewModelScope.launch {repository.update(dataModel)}
    }

    fun delete(dataModel: DataModel?) {
        viewModelScope.launch {repository.delete(dataModel)}
    }

    val id: Long
        get() = dataModel!!.id

    val name: String?
        get() = if (!TextUtils.isEmpty(dataModel!!.name)) dataModel.name else ""

    val bssid: String?
        get() = if (!TextUtils.isEmpty(dataModel!!.bssid)) dataModel.bssid else ""

    val lat: Double
        get() = dataModel!!.lat

    val lon: Double
        get() = dataModel!!.lon

    init {
        repository = PointRepository(application)
        allPoints = repository.all
    }
}