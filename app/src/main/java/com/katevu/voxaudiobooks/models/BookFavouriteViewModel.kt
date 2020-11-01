package com.katevu.voxaudiobooks.models

/**
 * Author: Kate Vu
 */
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.katevu.voxaudiobooks.databases.BookRepository

/**
 * View Model of BookFavouriteFragment
 */
class BookFavouriteViewModel internal constructor(

) : ViewModel() {

    //    private val TAG = "BookFragmentViewModel"
    private val bookRepository: BookRepository = BookRepository.get()

    private var _listBooks = bookRepository.getBooksDB()
    val listBooks: LiveData<List<BookParcel>>
        get() = _listBooks

    private val _spinner = MutableLiveData<Boolean>(false)

    /**
     * Show a loading spinner if true
     */
    val spinner
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
        _spinner.value = false
        //allBooks()
    }

    fun loadData() {
        _listBooks = bookRepository.getBooksDB()
    }
}
