package com.katevu.voxaudiobooks.api

import com.katevu.voxaudiobooks.models.Audio
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


const val URL_JSON_METADATA_PREFIX = "https://archive.org/metadata/"

/**
 * Network service to get data for a book with list of MediaFile
 */
class NetworkServiceAudio() {

    val audioService: AudioService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(URL_JSON_METADATA_PREFIX)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        audioService = retrofit.create(AudioService::class.java)
    }
}

interface AudioService {
    @GET("{identifier}")
    suspend fun getAudioBook(@Path("identifier") identifier: String): Audio
}
