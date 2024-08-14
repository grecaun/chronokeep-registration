package com.chronokeep.registration.serverlist

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chronokeep.registration.R
import com.chronokeep.registration.list_items.Server
import com.chronokeep.registration.network.ServerFinder
import com.chronokeep.registration.util.Constants
import com.chronokeep.registration.util.Globals
import com.chronokeep.registration.wait.DialogFragmentWait
import com.chronokeep.registration.registration.DialogFragmentLogin
import java.lang.ref.WeakReference

class DialogFragmentServerList : DialogFragment(), View.OnClickListener {
    private val tag = "Chrono.ConnectFragment"

    private var background: Thread? = null
    private var serverFinder: ServerFinder? = null

    private var serverListItems = ArrayList<Server>()
    private var serverListAdapter: ListAdapterServer? = null
    private var isActive = false
    private var stopped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate")
    }

    override fun onCreateView(
        aInflater: LayoutInflater,
        aContainer: ViewGroup?,
        aSavedInstanceState: Bundle?
    ) : View {
        Log.d(tag, "onCreateView")
        val rootView = aInflater.inflate(R.layout.fragment_connect, aContainer, false)
        val serverList = rootView.findViewById<RecyclerView>(R.id.serverlist)
        Log.d(tag, "Setting adapter to listview.")
        serverListAdapter = ListAdapterServer(serverListItems, this)
        serverList.adapter = serverListAdapter
        serverList.layoutManager = LinearLayoutManager(context)
        rootView.findViewById<Button>(R.id.refreshButton).setOnClickListener(this)
        rootView.findViewById<Button>(R.id.web_connect).setOnClickListener(this)
        rootView.findViewById<Button>(R.id.web_connect).setOnClickListener(this)
        return rootView
    }

    override fun onResume() {
        Log.d(tag, "onResume")
        super.onResume()
        if (background == null || stopped) {
            stopped = false
            serverListItems.clear()
            Log.d(tag, "Creating handler.")
            val handle = ServFindHandler(Looper.getMainLooper(), this, serverListItems, serverListAdapter)
            serverFinder = ServerFinder(handle)
            background = Thread(serverFinder)
            background!!.start()
            isActive = true
            activity?.findViewById<View>(R.id.refreshButton)?.visibility = View.GONE
            activity?.findViewById<View>(R.id.searchLayout)?.visibility = View.VISIBLE
        } else {
            val handle = ServFindHandler(Looper.getMainLooper(), this, serverListItems, serverListAdapter)
            serverFinder?.setHandler(handle)
        }
        if (serverFinder?.stopped() == true) {
            activity?.findViewById<View>(R.id.refreshButton)?.visibility = View.VISIBLE
            activity?.findViewById<View>(R.id.searchLayout)?.visibility = View.GONE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(view: View) {
        Log.d(tag, "onClick")
        if (view.tag == getString(R.string.tag_button)) {
            Log.d(tag, "Someone clicked the top bar button")
            activity?.findViewById<View>(R.id.refreshButton)?.visibility = View.GONE
            activity?.findViewById<View>(R.id.searchLayout)?.visibility = View.VISIBLE
            serverListItems.clear()
            serverListAdapter?.notifyDataSetChanged()
            Log.d(tag, "Creating handler.")
            val handle = ServFindHandler(Looper.getMainLooper(), this, serverListItems, serverListAdapter)
            Log.d(tag, "Starting thread to look for servers.")
            serverFinder = ServerFinder(handle)
            background = Thread(serverFinder)
            background!!.start()
        } else if (view.tag == getString(R.string.tag_web)) {
            Log.d(tag, "Someone wants to connect to the web registration")
            serverFinder?.stop()
            // TODO open login fragment
            val loginFrag = DialogFragmentLogin()
            val ft = parentFragmentManager.beginTransaction()
            val prev = parentFragmentManager.findFragmentByTag("fragment_login")
            if (prev != null) {
                ft.remove(prev)
            }
            val thisFrag = parentFragmentManager.findFragmentByTag("fragment_server_list")
            if (thisFrag != null) {
                ft.remove(thisFrag)
            }
            ft.addToBackStack(null)
            loginFrag.show(ft, "fragment_login")
        }
    }

    fun serverClicked(position: Int) {
        Log.d(tag, "Position in array is $position")
        if (position < 0) {
            return
        }
        serverFinder?.stop()
        val item = serverListItems[position]
        Globals.uniqueToken = item.id
        val nextFrag = DialogFragmentWait(item)
        stopped = true
        val ft = parentFragmentManager.beginTransaction()
        val thisFrag = parentFragmentManager.findFragmentByTag("fragment_server_list")
        if (thisFrag != null) {
            ft.remove(thisFrag)
        }
        ft.addToBackStack(null)
        nextFrag.show(ft, "fragment_wait")
    }

    private class ServFindHandler(
        looper: Looper,
        frag: DialogFragmentServerList,
        items: ArrayList<Server>,
        adapter: ListAdapterServer?
    ) : Handler(looper) {
        private val tag = "Chrono.SFH"
        val mConFrag = WeakReference(frag)
        val serverListItems = items
        val serverListAdapter = adapter

        @SuppressLint("NotifyDataSetChanged")
        override fun handleMessage(msg: Message) {
            val connectFragment = this.mConFrag.get()
            Log.d(tag, "New message!")
            if (connectFragment == null) { return }
            when (msg.what) {
                ServerFinder.msg_server_list_avail -> {
                    Log.d(tag, "Server list updated.")
                    serverListItems.clear()
                    serverListItems.addAll(ServerFinder.getAvailableServerItems())
                    serverListAdapter?.notifyDataSetChanged()
                }
                ServerFinder.msg_server_finder_done -> {
                    Log.d(tag, "Server search finished.")
                    val search = connectFragment.activity?.findViewById<View>(R.id.searchLayout)
                    if (search != null) {
                        search.visibility = View.GONE
                    }
                    val refresh = connectFragment.activity?.findViewById<View>(R.id.refreshButton)
                    if (refresh != null) {
                        refresh.visibility = View.VISIBLE
                    }
                    connectFragment.isActive = false
                }
                else -> {
                    Log.d(tag, "Unknown message received.")
                }
            }
        }
    }
}