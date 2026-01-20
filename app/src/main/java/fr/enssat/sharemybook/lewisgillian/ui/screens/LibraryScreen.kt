package fr.enssat.sharemybook.lewisgillian.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.enssat.sharemybook.lewisgillian.R
import fr.enssat.sharemybook.lewisgillian.domain.model.Book
import fr.enssat.sharemybook.lewisgillian.ui.ScannerActivity
import fr.enssat.sharemybook.lewisgillian.ui.components.BookCard
import fr.enssat.sharemybook.lewisgillian.ui.components.EmptyState
import fr.enssat.sharemybook.lewisgillian.ui.components.ErrorDialog
import fr.enssat.sharemybook.lewisgillian.ui.components.LoadingIndicator
import fr.enssat.sharemybook.lewisgillian.ui.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onBookClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var bookToDelete by remember { mutableStateOf<Book?>(null) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val isbn = result.data?.getStringExtra(ScannerActivity.SCAN_RESULT_KEY)
            val scanType = result.data?.getStringExtra(ScannerActivity.SCAN_TYPE_KEY)
            
            if (isbn != null && scanType == ScannerActivity.SCAN_TYPE_ISBN) {
                viewModel.searchBookByIsbn(isbn)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, ScannerActivity::class.java)
                    scannerLauncher.launch(intent)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_book))
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                uiState.books.isEmpty() -> {
                    EmptyState(
                        message = stringResource(R.string.empty_library)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = uiState.books,
                            key = { book -> book.uid }
                        ) { book ->
                            BookCard(
                                book = book,
                                onClick = { onBookClick(book.uid) },
                                onDeleteClick = { bookToDelete = book },
                                currentUserUid = uiState.currentUserUid
                            )
                        }
                    }
                }
            }

            if (uiState.showAddDialog && uiState.searchedBook != null) {
                val book = uiState.searchedBook!!
                AlertDialog(
                    onDismissRequest = { viewModel.dismissAddDialog() },
                    title = { Text(stringResource(R.string.add_this_book)) },
                    text = {
                        Column {
                            Text(stringResource(R.string.title_label, book.title))
                            Text(stringResource(R.string.authors_label, book.authors))
                            Text(stringResource(R.string.isbn_label, book.isbn))
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.addSearchedBook() }) {
                            Text(stringResource(R.string.add))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissAddDialog() }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            if (uiState.showManualEntryDialog) {
                var title by remember { mutableStateOf("") }
                var authors by remember { mutableStateOf("") }
                var isbn by remember { mutableStateOf(uiState.scannedIsbn ?: "") }

                AlertDialog(
                    onDismissRequest = { viewModel.dismissManualEntryDialog() },
                    title = { Text(stringResource(R.string.book_not_found)) },
                    text = {
                        Column {
                            Text(
                                text = stringResource(R.string.manual_entry_message),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text(stringResource(R.string.title_required)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = authors,
                                onValueChange = { authors = it },
                                label = { Text(stringResource(R.string.authors)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = isbn,
                                onValueChange = { isbn = it },
                                label = { Text(stringResource(R.string.isbn)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.addManualBook(title, authors, isbn) },
                            enabled = title.isNotBlank()
                        ) {
                            Text(stringResource(R.string.add))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissManualEntryDialog() }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            if (uiState.showDuplicateConfirmDialog && uiState.searchedBook != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDuplicateConfirmDialog() },
                    title = { Text(stringResource(R.string.book_already_exists)) },
                    text = {
                        Text(stringResource(R.string.duplicate_book_message, uiState.searchedBook!!.title))
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.confirmAddDuplicate() }) {
                            Text(stringResource(R.string.add))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissDuplicateConfirmDialog() }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            if (bookToDelete != null) {
                AlertDialog(
                    onDismissRequest = { bookToDelete = null },
                    title = { Text(stringResource(R.string.delete_book_title)) },
                    text = {
                        Text(stringResource(R.string.delete_book_message, bookToDelete!!.title))
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteBook(bookToDelete!!.uid)
                                bookToDelete = null
                            }
                        ) {
                            Text(stringResource(R.string.delete))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { bookToDelete = null }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            if (uiState.error != null) {
                ErrorDialog(
                    message = uiState.error!!,
                    onDismiss = { viewModel.clearError() }
                )
            }
        }
    }
}
