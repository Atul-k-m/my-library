package com.example.mylibrary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mylibrary.data.Book
import com.example.mylibrary.data.BookDao
import com.example.mylibrary.data.LibraryDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class LibraryViewModel(
    private val dao: BookDao,
    private val today: () -> LocalDate = LocalDate::now,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** Books matching [query]; null until the first load finishes. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val books: StateFlow<List<Book>?> = _query
        .flatMapLatest { dao.search(it.trim()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onQueryChange(value: String) {
        _query.value = value
    }

    /** Inserts a new book (id 0) or updates an existing one. Returns false, saving nothing, if the title is blank. */
    fun save(book: Book): Boolean {
        val cleaned = book.copy(title = book.title.trim(), author = book.author.trim())
        if (cleaned.title.isEmpty()) return false
        viewModelScope.launch {
            if (cleaned.id == 0L) dao.insert(cleaned) else dao.update(cleaned)
        }
        return true
    }

    fun delete(book: Book) {
        viewModelScope.launch { dao.delete(book) }
    }

    /** Returns false, changing nothing, if [borrower] is blank. */
    fun lend(book: Book, borrower: String): Boolean {
        val name = borrower.trim()
        if (name.isEmpty()) return false
        viewModelScope.launch { dao.update(book.copy(lentTo = name, lentOn = today())) }
        return true
    }

    fun markReturned(book: Book) {
        viewModelScope.launch { dao.update(book.copy(lentTo = null, lentOn = null)) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                LibraryViewModel(LibraryDatabase.get(checkNotNull(this[APPLICATION_KEY])).bookDao())
            }
        }
    }
}
