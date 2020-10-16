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
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.katevu.voxaudiobooks.models.Track
import com.katevu.voxaudiobooks.ui.MEDIA
import com.katevu.voxaudiobooks.ui.MEDIA_BASEURL
import java.io.IOException
import androidx.media.app.NotificationCompat as MediaNotificationCompat


private const val ACTION_PLAY: String = "com.katevu.voxaudiobooks.action.ACTION_PLAY"
private const val ACTION_PAUSE: String = "com.katevu.voxaudiobooks.action.ACTION_PAUSE"
private const val ACTION_PREVIOUS: String = "com.katevu.voxaudiobooks.action.ACTION_PREVIOUS"
private const val ACTION_NEXT: String = "com.katevu.voxaudiobooks.action.ACTION_NEXT"
private const val ACTION_STOP: String = "com.katevu.voxaudiobooks.action.ACTION_STOP"
private const val CHANNEL_CODE: String = "Media"

private const val NOTIFICATION_ID = 888
private const val CHANNEL_ID = "com.katevu.voxaudiobooks.channel.main"
private const val NOTIFICATION_CHANNEL_ID = "com.katevu.voxaudiobooks.notification.channel"

private const val TAG = "MediaPlayerService"

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
    private var track = Track()
    private var baseURL = ""

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

    //AudioPlayer notification ID
    override fun onBind(p0: Intent?): IBinder? {
        return binder;
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try {
            Log.d(TAG, ".onStartCommand called")
            //An audio file is passed to the service through putExtra();
            if (intent.hasExtra(MEDIA)) {
                Log.d(TAG, ".onStartCommand get MEDIA")
                track = intent.extras!!.getParcelable(MEDIA)!!
                Log.d(TAG, ".onStartCommand get track: $track")
            }

            if (intent.hasExtra(MEDIA_BASEURL)) {
                baseURL = intent.extras!!.getString(MEDIA_BASEURL).toString()
                Log.d(TAG, ".onStartCommand get track: $track")
            }


        } catch (e: NullPointerException) {
            Log.d(TAG, ".onStartCommand error")
            stopSelf()
        }
        //Request audio focus
        if (!requestAudioFocus()) {
            //Could not gain focus
            stopSelf()
        }

        if (!track.trackUrl.isNullOrEmpty()) {
            if (mediaSessionManager == null) {
                try {
                    initMediaSession()
                    initMediaPlayer()
                } catch (e: RemoteException) {
                    e.printStackTrace()
                    stopSelf()
                }
                Log.d(TAG, ".onStartCommand called")
                buildNotification(PlaybackStatus.PLAYING)
            }
        }

        handleIncomingActions(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    }

    override fun onCompletion(mMediaPlayer: MediaPlayer?) {
        stopMedia()
        stopSelf()
    }

    override fun onPrepared(mediaPlayer: MediaPlayer?) {
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
                "MediaPlayer Error MEDIA ERROR SERVER DIED $extra"
            )
            MEDIA_ERROR_UNKNOWN -> Log.d(TAG, "MediaPlayer Error MEDIA ERROR UNKNOWN $extra")
        }
        return false
    }

    override fun onSeekComplete(p0: MediaPlayer?) {
        //Invoked indicating the completion of a seek operation.
    }

    override fun onInfo(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        return false
    }

    override fun onBufferingUpdate(p0: MediaPlayer?, p1: Int) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
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
                }
                mediaPlayer!!.start()
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
            var trackUrl = baseURL.plus("/").plus(track.trackUrl)
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
            transportControls = mediaSession?.getController()?.getTransportControls()
            mediaSession?.setActive(true)
            mediaSession?.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

            updateMetaData()

            // Attach Callback to receive MediaSession updates
            mediaSession?.setCallback(object : MediaSessionCompat.Callback() {
                // Implement callbacks
                override fun onPlay() {
                    Log.d(TAG, ".onPlay called")
                    super.onPlay()
                    resumeMedia()
                    buildNotification(PlaybackStatus.PLAYING);
                }

                override fun onPause() {
                    super.onPause()
                    pauseMedia()
                    buildNotification(PlaybackStatus.PAUSED);

                }

                override fun onStop() {
                    super.onStop()
                    removeNotification();
                    //Stop the service
                    stopSelf()
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
        mediaSession?.setMetadata(
            MediaMetadataCompat.Builder()
                // Title.
                .putString(MediaMetadata.METADATA_KEY_TITLE, "title")
                // Artist.
                // Could also be the channel name or TV series.
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "artist")
                // Duration.
                // If duration isn't set, such as for live broadcasts, then the progress
                // indicator won't be shown on the seekbar.
                .putLong(MediaMetadata.METADATA_KEY_DURATION, 10000) // 4
                .build()
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        if (notificationChannel == null) {
            notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Media Playback",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel!!.setDescription("Media Playback Controls")
            notificationChannel!!.setShowBadge(false)
            notificationChannel!!.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC)
            notificationManager!!.createNotificationChannel(notificationChannel!!)
        }
    }


    private fun removeNotification() {
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

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
                // Next track
                playbackAction.action = ACTION_NEXT
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            3 -> {
                // Previous track
                playbackAction.action = ACTION_PREVIOUS
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            else -> {
            }
        }
        return null
    }


    private fun playMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()
        }
    }

    private fun stopMedia() {
        if (mediaPlayer == null) return
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
        }
    }

    private fun pauseMedia() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            resumePosition = mediaPlayer!!.currentPosition
        }
    }

    private fun resumeMedia() {
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


    private fun buildNotification(playbackStatus: PlaybackStatus) {

        var notificationAction = R.drawable.ic_media_pause //needs to be initialized
        var play_pauseAction: PendingIntent? = null

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = R.drawable.ic_media_pause
            //create the pause action
            play_pauseAction = playbackAction(1)
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_media_play
            //create the play action
            play_pauseAction = playbackAction(0)
        }

        // TODO: Step 2.0 add style
        val eggImage = BitmapFactory.decodeResource(
            applicationContext.resources,
            R.drawable.ic_dialog_email
        )
        val bigPicStyle = NotificationCompat.BigPictureStyle()
            .bigPicture(eggImage)
            .bigLargeIcon(null)
        // TODO: Step 1.2 get an instance of NotificationCompat.Builder
        // Build the notification
        Log.d(TAG, "mediaSessnToken: ${mediaSession!!.getSessionToken()}")
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            // TODO: Step 1.3 set title, text and icon to builder
            // Show controls on lock screen even when user hides sensitive content.
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_menu_directions)
            // Add media control buttons that invoke
            .addAction(R.drawable.ic_media_pause, "Pause", play_pauseAction) // #1
            // TODO: Step 2.1 add style to builder
            //.setStyle(bigPicStyle)
            .setStyle(
                MediaNotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession!!.sessionToken)
            )
            .setContentTitle("Wonderful music")
            .setContentText("My Awesome Band")
            .setLargeIcon(eggImage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        notificationManager!!.notify(NOTIFICATION_ID, builder.build())
    }

    private fun handleIncomingActions(playbackAction: Intent?) {
        if (playbackAction == null || playbackAction.action == null) return
        val actionString = playbackAction.action
        if (actionString.equals(ACTION_PLAY, ignoreCase = true)) {
            transportControls!!.play()
        } else if (actionString.equals(ACTION_PAUSE, ignoreCase = true)) {
            transportControls!!.pause()
        } else if (actionString.equals(ACTION_NEXT, ignoreCase = true)) {
            transportControls!!.skipToNext()
        } else if (actionString.equals(ACTION_PREVIOUS, ignoreCase = true)) {
            transportControls!!.skipToPrevious()
        } else if (actionString.equals(ACTION_STOP, ignoreCase = true)) {
            transportControls!!.stop()
        }
    }

}