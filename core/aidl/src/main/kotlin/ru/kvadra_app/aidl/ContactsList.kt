package ru.kvadra_app.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.kvadra_app.model.Contact

@Parcelize
data class ContactsList(
    val contacts: List<Contact>
): Parcelable