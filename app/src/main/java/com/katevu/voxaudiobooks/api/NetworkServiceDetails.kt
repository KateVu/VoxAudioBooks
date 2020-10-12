package com.katevu.voxaudiobooks.api

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class NetworkServiceDetails (val urlText: String) {

    val voxBookService: VoxBookService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(urlText)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        voxBookService = retrofit.create(VoxBookService::class.java)
    }
}

interface VoxBookService {
    @GET("{urlDetails}")
    suspend fun getBookDetails(@Path("urlDetails") urlDetails: String): String
}

