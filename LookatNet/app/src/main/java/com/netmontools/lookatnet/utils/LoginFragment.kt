package com.netmontools.lookatnet.utils

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.netmontools.lookatnet.App
import com.netmontools.lookatnet.R
import com.netmontools.lookatnet.ui.remote.view.FilesFragment
import com.netmontools.lookatnet.ui.remote.workers.LoginWorker
import java.util.concurrent.ExecutionException

class LoginFragment : DialogFragment(), DialogInterface.OnClickListener {
    lateinit var v: View
    private var currentBssid = ""
    private var currentAddr = ""
    private var loginBox: EditText? = null
    private var passwordBox: EditText? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        v = requireActivity().layoutInflater
                .inflate(R.layout.fragment_login, null)
        loginBox = v.findViewById(R.id.login)
        passwordBox = v.findViewById(R.id.password)
        currentBssid = requireArguments().getString(ARG_CURRENT_BSSID, "")
        currentAddr = requireArguments().getString(ARG_CURRENT_ADDR, "")
        return AlertDialog.Builder(requireActivity())
                .setView(v)
                .setTitle(R.string.dialog_login_title)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        //App.currentLogin = loginBox!!.text.toString()
        //App.currentPass = passwordBox!!.text.toString()
        val loginWorkRequest: OneTimeWorkRequest
        val myData = Data.Builder()
                .putString("bssid", currentBssid)
                .putString("address", currentAddr)
                .putString("user", loginBox!!.text.toString())
                .putString("pass", passwordBox!!.text.toString())
                .build()
        loginWorkRequest = OneTimeWorkRequest.Builder(LoginWorker::class.java)
                .setInputData(myData)
                .addTag("myLoginTag")
                .build()
        val wm = WorkManager.getInstance(App.instance)
        wm.enqueue(loginWorkRequest)
    }

    companion object {
        private const val ARG_CURRENT_BSSID = "currentn_bssid"
        private const val ARG_CURRENT_ADDR = "currentn_addr"
        const val TAG = "confirm login"
        fun newInstance(addr: String, bssid: String): LoginFragment {
            val args = Bundle()
            args.putString(ARG_CURRENT_BSSID, bssid)
            args.putString(ARG_CURRENT_ADDR, addr)
            val fragment = LoginFragment()
            fragment.arguments = args
            return fragment
        }
    }
}