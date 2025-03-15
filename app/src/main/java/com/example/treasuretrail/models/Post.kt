package com.example.treasuretrail.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: String,
    val userName: String,
    val userImageUri: String,
    val lostItemImageUri: String,
    val location: String,
    val category: String,
    val description: String,
    val contactInformation: String,
    val timestamp: Long
)
