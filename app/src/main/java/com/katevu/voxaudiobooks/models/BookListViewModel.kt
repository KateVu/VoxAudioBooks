package com.katevu.voxaudiobooks.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.katevu.voxaudiobooks.api.NetworkService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class BookListViewModel: ViewModel() {
//class BookListViewModel : ViewModel() {

    private val TAG = "BookListViewModel"

    var listBooks: LiveData<List<Book>>


    init {
        listBooks = NetworkService().allBooks()
    }
}