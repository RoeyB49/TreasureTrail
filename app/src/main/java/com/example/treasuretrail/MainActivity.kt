package com.example.treasuretrail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController

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
}
