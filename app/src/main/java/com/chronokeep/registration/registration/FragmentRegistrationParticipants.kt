package com.chronokeep.registration.registration

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chronokeep.registration.R
import com.chronokeep.registration.interfaces.ChronoActivity
import com.chronokeep.registration.interfaces.ChronoFragment
import com.chronokeep.registration.interfaces.ParticipantsWatcher
import com.chronokeep.registration.layouts.ClearEditText
import com.chronokeep.registration.network.ConnectionHandler
import com.chronokeep.registration.objects.registration.GetParticipantsRequest
import com.chronokeep.registration.objects.registration.Participant
import com.chronokeep.registration.util.Globals

class FragmentRegistrationParticipants: Fragment(), OnClickListener, ParticipantsWatcher, ChronoFragment {
    private val tag: String = "Chrono.RPartFrag"

    private var partList: RecyclerView? = null
    private var participantsAdapter: ListAdapterRegistrationParticipants

    init {
        val list = Globals.getRegistrationParticipants()
        participantsAdapter = ListAdapterRegistrationParticipants(list, this)
    }

    override fun updateTitle() {
        val act = activity as ChronoActivity?
        act?.setActivityTitle(Globals.getServerInfo().name)
    }

    override fun onResume() {
        Log.d(tag, "onResume")
        super.onResume()
        if (Globals.con == null || Globals.con?.alive() == false) {
            Log.d(tag, "Con is dead.")
            val act = activity
            if (act is ActivityRegistration) {
                act.finish()
            }
        }
        updateTitle()
        Globals.con?.setHandler(ConnectionHandler(Looper.getMainLooper(), this, activity))
    }

    override fun disconnected() {
        val act = activity
        if (act is ActivityRegistration) {
            act.finish()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun updateParticipants() {
        val list = Globals.getRegistrationParticipants()
        Log.d(tag, "updateRegistrationParticipants called")
        list.sortWith(compareBy<Participant> { part -> part.last }.thenBy { part -> part.first } )
        participantsAdapter.objects = list
        participantsAdapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "onCreateView")
        if (Globals.con == null || Globals.con?.alive() == false) {
            Log.d(tag, "Con is dead.")
            val act = activity
            if (act is ActivityRegistration) {
                act.finish()
            }
        }
        updateTitle()
        val rootView = inflater.inflate(R.layout.fragment_registration_participants, container, false)
        partList = rootView.findViewById(R.id.participants_list)
        partList?.adapter = participantsAdapter
        partList?.layoutManager = LinearLayoutManager(context)
        val newButton: Button = rootView.findViewById(R.id.add_participant)
        newButton.setOnClickListener(this)
        Globals.con?.setHandler(ConnectionHandler(Looper.getMainLooper(), this, activity))
        val searchBox = rootView.findViewById<ClearEditText>(R.id.search_box)
        searchBox?.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(s: CharSequence?, start: Int, end: Int, count: Int) {
                Log.d(tag, "Search text is ${s.toString()}")
                participantsAdapter.setSearchString(s.toString())
                participantsAdapter.notifyDataSetChanged()
            }
        })
        Globals.con?.sendAsyncMessage(GetParticipantsRequest().encode())
        return rootView
    }

    override fun onClick(view: View?) {
        // show a DialogFragmentEditParticipant with no built in information
        val readerFrag = FragmentEditParticipant(
            Participant(
                id = -1,
                bib = "",
                first = "",
                last = "",
                distance = "",
                gender = "",
                birthdate = "",
                mobile = "",
                sms = false,
                apparel = ""
            )
        )
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.registration_fragment_container, readerFrag)
            .addToBackStack("fragment_add_participant")
            .commit()
    }
}