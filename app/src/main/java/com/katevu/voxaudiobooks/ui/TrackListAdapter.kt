package com.katevu.voxaudiobooks.ui

/**
 * Author: Kate Vu
 */
import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.katevu.voxaudiobooks.R
import com.katevu.voxaudiobooks.models.MediaFile
import com.katevu.voxaudiobooks.utils.AudioState

class TrackListAdapter(var tracks: List<MediaFile>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_item_track2, parent, false)
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

    fun getTrack(position: Int): MediaFile? {
        return if (tracks.isNotEmpty()) tracks[position] else null
    }

    /**
     * TrackListHolder
     */
    private inner class TrackListHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val playButton = itemView.findViewById<ImageButton>(R.id.playButton)
        private val trackTitle = itemView.findViewById<TextView>(R.id.track_title)
        private val trackLength = itemView.findViewById<TextView>(R.id.track_length)
        //private val trackSize = itemView.findViewById<TextView>(R.id.track_size)

        private lateinit var track: MediaFile

        /**
         * Binding data
         */
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun bind(track: MediaFile) {
            this.track = track
            when (track.playbackState) {
                AudioState().IDLE ->
                    playButton.setImageBitmap(
                        BitmapFactory.decodeResource(
                            itemView.context.resources,
                            R.drawable.audiobook
                        )
                    )
                AudioState().PAUSE ->
                    playButton.setImageBitmap(
                        BitmapFactory.decodeResource(
                            itemView.context.resources,
                            R.drawable.icon_play
                        )
                    )
                AudioState().PLAYING ->
                    playButton.setImageBitmap(
                        BitmapFactory.decodeResource(
                            itemView.context.resources,
                            R.drawable.icon_pause
                        )
                    )

                else -> playButton.setImageBitmap(
                    BitmapFactory.decodeResource(
                        itemView.context.resources,
                        R.drawable.audiobook
                    )
                )

            }
            trackTitle.text = track.title
            trackLength.text =
                getDurationString(track.length).plus("    ").plus(getSize(track.size))
        }
    }

    fun getDurationString(duration: String): String {
//        Log.d(TAG, ".getDurationString called")
        return try {
            val durationValue = duration.toDouble().toLong()
            val hours = durationValue.toInt() / 3600
            var remainder = durationValue.toInt() - hours * 3600
            val mins = remainder / 60
            remainder -= mins * 60
            val secs = remainder

            when {
                (hours != 0) -> hours.toString().plus(":").plus(mins.toString()).plus(":").plus(
                    secs.toString()
                ).plus("s")

                (mins != 0) -> mins.toString().plus(":").plus(secs.toString()).plus("s")

                else -> secs.toString().plus("s")
            }
        } catch (e: NumberFormatException) {
//            Log.d(TAG, ".getDurationString catch error")
            duration.plus("s")
        }
    }

    fun getSize(size: String): String {
//        Log.d(TAG, ".getDurationString called")
        return try {
            val sizeValue = size.toDouble() * 0.000001
            val number2digites = Math.round(sizeValue * 100.0) / 100.0
            number2digites.toString().plus("M")
        } catch (e: NumberFormatException) {
            //            Log.d(TAG, ".getSize catch error")
            size.plus("M")
        }
    }
}
