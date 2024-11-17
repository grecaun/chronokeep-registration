package com.chronokeep.registration.registration

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.chronokeep.registration.R
import com.chronokeep.registration.interfaces.ChronoActivity
import com.chronokeep.registration.interfaces.ChronoFragment
import com.chronokeep.registration.interfaces.ParticipantsWatcher
import com.chronokeep.registration.objects.database.DatabaseParticipant
import com.chronokeep.registration.objects.registration.UpdateParticipantRequest
import com.chronokeep.registration.util.Globals

class FragmentAssignParticipantBib(
    private var participant: DatabaseParticipant,
    private val watcher: ParticipantsWatcher
) :
    Fragment(), OnClickListener, ParticipantsWatcher, ChronoFragment {
    private val tag: String = "Chrono.BibFra"

    private var name: TextView? = null
    private var distance: TextView? = null
    private var bib: EditText? = null
    private var apparel: TextView? = null


    override fun updateTitle() {
        val act = activity as ChronoActivity?
        this.context?.getString(R.string.assign_bib)?.let { act?.setActivityTitle(it) }
    }

    override fun onResume() {
        super.onResume()
        bib?.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(bib, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "onCreateView")
        updateTitle()
        val output = inflater.inflate(R.layout.dialogfragment_registration_bib_assign, container, false)
        name = output.findViewById(R.id.participant_name)
        distance = output.findViewById(R.id.participant_distance)
        bib = output.findViewById(R.id.edit_participant_bib)
        apparel = output.findViewById(R.id.participant_apparel)
        val cancel: Button = output.findViewById(R.id.cancel_button)
        cancel.setOnClickListener(this)
        val submit: Button = output.findViewById(R.id.submit_button)
        submit.setOnClickListener(this)
        updateFields()
        bib?.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(bib, InputMethodManager.SHOW_IMPLICIT)
        return output
    }

    private fun updateFields() {
        name?.text = this.context?.getString(R.string.name_placeholder, participant.first, participant.last)
        distance?.text = participant.distance
        bib?.setText(participant.bib)
        apparel?.text = participant.apparel
    }

    private fun fromFields(): DatabaseParticipant {
        return DatabaseParticipant(
            primary = participant.primary,
            id = participant.id,
            bib = bib?.text.toString(),
            first = participant.first,
            last = participant.last,
            birthdate = participant.birthdate,
            gender = participant.gender,
            distance = participant.distance,
            mobile = participant.mobile,
            sms = false,
            apparel = participant.apparel,
            chronokeep_info = participant.chronokeep_info
        )
    }

    override fun onClick(view: View?) {
        Log.d(tag, "onClick")
        if (view?.id == R.id.submit_button) {
            Globals.getDatabase()?.participantDao()?.updateParticipant(fromFields())
            /*Globals.getConnection()?.sendAsyncMessage(UpdateParticipantRequest(
                participant = fromFields()
            ).encode())*/
            watcher.updateParticipants()
        }
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.popBackStack()
    }

    override fun updateParticipants() {}
}