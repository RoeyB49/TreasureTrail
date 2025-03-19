package com.example.treasuretrail.models

import java.io.Serializable

data class Post(
    val id: String,
    val userId: String,
    val userName: String,
    val imageUrl: String,
    val title: String,
    val details: String,
    val category: String,
    val location: String,
    val contactInformation: String,
    val timestamp: Long
) : Serializable