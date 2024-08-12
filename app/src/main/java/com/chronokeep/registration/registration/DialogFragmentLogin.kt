package com.chronokeep.registration.registration

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.chronokeep.registration.R
import com.chronokeep.registration.network.chronokeep.ChronokeepInterface
import com.chronokeep.registration.objects.database.Database
import com.chronokeep.registration.util.Constants
import com.chronokeep.registration.util.Globals

class DialogFragmentLogin : DialogFragment(), OnClickListener {
    private val tag: String = "Chrono.Login"

    private var userNameView: EditText? = null
    private var passwordView: EditText? = null
    private var saveUsername: CheckBox? = null
    private var submitButton: Button? = null

    private var loginInfoContainer: LinearLayout? = null
    private var loadingContainer: LinearLayout? = null
    private var eventsContainer: LinearLayout? = null

    private var eventsSpinner: Spinner? = null

    private var database: Database? = null

    private val ckeep = ChronokeepInterface.getInstance()

    private enum class LoginState {
        USERNAME, EVENTS, LOADING
    }

    private var state: LoginState = LoginState.USERNAME

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val output = inflater.inflate(R.layout.dialogfragment_web_connect, container, false)
        database = Globals.getDatabase()
        saveUsername = output.findViewById(R.id.save_username)
        val userNameIsSaved = database?.settingDao()?.getSetting(Constants.setting_save_username)?.value
        saveUsername?.isChecked = userNameIsSaved == Constants.setting_true
        userNameView = output.findViewById(R.id.web_username)
        if (userNameIsSaved == Constants.setting_true) {
            userNameView?.setText(database?.settingDao()?.getSetting(Constants.setting_username)?.value)
        }
        passwordView = output.findViewById(R.id.web_password)
        loginInfoContainer = output.findViewById(R.id.login_info_container)
        loadingContainer = output.findViewById(R.id.loading_container)
        eventsContainer = output.findViewById(R.id.events_container)
        eventsSpinner = output.findViewById(R.id.events_spinner)
        submitButton = output.findViewById(R.id.submit_button)
        submitButton?.setOnClickListener(this)
        val cancelButton: Button = output.findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener(this)
        return output
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.submit_button -> {
                Log.d(tag, "onClick - submit")
                when (state) {
                    LoginState.USERNAME -> {
                        state = LoginState.LOADING
                        val email = userNameView!!.text.toString()
                        val pass = passwordView!!.text.toString()
                        val saveUser = saveUsername?.isChecked == true
                        // attempt to log in using chronokeep network calls
                        loadingContainer!!.visibility = View.VISIBLE
                        loginInfoContainer!!.visibility = View.GONE
                        eventsContainer!!.visibility = View.GONE
                        submitButton!!.isEnabled = false

                        // call login
                        // success calls get events
                            // and saves username if set to save
                            /* if (saveUser) {
                                database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_save_username, value=Constants.setting_true))
                                database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_username, value=userNameView?.text.toString()))
                            } else {
                                database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_save_username, value=Constants.setting_false))
                                database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_username, value=""))
                            }*/
                            // success sets events
                            // failure resets visibility
                        // failure resets visibility
                    }
                    LoginState.EVENTS -> {
                        // fetch participants
                        // success initiates activity and dismisses dialog
                        // failure resets visibility
                    }
                    LoginState.LOADING -> {
                        Toast.makeText(context, "Working, please wait.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            R.id.cancel_button -> {
                Log.d(tag, "onClick - cancel")
                dialog?.dismiss()
            }
        }
    }
}