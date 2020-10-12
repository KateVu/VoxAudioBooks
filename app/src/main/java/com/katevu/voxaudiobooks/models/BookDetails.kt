package com.katevu.voxaudiobooks.models

data class BookDetails (var bookID: String = "",
                        var bookThumbnail: String = "",
                        var bookCover: String = "",
                        var listTracks: MutableList<String> = ArrayList<String>())