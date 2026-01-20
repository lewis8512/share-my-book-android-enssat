package fr.enssat.sharemybook.lewisgillian.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import fr.enssat.sharemybook.lewisgillian.R
import fr.enssat.sharemybook.lewisgillian.ui.components.BookCard
import fr.enssat.sharemybook.lewisgillian.ui.components.EmptyState
import fr.enssat.sharemybook.lewisgillian.ui.components.ErrorDialog
import fr.enssat.sharemybook.lewisgillian.ui.components.LoadingIndicator
import fr.enssat.sharemybook.lewisgillian.ui.viewmodel.BorrowsViewModel

@Composable
fun BorrowsScreen(
    viewModel: BorrowsViewModel,
    onBookClick: (String) -> Unit,
    onScanBorrow: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onScanBorrow) {
                Icon(Icons.Outlined.QrCodeScanner, contentDescription = stringResource(R.string.scan_loan))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                uiState.borrowedBooks.isEmpty() -> {
                    EmptyState(
                        message = stringResource(R.string.empty_borrows)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = uiState.borrowedBooks,
                            key = { book -> book.uid }
                        ) { book ->
                            BookCard(
                                book = book,
                                onClick = { onBookClick(book.uid) },
                                onDeleteClick = null,
                                showActions = false,
                                currentUserUid = uiState.currentUserUid
                            )
                        }
                    }
                }
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
