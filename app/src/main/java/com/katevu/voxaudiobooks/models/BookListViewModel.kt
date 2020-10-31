package com.katevu.voxaudiobooks.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katevu.voxaudiobooks.api.NetworkService
import com.katevu.voxaudiobooks.api.NetworkServiceSearch
import com.katevu.voxaudiobooks.api.URL_SEARCH_FORMAT
import com.katevu.voxaudiobooks.databases.BookRepository
import kotlinx.coroutines.launch

class BookListViewModel internal constructor(
): ViewModel() {

    private val TAG = "BookListViewModel"
    private val bookRepository: BookRepository = BookRepository.get()

    private var _listBooks = MutableLiveData<List<BookParcel>>()
    val listBooks: LiveData<List<BookParcel>>
        get() = _listBooks

    private val _spinner = MutableLiveData<Boolean>(false)
    /**
     * Show a loading spinner if true
     */
    val spinner: LiveData<Boolean>
        get() = _spinner

    private val _snackbar = MutableLiveData<String?>()

    /**
     * Request a snackbar to display a string.
     */
    val snackbar: LiveData<String?>
        get() = _snackbar

    /**
     * Called immediately after the UI shows the snackbar.
     */
    fun onSnackbarShown() {
        _snackbar.value = null
    }

    init {
        _spinner.value = true
        allBooks()
        //queryBooks("Tom")
    }

    /**
     * Get Librovox collection book
     */
    fun allBooks() {
        viewModelScope.launch {
            try {
                val fetchResult = NetworkService().voxBooksService.getAllBooks()
//                Log.d(TAG, "Raw data received: $fetchResult")
                val result = fetchResult.channel?.items?.filterNot {
                    it.link.isBlank()
                    it.guid.isBlank()
                }
                val resultParcel = result?.map {
                        v -> BookParcel(v.guid,v.title,v.description,v.link,v.pubDate,v.creator,v.identifier,v.runtime,v.totalTracks, isFavourite(v.identifier))
                }
//                Log.d(TAG, "Data from internet: $resultParcel}")
                resultParcel?.let {
                    _listBooks.value = resultParcel
                }
                _spinner.value = false
            } catch (e: Exception) {
//                Log.d(TAG, ".allBooks error: ${e.message}")
                _listBooks.value = null
                _snackbar.value = "Cannot load the data!!!"
            }
        }
    }

    /**
     * Check if the book book is in Favourite list
     */
    suspend fun isFavourite (identifier: String):Boolean {
        val book = bookRepository.getBookDB(identifier)
        return (book != null)
    }

    /**
     * Get book from search query
     */
    fun queryBooks(query: String) {
        _listBooks.value = emptyList()
        //        query = String.format(URL_SEARCH_FORMAT, query);
        val queryValue = String.format(URL_SEARCH_FORMAT, query)
        Log.d(TAG, ".query Books url: ${queryValue}")

        viewModelScope.launch {
            try {
                val fetchResult = NetworkServiceSearch().searchBooksService.getQueryBooks(queryValue)
                Log.d(TAG, "Raw data received: $fetchResult")
                val result = fetchResult.channel?.items?.filterNot {
                    it.link.isBlank()
                    it.guid.isBlank()
                }

                val resultParcel = result?.map {
                    v -> BookParcel(v.guid,v.title,v.description,v.link,v.pubDate,v.creator,v.identifier,v.runtime,v.totalTracks, isFavourite(v.identifier))
                }

                Log.d(TAG, "Data from internet: $resultParcel}")
                resultParcel?.let {
                    _listBooks.value = resultParcel
                }
                _spinner.value = false
            } catch (e: Exception) {
                _spinner.value = false
                Log.d(TAG, ".query Books error: ${e.message}")
                _snackbar.value = "Cannot load the search data!!!"
            }
        }
        Log.d(TAG, ".queryBooks result: ${listBooks.value}")
    }
}
