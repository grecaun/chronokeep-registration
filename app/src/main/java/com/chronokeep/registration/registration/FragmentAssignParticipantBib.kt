package com.chronokeep.registration.registration

import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.chronokeep.registration.R
import com.chronokeep.registration.interfaces.ChronoActivity
import com.chronokeep.registration.interfaces.ChronoFragment
import com.chronokeep.registration.interfaces.ParticipantsWatcher
import com.chronokeep.registration.network.ConnectionHandler
import com.chronokeep.registration.objects.database.DatabaseParticipant
import com.chronokeep.registration.objects.registration.UpdateParticipantRequest
import com.chronokeep.registration.util.Globals

class FragmentAssignParticipantBib(
    private var participant: DatabaseParticipant
) :
    Fragment(), OnClickListener, ParticipantsWatcher, ChronoFragment {
    private val tag: String = "Chrono.BibFra"

    private var name: TextView? = null
    private var distance: TextView? = null
    private var bib: EditText? = null
    private var sms: SwitchCompat? = null
    private var apparel: TextView? = null
    private var mobile: EditText? = null


    override fun updateTitle() {
        val act = activity as ChronoActivity?
        this.context?.getString(R.string.assign_bib)?.let { act?.setActivityTitle(it) }
    }

    override fun onResume() {
        super.onResume()
        if (Globals.con == null || Globals.con?.alive() == false) {
            Log.d(tag, "Con is dead.")
            val act = activity
            if (act is ActivityRegistration) {
                act.finish()
            }
        }
        bib?.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(bib, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun disconnected() {
        val act = activity
        if (act is ActivityRegistration) {
            act.finish()
        }
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
        val output = inflater.inflate(R.layout.fragment_registration_bib_assign, container, false)
        name = output.findViewById(R.id.participant_name)
        distance = output.findViewById(R.id.participant_distance)
        bib = output.findViewById(R.id.edit_participant_bib)
        apparel = output.findViewById(R.id.participant_apparel)
        val cancel: Button = output.findViewById(R.id.cancel_button)
        cancel.setOnClickListener(this)
        val submit: Button = output.findViewById(R.id.submit_button)
        submit.setOnClickListener(this)
        updateFields()
        Globals.con?.setHandler(ConnectionHandler(Looper.getMainLooper(), this, activity))
        bib?.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(bib, InputMethodManager.SHOW_IMPLICIT)
        return output
    }

    private fun updateFields() {
        name?.text = this.context?.getString(R.string.name_placeholder, participant.first, participant.last)
        distance?.text = participant.distance
        bib?.setText(participant.bib)
        sms?.isChecked = participant.sms
        apparel?.text = participant.apparel
        mobile?.setText(participant.mobile)
    }

    private fun fromFields(): DatabaseParticipant {
        return DatabaseParticipant(
            id = participant.id,
            bib = bib?.text.toString(),
            first = participant.first,
            last = participant.last,
            birthdate = participant.birthdate,
            gender = participant.gender,
            distance = participant.distance,
            mobile = mobile?.text.toString(),
            sms = sms?.isChecked == true,
            apparel = participant.apparel,
        )
    }

    override fun onClick(view: View?) {
        Log.d(tag, "onClick")
        if (view?.id == R.id.submit_button) {
            // TODO update database
            Globals.con?.sendAsyncMessage(UpdateParticipantRequest(
                participant = fromFields()
            ).encode())
        }
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.popBackStack()
    }

    override fun updateParticipants() {
        /*
        for (part: DatabaseParticipant in Globals.getDatabase()) {
            if (part.id == participant.id) {
                participant = part
                break
            }
        }
        updateFields()
        */
    }
}