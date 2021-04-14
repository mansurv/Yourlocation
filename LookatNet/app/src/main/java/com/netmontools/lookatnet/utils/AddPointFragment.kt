package com.netmontools.lookatnet.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.netmontools.lookatnet.App
import com.netmontools.lookatnet.R
import com.netmontools.lookatnet.ui.point.worker.AddPointWorker
import com.netmontools.lookatnet.ui.remote.workers.AddWorker


class AddPointFragment() : DialogFragment(), DialogInterface.OnClickListener {
    lateinit var v: View
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private var titleBox: EditText? = null
    private var snipBox: EditText? = null

    @SuppressLint("CutPasteId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        v = requireActivity().layoutInflater
                .inflate(R.layout.fragment_add_point, null)
        titleBox = v.findViewById(R.id.title)
        snipBox = v.findViewById(R.id.snip)
        val longitudeBox = v.findViewById(R.id.lon) as EditText
        val latitudeBox = v.findViewById(R.id.lat) as EditText
        longitude = requireArguments().getDouble(LONGITUDE)
        latitude = requireArguments().getDouble(LATITUDE)
        longitudeBox.setText(requireArguments().getDouble(LONGITUDE).toString())
        latitudeBox.setText(latitude.toString())
        return AlertDialog.Builder(requireActivity())
                .setView(v)
                .setTitle(R.string.dialog_add_point_title)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        val addPointWorkRequest: OneTimeWorkRequest
        val myData = Data.Builder()
                .putString("title", titleBox!!.text.toString())
                .putString("snip", snipBox!!.text.toString())
                .putDouble("longitude", longitude)
                .putDouble("latitude", latitude)
                .build()
        addPointWorkRequest = OneTimeWorkRequest.Builder(AddPointWorker::class.java)
                .setInputData(myData)
                .addTag("myAddPointTag")
                .build()
        val wm = WorkManager.getInstance(App.instance)
        wm.enqueue(addPointWorkRequest)
    }

    companion object {
        private const val LONGITUDE = "current_longitude"
        private const val LATITUDE = "current_latitude"
        const val TAG = "confirm add_point"
        @JvmStatic
        fun newInstance(longitude: Double, latitude: Double): AddPointFragment {
            val args = Bundle()
            args.putDouble(LONGITUDE, longitude)
            args.putDouble(LATITUDE, latitude)
            val fragment = AddPointFragment()
            fragment.arguments = args
            return fragment
        }
    }
}