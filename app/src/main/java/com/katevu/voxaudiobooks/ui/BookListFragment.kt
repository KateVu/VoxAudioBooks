package com.katevu.voxaudiobooks.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.katevu.voxaudiobooks.R
import com.katevu.voxaudiobooks.models.Book
import com.katevu.voxaudiobooks.models.BookListViewModel
import com.katevu.voxaudiobooks.models.BookParcel
import com.squareup.picasso.Picasso

/**
 * A simple [Fragment] subclass.
 * Use the [BookListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
internal val BASE_URL_IMAGE = "https://archive.org/services/get-item-image.php"

class BookListFragment : Fragment() {

    interface Callbacks {
        fun onBookSelected(bookParcel: BookParcel)
    }

    private val TAG = "BookListFragment"
    private lateinit var bookRecyclerView: RecyclerView
    private var adapter: BookListAdapter? = null
    private var callbacks: Callbacks? = null
    private var bookParcel: BookParcel? = null

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
        Log.d(TAG, ".onCreateView called")
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_book_list, container, false)

        bookRecyclerView =
            view.findViewById(R.id.book_recycler_view) as RecyclerView
        bookRecyclerView.layoutManager = LinearLayoutManager(context)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookListViewModel.listBooks.observe(viewLifecycleOwner, Observer { listBooks  ->
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


    companion object {
        fun newInstance() =
            BookListFragment().apply {
            }
    }


    private inner class BookListAdapter(var books: List<Book>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.list_item_book, parent, false)
            return BookListHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val book = books[position]
            holder as BookListHolder
            holder.bind(book)
        }

        override fun getItemCount(): Int {
            return books.size
        }
    }

    /**
     * BookListHolder
     */
    private inner class BookListHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var book: Book
        private val bookTitle = itemView.findViewById<TextView>(R.id.title)
        private val bookDes = itemView.findViewById<TextView>(R.id.description)
        private val bookTime = itemView.findViewById<TextView>(R.id.total_time)
        private val bookThumbnail = itemView.findViewById<ImageView>(R.id.thumbnail)


        init {
            itemView.setOnClickListener(this)
        }
        /**
         * Binding data
         */
        fun bind(book: Book) {
            this.book = book
            bookTitle.text = this.book.title
            bookDes.text = this.book.description
            bookTime.text = this.book.totaltime.plus("s")

            val linkImage = BASE_URL_IMAGE.plus("?identifier=").plus(book.identifier)

            Picasso.get()
                .load(linkImage)
                .error(R.drawable.placeholder_image_icon_48dp)
                .placeholder(R.drawable.placeholder_image_icon_48dp)
                .into(bookThumbnail)
        }

        override fun onClick(p0: View?) {
//            Log.d(TAG, "book.link ${book.link}, bookquery: ${book.urlDetails}")
            bookParcel = BookParcel(book.id, book.title, book.description, book.link, book.urlDetails, book.identifier, book.num_sections.toInt(), book.authors)
            callbacks?.onBookSelected(bookParcel!!)
        }
    }

}
