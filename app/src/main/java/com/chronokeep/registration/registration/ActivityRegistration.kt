package com.chronokeep.registration.registration

import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.chronokeep.registration.R
import com.chronokeep.registration.about.DialogFragmentAbout
import com.chronokeep.registration.interfaces.ChronoActivity
import com.chronokeep.registration.interfaces.MenuWatcher
import com.chronokeep.registration.network.chronokeep.ChronokeepInterface
import com.chronokeep.registration.objects.database.DatabaseParticipant
import com.chronokeep.registration.objects.database.DatabaseSetting
import com.chronokeep.registration.objects.registration.AddUpdateParticipantsRequest
import com.chronokeep.registration.objects.registration.GetParticipantsRequest
import com.chronokeep.registration.serverlist.DialogFragmentServerList
import com.chronokeep.registration.util.Constants
import com.chronokeep.registration.util.Globals

class ActivityRegistration: AppCompatActivity(), ChronoActivity, MenuWatcher {
    private val tag: String = "Chrono.RAct"

    private var menu: Menu? = null

    private var pFrag: FragmentRegistrationParticipants? = null
    private val chronokeep = ChronokeepInterface.getInstance()

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
            val frag = FragmentRegistrationParticipants()
            pFrag = frag
            supportFragmentManager.beginTransaction().replace(R.id.registration_fragment_container, frag).commit()
            setActivityTitle("Chronokeep Registration")
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Globals.setMenuWatcher(this)
        startLockTask()
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume")
        Globals.setMenuWatcher(this)
        startLockTask()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.main, menu)
        updateMenu()
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val pinMenu = menu?.findItem(R.id.unpin)
        val actMan = getSystemService(Context.ACTIVITY_SERVICE)
        when (actMan) {
            ActivityManager.LOCK_TASK_MODE_PINNED,
            ActivityManager.LOCK_TASK_MODE_LOCKED-> {
                pinMenu?.setTitle(R.string.menu_unpin)
            }
            ActivityManager.LOCK_TASK_MODE_NONE -> {
                pinMenu?.setTitle(R.string.menu_pin)
            }
        }
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
                try {
                    val toUpload = ArrayList<DatabaseParticipant>()
                    val database = Globals.getDatabase()
                    val fullList = database?.participantDao()?.getParticipants() ?: return true
                    for (part in fullList) {
                        if (part.bib.isNotEmpty()) {
                            toUpload.add(part)
                        }
                    }
                    if (toUpload.isNotEmpty()) {
                        Globals.getConnection()?.sendAsyncMessage(
                            AddUpdateParticipantsRequest(
                                participants = toUpload
                            ).encode()
                        )
                    }
                } catch (e: Exception) {
                    Log.d(tag, "Exception when sending message: ${e.message}")
                    Globals.getConnection()?.stop()
                }
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
                val settingDao = Globals.getDatabase()?.settingDao()
                val access = settingDao?.getSetting(Constants.setting_auth_token)
                val refresh = settingDao?.getSetting(Constants.setting_refresh_token)
                val slug = settingDao?.getSetting(Constants.setting_event_slug)
                val year = settingDao?.getSetting(Constants.setting_event_year)
                if (access != null && access.value.isNotEmpty()
                    && refresh != null && refresh.value.isNotEmpty()
                    && slug != null && slug.value.isNotEmpty()
                    && year != null && year.value.isNotEmpty()) {
                    val participants = Globals.getDatabase()?.participantDao()?.getParticipants()
                    val updatedParticipants = ArrayList<DatabaseParticipant>()
                    val newParticipants = ArrayList<DatabaseParticipant>()
                    if (participants != null) {
                        for (p in participants) {
                            if (p.bib.isNotEmpty()) {
                                if (p.id.isNotEmpty()) {
                                    updatedParticipants.add(p)
                                } else {
                                    newParticipants.add(p)
                                }
                            }
                        }
                    }
                    if (updatedParticipants.isNotEmpty()) {
                        chronokeep.updateParticipant(
                            access.value,
                            refresh.value,
                            slug.value,
                            year.value,
                            updatedParticipants,
                            { response ->
                                if (response != null) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Update participants successful.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    settingDao.addSetting(
                                        DatabaseSetting(
                                            name = Constants.setting_auth_token,
                                            value = ""
                                        )
                                    )
                                    settingDao.addSetting(
                                        DatabaseSetting(
                                            name = Constants.setting_refresh_token,
                                            value = ""
                                        )
                                    )
                                    settingDao.addSetting(
                                        DatabaseSetting(
                                            name = Constants.setting_event_slug,
                                            value = ""
                                        )
                                    )
                                    settingDao.addSetting(
                                        DatabaseSetting(
                                            name = Constants.setting_event_year,
                                            value = ""
                                        )
                                    )
                                    Toast.makeText(
                                        applicationContext,
                                        "Unknown response from server when updating participants.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    updateMenu()
                                }
                            },
                            { message ->
                                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        )
                    }
                    if (newParticipants.isNotEmpty()) {
                        chronokeep.addParticipant(
                            access.value,
                            refresh.value,
                            slug.value,
                            year.value,
                            newParticipants,
                            { response ->
                                if (response != null) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Add participants successful.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    settingDao.addSetting(
                                        DatabaseSetting(
                                            name = Constants.setting_auth_token,
                                            value = ""
                                        )
                                    )
                                    settingDao.addSetting(
                                        DatabaseSetting(
                                            name = Constants.setting_refresh_token,
                                            value = ""
                                        )
                                    )
                                    settingDao.addSetting(
                                        DatabaseSetting(
                                            name = Constants.setting_event_slug,
                                            value = ""
                                        )
                                    )
                                    settingDao.addSetting(
                                        DatabaseSetting(
                                            name = Constants.setting_event_year,
                                            value = ""
                                        )
                                    )
                                    Toast.makeText(
                                        applicationContext,
                                        "Unknown response from server when adding participants.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    updateMenu()
                                }
                            },
                            { message ->
                                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        )
                    }
                }
                return true
            }
            R.id.download_web -> {
                Log.d(tag, "User wants to download from Chronokeep.")
                val settingDao = Globals.getDatabase()?.settingDao()
                val access = settingDao?.getSetting(Constants.setting_auth_token)
                val refresh = settingDao?.getSetting(Constants.setting_refresh_token)
                val slug = settingDao?.getSetting(Constants.setting_event_slug)
                val year = settingDao?.getSetting(Constants.setting_event_year)
                if (access != null && access.value.isNotEmpty()
                    && refresh != null && refresh.value.isNotEmpty()
                    && slug != null && slug.value.isNotEmpty()
                    && year != null && year.value.isNotEmpty()) {
                    chronokeep.getParticipants(
                        access.value,
                        refresh.value,
                        slug.value,
                        year.value,
                        { response ->
                            if (response != null) {
                                val newParts = ArrayList<DatabaseParticipant>()
                                for (p in response.participants) {
                                    newParts.add(p.toDatabaseParticipant())
                                    Globals.getDatabase()?.participantDao()?.addParticipants(newParts)
                                    Globals.setRegistrationDistances()
                                    pFrag?.updateParticipants()
                                }
                            } else {
                                settingDao.addSetting(DatabaseSetting(name=Constants.setting_auth_token, value=""))
                                settingDao.addSetting(DatabaseSetting(name=Constants.setting_refresh_token, value=""))
                                settingDao.addSetting(DatabaseSetting(name=Constants.setting_event_slug, value=""))
                                settingDao.addSetting(DatabaseSetting(name=Constants.setting_event_year, value=""))
                                Toast.makeText(applicationContext, "Unknown response from server.", Toast.LENGTH_SHORT).show()
                                updateMenu()
                            }
                        },
                        { message ->
                            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                return true
            }
            R.id.menu_connect -> {
                Log.d(tag, "User wants to connect.")
                val connectFragment = DialogFragmentServerList(pFrag!!, this)
                val ft = supportFragmentManager.beginTransaction()
                val prev = supportFragmentManager.findFragmentByTag("fragment_connect")
                if (prev != null) {
                    ft.remove(prev)
                }
                connectFragment.show(ft, "fragment_connect")
                return true
            }
            R.id.unpin -> {
                if (item.title.toString().equals(getString(R.string.menu_unpin), true)) {
                    Log.d(tag, "User requests unpin.")
                    stopLockTask()
                    item.setTitle(R.string.menu_pin)
                } else {
                    Log.d(tag, "User requests screen pin.")
                    startLockTask()
                    item.setTitle(R.string.menu_unpin)
                }
                return true
            }
            R.id.menu_clear -> {
                Log.d(tag, "User wants to clear participants.")
                AlertDialog.Builder(this)
                    .setIcon(R.drawable.baseline_delete_24)
                    .setTitle("Warning!")
                    .setMessage(getString(R.string.permanent))
                    .setPositiveButton("Yes") { d: DialogInterface, _: Int ->
                        run {
                            Globals.getDatabase()?.participantDao()?.deleteAllParticipants()
                            Globals.setRegistrationDistances()
                            pFrag?.updateParticipants()
                            d.dismiss()
                        }
                    }
                    .setNegativeButton("No") { d: DialogInterface, _: Int ->
                        run {
                            d.dismiss()
                        }
                    }
                    .show()
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