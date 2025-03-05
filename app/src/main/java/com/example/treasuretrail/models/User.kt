package com.example.treasuretrail.models


import androidx.core.net.toUri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    var id: String = "",

    @ColumnInfo(name = "username")
    val username: String? = "",

    @ColumnInfo(name = "email")
    val email: String? = "",

    @ColumnInfo(name = "password")
    val password: String? = "",

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String? = ""
) {


    // but only one might be populated at a time.
    var remoteImageUri: String? = ""

    @ColumnInfo(name = "image_uri")
    var localImageUri: String? = ""


    var imageUri: String?
        get() = localImageUri ?: remoteImageUri
        set(value) {
            val uri = value?.toUri()
            if (uri != null && (uri.scheme == "http" || uri.scheme == "https")) {
                remoteImageUri = value
            } else {
                localImageUri = value
            }
        }


    companion object {
        const val USERNAME_KEY = "username"
        const val EMAIL_KEY = "email"
        const val PASSWORD_KEY = "password"
        const val PHONE_KEY = "phoneNumber"
        const val IMAGE_URI_KEY = "imageUri"
    }


    fun fromJSON(json: Map<String, Any>): User {
        val username = json[USERNAME_KEY] as? String ?: ""
        val email = json[EMAIL_KEY] as? String ?: ""
        val password = json[PASSWORD_KEY] as? String ?: ""
        val phone = json[PHONE_KEY] as? String ?: ""
        val imageUri = json[IMAGE_URI_KEY] as? String

        return User(
            id = "",
            username = username,
            email = email,
            password = password,
            phoneNumber = phone
        ).apply {
            this.imageUri = imageUri
        }
    }


    val json: HashMap<String, String?>
        get() {
            return hashMapOf(
                USERNAME_KEY to username,
                EMAIL_KEY to email,
                PASSWORD_KEY to password,
                PHONE_KEY to phoneNumber,
                IMAGE_URI_KEY to imageUri
            )
        }
}
