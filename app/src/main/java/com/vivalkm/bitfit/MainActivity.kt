package com.vivalkm.bitfit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    lateinit var wlItems: MutableList<Item>;
    lateinit var wlStatusImages: MutableList<Int>
    lateinit var adapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val itemDao = (application as BitFitApplication).db.itemDao()
        val navbar = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val logFragment = LogFragment(itemDao)
        val dashboardFragment = LogFragment(itemDao)

        navbar.setOnItemSelectedListener { item ->
            lateinit var fragment: Fragment
            // Call helper method to swap the FrameLayout with the fragment
            when (item.itemId) {
                R.id.action_log -> replaceFragment(logFragment)
                R.id.action_dashboard -> replaceFragment(logFragment)
            }
            true
        }

        // Set default selection
        navbar.selectedItemId = R.id.action_log
    }

    private fun replaceFragment(logFragment: LogFragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_frame_layout, logFragment)
        fragmentTransaction.commit()
    }
}