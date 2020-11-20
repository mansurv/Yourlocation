package com.netmontools.lookatnet.ui.remote.viewmodel

import android.app.Application
import android.graphics.drawable.Drawable
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.netmontools.lookatnet.ui.remote.model.RemoteFolder
import com.netmontools.lookatnet.ui.remote.repository.FilesRepository
import kotlinx.coroutines.launch

class FilesViewModel(application: Application) : AndroidViewModel(application) {
    private val remoteFolder: RemoteFolder? = null
    private val repository: FilesRepository
    val allPoints: LiveData<List<RemoteFolder>>

    fun update(remoteFolder: RemoteFolder?) {
        viewModelScope.launch {repository.update(remoteFolder)}
    }

    val path: String?
        get() = if (!TextUtils.isEmpty(remoteFolder!!.path)) remoteFolder.path else ""

    val size: Long
        get() = remoteFolder!!.size

    val image: Drawable
        get() = remoteFolder!!.image

    init {
         repository = FilesRepository(application)
        allPoints = repository.allPoints!!
    }
}