package com.katevu.voxaudiobooks.ui

/**
 * Author: Kate Vu
 */
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

private const val BOOK_PARCEL = "book_parcel"
internal const val MEDIA = "media"

class BookDetailsFragment() : Fragment(),
    RecyclerItemClickListener.OnRecyclerClickListener,
    MediaPlayerService.OnMediaPlayerServiceListener {

    private val TAG = "BookDetailsFragment"

    private val bookDetailsViewModel: BookDetailsViewModel by viewModels()
    private lateinit var trackRecyclerView: RecyclerView
    private lateinit var bookCover: ImageView
    private lateinit var bookTitle: TextView
    private lateinit var isFavourite: ImageButton
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
        isFavourite = view.findViewById(R.id.buttonFavourite)

        updateUI(bookParcel)
        isFavourite.setOnClickListener {
            bookDetailsViewModel.updateFavourite(bookParcel)
            bookParcel.isFavourite = !bookParcel.isFavourite
            updateUI(bookParcel)
        }

        val spinner = view.findViewById(R.id.spinnerDetails) as ProgressBar
        spinner.visibility = View.VISIBLE

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

        bookDetailsViewModel.audioBook.observe(viewLifecycleOwner, { audio ->
            if (audio != null) {
                Log.d(TAG, "Book Details: $audio")
                bookAuthor.text = when {
                    ((audio.metadata != null) && !audio.metadata!!.creator.isNullOrBlank()) -> "by ".plus(
                        audio?.metadata?.creator
                    )
                    else -> ""
                }
                adapter = TrackListAdapter(audio.mediaFiles)
                trackRecyclerView.adapter = adapter
            } else {
                Toast.makeText(context, "THERE IS NO DATA AT THE MOMENT!!!", Toast.LENGTH_LONG)
                    .apply {
                        setGravity(Gravity.CENTER, 0, 0)
                        show()
                    }
            }
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

    override fun onDestroyView() {
        super.onDestroyView()

        if (serviceBound) {
            activity?.unbindService(serviceConnection)
            //service is active
            player?.removeNotification()
            player?.stopSelf()
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

        if (bookParcel.isFavourite) {
            isFavourite.setImageBitmap(
                BitmapFactory.decodeResource(
                    context?.resources,
                    R.drawable.favourite
                )
            )
        } else isFavourite.setImageBitmap(
            BitmapFactory.decodeResource(
                context?.resources,
                R.drawable.nofavourite
            )
        )
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

    override fun onItemClick(view: View?, position: Int) {
        Log.d(TAG, "onItemClick called $position")

        val track = adapter.getTrack(position)

        if (track != null) {
            when (track.playbackState) {
                //Play a new track
                AudioState().IDLE -> {
//                Log.d(TAG, ".onClick called with track identifier: ${track.identifier}, track name: ${track.name}")
                    //play the audio
                    playAudio(track)
                    //set status for pending file
                    if (bookDetailsViewModel.mPendingTrack != null) {
                        bookDetailsViewModel.mPendingTrack!!.playbackState = AudioState().IDLE
                    }
                    bookDetailsViewModel.mPendingTrack = track
                    track.playbackState = AudioState().PLAYING
                }
                //Pause a track
                AudioState().PLAYING -> {
                    if ((player != null) && (bookDetailsViewModel.mActiveTrack != null)) {
                        Log.d(TAG, ".Pause")
                        player?.pauseMedia()
                        player?.buildNotification(MediaPlayerService.PlaybackStatus.PAUSED);
                        //setup audiStatus
                        track.playbackState = AudioState().PAUSE
                    }
                }
                //Replay a track
                AudioState().PAUSE -> {
                    if ((player != null) && (bookDetailsViewModel.mActiveTrack != null)) {
                        Log.d(TAG, ".Pause")
                        player?.resumeMedia()
                        player?.buildNotification(MediaPlayerService.PlaybackStatus.PLAYING);
                        //setup audioStatus
                        track.playbackState = AudioState().PLAYING
                    }
                }
            }
            //Update UI
            adapter.notifyDataSetChanged()

        } else {
            Toast.makeText(context, "Sorry, some error happen", Toast.LENGTH_LONG).show()
        }

    }

    /**
     * Send Intent to MediaPlayerService with an object of MediaFile
     */
    private fun playAudio(track: MediaFile) {
//            Log.d(TAG, ".playAudio called")
        val playerIntent = Intent(context, MediaPlayerService::class.java)
        playerIntent.apply {
            track.identifier = bookParcel.identifier
            putExtra(MEDIA, track)
        }

        Log.d(TAG, "playAudio called with track: $track")
        activity?.startService(playerIntent)
        //Check is service is active
        if (!serviceBound) {
            activity?.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * MediaSession notifies a new media palyer
     */
    override fun onMediaStartNew() {
        if (bookDetailsViewModel.mActiveTrack != null) {
            //Update status of new playing file
            bookDetailsViewModel.mActiveTrack!!.playbackState = AudioState().IDLE
            bookDetailsViewModel.mActiveTrack = null
            adapter.notifyDataSetChanged()
        }
    }


    override fun onMediaPlay() {
    }

    /**
     * Notification object notifies to skip to next audio file
     */
    override fun onMediaSkipToPrevious() {
        if (bookDetailsViewModel.mActiveTrack != null) {
            val position: Int = adapter.tracks.indexOf(bookDetailsViewModel.mActiveTrack)
            if (position > 0) {
                onItemClick(null, position - 1)
            }
        }
    }

    /**
     * Notification object notifies to skip to previous audio file
     */
    override fun onMediaSkipToNext() {
        Log.d(TAG, ".onMediaSkipToNext called")
        if (bookDetailsViewModel.mActiveTrack != null) {
            val position: Int = adapter.tracks.indexOf(bookDetailsViewModel.mActiveTrack)
            if (position >= 0 && position < adapter.itemCount - 1) {
                onItemClick(null, position + 1)
            }
        }
    }

    /**
     * MedioSession object is about to play a media file
     * update new ActiveTrack = PendingTrack
     * set PendingTrack = null
     */
    override fun onMediaPrepared() {
        if (bookDetailsViewModel.mPendingTrack != null) {
            bookDetailsViewModel.mActiveTrack = bookDetailsViewModel.mPendingTrack
            bookDetailsViewModel.mPendingTrack = null
        }
    }

    /**
     * MediaSession notifies onPause
     * update status of ActiveTrack
     * update UI
     */
    override fun onMediaPause() {
//        Log.d(TAG, ".onMediaPause called")
        if (bookDetailsViewModel.mActiveTrack != null) {
            bookDetailsViewModel.mActiveTrack!!.playbackState = AudioState().PAUSE
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * MediaSession notifies onResume
     * update status of ActiveTrack
     * update UI
     */
    override fun onMediaResume() {
//        Log.d(TAG, ".onMediaPause called")
        if (bookDetailsViewModel.mActiveTrack != null) {
            bookDetailsViewModel.mActiveTrack!!.playbackState = AudioState().PLAYING
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * MediaSession notifies onComplete
     * reset ActiveTrack, PendingTrack to null
     * update UI
     */
    override fun onMediaComplete() {
        if (bookDetailsViewModel.mActiveTrack != null) {
            bookDetailsViewModel.mActiveTrack!!.playbackState = AudioState().IDLE
            bookDetailsViewModel.mActiveTrack = null
            bookDetailsViewModel.mPendingTrack = null
            adapter.notifyDataSetChanged()
        }

    }

    /**
     * MediaSession notifies onStop
     * reset ActiveTrack, PendingTrack to null
     * update UI
     */
    override fun onMediaStop() {
        if (bookDetailsViewModel.mActiveTrack != null) {
            bookDetailsViewModel.mActiveTrack!!.playbackState = AudioState().IDLE
            bookDetailsViewModel.mActiveTrack = null
            bookDetailsViewModel.mPendingTrack = null
            adapter.notifyDataSetChanged()
        }
    }

    override fun onMediaBuffering(percent: Int) {
    }

    /**
     * MediaSession notifies onError
     * update ActiveTrack if notNull
     * reset ActiveTrack, PendingTrack to null
     * update UI
     */
    override fun onMediaError(what: Int, extra: Int) {
        if (bookDetailsViewModel.mActiveTrack != null) {
            bookDetailsViewModel.mActiveTrack!!.playbackState = AudioState().IDLE
            bookDetailsViewModel.mActiveTrack = null
            bookDetailsViewModel.mPendingTrack = null
            adapter.notifyDataSetChanged()
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
