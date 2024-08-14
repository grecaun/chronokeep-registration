package com.chronokeep.registration.registration

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.chronokeep.registration.R
import com.chronokeep.registration.about.DialogFragmentAbout
import com.chronokeep.registration.interfaces.ChronoActivity
import com.chronokeep.registration.interfaces.MenuWatcher
import com.chronokeep.registration.objects.database.DatabaseParticipant
import com.chronokeep.registration.objects.registration.AddUpdateParticipantsRequest
import com.chronokeep.registration.objects.registration.GetParticipantsRequest
import com.chronokeep.registration.serverlist.DialogFragmentServerList
import com.chronokeep.registration.util.Constants
import com.chronokeep.registration.util.Globals

class ActivityRegistration: AppCompatActivity(), ChronoActivity, MenuWatcher {
    private val tag: String = "Chrono.RAct"

    private var menu: Menu? = null

    override fun getActivityTitle(): String {
        return supportActionBar?.title.toString()
    }

    override fun setActivityTitle(title: String) {
        supportActionBar?.title = title
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Globals.makeDatabase(applicationContext)
        setContentView(R.layout.activity_registration)
        Log.d(tag, "onCreate")
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.registration_fragment_container, FragmentRegistrationParticipants()).commit()
            setActivityTitle("Chronokeep Registration")
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Globals.setMenuWatcher(this)
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume")
        Globals.setMenuWatcher(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.main, menu)
        updateMenu()
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about -> {
                Log.d(tag, "User requests information about the app.")
                val aboutFrag = DialogFragmentAbout()
                val ft = supportFragmentManager.beginTransaction()
                val prev = supportFragmentManager.findFragmentByTag("fragment_about")
                if (prev != null) {
                    ft.remove(prev)
                }
                aboutFrag.show(ft, "fragment_about")
                return true
            }
            R.id.upload_local -> {
                Log.d(tag, "User wants to upload to local server.")
                val toUpload = ArrayList<DatabaseParticipant>()
                val database = Globals.getDatabase()
                val fullList = database?.participantDao()?.getParticipants() ?: return true
                for (part in fullList) {
                    if (part.bib.isNotEmpty()) {
                        toUpload.add(part)
                    }
                }
                Globals.getConnection()?.sendAsyncMessage(AddUpdateParticipantsRequest(
                    participants = toUpload
                ).encode())
                return true
            }
            R.id.download_local -> {
                Log.d(tag, "User wants to download from local server.")
                try {
                    Globals.getConnection()?.sendAsyncMessage(GetParticipantsRequest().encode())
                } catch (e: Exception) {
                    Log.d(tag, "Exception when sending message: ${e.message}")
                    Globals.getConnection()?.stop()
                }
                return true
            }
            R.id.upload_web -> {
                Log.d(tag, "User wants to upload to Chronokeep.")
                return true
            }
            R.id.download_web -> {
                Log.d(tag, "User wants to download from Chronokeep.")
                return true
            }
            R.id.menu_connect -> {
                Log.d(tag, "User wants to connect.")
                val connectFragment = DialogFragmentServerList()
                val ft = supportFragmentManager.beginTransaction()
                val prev = supportFragmentManager.findFragmentByTag("fragment_connect")
                if (prev != null) {
                    ft.remove(prev)
                }
                connectFragment.show(ft, "fragment_connect")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun updateMenu() {
        if (Globals.isConnected()) {
            menu?.findItem(R.id.menu_local)?.setVisible(true)
        }
        val database = Globals.getDatabase()
        if (database != null) {
            val token = database.settingDao().getSetting(Constants.setting_auth_token)
            val refresh = database.settingDao().getSetting(Constants.setting_refresh_token)
            if (token != null && refresh != null) {
                menu?.findItem(R.id.menu_chronokeep)?.setVisible(true)
            }
        }
    }
}