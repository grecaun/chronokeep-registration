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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chronokeep.registration.R
import com.chronokeep.registration.list_items.Server
import com.chronokeep.registration.network.ServerFinder
import com.chronokeep.registration.util.Constants
import com.chronokeep.registration.util.Globals
import com.chronokeep.registration.wait.FragmentWait
import com.chronokeep.registration.webregistration.DialogFragmentLogin
import java.lang.ref.WeakReference

class FragmentServerList : Fragment(), View.OnClickListener {
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
            val database = Globals.getDatabase()!!
            val token = database.settingDao().getSetting(Constants.setting_auth_token)
            val refresh = database.settingDao().getSetting(Constants.setting_refresh_token)
            val eventSlug = database.settingDao().getSetting(Constants.setting_event_slug)
            val eventYear = database.settingDao().getSetting(Constants.setting_event_slug)
            if (token != null && refresh != null && eventSlug != null) {
                Toast.makeText(context, "Already logged in!", Toast.LENGTH_SHORT).show()
                // TODO load activity after checking for participants, toast if not able to get participants
            } else {
                val loginFrag = DialogFragmentLogin()
                val ft = parentFragmentManager.beginTransaction()
                val prev = parentFragmentManager.findFragmentByTag("fragment_login")
                if (prev != null) {
                    ft.remove(prev)
                }
                loginFrag.show(ft, "fragment_login")
            }
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
        val nextFrag = FragmentWait(item)
        stopped = true
        //parentFragmentManager.beginTransaction()
        //    .replace(R.id.main_fragment_container, nextFrag)
        //    .addToBackStack("main")
        //    .commit()
    }

    private class ServFindHandler(
        looper: Looper,
        frag: FragmentServerList,
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