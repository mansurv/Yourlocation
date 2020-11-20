package com.netmontools.lookatnet.ui.remote.viewmodel

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.netmontools.lookatnet.ui.remote.model.RemoteModel
import com.netmontools.lookatnet.ui.remote.repository.RemoteRepository
import kotlinx.coroutines.launch

class RemoteViewModel(application: Application) : AndroidViewModel(application) {
    private val dataModel: RemoteModel? = null
    val repository: RemoteRepository
    val allRemotes: LiveData<List<RemoteModel>>

    fun insert(dataModel: RemoteModel?) {
        viewModelScope.launch {repository.insert(dataModel)}
    }

    fun update(dataModel: RemoteModel?) {
        viewModelScope.launch {repository.update(dataModel)}
    }

    fun delete(dataModel: RemoteModel?) {
        viewModelScope.launch {repository.delete(dataModel)}
    }

    val id: Long
        get() = dataModel!!.id

    val name: String?
        get() = if (!TextUtils.isEmpty(dataModel!!.name)) dataModel.name else ""

    val addr: String?
        get() = if (!TextUtils.isEmpty(dataModel!!.addr)) dataModel.addr else ""

    init {
        repository = RemoteRepository(application)
        allRemotes = repository.all
    }
}