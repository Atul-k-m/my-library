package com.example.mylibrary.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class BookDaoTest {

    private lateinit var db: LibraryDatabase
    private lateinit var dao: BookDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LibraryDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.bookDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun emptyQueryReturnsAllBooksSortedByTitleIgnoringCase() = runTest {
        // A case-sensitive sort would put "anathem" last.
        dao.insert(Book(title = "Dune"))
        dao.insert(Book(title = "anathem"))
        dao.insert(Book(title = "Brave New World"))

        val titles = dao.search("").first().map { it.title }

        assertEquals(listOf("anathem", "Brave New World", "Dune"), titles)
    }

    @Test
    fun searchMatchesTitleOrAuthorIgnoringCase() = runTest {
        dao.insert(Book(title = "Dune", author = "Frank Herbert"))
        dao.insert(Book(title = "Emma", author = "Jane Austen"))
        dao.insert(Book(title = "The Hobbit", author = "J.R.R. Tolkien"))

        assertEquals(listOf("Dune"), dao.search("dUN").first().map { it.title })
        assertEquals(listOf("Emma"), dao.search("austen").first().map { it.title })
        assertTrue(dao.search("zzz").first().isEmpty())
    }

    @Test
    fun updateStoresStatusAndLoanAndDeleteRemoves() = runTest {
        val id = dao.insert(Book(title = "Dune"))
        val lent = Book(
            id = id,
            title = "Dune",
            status = ReadingStatus.READING,
            lentTo = "Sam",
            lentOn = LocalDate.of(2026, 10, 12),
        )

        dao.update(lent)
        assertEquals(listOf(lent), dao.search("").first())

        dao.delete(lent)
        assertTrue(dao.search("").first().isEmpty())
    }
}
