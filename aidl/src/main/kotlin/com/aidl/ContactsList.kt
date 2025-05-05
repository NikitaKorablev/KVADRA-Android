package com.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.kvadra_app.core.data.Contact

@Parcelize
data class ContactsList(
    val contacts: List<Contact>
): Parcelable