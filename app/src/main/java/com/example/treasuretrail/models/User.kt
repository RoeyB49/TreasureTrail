package com.example.treasuretrail.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    var id: String = "",

    @ColumnInfo(name = "username")
    val username: String = "",

    @ColumnInfo(name = "email")
    val email: String = "",

    @ColumnInfo(name = "password")
    val password: String = "",

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String = "",

    @ColumnInfo(name = "image_uri")
    var imageUri: String? = null
) {
    companion object {
        const val ID_KEY = "id"
        const val USERNAME_KEY = "username"
        const val EMAIL_KEY = "email"
        const val PASSWORD_KEY = "password"
        const val PHONE_KEY = "phoneNumber"
        const val IMAGE_URI_KEY = "imageUri"
    }

    fun fromJSON(json: Map<String, Any>): User {
        return User(
            id = json[ID_KEY] as? String ?: "",
            username = json[USERNAME_KEY] as? String ?: "",
            email = json[EMAIL_KEY] as? String ?: "",
            password = json[PASSWORD_KEY] as? String ?: "",
            phoneNumber = json[PHONE_KEY] as? String ?: "",
            imageUri = json[IMAGE_URI_KEY] as? String
        )
    }

    val json: Map<String, Any?>
        get() = mapOf(
            ID_KEY to id,
            USERNAME_KEY to username,
            EMAIL_KEY to email,
            PASSWORD_KEY to password,
            PHONE_KEY to phoneNumber,
            IMAGE_URI_KEY to imageUri
        )
}
