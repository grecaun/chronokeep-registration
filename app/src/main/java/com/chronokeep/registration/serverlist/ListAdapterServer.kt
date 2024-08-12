package com.chronokeep.registration.serverlist

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chronokeep.registration.R
import com.chronokeep.registration.list_items.Server

class ListAdapterServer(val objects: MutableList<Server>, private val frag: FragmentServerList) :
    RecyclerView.Adapter<ListAdapterServer.ServerView>() {
    private val tag = "Chrono.SLA"

    class ServerView(private val frag: FragmentServerList, view: View) : RecyclerView.ViewHolder(view), OnClickListener {
        val nameView: TextView
        val idView: TextView
        var pos: Int = -1

        init {
            nameView = view.findViewById(R.id.sli_name)
            idView = view.findViewById(R.id.sli_identifier)
            val connectButton: Button = view.findViewById(R.id.sli_connect)
            connectButton.setOnClickListener(this)
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            frag.serverClicked(pos)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ServerView {
        Log.d(tag, "onCreateViewHolder")
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.row_serverlist, viewGroup, false)
        return ServerView(frag, view)
    }

    override fun onBindViewHolder(server: ServerView, position: Int) {
        server.pos = position
        server.nameView.text = objects[position].name
        server.idView.text = objects[position].id
    }

    override fun getItemCount() = this.objects.size
}