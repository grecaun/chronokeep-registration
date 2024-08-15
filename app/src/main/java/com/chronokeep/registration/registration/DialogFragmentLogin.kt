package com.chronokeep.registration.registration

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.chronokeep.registration.R
import com.chronokeep.registration.interfaces.ParticipantsWatcher
import com.chronokeep.registration.network.chronokeep.ChronokeepInterface
import com.chronokeep.registration.network.chronokeep.objects.ChronokeepEvent
import com.chronokeep.registration.objects.database.Database
import com.chronokeep.registration.objects.database.DatabaseParticipant
import com.chronokeep.registration.objects.database.DatabaseSetting
import com.chronokeep.registration.util.Constants
import com.chronokeep.registration.util.Globals
import java.time.LocalDate

class DialogFragmentLogin(
    private val watcher: ParticipantsWatcher?
) : DialogFragment(), OnClickListener {
    private val tag: String = "Chrono.Login"

    private var userNameView: EditText? = null
    private var passwordView: EditText? = null
    private var saveUsername: CheckBox? = null
    private var submitButton: Button? = null
    private var loginInfoContainer: LinearLayout? = null

    private var loadingContainer: LinearLayout? = null
    private var errorView: TextView? = null

    private var eventsContainer: LinearLayout? = null
    private var eventsSpinner: Spinner? = null
    private var eventErrorView: TextView? = null

    private val eventDict: HashMap<String, ChronokeepEvent> = HashMap()

    private var database: Database? = null

    private val chronokeep = ChronokeepInterface.getInstance()

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
        errorView = output.findViewById(R.id.error_view)
        eventErrorView = output.findViewById(R.id.event_error_view)
        submitButton?.setOnClickListener(this)
        val cancelButton: Button = output.findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener(this)
        val token = database!!.settingDao().getSetting(Constants.setting_auth_token)
        val refresh = database!!.settingDao().getSetting(Constants.setting_refresh_token)
        if (token != null && token.value.isNotEmpty() && refresh != null && refresh.value.isNotEmpty()) {
            loadingContainer!!.visibility = View.VISIBLE
            loginInfoContainer!!.visibility = View.GONE
            eventsContainer!!.visibility = View.GONE
            submitButton!!.isEnabled = false
            getEvents(token.value, refresh.value)
        }
        return output
    }

    private val eventComparator = Comparator<ChronokeepEvent> { one, two ->
        val oneDate = LocalDate.parse(one?.recent_time)
        val twoDate = LocalDate.parse(two?.recent_time)
        twoDate.compareTo(oneDate)
    }

    private fun getParticipants(access: String, refresh: String, slug: String) {
        chronokeep.getParticipants(
            access,
            refresh,
            slug,
            @SuppressLint("SetTextI18n")
            { response ->
                if (response != null) {
                    val newParts = ArrayList<DatabaseParticipant>()
                    for (p in response.participants) {
                        newParts.add(p.toDatabaseParticipant())
                    }
                    database?.participantDao()?.addParticipants(newParts)
                    watcher?.updateParticipants()
                } else {
                    database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_auth_token, value=""))
                    database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_refresh_token, value=""))
                    loadingContainer!!.visibility = View.GONE
                    loginInfoContainer!!.visibility = View.VISIBLE
                    eventsContainer!!.visibility = View.GONE
                    errorView?.text = "Error getting participants. Try again."
                    errorView?.visibility = View.VISIBLE
                    submitButton!!.isEnabled = true
                    state = LoginState.USERNAME
                }
            },
            @SuppressLint("SetTextI18n")
            { message ->
                Log.d(tag, "Error message: $message")
                database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_auth_token, value=""))
                database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_refresh_token, value=""))
                loadingContainer!!.visibility = View.GONE
                loginInfoContainer!!.visibility = View.VISIBLE
                eventsContainer!!.visibility = View.GONE
                errorView?.text = "Error getting participants. Try again."
                errorView?.visibility = View.VISIBLE
                submitButton!!.isEnabled = true
                state = LoginState.USERNAME
            }
        )
    }

    private fun getEvents(access: String, refresh: String) {
        chronokeep.getEvents(
            access,
            refresh,
            @SuppressLint("SetTextI18n")
            { response ->
                // success sets events
                if (response != null) {
                    eventDict.clear()
                    val eventNames = ArrayList<String>()
                    val sortedEvents = response.events.sortedWith( eventComparator )
                    for (e in sortedEvents) {
                        eventDict[e.name] = e
                        eventNames.add(e.name)
                    }
                    val adapt = ArrayAdapter(
                        this.requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        eventNames
                    )
                    eventsSpinner?.adapter = adapt
                    loadingContainer?.visibility = View.GONE
                    loginInfoContainer?.visibility = View.GONE
                    eventsContainer?.visibility = View.VISIBLE
                    submitButton!!.isEnabled = true
                    state = LoginState.EVENTS
                } else {
                    database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_auth_token, value=""))
                    database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_refresh_token, value=""))
                    loadingContainer!!.visibility = View.GONE
                    loginInfoContainer!!.visibility = View.VISIBLE
                    eventsContainer!!.visibility = View.GONE
                    errorView?.text = "Error getting events. Try again."
                    errorView?.visibility = View.VISIBLE
                    submitButton!!.isEnabled = true
                    state = LoginState.USERNAME
                }
            },
            @SuppressLint("SetTextI18n")
            { message ->
                Log.d(tag, "Error message: $message")
                database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_auth_token, value=""))
                database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_refresh_token, value=""))
                loadingContainer!!.visibility = View.GONE
                loginInfoContainer!!.visibility = View.VISIBLE
                eventsContainer!!.visibility = View.GONE
                errorView?.text = "Error getting events. Try again."
                errorView?.visibility = View.VISIBLE
                submitButton!!.isEnabled = true
                state = LoginState.USERNAME
            }
        )
        // failure resets visibility
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(view: View?) {
        errorView?.visibility = View.GONE
        eventErrorView?.visibility = View.GONE
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
                        chronokeep.login(
                            email,
                            pass,
                            @SuppressLint("SetTextI18n")
                            { response ->
                                if (response != null) {
                                    val settingDao = database!!.settingDao()
                                    // save username if set to save
                                    if (saveUser) {
                                        settingDao.addSetting(DatabaseSetting(name=Constants.setting_save_username, value=Constants.setting_true))
                                        settingDao.addSetting(DatabaseSetting(name=Constants.setting_username, value=userNameView?.text.toString()))
                                    } else {
                                        settingDao.addSetting(DatabaseSetting(name=Constants.setting_save_username, value=Constants.setting_false))
                                        settingDao.addSetting(DatabaseSetting(name=Constants.setting_username, value=""))
                                    }
                                    // save token and refresh token
                                    settingDao.addSetting(DatabaseSetting(name=Constants.setting_auth_token, value=response.access_token))
                                    settingDao.addSetting(DatabaseSetting(name=Constants.setting_refresh_token, value=response.refresh_token))
                                    // call get events
                                    getEvents(response.access_token, response.refresh_token)
                                } else {
                                    database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_auth_token, value=""))
                                    database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_refresh_token, value=""))
                                    loadingContainer!!.visibility = View.GONE
                                    loginInfoContainer!!.visibility = View.VISIBLE
                                    eventsContainer!!.visibility = View.GONE
                                    errorView?.text = "Unable to login."
                                    errorView?.visibility = View.VISIBLE
                                    submitButton!!.isEnabled = true
                                    state = LoginState.USERNAME
                                }
                            },
                            @SuppressLint("SetTextI18n")
                            { message ->
                                Log.d(tag, "Error message: $message")
                                // failure resets visibility
                                database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_auth_token, value=""))
                                database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_refresh_token, value=""))
                                loadingContainer!!.visibility = View.GONE
                                loginInfoContainer!!.visibility = View.VISIBLE
                                eventsContainer!!.visibility = View.GONE
                                errorView?.text = "Unable to login."
                                errorView?.visibility = View.VISIBLE
                                submitButton!!.isEnabled = true
                                state = LoginState.USERNAME
                            }
                        )
                    }
                    LoginState.EVENTS -> {
                        if (eventsSpinner?.selectedItem != null && eventDict.containsKey(eventsSpinner!!.selectedItem)) {
                            val event = eventDict[eventsSpinner!!.selectedItem]!!
                            val settingsDao = database!!.settingDao()
                            settingsDao.addSetting(DatabaseSetting(name=Constants.setting_event_slug, value=event.slug))
                            val access = settingsDao.getSetting(name=Constants.setting_auth_token)
                            val refresh = settingsDao.getSetting(name=Constants.setting_refresh_token)
                            if (access != null && access.value.isNotEmpty() && refresh != null && refresh.value.isNotEmpty()) {
                                getParticipants(access.value, refresh.value, event.slug)
                            } else {
                                // access tokens not set
                                database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_auth_token, value=""))
                                database?.settingDao()?.addSetting(DatabaseSetting(name=Constants.setting_refresh_token, value=""))
                                settingsDao.addSetting(DatabaseSetting(name=Constants.setting_event_slug, value=""))
                                loadingContainer!!.visibility = View.GONE
                                loginInfoContainer!!.visibility = View.VISIBLE
                                eventsContainer!!.visibility = View.GONE
                                errorView?.text = "Error getting participants."
                                errorView?.visibility = View.VISIBLE
                                submitButton!!.isEnabled = true
                                state = LoginState.USERNAME
                            }
                        } else {
                            // no event selected
                            eventErrorView?.text = "No event selected."
                            eventErrorView?.visibility = View.VISIBLE
                        }
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