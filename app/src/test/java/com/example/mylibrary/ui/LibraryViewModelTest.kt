package com.example.mylibrary.ui

import com.example.mylibrary.data.Book
import com.example.mylibrary.data.ReadingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val today = LocalDate.of(2026, 10, 18)
    private val dao = FakeBookDao()
    private lateinit var viewModel: LibraryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = LibraryViewModel(dao, today = { today })
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun saveRejectsBlankTitle() {
        assertFalse(viewModel.save(Book(title = "   ")))
        assertTrue(dao.books.value.isEmpty())
    }

    @Test
    fun saveTrimsAndInsertsNewBook() {
        assertTrue(viewModel.save(Book(title = "  Dune ", author = " Frank Herbert ")))

        val saved = dao.books.value.single()
        assertEquals("Dune", saved.title)
        assertEquals("Frank Herbert", saved.author)
        assertEquals(ReadingStatus.TO_READ, saved.status)
    }

    @Test
    fun saveUpdatesExistingBook() {
        viewModel.save(Book(title = "Dune"))
        val existing = dao.books.value.single()

        viewModel.save(existing.copy(status = ReadingStatus.READ))

        assertEquals(listOf(existing.copy(status = ReadingStatus.READ)), dao.books.value)
    }

    @Test
    fun lendRecordsBorrowerAndToday() {
        viewModel.save(Book(title = "Dune"))

        assertTrue(viewModel.lend(dao.books.value.single(), "  Sam "))

        val lent = dao.books.value.single()
        assertEquals("Sam", lent.lentTo)
        assertEquals(today, lent.lentOn)
    }

    @Test
    fun lendRejectsBlankBorrower() {
        viewModel.save(Book(title = "Dune"))

        assertFalse(viewModel.lend(dao.books.value.single(), " "))
        assertNull(dao.books.value.single().lentTo)
    }

    @Test
    fun markReturnedClearsLoan() {
        viewModel.save(Book(title = "Dune"))
        viewModel.lend(dao.books.value.single(), "Sam")

        viewModel.markReturned(dao.books.value.single())

        val returned = dao.books.value.single()
        assertNull(returned.lentTo)
        assertNull(returned.lentOn)
    }

    @Test
    fun deleteRemovesBook() {
        viewModel.save(Book(title = "Dune"))

        viewModel.delete(dao.books.value.single())

        assertTrue(dao.books.value.isEmpty())
    }

    @Test
    fun booksFollowSearchQuery() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.books.collect {} }
        viewModel.save(Book(title = "Dune", author = "Frank Herbert"))
        viewModel.save(Book(title = "Emma", author = "Jane Austen"))
        assertEquals(2, viewModel.books.value?.size)

        viewModel.onQueryChange("austen")

        assertEquals("austen", viewModel.query.value)
        assertEquals(listOf("Emma"), viewModel.books.value?.map { it.title })
    }
}
