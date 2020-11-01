package com.katevu.voxaudiobooks.ui

/**
 * Kate Vu
 */
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.katevu.voxaudiobooks.R
import com.katevu.voxaudiobooks.models.BookFavouriteViewModel
import com.katevu.voxaudiobooks.models.BookParcel

class BookFavouriteFragment : Fragment(),
    RecyclerItemClickListener.OnRecyclerClickListener {

    //send book back to MainActivity when onItemClick
    interface Callbacks {
        fun onBookSelected(bookParcel: BookParcel)
    }

    private val TAG = "BookFavouriteFragment"
    private lateinit var bookRecyclerView: RecyclerView
    private var adapter: BookListAdapter? = null
    private var callbacks: Callbacks? = null

    private val bookFavouriteViewModel: BookFavouriteViewModel by lazy {
        ViewModelProvider(this).get(
            BookFavouriteViewModel::class.java
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
//        Log.d(TAG, ".onCreate called")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        Log.d(TAG, ".onCreateView called")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bookfa_list, container, false)

        val spinner = view.findViewById(R.id.spinner) as ProgressBar
        spinner.visibility = View.VISIBLE

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

        bookFavouriteViewModel.spinner.observe(viewLifecycleOwner) { show ->
            spinner.visibility = if (show) View.VISIBLE else View.GONE
        }

        // Show a snackbar whenever the [ViewModel.snackbar] is updated a non-null value
        bookFavouriteViewModel.snackbar.observe(viewLifecycleOwner) { text ->
            text?.let {
                Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show()
                bookFavouriteViewModel.onSnackbarShown()
            }
        }

        bookFavouriteViewModel.loadData()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        Log.d(TAG, ".onViewCreated called")
        bookFavouriteViewModel.listBooks.observe(viewLifecycleOwner, { listBooks ->
            if (listBooks != null) {
//                Log.d(TAG, "onViewCreated: ${listBooks.size}")
                updateUI(listBooks)
            } else {
                Toast.makeText(context, "THERE IS NO FAVOURITE BOOKS NOW!!!", Toast.LENGTH_LONG)
                    .apply {
                        setGravity(Gravity.CENTER, 0, 0)
                        show()
                    }
            }
        })
    }

    override fun onAttach(context: Context) {
//        Log.d(TAG, ".onAttach called")
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onDetach() {
//        Log.d(TAG, ".onDetach called")
        super.onDetach()
        callbacks = null
    }

    private fun updateUI(books: List<BookParcel>) {
//        Log.d(TAG, ".updateUI called")
        adapter = BookListAdapter(books)
        bookRecyclerView.adapter = adapter
    }

    override fun onItemClick(view: View?, position: Int) {
//        Log.d(TAG, "onItemClick called")
        val bookParcel = adapter?.getBook(position)
        callbacks?.onBookSelected(bookParcel!!)
    }

    companion object {
        fun newInstance() =
            BookFavouriteFragment().apply {
            }
    }
}
