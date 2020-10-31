package com.katevu.voxaudiobooks

import android.app.Application
import com.katevu.voxaudiobooks.databases.BookRepository

class VoxApplication: Application() {
        override fun onCreate() {
            super.onCreate()
            BookRepository.initialize(this)
    }
}