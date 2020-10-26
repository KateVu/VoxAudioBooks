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
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.katevu.voxaudiobooks.R
import com.katevu.voxaudiobooks.R.drawable
import com.katevu.voxaudiobooks.models.BookDetailsViewModel
import com.katevu.voxaudiobooks.models.BookParcel
import com.katevu.voxaudiobooks.models.MediaFile
import com.katevu.voxaudiobooks.utils.AudioState
import com.katevu.voxaudiobooks.utils.MediaPlayerService
import com.katevu.voxaudiobooks.utils.MediaPlayerService.LocalBinder
import com.ms.square.android.expandabletextview.ExpandableTextView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_book_details.*

private const val BOOK_PARCEL = "book_parcel"
internal const val MEDIA = "media"

private const val TEST = "https://archive.org/download"
class BookDetailsFragment() : Fragment(),
    RecyclerItemClickListener.OnRecyclerClickListener,
    MediaPlayerService.OnMediaPlayerServiceListener {
    val Broadcast_PLAY_NEW_AUDIO = "com.katevu.voxaudiobooks.ui.PlayNewAudio"

    private val TAG = "BookDetailsFragment"

    private val bookDetailsViewModel: BookDetailsViewModel by viewModels()
    private lateinit var trackRecyclerView: RecyclerView
    private lateinit var bookCover: ImageView
    private lateinit var bookTitle: TextView
    //private lateinit var bookDescription: TextView
    private lateinit var bookAuthor: TextView
    private lateinit var expTv1: ExpandableTextView

    private lateinit var adapter: TrackListAdapter

    private var player: MediaPlayerService? = null
    var serviceBound = false

    private var bookParcel = BookParcel()

    override fun onCreate(savedInstanceState: Bundle?) {
//        Log.d(TAG, "onCreate called")
        super.onCreate(savedInstanceState)

        if (arguments != null) bookParcel = arguments?.getParcelable(BOOK_PARCEL)!!
        bookDetailsViewModel.getAudio(bookParcel.identifier)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        Log.d(TAG, ".onCreateView called")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_book_details2, container, false)

        bookCover = view.findViewById(R.id.imageBookCover)
        bookTitle = view.findViewById(R.id.book_details_title)
        //bookDescription = view.findViewById(R.id.book_details_description)
        bookAuthor = view.findViewById(R.id.book_details_author)
        expTv1 = view.findViewById(R.id.expand_text_view)

        updateUI(bookParcel)
        val spinner = view.findViewById(R.id.spinnerDetails) as ProgressBar
        spinner.visibility= View.VISIBLE

        trackRecyclerView = view.findViewById(R.id.track_recycler_view) as RecyclerView
        trackRecyclerView.layoutManager = LinearLayoutManager(context)
        trackRecyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(
                context,
                trackRecyclerView,
                this
            )
        )

        bookDetailsViewModel.spinner.observe(viewLifecycleOwner) { show ->
            spinner.visibility = if (show) View.VISIBLE else View.GONE
        }

        // Show a snackbar whenever the [ViewModel.snackbar] is updated a non-null value
        bookDetailsViewModel.snackbar.observe(viewLifecycleOwner) { text ->
            text?.let {
                Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show()
                bookDetailsViewModel.onSnackbarShown()
            }
        }


//        bookDetailsViewModel.bookDetails.observe(viewLifecycleOwner, { book ->
//            Log.d(TAG, "Book Details: $book")
//            adapter = TrackListAdapter(book.listTracks)
//            trackRecyclerView.adapter = adapter
//        })

        bookDetailsViewModel.audioBook.observe(viewLifecycleOwner, { audio ->
            Log.d(TAG, "Book Details: $audio")
            book_details_author.text = when {
                ((audio != null) && (audio.metadata != null) && (!audio.metadata!!.creator.isNullOrBlank())) -> "by ".plus(
                    audio?.metadata?.creator
                )
                else -> ""
            }
            adapter = TrackListAdapter(audio.mediaFiles)
            trackRecyclerView.adapter = adapter
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    override fun onItemClick(view: View?, position: Int) {
        Log.d(TAG, "onItemClick called $position")

        val track = adapter.getTrack(position)

        if (track != null) {
            when (track?.playbackState) {
                //Play a new track
                AudioState().IDLE -> {
                    //Play audio
//                Log.d(TAG, ".onClick called with track identifier: ${track.identifier}, track name: ${track.name}")
                    track?.let { playAudio(it) }
                    //update status of the new track
                    track.playbackState = AudioState().PLAYING
                    bookDetailsViewModel.setAudioStatus(track.name, track)
                    //update status of pending track
                    val mActiveTrack = bookDetailsViewModel.mActiveTrack
                    if (mActiveTrack != null) {
                        mActiveTrack.playbackState = AudioState().IDLE
                        bookDetailsViewModel.setAudioStatus(mActiveTrack.name, mActiveTrack)
                    }
                }
                //Pause a track
                AudioState().PLAYING -> {
                    if ((player != null) && (bookDetailsViewModel.mActiveTrack != null)) {
                        Log.d(TAG, ".Pause")
                        player?.pauseMedia()
                        player?.buildNotification(MediaPlayerService.PlaybackStatus.PAUSED);
                        //setup audiStatus
                        track.playbackState = AudioState().PAUSE
                        bookDetailsViewModel.setAudioStatus(track.name, track)
                    }
                }
                //Replay a track
                AudioState().PAUSE -> {
                    if ((player != null) && (bookDetailsViewModel.mActiveTrack != null)) {
                        Log.d(TAG, ".Pause")
                        player?.resumeMedia()
                        player?.buildNotification(MediaPlayerService.PlaybackStatus.PLAYING);
                        //setup audiStatus
                        track.playbackState = AudioState().PLAYING
                        bookDetailsViewModel.setAudioStatus(track.name, track)
                    }
                }
            }
            bookDetailsViewModel.mActiveTrack = track
            adapter.notifyDataSetChanged()

        } else {
            Toast.makeText(context, "Sorry, some error happen", Toast.LENGTH_LONG).show()
        }

    }

    private fun playAudio(track: MediaFile) {
        //Check is service is active
//            Log.d(TAG, ".playAudio called")
            val playerIntent = Intent(context, MediaPlayerService::class.java)
            playerIntent.apply {
                track.identifier = bookParcel.identifier
                putExtra(MEDIA, track)
            }

        Log.d(TAG, "playAudio called with track: $track")
            activity?.startService(playerIntent)
        if (!serviceBound) {
            activity?.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun updateUI(bookParcel: BookParcel) {
        bookTitle.text = bookParcel.title
        //bookDescription.text = bookParcel.description
        expTv1.setText(bookParcel.description)

        val linkImage = URL_COVER_LARGE_PREFIX.plus(bookParcel.identifier)
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
            Toast.makeText(context, "Audio is going to play", Toast.LENGTH_SHORT).show()
            player?.addListener(this@BookDetailsFragment)

        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
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

    override fun onMediaStartNew() {
        if (bookDetailsViewModel.mActiveTrack != null) {
            bookDetailsViewModel?.mActiveTrack!!.playbackState = AudioState().PLAYING
            bookDetailsViewModel?.mActiveTrack = null
            adapter.notifyDataSetChanged()
        }
    }


    override fun onMediaPlay() {
    }

    override fun onMediaSkipToPrevious() {
        if (bookDetailsViewModel.mActiveTrack != null) {
            val position: Int = adapter.tracks.indexOf(bookDetailsViewModel.mActiveTrack)
            if (position > 0) { onItemClick(null, position - 1) }
        }
    }

    override fun onMediaSkipToNext() {
        Log.d(TAG, ".onMediaSkipToNext called")
        if (bookDetailsViewModel.mActiveTrack != null) {
            val position: Int = adapter.tracks.indexOf(bookDetailsViewModel.mActiveTrack)
            if (position >= 0 && position < adapter.itemCount - 1) {
                onItemClick(null, position + 1)
            }
        }
    }

    override fun onMediaPrepared() {
        TODO("Not yet implemented")
    }

    override fun onMediaPause() {
//        Log.d(TAG, ".onMediaPause called")
        if (bookDetailsViewModel.mActiveTrack != null) {
            bookDetailsViewModel.mActiveTrack!!.playbackState = AudioState().PAUSE
            bookDetailsViewModel.setAudioStatus(
                bookDetailsViewModel.mActiveTrack!!.name,
                bookDetailsViewModel.mActiveTrack
            )
            adapter.notifyDataSetChanged()
        }
    }

    override fun onMediaResume() {
//        Log.d(TAG, ".onMediaPause called")
        if (bookDetailsViewModel.mActiveTrack != null) {
            bookDetailsViewModel.mActiveTrack!!.playbackState = AudioState().PLAYING
            bookDetailsViewModel.setAudioStatus(
                bookDetailsViewModel.mActiveTrack!!.name,
                bookDetailsViewModel.mActiveTrack
            )
            adapter.notifyDataSetChanged()
        }
    }

    override fun onMediaComplete() {
        if (bookDetailsViewModel.mActiveTrack != null) {
            bookDetailsViewModel.mActiveTrack!!.playbackState = AudioState().IDLE
            bookDetailsViewModel.setAudioStatus(
                bookDetailsViewModel.mActiveTrack!!.name,
                bookDetailsViewModel.mActiveTrack
            )
            bookDetailsViewModel.mActiveTrack = null
            adapter.notifyDataSetChanged()
        }
    }

    override fun onMediaStop() {
        if (bookDetailsViewModel.mActiveTrack != null) {
            bookDetailsViewModel.mActiveTrack!!.playbackState = AudioState().IDLE
            bookDetailsViewModel.setAudioStatus(
                bookDetailsViewModel.mActiveTrack!!.name,
                bookDetailsViewModel.mActiveTrack
            )
            bookDetailsViewModel.mActiveTrack = null
            adapter.notifyDataSetChanged()
        }
    }

    override fun onMediaBuffering(percent: Int) {
    }

    override fun onMediaError(what: Int, extra: Int) {
        if (bookDetailsViewModel.mActiveTrack != null) {
            bookDetailsViewModel.mActiveTrack!!.playbackState = AudioState().IDLE
            bookDetailsViewModel.setAudioStatus(
                bookDetailsViewModel.mActiveTrack!!.name,
                bookDetailsViewModel.mActiveTrack
            )
            bookDetailsViewModel.mActiveTrack = null
            adapter.notifyDataSetChanged()
        }
    }

}
