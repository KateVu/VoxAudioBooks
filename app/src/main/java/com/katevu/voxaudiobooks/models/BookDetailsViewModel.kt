package com.katevu.voxaudiobooks.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katevu.voxaudiobooks.api.NetworkServiceAudio
import com.katevu.voxaudiobooks.api.NetworkServiceDetails
import com.katevu.voxaudiobooks.utils.AudioState
import kotlinx.coroutines.launch

class BookDetailsViewModel: ViewModel() {
    private val TAG = "BookDetailsViewModel"

    private var _bookDetails = MutableLiveData<BookDetails>()
    val bookDetails: LiveData<BookDetails>
        get() = _bookDetails

    private var _audioBook = MutableLiveData<Audio>()
    val audioBook: LiveData<Audio>
        get() = _audioBook


    var mActiveTrack: MediaFile? = null
    var mPendingPending: Track? = null

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

    fun getAudio(identifier: String) {
//        Log.d(TAG, ".getBook called with baseurl: $urlText and query: $urlDetails")
        viewModelScope.launch {
            try {
                var result = NetworkServiceAudio().audioService.getAudioBook(identifier)
//                Log.d(TAG, ".getBook call result: $result")
                if (result != null) {
                    _audioBook.value = result.apply {
                        mediaFiles = mediaFiles.filter {
                            it.format.equals("64Kbps MP3")
                        }.toMutableList()
                        mediaFiles.map{it -> it.playbackState = AudioState().IDLE}
                    }
                    _spinner.value = false
                } else {
                    _audioBook.value = null
                    _snackbar.value = "Cannot load the audio book data!!!"
                }
            } catch (e: Exception) {
                _snackbar.value = "Cannot load the data!!!"
                Log.d(TAG, ".getBook error: ${e.message}")
            }
        }
    }

    fun setAudioStatus(name: String, newTrack: MediaFile?): Boolean {
        val index = getTrackIndex(name)
        index?.let {
            newTrack?.let {
                    it1 -> _audioBook.value?.mediaFiles?.set(it, it1)
                return true
            }
        }
        return false
    }

    fun getMediaFileIndex(name: String): Int? {
        return  _audioBook.value?.mediaFiles?.indexOfFirst { it.name == name }
    }

    fun getBook(urlText: String,urlDetails: String) {
//        Log.d(TAG, ".getBook called with baseurl: $urlText and query: $urlDetails")
        viewModelScope.launch {
            try {

                var result = NetworkServiceDetails(urlText).voxBookService.getBookDetails(urlDetails)
//                Log.d(TAG, ".getBook call result: $result")
                var parseXml = ParseXML()
                if (parseXml.parse(result)) {
                    var resultXML = parseXml.book
//                    Log.d(TAG, "getBook called: $resultXML")
                    _bookDetails.value = resultXML
                    _spinner.value = false
                } else {
                    _bookDetails.value = null
                    _snackbar.value = "Cannot load the data!!!"
                }
            } catch (e: Exception) {
                _snackbar.value = "Cannot load the data!!!"
                Log.d(TAG, ".getBook error: ${e.message}")
            }
        }
    }


    fun getTrackIndex(trackUrl: String): Int? {
        return  _bookDetails.value?.listTracks?.indexOfFirst { it.trackUrl == trackUrl }
    }

    fun setTrack(trackUrl: String, newTrack: Track?) {
        val index = getTrackIndex(trackUrl)
        index?.let { newTrack?.let { it1 -> bookDetails.value?.listTracks?.set(it, it1) } }
    }
}