package com.example.dz1

import android.app.Application

class NotesApplication: Application() {
    companion object{
        var instance: NotesApplication? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}