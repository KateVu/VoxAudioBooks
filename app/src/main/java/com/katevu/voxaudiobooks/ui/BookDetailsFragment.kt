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
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import kotlinx.android.synthetic.main.list_item_track.view.*

private const val BOOK_PARCEL = "book_parcel"

class BookDetailsFragment() : Fragment() {
    val Broadcast_PLAY_NEW_AUDIO = "com.katevu.voxaudiobooks.ui.PlayNewAudio"

    private val TAG = "BookDetailsFragment"

    private val bookDetailsViewModel: BookDetailsViewModel by viewModels()
    private lateinit var trackRecyclerView: RecyclerView
    private lateinit var bookCover: ImageView
    private lateinit var bookTitle: TextView
    private lateinit var bookDescription: TextView

    private lateinit var adapter: TrackListAdapter

    private var player: MediaPlayerService? = null
    var serviceBound = false

    private var bookParcel = BookParcel()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate called")
        super.onCreate(savedInstanceState)

        if (arguments != null) bookParcel = arguments?.getParcelable(BOOK_PARCEL)!!
        bookDetailsViewModel.getBook(bookParcel.link, bookParcel.urlDetails)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, ".onCreateView called")
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_book_details, container, false)

        bookCover = view.findViewById(R.id.imageBookCover)
        bookTitle = view.findViewById(R.id.book_details_title)
        bookDescription = view.findViewById(R.id.book_details_description)

        updateUI(bookParcel)
        trackRecyclerView = view.findViewById(R.id.track_recycler_view) as RecyclerView
        trackRecyclerView.layoutManager = LinearLayoutManager(context)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookDetailsViewModel.bookDetails.observe(viewLifecycleOwner, { book ->
            Log.d(TAG, "Book Details: $book")
            adapter = TrackListAdapter(book.listTracks)
            trackRecyclerView.adapter = adapter
        })

    }

    private fun updateUI(bookParcel: BookParcel) {
        bookTitle.text = bookParcel.title
        bookDescription.text = bookParcel.description
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


    private inner class TrackListAdapter(var tracks: List<Track>) :
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
            //holder.itemView.playButton.setOnClickListener(this)
            holder.bind(track)
        }

        override fun getItemCount(): Int {
            return tracks.size
        }


        /**
         * TrackListHolder
         */
        private inner class TrackListHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

            private val playButton = itemView.findViewById<ImageButton>(R.id.playButton)
            private val trackTitle = itemView.findViewById<TextView>(R.id.track_title)
            private val trackLength = itemView.findViewById<TextView>(R.id.track_length)
            private val trackSize = itemView.findViewById<TextView>(R.id.track_size)

            private lateinit var track: Track

            init {
                itemView.playButton.setOnClickListener(this)
            }
            /**
             * Binding data
             */
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            fun bind(track: Track) {
                this.track = track
                val isPlaying = track.isPlaying
                if (!isPlaying) {
                    playButton.setImageDrawable(resources.getDrawable(R.drawable.play_button_round))
                } else {
                    playButton.setImageDrawable(resources.getDrawable(R.drawable.pause_button_round))
                }
                trackTitle.text = track.trackTitle
                trackLength.text = getDurationString(track.trackLength)
                trackSize.text = getSize(track.trackSize)
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onClick(p0: View?) {
                val url = bookParcel.link.plus(track.trackUrl)
//            Log.d(TAG, ".onClick called with url: $url")
                bookDetailsViewModel.updateStatus(track.trackUrl)
                notifyDataSetChanged()
                playAudio(url)
            }
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


    fun getDurationString (duration: String): String {
        Log.d(TAG, ".getDurationString called")
        var result: String = ""
        try {
            val durationValue = duration.toDouble().toLong()
            val hours = durationValue.toInt() / 3600
            var remainder = durationValue.toInt() - hours * 3600
            val mins = remainder / 60
            remainder = remainder - mins * 60
            val secs = remainder

            if (hours != 0) {
                result =  "Time: ".plus(hours.toString().plus(":").plus(mins.toString()).plus(":").plus(secs.toString()).plus("s"))
            } else {
                if (mins != 0) {
                    result = "Time: ".plus(mins.toString().plus(":").plus(secs.toString()).plus("s"))
                } else {
                    result = "Time: ".plus(secs.toString().plus("s"))
                }
            }

        } catch (e: NumberFormatException) {
            Log.d(TAG, ".getDurationString catch error")
            result = "Time: ".plus(duration)
        }
        return result
    }

    fun getSize (size: String): String {
        Log.d(TAG, ".getDurationString called")
        var result: String = ""
        try {
            val sizeValue = size.toDouble() * 0.000001
            val number2digites = Math.round(sizeValue * 100.0) / 100.0
            result = "Size: ".plus(number2digites.toString()).plus("M")
        } catch (e: NumberFormatException) {
            Log.d(TAG, ".getSize catch error")
            result = "Size: ".plus(size).plus("M")
        }
        return result
    }

}