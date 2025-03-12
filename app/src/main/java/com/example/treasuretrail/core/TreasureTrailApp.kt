package com.example.treasuretrail

import android.app.Application
import com.google.firebase.FirebaseApp

class TreasureTrailApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase for the entire app
        FirebaseApp.initializeApp(this)
    }
}
