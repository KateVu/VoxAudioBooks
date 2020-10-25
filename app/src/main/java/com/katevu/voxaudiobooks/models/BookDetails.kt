package com.katevu.voxaudiobooks.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class BookDetails(
    var bookID: String = "",
    var listTracks: MutableList<Track> = ArrayList<Track>()
)

@Parcelize
data class Track(
    var bookCover: String = "",
    var baseUrl: String = "",
    var trackNo: String = "",
    var trackUrl: String = "",
    var trackTitle: String = "",
    var trackAlbum: String = "",
    var trackArtist: String = "",
    var trackLength: String = "",
    var trackSize: String = "",
    var playbackState: Int = 0
): Parcelable

data class Audio (
    @SerializedName("files") var mediaFiles: MutableList<MediaFile>,
    @SerializedName("metadata") var metadata: Metadata? = null,
    )

data class Metadata (
    var identifier : String = "",
    var creator: String? = null,
    )

@Parcelize
data class MediaFile(
    // url -> https://archive.org/download/[identifier]/[name]

    var identifier: String = "",
    var track: String = "",
    var name: String = "",
    var title: String = "",
    var album: String = "",
    @SerializedName("creator") var artist: String = "",
    var length: String = "",
    var size: String = "",
    var mtime: String = "",
    var source: String = "",
    var format: String = "",
    var playbackState: Int = 0
): Parcelable
