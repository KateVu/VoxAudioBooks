package com.katevu.voxaudiobooks.models

import com.google.gson.annotations.SerializedName

data class Book (
    var title: String = "",
    var id: String = "",
    @SerializedName("url_zip_file") var urlText: String = ""


)