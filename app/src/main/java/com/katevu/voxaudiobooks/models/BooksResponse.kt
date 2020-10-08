package com.katevu.voxaudiobooks.models

import com.google.gson.annotations.SerializedName

class BooksResponse {
    @SerializedName("books")
    lateinit var listBooks: List<Book>
}
