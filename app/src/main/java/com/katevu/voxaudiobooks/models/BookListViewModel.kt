package com.katevu.voxaudiobooks.models

/**
 * Author: Kate Vu
 */

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katevu.voxaudiobooks.databases.BookRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class BookListViewModel internal constructor(
) : ViewModel() {

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
        //allBooks()
        //queryBooks("Alice")
    }

    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            try {
                _spinner.value = true
                block()
            } catch (error: Throwable) {
                _snackbar.value = error.message
            } finally {
                _spinner.value = false
            }
        }
    }

    /**
     * Get Librovox collection book in BookListViewModel
     */
    fun allBooks() {
        launchDataLoad {
            _listBooks.value = bookRepository.getAllBooks()
        }
    }

    /**
     * Get book from search query
     */
    fun queryBooks(query: String) {
        launchDataLoad {
            _listBooks.value = bookRepository.getQueryBooks(query)
        }
    }
}
