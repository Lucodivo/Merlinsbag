package com.inasweaterpoorlyknit.core.database

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.dao.PurgeDatabaseDao
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
open class DatabaseTests {
  lateinit var db: NoopDatabase
  lateinit var articleDao: ArticleDao
  lateinit var ensembleDao: EnsembleDao
  lateinit var purgeDatabaseDao: PurgeDatabaseDao

  @Before
  fun beforeEach(){
    db = Room.inMemoryDatabaseBuilder(
      InstrumentationRegistry.getInstrumentation().targetContext,
      NoopDatabase::class.java
    ).allowMainThreadQueries()
        .build()
    articleDao = db.ArticleDao()
    ensembleDao = db.EnsembleDao()
    purgeDatabaseDao = PurgeDatabaseDao(db)
  }

  @After fun afterEach() = db.close()
}