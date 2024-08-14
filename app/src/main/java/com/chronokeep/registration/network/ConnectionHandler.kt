package com.chronokeep.registration.network

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.chronokeep.registration.interfaces.ChronoFragment
import com.chronokeep.registration.interfaces.ParticipantsWatcher
import com.chronokeep.registration.objects.registration.RegistrationError
import com.chronokeep.registration.registration.ActivityRegistration
import com.chronokeep.registration.util.Globals
import com.chronokeep.registration.wait.DialogFragmentWait
import java.lang.ref.WeakReference

class ConnectionHandler(looper: Looper, frag: Fragment, private val act: Activity?): Handler(looper) {
    private val tag = "Chrono.ConHandler"
    private val mFrag: WeakReference<Fragment> = WeakReference<Fragment>(frag)
    private var dFrag: WeakReference<Fragment> = WeakReference<Fragment>(frag)

    fun setDFrag(frag: Fragment) {
        dFrag = WeakReference<Fragment>(frag)
    }

    override fun handleMessage(msg: Message) {
        val frag = mFrag.get() ?: return
        when (msg.what) {
            Connection.msg_connection_closed -> {
                Log.d(tag, "Connection closed.")
                try {
                    Globals.con?.stop()
                    Globals.conThread = null
                } catch (_: Exception) {
                    Log.d(tag, "error closing connection")
                }
                if (frag is ChronoFragment) {
                    frag.disconnected()
                }
            }
            Connection.msg_connection_unavailable -> {
                Log.d(tag, "Unable to connect.")
                try {
                    Globals.con?.stop()
                    Globals.conThread = null
                } catch (_: Exception) {
                    Log.d(tag, "error closing connection")
                }
                if (frag is ChronoFragment) {
                    frag.disconnected()
                }
            }
            Connection.msg_connection_open -> {
                Log.d(tag, "Connection open.")
                if (frag is DialogFragmentWait) {
                    Globals.connected = true
                    val intent = Intent(act, ActivityRegistration::class.java)
                    act?.startActivity(intent)
                }
            }
            Connection.msg_connection_success -> {
                Log.d(tag, "Success message received.")
            }
            Connection.msg_connection_participants -> {
                Log.d(tag, "Participants received.")
                if (frag is ParticipantsWatcher) {
                    frag.updateParticipants()
                }
            }
            Connection.msg_connection_error -> {
                Log.d(tag, "Error received.")
                val toastMessage: String
                val errMsg = Globals.getRegistrationError()
                toastMessage = when (errMsg) {
                    RegistrationError.UNKNOWN_MESSAGE -> "Server did not recognize communication."
                    RegistrationError.DISTANCE_NOT_FOUND -> "Distance not found."
                    RegistrationError.PARTICIPANT_NOT_FOUND -> "Participant not found."
                    else -> ""
                }
                Log.d(tag, "$(toastMessage)")
                if (toastMessage.isNotEmpty()) {
                    if (frag.context != null) {
                        Toast.makeText(frag.context, toastMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}