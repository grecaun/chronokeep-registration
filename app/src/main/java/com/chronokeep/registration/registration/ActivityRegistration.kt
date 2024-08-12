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
import com.chronokeep.registration.util.Globals

class ActivityRegistration: AppCompatActivity(), ChronoActivity {
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
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.main, menu)
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
        }
        return super.onOptionsItemSelected(item)
    }
}