package com.chronokeep.registration.wait

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.chronokeep.registration.R
import com.chronokeep.registration.list_items.Server
import com.chronokeep.registration.network.Connection
import com.chronokeep.registration.network.ConnectionHandler
import com.chronokeep.registration.util.Globals
import java.net.InetAddress

class DialogFragmentWait(item: Server) : DialogFragment() {
    private val tag = "Chrono.WaitFrag"
    var name: String = item.name
    private val address: InetAddress? = item.address
    private var port: Int = item.port

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "onCreateView")
        val act = activity
        if (act != null && this.address != null) {
            val handle = ConnectionHandler(Looper.getMainLooper(), this, act)
            Globals.con = Connection(this.address, this.port, handle)
            Globals.conThread = Thread(Globals.con)
            Globals.conThread?.start()
        }
        return inflater.inflate(R.layout.dialogfragment_wait, container, false)
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog!!.window?.setLayout(width, height)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume")
        if (Globals.connected) {
            this.dismiss()
            Globals.connected = false
        }
    }
}