package com.cardscanner.app

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactInfo(
    val firstName: String = "",
    val lastName: String = "",
    val organization: String = "",
    val title: String = "",
    val phones: List<String> = emptyList(),
    val emails: List<String> = emptyList(),
    val website: String = "",
    val address: String = ""
) : Parcelable
