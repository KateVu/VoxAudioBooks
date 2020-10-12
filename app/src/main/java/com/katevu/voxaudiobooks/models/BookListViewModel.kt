package com.katevu.voxaudiobooks.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katevu.voxaudiobooks.api.NetworkService
import kotlinx.coroutines.launch

class BookListViewModel : ViewModel() {

    private val TAG = "BookListViewModel"

    private val _listBooks = MutableLiveData<List<Book>>()

    val listBooks: LiveData<List<Book>>
        get() = _listBooks

    init {
        allBooks()
    }

    fun allBooks() {
        viewModelScope.launch {
            try {
                val fetchResult = NetworkService().voxBooksService.getAllBooks()
                _listBooks.value = fetchResult.listBooks.filterNot {
                    it._urlText.isBlank()
                }
            } catch (e: Exception) {
                Log.d(TAG, ".allBooks error: ${e.message}")
            }
        }
    }
}