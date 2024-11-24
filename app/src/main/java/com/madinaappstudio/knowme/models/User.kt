package com.madinaappstudio.knowme.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User (
    var userUid: String? = null,
    var username: String? = null,
    var name: String? = null,
    var email: String? = null,
    var age: Int? = null,
    var about: String? = null,
    var location: String? = null,
    var profilePicture: String? = null
) : Parcelable
