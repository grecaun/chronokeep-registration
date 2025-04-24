package com.chronokeep.registration.registration

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.chronokeep.registration.R
import com.chronokeep.registration.objects.database.DatabaseParticipant

class ListAdapterRegistrationParticipants(
    var objects: MutableList<DatabaseParticipant>,
    private val frag: FragmentRegistrationParticipants
) :
    RecyclerView.Adapter<ListAdapterRegistrationParticipants.ParticipantView>() {
    private val tag: String = "Chrono.PartLA"

    private var searchString = ""
    private var onlyBibs = false

    class ParticipantView(private val adapter: ListAdapterRegistrationParticipants, view: View) :
        RecyclerView.ViewHolder(view), OnClickListener {
        private val tag = "Chrono.PartView"
        var pos: Int = -1
        val nameView: TextView = view.findViewById(R.id.part_name)
        val ageView: TextView = view.findViewById(R.id.part_age)
        val genderView: TextView = view.findViewById(R.id.part_gender)
        val bibView: TextView = view.findViewById(R.id.part_bib)
        val distanceView: TextView = view.findViewById(R.id.part_dist)

        override fun onClick(view: View?) {
            Log.d(tag, "bib button click")
            adapter.editParticipant(pos)
        }
    }

    fun editParticipant(position: Int) {
        if (position < 0 || position >= objects.size) {
            Toast.makeText(frag.requireContext(), "Unable to find participant.", Toast.LENGTH_SHORT).show()
            return
        }
        val participant = objects[position]
        val editFrag = FragmentEditParticipant(participant = participant, watcher = frag)
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
        holder.itemView.setOnClickListener(holder)
        val part = objects[holder.pos]
        holder.ageView.text = part.age()
        holder.genderView.text = part.gender
        holder.nameView.text = frag.context?.getString(R.string.name_placeholder, part.first, part.last)
        holder.bibView.text = part.bib
        holder.distanceView.text = part.distance
    }

    fun setOnlyBibs(bibs: Boolean) {
        onlyBibs = bibs
    }

    fun setSearchString(search: String) {
        searchString = search.trim()
    }

    override fun getItemId(index: Int): Long {
        var oldIndex = index
        for (i in 0 until objects.size) {
            // if we match then we decrement the position
            // when our oldIndex hits 0, we've found what we're looking for
            // Matching requires --
            // Empty search string or their first / last contains the search string, or their bib matches exactly
            // OnlyBibs is either not enabled, or it is and the bib value is not empty
            if ((searchString.isEmpty() ||
                    objects[i].first.startsWith(searchString, true) ||
                    objects[i].last.startsWith(searchString, true) ||
                    objects[i].bib.equals(searchString, true)) &&
                (!onlyBibs || objects[i].bib.isNotEmpty())){
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
            // Matching requires --
                // Empty search string or their first / last contains the search string, or their bib matches exactly
                // OnlyBibs is either not enabled, or it is and the bib value is not empty
            if ((searchString.isEmpty() ||
                    objects[i].first.startsWith(searchString, true) ||
                    objects[i].last.startsWith(searchString, true) ||
                    objects[i].bib.equals(searchString, true)) &&
                (!onlyBibs || objects[i].bib.isNotEmpty())) {
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
        if ((searchString.isEmpty() ||
                participant.first.startsWith(searchString, true) ||
                participant.last.startsWith(searchString, true) ||
                participant.bib.equals(searchString, true)) &&
            (!onlyBibs || participant.bib.isNotEmpty())) {
                count += 1
            }
        }
        return count
    }
}