package com.katevu.voxaudiobooks

import android.app.Application
import com.katevu.voxaudiobooks.databases.BookRepository

/**
 * Author: Kate Vu
 */
class VoxApplication: Application() {
        override fun onCreate() {
            super.onCreate()
            //init BookRepository
            BookRepository.initialize(this)
    }
}