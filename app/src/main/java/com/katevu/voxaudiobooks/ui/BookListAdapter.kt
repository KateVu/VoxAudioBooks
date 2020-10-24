package com.katevu.voxaudiobooks.ui

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
                book.id,
                book.title,
                book.description,
                book.link,
                book.urlDetails,
                book.identifier,
                book.num_sections.toInt(),
                book.authors
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
            bookTime.text = this.book.totaltime.plus("s")

            val linkImage = BASE_URL_IMAGE.plus("?identifier=").plus(book.identifier)

            Picasso.get()
                .load(linkImage)
                .error(R.drawable.placeholder_image_icon_48dp)
                .placeholder(R.drawable.placeholder_image_icon_48dp)
                .into(bookThumbnail)
        }
    }
}