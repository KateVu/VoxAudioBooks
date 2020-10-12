package com.katevu.voxaudiobooks.api

import com.katevu.voxaudiobooks.models.BooksResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

private const val TAG = "NetworkService"
private const val URL = "https://raw.githubusercontent.com/"
private const val URL1 = "https://librivox.org/api/feed/audiobooks/"

class NetworkService {

    val voxBooksService: VoxBooksService

    init {

        val retrofit = Retrofit.Builder()
            .baseUrl(URL1)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        voxBooksService = retrofit.create(VoxBooksService::class.java)
    }
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */

interface VoxBooksService {
    @GET("?format=json")
    suspend fun getAllBooks(): BooksResponse
}

