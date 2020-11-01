package com.katevu.voxaudiobooks.api

/**
 * author: KateVu
 */
import com.katevu.voxaudiobooks.models.RSS
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

private const val URL_ARCHIVE_LIBRIVOX = "https://archive.org/services/"
private const val URL_SEARCH_BASE = "https://archive.org/"
internal const val URL_SEARCH_FORMAT = "https://archive.org/advancedsearch.php?q=(%s)+AND+collection:(librivoxaudio)&output=rss"
//        query = String.format(URL_SEARCH_FORMAT, query);

/**
 * Network service to query Librovox collection book from archive server
 */
class NetworkService {
    val voxBooksService: VoxBooksService

    init {

        val retrofit = Retrofit.Builder()
            .baseUrl(URL_ARCHIVE_LIBRIVOX)
            //.addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
        voxBooksService = retrofit.create(VoxBooksService::class.java)
    }
}

/**
 * interface for book collection
 */
interface VoxBooksService {
    @GET("collection-rss.php?collection=librivoxaudio")
    suspend fun getAllBooks(): RSS
}

/**
 * Network service for search book function
 */
class NetworkServiceSearch {
    val searchBooksService: SearchBooksService
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(URL_SEARCH_BASE)
            //.addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
        searchBooksService = retrofit.create(SearchBooksService::class.java)
    }
}

interface SearchBooksService {
    @GET()
    suspend fun getQueryBooks(@Url fullUrl: String): RSS
}
