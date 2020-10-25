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
    }

    fun allBooks() {
        viewModelScope.launch {
            try {
                val fetchResult = NetworkService().voxBooksService.getAllBooks()
                Log.d(TAG, "Raw data received: $fetchResult")
                _listBooks.value = fetchResult.channel?.items?.filterNot {
                    it.link.isBlank()
                    it.guid.isBlank()
                }
                _spinner.value = false
            } catch (e: Exception) {
                Log.d(TAG, ".allBooks error: ${e.message}")
                _snackbar.value = "Cannot load the data!!!"
            }
        }
    }


}
