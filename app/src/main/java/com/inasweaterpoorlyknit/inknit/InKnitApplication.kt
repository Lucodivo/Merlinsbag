package com.inasweaterpoorlyknit.inknit

import android.app.Application
import androidx.room.Room
import com.inasweaterpoorlyknit.inknit.database.model.AppDatabase

class InKnitApplication : Application(){
    // TODO: Inject into ViewModels using Hilt?
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "inknit-db"
        ).build()
    }
}