package com.katevu.voxaudiobooks.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.katevu.voxaudiobooks.R
import com.katevu.voxaudiobooks.models.Book
import com.katevu.voxaudiobooks.models.BookListViewModel
import com.katevu.voxaudiobooks.models.BookParcel

/**
 * A simple [Fragment] subclass.
 * Use the [BookListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
internal const val BASE_URL_IMAGE = "https://archive.org/services/get-item-image.php"

class BookListFragment : Fragment(),
    RecyclerItemClickListener.OnRecyclerClickListener{

    interface Callbacks {
        fun onBookSelected(bookParcel: BookParcel)
    }

    private val TAG = "BookListFragment"
    private lateinit var bookRecyclerView: RecyclerView
    private var adapter: BookListAdapter? = null
    private var callbacks: Callbacks? = null

//    private val bookListViewModel: bookListViewModel by lazy { ViewModelProvider(this).get(BookListViewModel::class.java) }

    private val bookListViewModel: BookListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        Log.d(TAG, ".onCreate called")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        Log.d(TAG, ".onCreateView called")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_book_list, container, false)

        bookRecyclerView =
            view.findViewById(R.id.book_recycler_view) as RecyclerView
        bookRecyclerView.layoutManager = LinearLayoutManager(context)
        bookRecyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(
                context,
                bookRecyclerView,
                this
            )
        )
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookListViewModel.listBooks.observe(viewLifecycleOwner, { listBooks  ->
            Log.d(TAG, "Response received: ${listBooks[0].authors}")
            updateUI(listBooks)
        })
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_book_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private fun updateUI(books: List<Book>) {
        Log.d(TAG, ".updateUI called")
        adapter = BookListAdapter(books)
        bookRecyclerView.adapter = adapter
    }

    override fun onItemClick(view: View, position: Int) {
//        Log.d(TAG, "onItemClick called")
        val bookParcel = adapter?.getBook(position)
        callbacks?.onBookSelected(bookParcel!!)
    }

    companion object {
        fun newInstance() =
            BookListFragment().apply {
            }
    }
}
