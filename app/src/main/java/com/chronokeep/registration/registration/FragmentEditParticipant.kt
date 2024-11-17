package com.chronokeep.registration.registration

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.chronokeep.registration.R
import com.chronokeep.registration.interfaces.ChronoActivity
import com.chronokeep.registration.interfaces.ChronoFragment
import com.chronokeep.registration.interfaces.ParticipantsWatcher
import com.chronokeep.registration.objects.database.DatabaseParticipant
import com.chronokeep.registration.util.Globals
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FragmentEditParticipant(
    private var participant: DatabaseParticipant,
    private val watcher: ParticipantsWatcher
) :
    Fragment(), OnClickListener, ParticipantsWatcher, ChronoFragment {
    private val tag: String = "Chrono.EditPartDia"

    private var eventHolder: LinearLayout? = null
    private var event: Spinner? = null
    private var distance: Spinner? = null
    private var bib: EditText? = null
    private var first: EditText? = null
    private var last: EditText? = null
    private var gender: Spinner? = null
    private var otherGender: EditText? = null
    private var birthdate: EditText? = null
    private var eventAdapter: ArrayAdapter<String>? = null
    private var distanceAdapter: ArrayAdapter<String>? = null
    private var genderAdapter: ArrayAdapter<CharSequence>? = null
    private var apparel: TextView? = null

    private var chronokeepInfoDict: HashMap<String, String> = HashMap()

    override fun updateTitle() {
        val act = activity as ChronoActivity?
        this.context?.getString(R.string.edit_participant)?.let { act?.setActivityTitle(it) }
    }

    override fun onResume() {
        super.onResume()
        updateTitle()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val output = inflater.inflate(R.layout.dialogfragment_registration_edit_participant, container, false)
        eventHolder = output.findViewById(R.id.event_wrapper)
        event = output.findViewById(R.id.edit_participant_event)
        distance = output.findViewById(R.id.edit_participant_distance)
        bib = output.findViewById(R.id.edit_participant_bib)
        first = output.findViewById(R.id.edit_participant_first_name)
        last = output.findViewById(R.id.edit_participant_last_name)
        gender = output.findViewById(R.id.edit_participant_gender)
        otherGender = output.findViewById(R.id.edit_participant_gender_other)
        apparel = output.findViewById(R.id.participant_apparel)
        genderAdapter = ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.genders,
            android.R.layout.simple_spinner_dropdown_item
        )
        gender?.adapter = genderAdapter
        gender?.onItemSelectedListener = object: OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (gender!!.selectedItem.toString().equals("other", true)) {
                    otherGender?.visibility = VISIBLE
                    otherGender?.setText(participant.gender)
                } else {
                    otherGender?.visibility = GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                otherGender?.visibility = GONE
            }
        }
        birthdate = output.findViewById(R.id.edit_participant_birthdate)
        val cancel: Button = output.findViewById(R.id.cancel_button)
        cancel.setOnClickListener(this)
        val submit: Button = output.findViewById(R.id.submit_button)
        submit.setOnClickListener(this)
        updateFields()
        return output
    }

    @SuppressLint("SetTextI18n")
    private fun updateFields() {
        chronokeepInfoDict.clear()
        for (info: String in Globals.getRegistrationYears()) {
            val splitInfo = info.split(",")
            if (splitInfo.size > 2) {
                chronokeepInfoDict["${splitInfo[1]} ${splitInfo[2]}"] = info
            }
        }
        val infoVals = ArrayList(chronokeepInfoDict.keys)
        if (infoVals.size < 1) {
            infoVals.add("local")
        }
        eventAdapter = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            infoVals
        )
        event?.adapter = eventAdapter
        event?.setSelection(0)
        for (info: String in infoVals) {
            if (chronokeepInfoDict[info] == participant.chronokeep_info
                && eventAdapter!!.getPosition(info) >= 0) {
                event?.setSelection(eventAdapter!!.getPosition(info))
            }
        }
        if (infoVals.size > 1) {
            eventHolder?.visibility = VISIBLE
        } else {
            //eventHolder?.visibility = GONE
        }
        distanceAdapter = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            Globals.getRegistrationDistances()
        )
        distance?.adapter = distanceAdapter
        if (distanceAdapter!!.getPosition(participant.distance) >= 0) {
            distance?.setSelection(distanceAdapter!!.getPosition(participant.distance))
        }
        bib?.setText(participant.bib)
        first?.setText(participant.first)
        last?.setText(participant.last)
        if (genderAdapter!!.getPosition(participant.gender) >= 0) {
            gender?.setSelection(genderAdapter!!.getPosition(participant.gender))
            otherGender?.visibility = GONE
        } else if (participant.gender.isNotEmpty()) {
            gender?.setSelection(genderAdapter!!.getPosition(getString(R.string.other)))
            otherGender?.visibility = VISIBLE
            otherGender?.setText(participant.gender)
        }
        var date = LocalDate.now()
        if (participant.birthdate.isNotEmpty()) {
            date = LocalDate.parse(participant.birthdate, DateTimeFormatter.ofPattern("M/d/yyyy"))
        }
        val day = date.dayOfMonth
        val month = date.monthValue
        val year = date.year
        birthdate?.setText("$month/$day/$year")
        apparel?.text = participant.apparel
    }

    private fun fromFields(): DatabaseParticipant {
        var gender = gender?.selectedItem.toString()
        if (gender.equals("other", true)) {
            gender = otherGender?.text.toString()
        }
        var info = event?.selectedItem.toString()
        if (chronokeepInfoDict.containsKey(info)) {
            info = chronokeepInfoDict[info]!!
        }
        Log.d(tag, "gender == $gender")
        return DatabaseParticipant(
            primary = participant.primary,
            id = participant.id,
            bib = bib?.text.toString(),
            first = first?.text.toString(),
            last = last?.text.toString(),
            birthdate = birthdate?.text.toString(),
            gender = gender,
            distance = distance?.selectedItem.toString(),
            mobile = participant.mobile,
            sms = participant.sms,
            apparel = participant.apparel,
            chronokeep_info = info
        )
    }

    override fun onClick(view: View?) {
        Log.d(tag, "onClick")
        if (view?.id == R.id.submit_button) {
            Log.d(tag, "Submit clicked.")
            if (participant.primary < 1 && participant.id.isEmpty()) {
                Log.d(tag, "New participant: ${fromFields()}")
                Globals.getDatabase()?.participantDao()?.addParticipant(fromFields())
                /*Globals.getConnection()?.sendAsyncMessage(AddParticipantRequest(
                    participant = fromFields()
                ).encode())*/
            } else {
                Log.d(tag, "Updating participant: ${fromFields()}")
                Globals.getDatabase()?.participantDao()?.updateParticipant(fromFields())
                /*Globals.getConnection()?.sendAsyncMessage(UpdateParticipantRequest(
                    participant = fromFields()
                ).encode())*/
            }
            watcher.updateParticipants()
        }
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.popBackStack()
    }

    override fun updateParticipants() {}
}