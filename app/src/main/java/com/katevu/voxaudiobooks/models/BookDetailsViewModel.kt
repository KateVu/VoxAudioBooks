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
//        Log.d(TAG, ".getBook called with url: $urlText")
        viewModelScope.launch {
            try {
//                _bookDetails.value = NetworkServiceDetails(urlText).voxBookService.getBookDetails(urlDetails)

                var result = NetworkServiceDetails(urlText).voxBookService.getBookDetails(urlDetails)
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

}