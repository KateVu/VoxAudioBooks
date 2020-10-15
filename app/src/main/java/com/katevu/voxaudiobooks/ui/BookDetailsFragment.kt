package com.katevu.voxaudiobooks.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.katevu.voxaudiobooks.R
import com.katevu.voxaudiobooks.models.BookDetailsViewModel
import com.katevu.voxaudiobooks.utils.MediaPlayerService
import com.katevu.voxaudiobooks.utils.MediaPlayerService.LocalBinder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_item_track.view.*


private const val BASE_URL = "based_url"
private const val DETAILS_URL = "details_url"


class BookDetailsFragment() : Fragment() {
    val Broadcast_PLAY_NEW_AUDIO = "com.katevu.voxaudiobooks.ui.PlayNewAudio"

    private val TAG = "BookDetailsFragment"

    private val bookDetailsViewModel: BookDetailsViewModel by viewModels()
    private lateinit var trackRecyclerView: RecyclerView
    private lateinit var bookCover: ImageView
    private lateinit var adapter: TrackListAdapter
    private var player: MediaPlayerService? = null
    var serviceBound = false
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
        bookCover = view.findViewById<ImageView>(R.id.imageBookCover)
        trackRecyclerView = view.findViewById<RecyclerView>(R.id.track_recycler_view) as RecyclerView
        trackRecyclerView.layoutManager = LinearLayoutManager(context)
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookDetailsViewModel.bookDetails.observe(viewLifecycleOwner, Observer { book ->
            Log.d(TAG, "Book Details: $book")
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

    //Binding this Client to the AudioPlayer Service
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocalBinder
            player = binder.getService()
            serviceBound = true
            Toast.makeText(context, "Service Bound", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

    private fun playAudio(media: String) {
        //Check is service is active
        if (!serviceBound) {
            val playerIntent = Intent(context, MediaPlayerService::class.java)
            playerIntent.putExtra("media", media)

            getActivity()?.startService(playerIntent)
            getActivity()?.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        } else {
            //Service is active
            //Send media with BroadcastReceiver

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
            getActivity()?.sendBroadcast(broadcastIntent)
        }
    }


    private inner class TrackListAdapter(var tracks: List<String>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.list_item_track, parent, false)
            return TrackListHolder(view)
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
        private val seekbar = itemView.findViewById<SeekBar>(R.id.seekBar)
        private val playButton = itemView.findViewById<ImageButton>(R.id.playButton)

        private lateinit var trackUrl: String

        init {
            itemView.playButton.setOnClickListener(this)
            seekbar.setClickable(false);
        }
        /**
         * Binding data
         */
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun bind(trackUrl: String) {
            this.trackUrl = trackUrl
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onClick(p0: View?) {
            val url = urlText.plus(trackUrl)
            Log.d(TAG, ".onClick called with url: $url")
            playAudio(url)

        }


    }

    companion object {
        fun newInstance(urlText: String, urlDetails: String): BookDetailsFragment {
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