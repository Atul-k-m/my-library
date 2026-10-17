package com.example.mylibrary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mylibrary.data.Book
import com.example.mylibrary.data.ReadingStatus
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
private val capitalizeWords = KeyboardOptions(capitalization = KeyboardCapitalization.Words)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(viewModel: LibraryViewModel = viewModel(factory = LibraryViewModel.Factory)) {
    val books by viewModel.books.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    // Which dialog is open, if any. Editing a Book with id 0 means adding a new one.
    var editing by remember { mutableStateOf<Book?>(null) }
    var lending by remember { mutableStateOf<Book?>(null) }
    var deleting by remember { mutableStateOf<Book?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Library") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = Book(title = "") }) {
                Icon(Icons.Default.Add, contentDescription = "Add book")
            }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search title or author") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
            )
            val list = books
            when {
                list == null -> Unit // still loading
                list.isEmpty() -> EmptyMessage(
                    if (query.isBlank()) "No books yet — tap + to add one" else "No matches"
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(list, key = { it.id }) { book ->
                        BookRow(
                            book = book,
                            onClick = { editing = book },
                            onLend = { lending = book },
                            onReturn = { viewModel.markReturned(book) },
                        )
                    }
                }
            }
        }
    }

    editing?.let { book ->
        BookDialog(
            book = book,
            onDismiss = { editing = null },
            onSave = { if (viewModel.save(it)) editing = null },
            onDelete = { editing = null; deleting = book }.takeIf { book.id != 0L },
        )
    }
    lending?.let { book ->
        LendDialog(
            book = book,
            onDismiss = { lending = null },
            onLend = { name -> if (viewModel.lend(book, name)) lending = null },
        )
    }
    deleting?.let { book ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete “${book.title}”?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(book); deleting = null }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BookRow(book: Book, onClick: () -> Unit, onLend: () -> Unit, onReturn: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(book.title, style = MaterialTheme.typography.titleMedium)
                if (book.author.isNotEmpty()) {
                    Text(
                        book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusTag(book.status)
                if (book.lentTo != null) {
                    val since = book.lentOn?.let { " · ${it.format(dateFormat)}" }.orEmpty()
                    Text(
                        "Lent to ${book.lentTo}$since",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
            if (book.isLent) {
                TextButton(onClick = onReturn) { Text("Return") }
            } else {
                TextButton(onClick = onLend) { Text("Lend") }
            }
        }
    }
}

@Composable
private fun StatusTag(status: ReadingStatus) {
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
        Text(
            status.label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun BookDialog(
    book: Book,
    onDismiss: () -> Unit,
    onSave: (Book) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var status by remember { mutableStateOf(book.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (book.id == 0L) "Add book" else "Edit book") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    keyboardOptions = capitalizeWords,
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    singleLine = true,
                    keyboardOptions = capitalizeWords,
                )
                Column(Modifier.selectableGroup()) {
                    ReadingStatus.entries.forEach { option ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = status == option,
                                    onClick = { status = option },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = status == option, onClick = null)
                            Text(option.label, Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(book.copy(title = title, author = author, status = status)) },
                enabled = title.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}

@Composable
private fun LendDialog(book: Book, onDismiss: () -> Unit, onLend: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lend “${book.title}”") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Borrower's name") },
                singleLine = true,
                keyboardOptions = capitalizeWords,
            )
        },
        confirmButton = {
            TextButton(onClick = { onLend(name) }, enabled = name.isNotBlank()) { Text("Lend") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
