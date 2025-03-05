package com.example.treasuretrail

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "setContentView(R.layout.activity_main) called!")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        if (navController != null) {
            bottomNavigationView.setupWithNavController(navController)

            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (destination.id == R.id.welcomeFragment || destination.id == R.id.registerFragment) {
                    bottomNavigationView.visibility = android.view.View.GONE
                } else {
                    bottomNavigationView.visibility = android.view.View.VISIBLE
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // If using the Navigation component, navigate up:
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
