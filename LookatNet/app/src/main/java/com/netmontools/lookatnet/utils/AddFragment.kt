package com.netmontools.lookatnet.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.netmontools.lookatnet.App
import com.netmontools.lookatnet.R
import com.netmontools.lookatnet.ui.remote.workers.AddWorker


class AddFragment : DialogFragment(), DialogInterface.OnClickListener {
    lateinit var v: View
    private var currentBssid = ""
    private var subnetIP = ""
    private var nameBox: EditText? = null
    private var addrBox: EditText? = null
    private var loginBox: EditText? = null
    private var passwordBox: EditText? = null

    @SuppressLint("CutPasteId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        v = requireActivity().layoutInflater
                .inflate(R.layout.fragment_add, null)
        nameBox = v.findViewById(R.id.name)
        addrBox = v.findViewById(R.id.addr)
        loginBox = v.findViewById(R.id.login)
        passwordBox = v.findViewById(R.id.password)
        currentBssid = requireArguments().getString(ARG_CURRENT_BSSID, "")
        subnetIP = requireArguments().getString(ARG_SUBNET_ADDR, "")
        return AlertDialog.Builder(requireActivity())
                .setView(v)
                .setTitle(R.string.dialog_add_title)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        val addWorkRequest: OneTimeWorkRequest
        val myData = Data.Builder()
                .putString("bssid", currentBssid)
                .putString("subnetIP", subnetIP)
                .putString("name", nameBox!!.text.toString())
                .putString("addr", addrBox!!.text.toString())
                .putString("user", loginBox!!.text.toString())
                .putString("pass", passwordBox!!.text.toString())
                .build()
        addWorkRequest = OneTimeWorkRequest.Builder(AddWorker::class.java)
                .setInputData(myData)
                .addTag("myAddTag")
                .build()
        val wm = WorkManager.getInstance(App.instance)
        wm.enqueue(addWorkRequest)
    }

    companion object {
        private const val ARG_CURRENT_BSSID = "currentn_bssid"
        private const val ARG_SUBNET_ADDR = "subnet_addr"
        const val TAG = "confirm add"
        fun newInstance(subnetIP: String, bssid: String): AddFragment {
            val args = Bundle()
            args.putString(ARG_CURRENT_BSSID, bssid)
            args.putString(ARG_SUBNET_ADDR, subnetIP)
            val fragment = AddFragment()
            fragment.arguments = args
            return fragment
        }
    }
}