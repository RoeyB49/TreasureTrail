package com.example.treasuretrail.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val imageUrl : String,
    val location: String,
    val category: String,
    val details : String,
    val title: String,
    val contactInformation: String,
    val timestamp: Long
) : Serializable
