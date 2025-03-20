package com.example.treasuretrail.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    var id: String = "",
    @ColumnInfo(name = "displayName")
    val displayName: String = "",
    @ColumnInfo(name = "email")
    val email: String = "",
    @ColumnInfo(name = "password")
    val password: String = "",
    @ColumnInfo(name = "phone")
    val phone: String = "",
    @ColumnInfo(name = "photoUrl")
    var photoUrl: String? = null
) {
    companion object {
        const val ID_KEY = "id"
        const val USERNAME_KEY = "displayName"
        const val EMAIL_KEY = "email"
        const val PASSWORD_KEY = "password"
        const val PHONE_KEY = "phone"
        const val PHOTO_URL_KEY = "photoUrl"
    }

    fun fromJSON(json: Map<String, Any>): User {
        return User(
            id = json[ID_KEY] as? String ?: "",
            displayName = json[USERNAME_KEY] as? String ?: "",
            email = json[EMAIL_KEY] as? String ?: "",
            password = json[PASSWORD_KEY] as? String ?: "",
            phone = json[PHONE_KEY] as? String ?: "",
            photoUrl = json[PHOTO_URL_KEY] as? String ?: ""
        )
    }

    val json: Map<String, Any?>
        get() = mapOf(
            ID_KEY to id,
            USERNAME_KEY to displayName,
            EMAIL_KEY to email,
            PASSWORD_KEY to password,
            PHONE_KEY to phone,
            PHOTO_URL_KEY to photoUrl
        )
}


