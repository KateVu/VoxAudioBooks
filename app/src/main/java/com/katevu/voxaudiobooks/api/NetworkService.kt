package com.katevu.voxaudiobooks.api

import com.katevu.voxaudiobooks.models.RSS
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET

private const val URL1 = "https://archive.org/services/collection-rss.php/"
private const val URL_ARCHIVE_LIBRIVOX = "https://archive.org/services/"


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
 * A public Api object that exposes the lazy-initialized Retrofit service
 */

interface VoxBooksService {
    @GET("collection-rss.php?collection=librivoxaudio")
    suspend fun getAllBooks(): RSS
}

