package com.katevu.voxaudiobooks.api

import com.katevu.voxaudiobooks.models.BooksResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class NetworkServiceDetails (val urlDetails: String) {

    val voxBooksService: VoxBooksService

    init {

        val retrofit = Retrofit.Builder()
            .baseUrl(urlDetails)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        voxBooksService = retrofit.create(VoxBooksService::class.java)
    }
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */

interface VoxBookService {
    @GET("/")
    suspend fun getBook(): BooksResponse
}

