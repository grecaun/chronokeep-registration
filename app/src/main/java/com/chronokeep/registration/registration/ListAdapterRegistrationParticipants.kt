package com.chronokeep.registration.registration

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.chronokeep.registration.R
import com.chronokeep.registration.objects.database.DatabaseParticipant

class ListAdapterRegistrationParticipants(var objects: MutableList<DatabaseParticipant>, private val frag: Fragment) :
    RecyclerView.Adapter<ListAdapterRegistrationParticipants.ParticipantView>() {
    private val tag: String = "Chrono.PartLA"

    private var searchString = ""

    class ParticipantView(private val adapter: ListAdapterRegistrationParticipants, view: View) :
        RecyclerView.ViewHolder(view), OnClickListener {
        private val tag = "Chrono.PartView"
        var pos: Int = -1
        val nameView: TextView = view.findViewById(R.id.part_name)
        val ageView: TextView = view.findViewById(R.id.part_age)
        val genderView: TextView = view.findViewById(R.id.part_gender)
        private val bibButton: Button = view.findViewById(R.id.assign_bib)
        private val editButton: Button = view.findViewById(R.id.part_edit)

        init {
            bibButton.setOnClickListener(this)
            editButton.setOnClickListener {
                Log.d(tag, "edit button click")
                adapter.editParticipant(pos)
            }
        }

        override fun onClick(view: View?) {
            Log.d(tag, "bib button click")
            adapter.assignBib(pos)
        }
    }

    fun assignBib(position: Int) {
        if (position < 0 || position >= objects.size) {
            Toast.makeText(frag.requireContext(), "Unable to find participant.", Toast.LENGTH_SHORT).show()
            return
        }
        val participant = objects[position]
        val bibFrag = FragmentAssignParticipantBib(participant = participant)
        frag.requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.registration_fragment_container, bibFrag)
            .addToBackStack("fragment_bib_participant")
            .commit()
    }

    fun editParticipant(position: Int) {
        if (position < 0 || position >= objects.size) {
            Toast.makeText(frag.requireContext(), "Unable to find participant.", Toast.LENGTH_SHORT).show()
            return
        }
        val participant = objects[position]
        val editFrag = FragmentEditParticipant(participant = participant)
        frag.requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.registration_fragment_container, editFrag)
            .addToBackStack("fragment_edit_participant")
            .commit()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantView {
        Log.d(tag, "onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_registration_participant, parent, false)
        return ParticipantView(this, view)
    }

    override fun onBindViewHolder(holder: ParticipantView, position: Int) {
        holder.pos = getItemId(position).toInt()
        val part = objects[holder.pos]
        holder.ageView.text = part.age()
        holder.genderView.text = part.gender
        holder.nameView.text = frag.context?.getString(R.string.name_placeholder, part.first, part.last)
    }

    fun setSearchString(search: String) {
        searchString = search.trim()
    }

    override fun getItemId(index: Int): Long {
        var oldIndex = index
        for (i in 0 until objects.size) {
            // if we match then we decrement the position
            // when our oldIndex hits 0, we've found what we're looking for
            if (searchString.isEmpty() ||
                objects[i].first.startsWith(searchString, true) ||
                objects[i].last.startsWith(searchString, true)) {
                if (oldIndex == 0) {
                    return i.toLong()
                } else {
                    oldIndex -= 1
                }
            }
        }
        return 0
    }

    override fun getItemViewType(index: Int): Int {
        var oldIndex = index
        for (i in 0 until objects.size) {
            // if we match then we decrement the position
            // when our oldIndex hits 0, we've found what we're looking for
            if (searchString.isEmpty() ||
                objects[i].first.startsWith(searchString, true) ||
                objects[i].last.startsWith(searchString, true)) {
                if (oldIndex == 0) {
                    return i
                } else {
                    oldIndex -= 1
                }
            }
        }
        return 0
    }

    override fun getItemCount(): Int {
        var count = 0
        for (participant in objects) {
            if (searchString.isEmpty() ||
                participant.first.startsWith(searchString, true) ||
                participant.last.startsWith(searchString, true)) {
                count += 1
            }
        }
        return count
    }
}