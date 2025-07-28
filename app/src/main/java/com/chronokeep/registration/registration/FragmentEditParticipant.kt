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
import com.chronokeep.registration.network.chronokeep.ChronokeepInterface
import com.chronokeep.registration.objects.database.DatabaseParticipant
import com.chronokeep.registration.objects.database.DatabaseSetting
import com.chronokeep.registration.objects.registration.AddParticipantRequest
import com.chronokeep.registration.objects.registration.UpdateParticipantRequest
import com.chronokeep.registration.util.Constants
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
    private val chronokeep = ChronokeepInterface.getInstance()

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
        event?.visibility = VISIBLE
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
            if (splitInfo.size > 1) {
                chronokeepInfoDict["${splitInfo[1]} ${splitInfo[0]}"] = info
            } else {
                chronokeepInfoDict[info] = info
            }
        }
        val infoVals = ArrayList(chronokeepInfoDict.keys)
        if (infoVals.isEmpty()) {
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
            eventHolder?.visibility = GONE
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
        val registrationId = participant.id.ifEmpty {
            "${first?.text.toString()}${last?.text.toString()}${distance?.selectedItem.toString()}${birthdate?.text.toString()}${info}"
        }
        return DatabaseParticipant(
            primary = participant.primary,
            id = registrationId,
            bib = bib?.text.toString(),
            first = first?.text.toString(),
            last = last?.text.toString(),
            birthdate = birthdate?.text.toString(),
            gender = gender,
            distance = distance?.selectedItem.toString(),
            mobile = participant.mobile,
            sms = participant.sms,
            apparel = participant.apparel,
            chronokeep_info = info,
            uploaded = false,
            updated_at = participant.updated_at,
        )
    }

    override fun onClick(view: View?) {
        Log.d(tag, "onClick")
        if (view?.id == R.id.submit_button) {
            Log.d(tag, "Submit clicked.")
            val part = fromFields()
            if (participant.primary < 1 && participant.id.isEmpty()) {
                Log.d(tag, "New participant: ${part.first} ${part.last}")
                Globals.getDatabase()?.participantDao()?.addParticipant(part)
                try {
                    Globals.getConnection()?.sendAsyncMessage(
                        AddParticipantRequest(
                            participant = part
                        ).encode())
                } catch (_: Exception) {}
            } else {
                Log.d(tag, "Updating participant: ${part.first} ${part.last}")
                Globals.getDatabase()?.participantDao()?.updateParticipant(part)
                try {
                    Globals.getConnection()?.sendAsyncMessage(
                        UpdateParticipantRequest(
                            participant = part
                        ).encode())
                } catch (_: Exception) {}
            }
            // Update participants on Chronokeep
            val settingDao = Globals.getDatabase()?.settingDao()
            val access = settingDao?.getSetting(Constants.setting_auth_token)
            val refresh = settingDao?.getSetting(Constants.setting_refresh_token)
            if (access != null && access.value.isNotEmpty()
                && refresh != null && refresh.value.isNotEmpty()) {
                var updatedAfter = Globals.getUpdatedAfter()
                val participants = Globals.getDatabase()?.participantDao()?.getNotUploaded()
                val updatedParticipants = ArrayList<DatabaseParticipant>()
                val newParticipants = ArrayList<DatabaseParticipant>()
                if (participants != null) {
                    for (p in participants) {
                        if (p.updated_at > updatedAfter) {
                            updatedAfter = p.updated_at
                        }
                        if (p.bib.isNotEmpty()) {
                            if (p.id.isNotEmpty()) {
                                updatedParticipants.add(p)
                            } else {
                                newParticipants.add(p)
                            }
                        }
                    }
                }
                Log.d(tag, "Updating ${updatedParticipants.count()} participants.")
                if (updatedParticipants.isNotEmpty()) {
                    val splitParts = HashMap<String, ArrayList<DatabaseParticipant>>()
                    for (p: DatabaseParticipant in updatedParticipants) {
                        if (!splitParts.containsKey(p.chronokeep_info)) {
                            splitParts[p.chronokeep_info] = ArrayList()
                        }
                        splitParts[p.chronokeep_info]?.add(p)
                    }
                    for (info: String in splitParts.keys) {
                        val infoSplit = info.split(",")
                        if (infoSplit.size > 1
                            && infoSplit[0].isNotBlank()
                            && infoSplit[1].isNotBlank()
                            && splitParts.containsKey(info)
                            && splitParts[info]!!.isNotEmpty()
                        ) {
                            val slug = infoSplit[0]
                            val year = infoSplit[1]
                            chronokeep.updateParticipant(
                                access.value,
                                refresh.value,
                                slug,
                                year,
                                splitParts[info]!!,
                                updatedAfter,
                                { response ->
                                    if (response != null) {
                                        val count = splitParts[info]!!.size
                                        Log.d(tag, "Count is $count after update.")
                                        val newParts = ArrayList<DatabaseParticipant>()
                                        for (p in response.updated_participants) {
                                            newParts.add(p.toDatabaseParticipant("$slug,$year"))
                                            if (p.updated_at > updatedAfter) {
                                                updatedAfter = p.updated_at
                                            }
                                        }
                                        Globals.getDatabase()?.participantDao()?.addParticipants(newParts)
                                        if (count > 0){
                                            for (p in updatedParticipants) {
                                                p.uploaded = true
                                                Globals.getDatabase()?.participantDao()?.setUploaded(p.primary)
                                            }
                                        }
                                    }
                                },
                                {}
                            )
                        }
                    }
                }
                Log.d(tag, "Adding ${newParticipants.count()} participants.")
                if (newParticipants.isNotEmpty()) {
                    val splitParts = HashMap<String, ArrayList<DatabaseParticipant>>()
                    for (p: DatabaseParticipant in updatedParticipants) {
                        if (!splitParts.containsKey(p.chronokeep_info)) {
                            splitParts[p.chronokeep_info] = ArrayList()
                        }
                        splitParts[p.chronokeep_info]?.add(p)
                    }
                    for (info: String in splitParts.keys) {
                        val infoSplit = info.split(",")
                        if (infoSplit.size > 1
                            && infoSplit[0].isNotBlank()
                            && infoSplit[1].isNotBlank()
                            && splitParts.containsKey(info)
                            && splitParts[info]!!.isNotEmpty()
                        ) {
                            val slug = infoSplit[0]
                            val year = infoSplit[1]
                            chronokeep.addParticipant(
                                access.value,
                                refresh.value,
                                slug,
                                year,
                                newParticipants,
                                updatedAfter,
                                { response ->
                                    if (response != null) {
                                        val count = splitParts[info]!!.size
                                        Log.d(tag, "Count is $count after update.")
                                        val newParts = ArrayList<DatabaseParticipant>()
                                        for (p in response.updated_participants) {
                                            newParts.add(p.toDatabaseParticipant("$slug,$year"))
                                            if (p.updated_at > updatedAfter) {
                                                updatedAfter = p.updated_at
                                            }
                                        }
                                        Globals.getDatabase()?.participantDao()?.addParticipants(newParts)
                                        if (count > 0){
                                            for (p in updatedParticipants) {
                                                p.uploaded = true
                                                Globals.getDatabase()?.participantDao()?.updateParticipant(p)
                                            }
                                        }
                                    } else {
                                        settingDao.addSetting(DatabaseSetting(name= Constants.setting_auth_token, value=""))
                                        settingDao.addSetting(DatabaseSetting(name= Constants.setting_refresh_token, value=""))
                                    }
                                },
                                {}
                            )
                        }
                    }
                }
                Globals.setUpdatedAfter(updatedAfter)
            }
            watcher.updateParticipants()
        }
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentManager.popBackStack()
    }

    override fun updateParticipants() {}
}