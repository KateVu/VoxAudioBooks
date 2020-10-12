package com.katevu.voxaudiobooks.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.katevu.voxaudiobooks.R
import com.katevu.voxaudiobooks.models.BookDetailsViewModel
import com.squareup.picasso.Picasso

private const val BASE_URL = "based_url"
private const val DETAILS_URL = "details_url"


class BookDetailsFragment() : Fragment() {
    private val TAG = "BookDetailsFragment"

    private val bookDetailsViewModel: BookDetailsViewModel by viewModels()
    private lateinit var trackRecyclerView: RecyclerView
    private lateinit var bookCover: ImageView
    private lateinit var adapter: TrackListAdapter
    private var urlText = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate called")
        super.onCreate(savedInstanceState)
        urlText = arguments?.getString(BASE_URL).toString()
        val urlDetails: String = arguments?.getString(DETAILS_URL).toString()
        bookDetailsViewModel.getBook(urlText, urlDetails)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, ".onCreateView called")
        // Inflate the layout for this fragment
        var view =  inflater.inflate(R.layout.fragment_book_details, container, false)

        trackRecyclerView = view.findViewById<RecyclerView>(R.id.track_recycler_view) as RecyclerView
        trackRecyclerView.layoutManager = LinearLayoutManager(context)
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookCover = view.findViewById<ImageView>(R.id.imageBookCover)

        bookDetailsViewModel.bookDetails.observe(viewLifecycleOwner, Observer { book ->
            Log.d(TAG,"Book Details: $book")
            val bookCoverLink = urlText.plus(book.bookCover)
            Log.d(TAG, ".onViewCreated: bookCoverLink: $bookCoverLink")
            Picasso.get()
                .load(bookCoverLink)
                .error(R.drawable.placeholder_image_icon_48dp)
                .placeholder(R.drawable.placeholder_image_icon_48dp)
                .into(bookCover)

            adapter = TrackListAdapter(book.listTracks)
            trackRecyclerView.adapter = adapter

        })

    }

    private inner class TrackListAdapter(var tracks: List<String>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.list_item_track, parent, false)
            return TrackListHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val track = tracks[position]
            holder as TrackListHolder
            holder.bind(track)
        }

        override fun getItemCount(): Int {
            return tracks.size
        }
    }

    /**
     * TrackListHolder
     */
    private inner class TrackListHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val trackTitle = itemView.findViewById<TextView>(R.id.trackTitle)

        init {
            itemView.setOnClickListener(this)
        }
        /**
         * Binding data
         */
        fun bind(trackTitleValue: String) {
            trackTitle.text = urlText.plus(trackTitleValue)
        }

        override fun onClick(p0: View?) {
            Toast.makeText(context, "Click on track", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        fun newInstance(urlText: String,urlDetails: String): BookDetailsFragment {
            val args: Bundle = Bundle().apply {
                putString(BASE_URL, urlText)
                putString(DETAILS_URL, urlDetails)
            }
            return BookDetailsFragment().apply {
                arguments = args
            }
        }
    }
}