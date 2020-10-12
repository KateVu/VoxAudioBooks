package com.katevu.voxaudiobooks.models

import com.google.gson.annotations.SerializedName

data class Book (
    var title: String = "",
    var id: String = "",
    @SerializedName("url_zip_file") var _urlText: String = ""
) {
    val urlText
    get() = removeLastIndex(_urlText)
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

    val urlDetails
    get() = getDetailsUrl(_urlText)
        fun getDetailsUrl(originUrl: String): String {
            return when(originUrl) {
                "" -> ""
                else -> {
                    var string: MutableList<String> = originUrl.split('/') as MutableList<String>
                    string.removeLast()
                    string.last().plus("_files.xml")
                }
            }
        }

}

