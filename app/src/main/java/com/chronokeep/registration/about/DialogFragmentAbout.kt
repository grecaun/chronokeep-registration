package com.chronokeep.registration.about

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.chronokeep.registration.BuildConfig
import com.chronokeep.registration.R

class DialogFragmentAbout: DialogFragment() {
    private val tag: String = "Chrono.About"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "onCreateView")
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val output = inflater.inflate(R.layout.dialogfragment_about, container, false)
        val versionView = output.findViewById<TextView>(R.id.versionName)
        if (versionView != null) {
            versionView.text = BuildConfig.VERSION_NAME
        }
        return output
    }
}