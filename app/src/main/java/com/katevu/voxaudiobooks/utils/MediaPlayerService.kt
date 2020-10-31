package com.katevu.voxaudiobooks.utils

import android.R
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.media.session.MediaSessionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.katevu.voxaudiobooks.models.MediaFile
import com.katevu.voxaudiobooks.ui.MEDIA
import com.katevu.voxaudiobooks.ui.URL_COVER_LARGE_PREFIX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import java.util.*
import androidx.media.app.NotificationCompat as MediaNotificationCompat


private const val ACTION_PLAY: String = "com.katevu.voxaudiobooks.action.ACTION_PLAY"
private const val ACTION_PAUSE: String = "com.katevu.voxaudiobooks.action.ACTION_PAUSE"
private const val ACTION_PREVIOUS: String = "com.katevu.voxaudiobooks.action.ACTION_PREVIOUS"
private const val ACTION_NEXT: String = "com.katevu.voxaudiobooks.action.ACTION_NEXT"
private const val ACTION_STOP: String = "com.katevu.voxaudiobooks.action.ACTION_STOP"
private const val CHANNEL_CODE: String = "Media"

private const val NOTIFICATION_ID = 8888
private const val CHANNEL_ID = "com.katevu.voxaudiobooks.channel.main"
private const val URL_AUDIO_LIBRIVOX = "https://archive.org/download/"

private const val TAG = "MediaPlayerService"

//Handle incoming phone calls
private var ongoingCall = false
private var phoneStateListener: PhoneStateListener? = null
private var telephonyManager: TelephonyManager? = null

class MediaPlayerService : Service(),
    OnCompletionListener,
    OnPreparedListener,
    OnErrorListener,
    OnSeekCompleteListener,
    OnInfoListener,
    OnBufferingUpdateListener,
    OnAudioFocusChangeListener {

    // audio manager
    private var audioManager: AudioManager? = null

    //mediaPlayer and url
    private var mediaPlayer: MediaPlayer? = null
    private var track = MediaFile()

    //Used to pause/resume MediaPlayer
    private var resumePosition = 0

    // Binder given to clients
    private val binder = LocalBinder()

    //MediaSession
    private var mediaSessionManager: MediaSessionManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null
    private var notificationManager: NotificationManager? = null
    private var notificationChannel: NotificationChannel? = null

    //Call back to update list of track
    private val mListeners: MutableList<OnMediaPlayerServiceListener> =
        ArrayList<OnMediaPlayerServiceListener>()

    override fun onBind(p0: Intent?): IBinder? {
        return binder;
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, ".onStartCommand called")

        //Request audio focus
        if (!requestAudioFocus()) {
            //Could not gain focus
            stopSelf()
        }

        var trackIntent: MediaFile? = null
        if (intent.hasExtra(MEDIA)) {
            Log.d(TAG, ".onStartCommand get MEDIA")
            trackIntent = intent.extras!!.getParcelable(MEDIA)!!
            Log.d(TAG, ".onStartCommand get track: $trackIntent")
        }

        val action = intent.action
        if (mediaSessionManager == null) {
            try {
                initMediaSession()
            } catch (e: RemoteException) {
                e.printStackTrace()
                stopSelf()
            }
        }

        // playing or pausing different audio and this is about to start a new audio
        if (action == null && mediaPlayer != null) {
            stopMedia()
            Log.d(TAG, ".onStartCommand called for new audio")
        }

        if (trackIntent != null) {
            track = trackIntent
            initMediaPlayer()
            updateMetaData()

            Log.d(TAG, ".onStartCommand called")
            buildNotification(PlaybackStatus.PLAYING)
            for (listener in mListeners) listener.onMediaStartNew()
        }

        Log.d(TAG, ".onStartCommand ends")

        handleIncomingActions(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        //init notificationManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //setup callstateListener
        setupCallStateListener()
    }

    override fun onCompletion(mMediaPlayer: MediaPlayer?) {
        stopMedia()
        for (listener in mListeners) {
            listener.onMediaComplete()
        }
        removeNotification()
        stopSelf()
    }

    override fun onPrepared(mediaPlayer: MediaPlayer?) {

            for (listener in mListeners) {
                listener.onMediaPrepared()
            }
            playMedia()
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        when (what) {
            MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> Log.d(
                TAG,
                "MediaPlayer Error: MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra"
            )
            MEDIA_ERROR_SERVER_DIED -> Log.d(
                TAG,
                "MediaPlayer Error: MEDIA ERROR SERVER DIED $extra"
            )
            MEDIA_ERROR_UNKNOWN -> Log.d(TAG, "MediaPlayer Error: MEDIA ERROR UNKNOWN $extra")
        }
        for (listener in mListeners) {
            listener.onMediaError(what, extra)
        }

        return false
    }

    override fun onSeekComplete(p0: MediaPlayer?) {
        //Invoked indicating the completion of a seek operation.
    }

    override fun onInfo(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        return false
    }

    override fun onBufferingUpdate(mediaPlayer: MediaPlayer?, percent: Int) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
        for (listener in mListeners) {
            listener.onMediaBuffering(percent)
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // resume playback
                if (mediaPlayer == null) {
                    initMediaPlayer()
                } else {
                    (!mediaPlayer!!.isPlaying)
                    mediaPlayer!!.start()
                }
                mediaPlayer!!.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer!!.isPlaying) mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = null
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->             // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer!!.isPlaying) mediaPlayer!!.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->             // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer!!.isPlaying) mediaPlayer!!.setVolume(0.1f, 0.1f)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer?.release()
        }
        removeAudioFocus();

        //unregister phone state listener
        if (phoneStateListener != null) {
            telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }

    }

    private fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val result = audioManager!!.requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager!!.abandonAudioFocus(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun initMediaPlayer() {
        // ...initialize the MediaPlayer here...
        mediaPlayer = MediaPlayer()
        //Set up MediaPlayer event listeners

        mediaPlayer?.apply {
            setOnCompletionListener(this@MediaPlayerService)
            setOnErrorListener(this@MediaPlayerService)
            setOnPreparedListener(this@MediaPlayerService)
            setOnBufferingUpdateListener(this@MediaPlayerService)
            setOnSeekCompleteListener(this@MediaPlayerService)
            setOnInfoListener(this@MediaPlayerService)
        }
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer?.reset()
        mediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        try {
            // url -> https://archive.org/download/[identifier]/[name]

            val trackUrl = URL_AUDIO_LIBRIVOX.plus(track.identifier).plus("/").plus(track.name)
            // Set the data source to the mediaFile location
            mediaPlayer?.setDataSource(trackUrl)
        } catch (e: IOException) {
            e.printStackTrace()
            stopSelf()
        }
        mediaPlayer?.prepareAsync()
    }


    @SuppressLint("ServiceCast")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Throws(RemoteException::class)
    private fun initMediaSession() {
        Log.d(TAG, ".initMediaSession called")
        if (mediaSessionManager != null) {
            Log.d(TAG, "mediaSessionManager exists")
        } else {
            Log.d(TAG, "create mediaSession")

            mediaSessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
            //
            mediaSession = MediaSessionCompat(applicationContext, "AudioPlayer")
            transportControls = mediaSession?.controller?.transportControls
            mediaSession?.isActive = true
            updateMetaData()

            // Attach Callback to receive MediaSession updates
            mediaSession?.setCallback(object : MediaSessionCompat.Callback() {
                // Implement callbacks
                override fun onPlay() {
                    Log.d(TAG, ".onPlay called")
                    super.onPlay()
                    resumeMedia()
                    buildNotification(PlaybackStatus.PLAYING)
                    for (listener in mListeners) {
                        listener.onMediaResume()
                    }
                }

                override fun onPause() {
                    super.onPause()
                    pauseMedia()
                    buildNotification(PlaybackStatus.PAUSED)
                    for (listener in mListeners) {
                        listener.onMediaPause()
                    }
                }

                override fun onStop() {
                    super.onStop()
                    removeNotification();
                    for (listener in mListeners) {
                        listener.onMediaStop()
                    }
                    //Stop the service
                    stopSelf()
                }

                override fun onSkipToNext() {
                    Log.d(TAG, "fun onSkipToNext called")
                    super.onSkipToNext()
                    for (listener in mListeners) {
                        listener.onMediaSkipToNext()
                    }
                }

                override fun onSkipToPrevious() {
                    super.onSkipToPrevious()
                    for (listener in mListeners) {
                        listener.onMediaSkipToPrevious()
                    }
                }

                override fun onSeekTo(position: Long) {
                    super.onSeekTo(position)
                }
            })
            if (mediaSession != null) {
                Log.d(TAG, "create mediaSession successfull")
            }
        }
    }

    private fun updateMetaData() {
        val artwork: Bitmap = BitmapFactory.decodeResource(
            getResources(),
            R.drawable.ic_media_play
        )
        mediaSession?.apply {
            setMetadata(
                MediaMetadataCompat.Builder()
                    // Title.
                    .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
                    // Artist.
                    // Could also be the channel name or TV series.
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist)
                    .putString(
                        MediaMetadata.METADATA_KEY_ALBUM_ART_URI,
                        URL_COVER_LARGE_PREFIX.plus(track.identifier)
                    )
                    // Duration.
                    // If duration isn't set, such as for live broadcasts, then the progress
                    // indicator won't be shown on the seekbar.
                    .putLong(
                        MediaMetadata.METADATA_KEY_DURATION,
                        getDurationLong(track.length)
                    )
                    .build()
            )
        }



    }

    /**
     * @duration: track.length: String
     * return long type for input updateMetadata
     */
    fun getDurationLong(duration: String): Long {
//        Log.d(TAG, ".getDurationString called")
        var result: Long = 0
        try {
            val durationValue = duration.toDouble().toLong()
        } catch (e: NumberFormatException) {
//            Log.d(TAG, ".getDurationString catch error")
            result = 0
        }
        return result
    }

    /**
     * create notification channel
     * is input for buildNotification fun
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        if (notificationChannel == null) {
            notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "VoxAudioBooks",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel!!.setDescription("Media Playback Controls")
            notificationChannel!!.setShowBadge(false)
            notificationChannel!!.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC)
            notificationManager!!.createNotificationChannel(notificationChannel!!)
        }
    }

    /**
     * remove notification when finished or distroy
     */
    fun removeNotification() {
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    /**
     * Handle action put in notification
     */
    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(this, MediaPlayerService::class.java)
        when (actionNumber) {
            0 -> {
                // Play
                playbackAction.action = ACTION_PLAY
                Log.d(TAG, ".playBackAction call with ACTION_PLAY")
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)

            }
            1 -> {
                // Pause
                playbackAction.action = ACTION_PAUSE
                Log.d(TAG, ".playBackAction call with ACTION_PAUSE")
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            2 -> {
                // Previous track
                playbackAction.action = ACTION_PREVIOUS
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            3 -> {
                // Next track
                playbackAction.action = ACTION_NEXT
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            else -> {
            }
        }
        return null
    }

    fun playMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()
        }
    }

    fun stopMedia() {
        if ((mediaPlayer != null) && (mediaPlayer!!.isPlaying)) {
            mediaPlayer!!.stop()
        }
    }

    fun pauseMedia() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            resumePosition = mediaPlayer!!.currentPosition
        }
    }

    fun resumeMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.seekTo(resumePosition)
            mediaPlayer!!.start()
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): MediaPlayerService = this@MediaPlayerService
    }

    enum class PlaybackStatus {
        PLAYING,
        PAUSED
    }

    /**
     * Build notification
     */
    fun buildNotification(playbackStatus: PlaybackStatus) {
        var notificationAction = R.drawable.ic_media_pause //needs to be initialized
        var play_pauseAction: PendingIntent? = null

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            Log.d(TAG, ".buildNotification called with playBackStatus: $playbackStatus")
            notificationAction = R.drawable.ic_media_pause
            //create the pause action
            play_pauseAction = playbackAction(1)
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_media_play
            //create the play action
            play_pauseAction = playbackAction(0)
        }

        //Create channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        // Build the notification
//        Log.d(TAG, "mediaSessnToken: ${mediaSession!!.getSessionToken()}")
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            // Show controls on lock screen even when user hides sensitive content.
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(com.katevu.voxaudiobooks.R.drawable.earphone)
            // Add media control buttons that invoke
            .addAction(R.drawable.ic_media_previous, "prev", playbackAction(2))
            .addAction(notificationAction, "Play/Pause", play_pauseAction) // #1
            .addAction(R.drawable.ic_media_next, "next", playbackAction(3))
            .setStyle(
                MediaNotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession!!.sessionToken)
            )
            .setContentTitle(track.title.toUpperCase())
            .setContentText(track.artist)
            //.setLargeIcon(Picasso.get().load(linkCover).)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        applyImageUrl(notification, URL_COVER_LARGE_PREFIX.plus(track.identifier))
        notificationManager!!.notify(NOTIFICATION_ID, notification.build())

    }

    fun applyImageUrl(
        builder: NotificationCompat.Builder,
        imageUrl: String
    ) = runBlocking {
        val url = URL(imageUrl)
        withContext(Dispatchers.IO) {
            try {
                val input = url.openStream()
                BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                null
            }
        }?.let { bitmap ->
            builder.setLargeIcon(bitmap)
//            val palette: Palette = Palette.from(bitmap).generate()
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//                builder.setColor(palette.getDominantColor(Color.BLACK))
//                    .setColorized(true)
//            }
        }
    }

    private fun handleIncomingActions(playbackAction: Intent?) {
        if (playbackAction == null || playbackAction.action == null) return
        val actionString = playbackAction.action
        when {
            actionString.equals(ACTION_PLAY, ignoreCase = true) -> {
                transportControls!!.play()
            }
            actionString.equals(ACTION_PAUSE, ignoreCase = true) -> {
                transportControls!!.pause()
            }
            actionString.equals(ACTION_NEXT, ignoreCase = true) -> {
                transportControls!!.skipToNext()

                Log.d(TAG, ".handleIncomingAction: skipToNext()")
            }
            actionString.equals(ACTION_PREVIOUS, ignoreCase = true) -> {
                transportControls!!.skipToPrevious()
            }
            actionString.equals(ACTION_STOP, ignoreCase = true) -> {
                transportControls!!.stop()
            }
        }
    }

    /**
     * Handle incomming phone call
     */
    private fun setupCallStateListener() {
        // Get the telephony manager
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        //Init PhoneStateListener object
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String) {
                when (state) {
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> if (mediaPlayer != null) {
                        pauseMedia()
                        ongoingCall = true
                    }
                    TelephonyManager.CALL_STATE_IDLE ->
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false
                                resumeMedia()
                            }
                        }
                }
            }
        }
        //register PhoneStateListener to telemphony manager
        telephonyManager!!.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    //add listener to notify fragment when there is action in MediaSession object
    fun addListener(listener: OnMediaPlayerServiceListener) {
        mListeners.add(listener)
    }

    interface OnMediaPlayerServiceListener {
        fun onMediaStartNew()
        fun onMediaPlay()
        fun onMediaSkipToPrevious()
        fun onMediaSkipToNext()
        fun onMediaPrepared()
        fun onMediaPause()
        fun onMediaResume()
        fun onMediaComplete()
        fun onMediaStop()
        fun onMediaBuffering(percent: Int)
        fun onMediaError(what: Int, extra: Int)
    }
}
