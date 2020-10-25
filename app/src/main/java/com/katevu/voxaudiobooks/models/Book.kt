package com.katevu.voxaudiobooks.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.jsoup.Jsoup
import org.simpleframework.xml.*
import java.util.*

@Root(name = "rss", strict = false)
data class RSS @JvmOverloads constructor(
    @field: Element(name = "channel")
    var channel: channel? = null
)

@Root(name = "channel", strict = false)
data class channel @JvmOverloads constructor(
    @field: ElementList(inline = true)
    var items: List<Book>? = null
)

@Root(name = "item", strict = false)
data class Book @JvmOverloads constructor(

    @field: Element(name = "guid")
    var guid: String = "",

    @field:Path("title")
    @field:Text(required = false)
    var title: String = "",

    @field:Path("description")
    @field:Text(required = false)
    var _description: String = "",

    @field: Element(name = "link")
    var link: String = "",

    @field: Element(name = "pubDate")
    var pubDate: String = "",

    // json data
    var creator: String? = null,
    var runtime: String? = null,
    var totalTracks: Int = 0,
    var tracks: MutableList<Track> = ArrayList<Track>()

) {
    val identifier
        get() = getIdentifier(guid)

    val description: String
        get() = Jsoup.parse(_description).text()

    fun getIdentifier(originUrl: String): String {
        return when (originUrl) {
            "" -> ""
            else -> {
                var string: MutableList<String> = originUrl.split('/') as MutableList<String>
                string.last()
            }
        }
    }
}

@Parcelize
data class BookParcel(
    var guid: String = "",
    var title: String = "",
    var description: String = "",
    var link: String = "",
    var pubDate: String = "",
    var creator: String? = "",
    var identifier: String = "",
    var runtime: String? = "",
    var totalTracks: Int = 0,
    var tracks: List<Track> = emptyList()
) : Parcelable