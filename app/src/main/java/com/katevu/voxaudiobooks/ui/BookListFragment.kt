package com.katevu.voxaudiobooks.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.katevu.voxaudiobooks.R
import com.katevu.voxaudiobooks.models.BookListViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [BookListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class BookListFragment : Fragment() {
    private val TAG = "BookListFragment"
    private lateinit var bookRecyclerView: RecyclerView

//    private val bookListViewModel: bookListViewModel by lazy { ViewModelProvider(this).get(BookListViewModel::class.java) }

    private val bookListViewModel: BookListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, ".onCreate called")
        bookListViewModel.listBooks.observe(this, Observer { listBooks  -> Log.d(TAG, "Response received: $listBooks")})
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_book_list, container, false)
    }

    companion object {
        fun newInstance() =
            BookListFragment().apply {
            }
    }
}

