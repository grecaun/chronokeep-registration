package com.chronokeep.registration.notification

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.chronokeep.registration.R

@Suppress("SpellCheckingInspection")
class DialogFragmentNotification(
    private val message: String
): DialogFragment() {
    private val tag: String = "Chrono.Notif"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "onCreateView")
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        val output = inflater.inflate(R.layout.dialogfragment_notification, container, false)
        val messageView = output.findViewById<TextView>(R.id.notification_view)
        if (messageView != null) {
            messageView.text = message
        }
        return output
    }
}