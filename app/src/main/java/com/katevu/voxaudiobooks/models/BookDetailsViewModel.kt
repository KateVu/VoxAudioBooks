package com.katevu.voxaudiobooks.models

/**
 * Author: Kate Vu
 */
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katevu.voxaudiobooks.databases.BookRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class BookDetailsViewModel : ViewModel() {
    private val TAG = "BookDetailsViewModel"

    private val bookRepository: BookRepository = BookRepository.get()

    private var _audioBook = MutableLiveData<Audio>()
    val audioBook: LiveData<Audio>
        get() = _audioBook

    //active track
    var mActiveTrack: MediaFile? = null
    var mPendingTrack: MediaFile? = null


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

    init {
        _spinner.value = true
    }

    /**
     * Called immediately after the UI shows the snackbar.
     */
    fun onSnackbarShown() {
        _snackbar.value = null
    }

    fun updateFavourite(book: BookParcel) {
        bookRepository.getBookDB2(book.identifier)
        viewModelScope.launch {
//            Log.d(TAG, ".updateFavourite: delete")
            val bookResult = bookRepository.getBookDB(book.identifier)
            val isFavourite = (bookResult != null)
            if (isFavourite) {
//                Log.d(TAG, ".updateFavourite: delete")
                bookRepository.deleteBook(book)
            } else {
                Log.d(TAG, ".updateFavourite: add")
                bookRepository.addBook(book)
            }
        }
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
     * Get list of audio files of a book
     * @param: identifier: ID of the book
     * @return: List<MediaFile>, assigned to _audioBook.value
     */
    fun getAudio(identifier: String) {
        launchDataLoad {
            _audioBook.value = bookRepository.getAudioBook(identifier)
        }
    }

//    fun getAudio(identifier: String) {
////        Log.d(TAG, ".getBook called with baseurl: $urlText and query: $urlDetails")
//        viewModelScope.launch {
//            try {
//                var result = NetworkServiceAudio().audioService.getAudioBook(identifier)
////                Log.d(TAG, ".getBook call result: $result")
//                if (result != null) {
//                    _audioBook.value = result.apply {
//                        mediaFiles = mediaFiles.filter {
//                            it.format.equals("64Kbps MP3")
//                        }.toMutableList()
//                        mediaFiles.map{it -> it.playbackState = AudioState().IDLE}
//                    }
//                    _spinner.value = false
//                } else {
//                    _audioBook.value = null
//                    _snackbar.value = "Cannot load the audio book data!!!"
//                }
//            } catch (e: Exception) {
//                _snackbar.value = "Cannot load the data!!!"
//                Log.d(TAG, ".getBook error: ${e.message}")
//            }
//        }
//    }
}