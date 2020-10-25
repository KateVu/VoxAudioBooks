package com.katevu.voxaudiobooks.api

import com.katevu.voxaudiobooks.models.Audio
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


const val URL_JSON_METADATA_PREFIX = "https://archive.org/metadata/"

class NetworkServiceDetails(val urlText: String) {

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
