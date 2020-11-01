package com.katevu.voxaudiobooks.ui

/**
 * Author: Kate Vu
 */
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.katevu.voxaudiobooks.R
import com.katevu.voxaudiobooks.models.BookParcel
import com.squareup.picasso.Picasso

private const val TAG = "BookListAdapter"
class BookListAdapter(var books: List<BookParcel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_item_book2, parent, false)
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
        return if (books.isNotEmpty()) {
            books[position]
        } else {
            null
        }
    }

    /**
     * BookListHolder
     */
    private inner class BookListHolder(view: View) : RecyclerView.ViewHolder(view) {
        private lateinit var book: BookParcel
        private val bookTitle = itemView.findViewById<TextView>(R.id.title)
        private val bookDes = itemView.findViewById<TextView>(R.id.description)
        private val bookTime = itemView.findViewById<TextView>(R.id.total_time)
        private val bookThumbnail = itemView.findViewById<ImageView>(R.id.thumbnail)
        private val bookFavourite = itemView.findViewById<ImageButton>(R.id.buttonFavourite)

        /**
         * Binding data
         */
        fun bind(book: BookParcel) {
            this.book = book
            bookTitle.text = this.book.title
            bookDes.text = this.book.description
            bookTime.text = this.book.pubDate

            val linkImage = URL_COVER_LARGE_PREFIX.plus(book.identifier)
//            Log.d(TAG, "Cover image: $linkImage")
            Picasso.get()
                .load(linkImage)
                .error(R.drawable.placeholder_image_icon_48dp)
                .placeholder(R.drawable.placeholder_image_icon_48dp)
                .into(bookThumbnail)

            if (this.book.isFavourite) {
                bookFavourite.setImageBitmap(BitmapFactory.decodeResource(itemView.context.resources, R.drawable.favourite))
            } else {
                bookFavourite.setImageBitmap(BitmapFactory.decodeResource(itemView.context.resources, R.drawable.nofavourite))
            }
        }
    }
}