package com.example.mylibrary.ui

import com.example.mylibrary.data.Book
import com.example.mylibrary.data.BookDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory stand-in for the Room DAO. The real SQL is covered by BookDaoTest. */
class FakeBookDao : BookDao {

    val books = MutableStateFlow<List<Book>>(emptyList())
    private var nextId = 1L

    override fun search(query: String): Flow<List<Book>> = books.map { list ->
        list.filter { it.title.contains(query, ignoreCase = true) || it.author.contains(query, ignoreCase = true) }
    }

    override suspend fun insert(book: Book): Long {
        val id = nextId++
        books.update { it + book.copy(id = id) }
        return id
    }

    override suspend fun update(book: Book) {
        books.update { list -> list.map { if (it.id == book.id) book else it } }
    }

    override suspend fun delete(book: Book) {
        books.update { list -> list.filterNot { it.id == book.id } }
    }
}
