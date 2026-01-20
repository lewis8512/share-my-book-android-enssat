package fr.enssat.sharemybook.lewisgillian.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.enssat.sharemybook.lewisgillian.R
import fr.enssat.sharemybook.lewisgillian.domain.model.Book
import fr.enssat.sharemybook.lewisgillian.data.repository.BookRepository
import fr.enssat.sharemybook.lewisgillian.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

data class LibraryUiState(
    val books: List<Book> = emptyList(),
    val currentUserUid: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchedBook: Book? = null,
    val showAddDialog: Boolean = false,
    val showManualEntryDialog: Boolean = false,
    val scannedIsbn: String? = null,
    val showDuplicateConfirmDialog: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    application: Application,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private fun getString(resId: Int): String = getApplication<Application>().getString(resId)
    private fun getString(resId: Int, vararg args: Any): String = getApplication<Application>().getString(resId, *args)

    init {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            userRepository.observeCurrentUser()
                .filterNotNull()
                .flatMapLatest { user ->
                    _uiState.value = _uiState.value.copy(currentUserUid = user.uid)
                    bookRepository.getAllUserBooks(user.uid)
                }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        error = getString(R.string.error_loading, e.message ?: ""),
                        isLoading = false
                    )
                }
                .collectLatest { books ->
                    _uiState.value = _uiState.value.copy(
                        books = books,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun searchBookByIsbn(isbn: String) {
        if (isbn.isBlank()) {
            _uiState.value = _uiState.value.copy(error = getString(R.string.error_isbn_empty))
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val user = userRepository.getCurrentUser()
                if (user == null) {
                    _uiState.value = _uiState.value.copy(
                        error = getString(R.string.error_profile_required),
                        isLoading = false
                    )
                    return@launch
                }

                val bookExists = bookRepository.bookExistsForIsbn(isbn, user.uid)
                val book = bookRepository.searchBookByIsbn(isbn, user.uid)

                if (book != null) {
                    _uiState.value = _uiState.value.copy(
                        searchedBook = book,
                        showAddDialog = !bookExists,
                        showDuplicateConfirmDialog = bookExists,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        scannedIsbn = isbn,
                        showManualEntryDialog = true,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    scannedIsbn = isbn,
                    showManualEntryDialog = true,
                    isLoading = false
                )
            }
        }
    }

    fun addSearchedBook() {
        val book = _uiState.value.searchedBook ?: return

        if (book.isbn.isBlank() || book.title.isBlank() || book.ownerUuid.isBlank()) {
            _uiState.value = _uiState.value.copy(error = getString(R.string.error_book_info_incomplete))
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                bookRepository.addBook(book)
                _uiState.value = _uiState.value.copy(
                    searchedBook = null,
                    showAddDialog = false,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: getString(R.string.error_unknown),
                    isLoading = false
                )
            }
        }
    }

    fun deleteBook(bookUid: String) {
        if (bookUid.isBlank()) return

        viewModelScope.launch {
            try {
                bookRepository.deleteBook(bookUid)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: getString(R.string.error_unknown)
                )
            }
        }
    }

    fun dismissAddDialog() {
        _uiState.value = _uiState.value.copy(
            showAddDialog = false,
            searchedBook = null
        )
    }

    fun dismissDuplicateConfirmDialog() {
        _uiState.value = _uiState.value.copy(
            showDuplicateConfirmDialog = false,
            searchedBook = null
        )
    }

    fun confirmAddDuplicate() {
        _uiState.value = _uiState.value.copy(showDuplicateConfirmDialog = false)
        addSearchedBook()
    }

    fun dismissManualEntryDialog() {
        _uiState.value = _uiState.value.copy(
            showManualEntryDialog = false,
            scannedIsbn = null
        )
    }

    fun addManualBook(title: String, authors: String, isbn: String) {
        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(error = getString(R.string.error_title_required))
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val user = userRepository.getCurrentUser()
                if (user == null) {
                    _uiState.value = _uiState.value.copy(
                        error = getString(R.string.error_profile_required),
                        isLoading = false
                    )
                    return@launch
                }

                val book = Book(
                    uid = java.util.UUID.randomUUID().toString(),
                    isbn = isbn.ifBlank { "MANUAL-${System.currentTimeMillis()}" },
                    title = title.trim(),
                    authors = authors.trim().ifBlank { "Auteur inconnu" },
                    coverUrl = null,
                    ownerUuid = user.uid,
                    borrowerUuid = null,
                    createdAt = System.currentTimeMillis()
                )

                bookRepository.addBook(book)
                _uiState.value = _uiState.value.copy(
                    showManualEntryDialog = false,
                    scannedIsbn = null,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: getString(R.string.error_unknown),
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
