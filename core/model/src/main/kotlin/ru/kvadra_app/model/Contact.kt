package ru.kvadra_app.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact (
    val name: String,
    val phoneNumber: String,
    val idIndex: Long,
    val rawContactId: Long
) : Parcelable
