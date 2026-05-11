package com.kvadra_app.core.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact (
    val name: String,
    val phoneNumber: String,
    val idIndex: Long,
    val rawContactId: Long
) : Parcelable

data class ContactItem(
    val contact: Contact?,
    val header: Char?
)