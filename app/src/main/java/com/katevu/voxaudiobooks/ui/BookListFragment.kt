package com.katevu.voxaudiobooks.ui

/**
 * Author: KateVu
 */
import android.app.SearchManager
import android.app.SearchableInfo
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.katevu.voxaudiobooks.R
import com.katevu.voxaudiobooks.models.BookListViewModel
import com.katevu.voxaudiobooks.models.BookParcel
import kotlinx.android.synthetic.main.fragment_book_list.*

/**
 * A simple [Fragment] subclass.
 * Use the [BookListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
internal const val URL_COVER_LARGE_PREFIX =
    "https://archive.org/services/get-item-image.php?identifier="

class BookListFragment : Fragment(),
    RecyclerItemClickListener.OnRecyclerClickListener {

    interface Callbacks {
        fun onBookSelected(bookParcel: BookParcel)
    }

    private val TAG = "BookListFragment"
    private lateinit var bookRecyclerView: RecyclerView
    private var adapter: BookListAdapter? = null
    private var callbacks: Callbacks? = null

    private val bookListViewModel: BookListViewModel by viewModels()

    private var searchView: SearchView? = null

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
        val toolbar = view.findViewById<View>(R.id.toolbar) as Toolbar
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)

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

        bookListViewModel.spinner.observe(viewLifecycleOwner) { show ->
            spinner.visibility = if (show) View.VISIBLE else View.GONE
        }

        // Show a snackbar whenever the [ViewModel.snackbar] is updated a non-null value
        bookListViewModel.snackbar.observe(viewLifecycleOwner) { text ->
            text?.let {
                Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show()
                bookListViewModel.onSnackbarShown()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookListViewModel.allBooks()
        bookListViewModel.listBooks.observe(
            viewLifecycleOwner,
            { listBooks -> updateUI(listBooks) })

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
        inflater.inflate(R.menu.menu_search, menu)

        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.app_bar_search).actionView as SearchView
        searchView?.isIconifiedByDefault = false

        searchView?.isFocusable = true
        searchView?.requestFocus()
        searchView?.requestFocusFromTouch()
        searchView?.queryHint = "Search Vox AudioBooks"

        val searchableInfo: SearchableInfo? =
            searchManager.getSearchableInfo(activity?.componentName)
        searchView?.setSearchableInfo(searchableInfo)
        searchView?.isIconified = false
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter = BookListAdapter(emptyList())
                Log.d(TAG, "setOnQueryTextListener called")
                query?.let {
                    spinner.visibility = View.VISIBLE
                    bookListViewModel.queryBooks(it)
                    adapter?.notifyDataSetChanged()
                }
                searchView?.clearFocus()
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }
        })

        Log.d(TAG, ".onCreateOptionsMenu: returning")
        super.onCreateOptionsMenu(menu, inflater);

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
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
            BookListFragment().apply {
            }
    }
}
