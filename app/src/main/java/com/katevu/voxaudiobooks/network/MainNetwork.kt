package com.katevu.voxaudiobooks.network//package com.katevu.voxaudiobooks.network
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import com.katevu.voxaudiobooks.api.VoxBooksAPI
//import com.katevu.voxaudiobooks.api.VoxBooksService
//import com.katevu.voxaudiobooks.models.Book
//import com.katevu.voxaudiobooks.models.BooksResponse
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//private const val TAG = "MainNetwork"
//private const val BASE_API = "https://librivox.org/api/feed/audiobooks/"
//
//class MainNetwork {
//
//    private val voxApi: VoxBooksService
//
//    init {
//
//        val retrofit: Retrofit = Retrofit.Builder()
//            .baseUrl(BASE_API)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        voxApi = retrofit.create(VoxBooksService::class.java)
//
//    }
//
//
//    fun fetchBooks(): LiveData<List<Book>> {
//        val responseLiveData: MutableLiveData<List<Book>> = MutableLiveData()
//        val voxRequest: Call<BooksResponse> = voxApi.getAllBooks()
//        voxRequest.enqueue(object : Callback<BooksResponse> {
//            override fun onFailure(call: Call<BooksResponse>, t: Throwable) {
//                Log.e(TAG, "Failed to fetch data", t)
//            }
//
//            override fun onResponse(call: Call<BooksResponse>, response: Response<BooksResponse>) {
//                Log.d(TAG, "Response received")
////                val voxResponse: VoxResponse? = response.body()
//                val booksResponse: BooksResponse? = response.body()
//
//                var listBooks: List<Book> = booksResponse?.libraryItems ?: mutableListOf()
////                listBooks = listBooks.filterNot {
////                    it.urlText.isBlank()
////                }
//                responseLiveData.value = listBooks
//            }
//        })
//        return responseLiveData
//    }
//}