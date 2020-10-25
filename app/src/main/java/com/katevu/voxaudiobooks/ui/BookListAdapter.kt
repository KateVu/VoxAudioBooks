package com.katevu.voxaudiobooks.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.katevu.voxaudiobooks.R
import com.katevu.voxaudiobooks.models.Book
import com.katevu.voxaudiobooks.models.BookParcel
import com.squareup.picasso.Picasso

private const val TAG = "BookListAdapter"
class BookListAdapter(var books: List<Book>) :
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

    fun getBook(position: Int): BookParcel? {
        if (books.isNotEmpty()) {
            val book = books[position]
            return BookParcel(
                book.guid,
                book.title,
                book.description,
                book.link,
                book.pubDate,
                book?.creator,
                book.identifier,
                book?.runtime,
                book.totalTracks,
                book.tracks
            )
        } else {
            return null
        }
    }

    /**
     * BookListHolder
     */
    private inner class BookListHolder(view: View) : RecyclerView.ViewHolder(view) {
        private lateinit var book: Book
        private val bookTitle = itemView.findViewById<TextView>(R.id.title)
        private val bookDes = itemView.findViewById<TextView>(R.id.description)
        private val bookTime = itemView.findViewById<TextView>(R.id.total_time)
        private val bookThumbnail = itemView.findViewById<ImageView>(R.id.thumbnail)

        /**
         * Binding data
         */
        fun bind(book: Book) {
            this.book = book
            bookTitle.text = this.book.title
            bookDes.text = this.book.description
            bookTime.text = this.book.pubDate

            val linkImage = URL_COVER_LARGE_PREFIX.plus(book.identifier)
            Log.d(TAG, "Cover image: $linkImage")
            Picasso.get()
                .load(linkImage)
                .error(R.drawable.placeholder_image_icon_48dp)
                .placeholder(R.drawable.placeholder_image_icon_48dp)
                .into(bookThumbnail)
        }
    }
}