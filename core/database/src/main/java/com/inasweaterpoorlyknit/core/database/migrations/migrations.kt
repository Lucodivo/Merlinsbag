package com.inasweaterpoorlyknit.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
  override fun migrate(db: SupportSQLiteDatabase) {
    TODO("Not yet implemented")
    //db.execSQL("ALTER TABLE Book ADD COLUMN pub_year INTEGER")
  }
}

//Room.databaseBuilder(applicationContext, MyDb::class.java, "database-name")
//.addMigrations(MIGRATION_1_2).build()
