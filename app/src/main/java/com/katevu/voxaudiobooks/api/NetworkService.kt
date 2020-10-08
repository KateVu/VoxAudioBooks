package com.katevu.voxaudiobooks.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.katevu.voxaudiobooks.models.Book
import com.katevu.voxaudiobooks.models.BooksResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

private const val TAG = "NetworkService"
private const val URL = "https://raw.githubusercontent.com/"
private const val URL1 = "https://librivox.org/api/feed/audiobooks/"

class NetworkService {

    private val voxBooksService: VoxBooksService

    init {

        val retrofit = Retrofit.Builder()
            .baseUrl(URL1)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        voxBooksService = retrofit.create(VoxBooksService::class.java)
    }


    fun allBooks(): LiveData<List<Book>> {
        val responseLiveData: MutableLiveData<List<Book>> = MutableLiveData()
        val flickrRequest: Call<BooksResponse> = voxBooksService.getAllBooks()

        flickrRequest.enqueue(object : Callback<BooksResponse> {

            override fun onFailure(call: Call<BooksResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }

            override fun onResponse(call: Call<BooksResponse>, response: Response<BooksResponse>) {
                Log.d(TAG, "Response received")
                val flickrResponse: BooksResponse? = response.body()
                var galleryItems: List<Book> = flickrResponse?.listBooks
                    ?: mutableListOf()
                responseLiveData.value = galleryItems
            }
        })

        return responseLiveData
    }

}

interface VoxBooksService {
    @GET("?format=json")
    fun getAllBooks(): Call<BooksResponse>
}