package com.katevu.voxaudiobooks.models

/**
 * Author: Kate Vu
 */
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import org.jsoup.Jsoup
import org.simpleframework.xml.*

@Root(name = "rss", strict = false)
data class RSS @JvmOverloads constructor(
    @field: Element(name = "channel", required = false)
    var channel: channel? = null
)

@Root(name = "channel", strict = false)
data class channel @JvmOverloads constructor(
    @field: ElementList(inline = true, required = false)
    var items: List<Book>? = null
)


@Root(name = "item", strict = false)
data class Book @JvmOverloads constructor(

    @field: Element(name = "guid", required = false)
    var guid: String = "",

    @field:Path("title")
    @field:Text(required = false)
    var title: String = "",

    @field:Path("description")
    @field:Text(required = false)
    var _description: String = "",

    @field: Element(name = "link", required = false)
    var link: String = "",

    @field: Element(name = "pubDate", required = false)
    var pubDate: String = "",

    // json data
    var creator: String? = null,
    var runtime: String? = null,
    var totalTracks: Int = 0
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

@Entity(tableName = "books")
@Parcelize
data class BookParcel(
    var guid: String = "",
    var title: String = "",
    var description: String = "",
    var link: String = "",
    var pubDate: String = "",
    var creator: String? = "",
    @PrimaryKey var identifier: String = "",
    var runtime: String? = "",
    var totalTracks: Int = 0,
    var isFavourite: Boolean = false
) : Parcelable
