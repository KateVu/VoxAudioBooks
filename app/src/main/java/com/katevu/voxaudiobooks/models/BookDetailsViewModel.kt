package com.katevu.voxaudiobooks.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katevu.voxaudiobooks.api.NetworkServiceDetails
import kotlinx.coroutines.launch

class BookDetailsViewModel: ViewModel() {
    private val TAG = "BookDetailsViewModel"

    private var _bookDetails = MutableLiveData<BookDetails>()
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

    fun getTrack(trackUrl: String): Track? {
        return  _bookDetails.value?.listTracks?.firstOrNull { it.trackUrl == trackUrl }
    }

    fun getPlayTrack(): Track? {
        return _bookDetails.value?.listTracks?.firstOrNull { it.isPlaying == true }
    }

    fun setStatus(trackUrl: String, isPlaying: Boolean) {
        _bookDetails.value?.listTracks?.find {it.trackUrl ==  trackUrl }?.isPlaying = isPlaying
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