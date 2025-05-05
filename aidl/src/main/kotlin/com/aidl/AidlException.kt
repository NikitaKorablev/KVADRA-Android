package com.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class AidlException(
    private val errorMessage: String?,
    private val errorCode: Int = RUNTIME_EXCEPTION
) : Parcelable {

    companion object {
        const val RUNTIME_EXCEPTION = 1000
        const val ERROR_REMOVING_DUPLICATION_EXCEPTION = 1001
        const val PERMISSION_DENIED_EXCEPTION = 1002
    }

    fun toException(): Exception {
        return when (errorCode) {
            RUNTIME_EXCEPTION -> RuntimeException(errorMessage)
            ERROR_REMOVING_DUPLICATION_EXCEPTION -> Exception(errorMessage)
            PERMISSION_DENIED_EXCEPTION -> Exception(errorMessage)
            else -> RuntimeException(errorMessage)
        }
    }
}