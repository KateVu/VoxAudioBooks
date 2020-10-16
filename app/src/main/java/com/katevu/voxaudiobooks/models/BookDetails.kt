package com.katevu.voxaudiobooks.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class BookDetails(
    var bookID: String = "",
    var listTracks: MutableList<Track> = ArrayList<Track>()
)

@Parcelize
data class Track(
    var trackNo: String = "",
    var trackUrl: String = "",
    var trackTitle: String = "",
    var trackAlbum: String = "",
    var trackLength: String = "",
    var trackSize: String = "",
    var isPlaying: Boolean = false
): Parcelable

