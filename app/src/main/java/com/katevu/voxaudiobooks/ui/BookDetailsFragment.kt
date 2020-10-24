package com.katevu.voxaudiobooks.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.katevu.voxaudiobooks.R
import com.katevu.voxaudiobooks.R.drawable
import com.katevu.voxaudiobooks.models.BookDetailsViewModel
import com.katevu.voxaudiobooks.models.BookParcel
import com.katevu.voxaudiobooks.models.Track
import com.katevu.voxaudiobooks.utils.MediaPlayerService
import com.katevu.voxaudiobooks.utils.MediaPlayerService.LocalBinder
import com.squareup.picasso.Picasso

private const val BOOK_PARCEL = "book_parcel"
internal const val MEDIA = "media"

class BookDetailsFragment() : Fragment(),
    RecyclerItemClickListener.OnRecyclerClickListener {
    val Broadcast_PLAY_NEW_AUDIO = "com.katevu.voxaudiobooks.ui.PlayNewAudio"

    private val TAG = "BookDetailsFragment"

    private val bookDetailsViewModel: BookDetailsViewModel by viewModels()
    private lateinit var trackRecyclerView: RecyclerView
    private lateinit var bookCover: ImageView
    private lateinit var bookTitle: TextView
    private lateinit var bookDescription: TextView
    private lateinit var bookAuthor: TextView

    private lateinit var adapter: TrackListAdapter

    private var player: MediaPlayerService? = null
    var serviceBound = false

    private var bookParcel = BookParcel()

    override fun onCreate(savedInstanceState: Bundle?) {
//        Log.d(TAG, "onCreate called")
        super.onCreate(savedInstanceState)

        if (arguments != null) bookParcel = arguments?.getParcelable(BOOK_PARCEL)!!
        bookDetailsViewModel.getBook(bookParcel.link, bookParcel.urlDetails)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        Log.d(TAG, ".onCreateView called")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_book_details, container, false)

        bookCover = view.findViewById(R.id.imageBookCover)
        bookTitle = view.findViewById(R.id.book_details_title)
        bookDescription = view.findViewById(R.id.book_details_description)
        bookAuthor = view.findViewById(R.id.book_details_author)

        updateUI(bookParcel)
        trackRecyclerView = view.findViewById(R.id.track_recycler_view) as RecyclerView
        trackRecyclerView.layoutManager = LinearLayoutManager(context)
        trackRecyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(
                context,
                trackRecyclerView,
                this
            )
        )
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookDetailsViewModel.bookDetails.observe(viewLifecycleOwner, { book ->
//            Log.d(TAG, "Book Details: $book")
            adapter = TrackListAdapter(book.listTracks)
            trackRecyclerView.adapter = adapter
        })

    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBoolean("ServiceState", serviceBound)
        super.onSaveInstanceState(savedInstanceState)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        savedInstanceState?.let {
            serviceBound = savedInstanceState.getBoolean("ServiceState")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (serviceBound) {
            activity?.unbindService(serviceConnection)
            //service is active
            player?.stopSelf()
        }
    }

    override fun onItemClick(view: View, position: Int) {
        Log.d(TAG, "onItemClick called")

        val track = adapter.getTrack(position)
        val url = bookParcel.link.plus(track?.trackUrl)
        Log.d(TAG, ".onClick called with url: $url")
        track?.trackUrl?.let { bookDetailsViewModel.updateStatus(it) }
        track?.let { playAudio(it) }
        adapter.notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(bookParcel: BookParcel) {
        bookTitle.text = bookParcel.title
        bookDescription.text = bookParcel.description
        bookAuthor.text = "by ".plus(bookParcel.authors.first().first_name).plus(" ")
            .plus(bookParcel.authors.first().last_name)

        val linkImage = BASE_URL_IMAGE.plus("?identifier=").plus(bookParcel.identifier)
        Picasso.get()
            .load(linkImage)
            .error(drawable.placeholder_image_icon_48dp)
            .placeholder(drawable.placeholder_image_icon_48dp)
            .into(bookCover)
    }

    //Binding this Client to the AudioPlayer Service
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocalBinder
            player = binder.getService()
            serviceBound = true
            Toast.makeText(context, "Service Bound", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

    private fun playAudio(track: Track) {
        //Check is service is active
        if (!serviceBound) {
//            Log.d(TAG, ".playAudio called")
            val playerIntent = Intent(context, MediaPlayerService::class.java)
            playerIntent.apply {
                val linkImage = BASE_URL_IMAGE.plus("?identifier=").plus(bookParcel.identifier)
                track.bookCover = linkImage
                track.baseUrl = bookParcel.link
                putExtra(MEDIA, track)
            }
            activity?.startService(playerIntent)
            activity?.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        } else {
            //Service is active
            //Send media with BroadcastReceiver

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
            activity?.sendBroadcast(broadcastIntent)
        }
    }

    companion object {
        fun newInstance(bookParcel: BookParcel): BookDetailsFragment {
            val args: Bundle = Bundle().apply {
                putParcelable(BOOK_PARCEL, bookParcel)
            }
            return BookDetailsFragment().apply {
                arguments = args
            }
        }
    }
}
