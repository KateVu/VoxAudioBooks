package com.katevu.voxaudiobooks.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.katevu.voxaudiobooks.R
import com.katevu.voxaudiobooks.models.Book
import com.katevu.voxaudiobooks.models.BookListViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [BookListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class BookListFragment : Fragment() {
    private val TAG = "BookListFragment"
    private lateinit var bookRecyclerView: RecyclerView
    private var adapter: BookListAdapter? = null


//    private val bookListViewModel: bookListViewModel by lazy { ViewModelProvider(this).get(BookListViewModel::class.java) }

    private val bookListViewModel: BookListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, ".onCreate called")
        bookListViewModel.listBooks.observe(this, Observer { listBooks  ->
            Log.d(TAG, "Response received: ${listBooks[0]}; basedURLText: ${listBooks[0].urlText}; detailsURL: ${listBooks[0].urlDetails}")
            updateUI(listBooks)
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, ".onCreateView called")
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_book_list, container, false)

        bookRecyclerView =
            view.findViewById(R.id.book_recycler_view) as RecyclerView
        bookRecyclerView.layoutManager = LinearLayoutManager(context)
        return view
    }




    private fun updateUI(books: List<Book>) {
        Log.d(TAG, ".updateUI called")
        adapter = BookListAdapter(books)
        bookRecyclerView.adapter = adapter
    }


    companion object {
        fun newInstance() =
            BookListFragment().apply {
            }
    }
}

