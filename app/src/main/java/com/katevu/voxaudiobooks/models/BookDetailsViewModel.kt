package com.katevu.voxaudiobooks.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katevu.voxaudiobooks.api.NetworkServiceDetails
import com.katevu.voxaudiobooks.utils.AudioState
import kotlinx.coroutines.launch

class BookDetailsViewModel: ViewModel() {
    private val TAG = "BookDetailsViewModel"

    private var _bookDetails = MutableLiveData<BookDetails>()
    var mActiveTrack: Track? = null
    var mPendingPending: Track? = null
    val bookDetails: LiveData<BookDetails>
        get() = _bookDetails

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
                } else {
                    _bookDetails.value = null
                    Log.d(TAG, "parse XML error")
                }
            } catch (e: Exception) {
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


    fun getPlayTrack(): Track? {
        return _bookDetails.value?.listTracks?.firstOrNull { it.playbackState == AudioState().PLAYING }
    }

    fun setStatus(trackUrl: String, isPlaying: Boolean) {
        _bookDetails.value?.listTracks?.find {it.trackUrl ==  trackUrl }?.playbackState = AudioState().IDLE
    }

    fun updateStatus(trackUrl: String) {
        var currentTrack = getPlayTrack()
        if (currentTrack != null) {
            if (currentTrack.trackUrl == trackUrl) {
                setStatus(trackUrl, false)
            } else {
                setStatus(currentTrack.trackUrl, false)
                setStatus(trackUrl, true)
            }
        } else {
            setStatus(trackUrl, true)
        }
    }
}