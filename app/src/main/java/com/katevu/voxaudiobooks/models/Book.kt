package com.katevu.voxaudiobooks.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import org.jsoup.Jsoup

data class Book(
    var id: String = "",
    var title: String = "",
    var url_zip_file: String = "",
    @SerializedName("description") var _description: String = "",
    var totaltime: String = "",
    var num_sections: String = ""
) {
    val link
        get() = removeLastIndex(url_zip_file)

    val urlDetails
        get() = getDetailsUrl()

    val identifier
        get() = getIdentifier(url_zip_file)

    val description
        get() = Jsoup.parse(_description).text()

    fun getDetailsUrl(): String {
        return when (identifier) {
            "" -> ""
            else -> {
                identifier.plus("_files.xml")
            }
        }
    }
    fun removeLastIndex(originUrl: String): String {
        return when (originUrl) {
            "" -> ""
            else -> {
                var string: MutableList<String> = originUrl.split('/') as MutableList<String>
                string.removeLast()
                var url = string.joinToString('/'.toString()).plus("/")
                url.replaceFirst("http", "https")
            }
        }
    }
    fun getIdentifier (originUrl: String): String {
        return when (originUrl) {
            "" -> ""
            else -> {
                var string: MutableList<String> = originUrl.split('/') as MutableList<String>
                string.removeLast()
                string.last()
            }
        }
    }

}

@Parcelize
data class BookParcel (var id: String = "",
                       var title: String = "",
                       var description: String = "",
                       var link: String = "",
                       var urlDetails: String = "",
                       var identifier: String = "",
                       var numSection: Int = 0): Parcelable