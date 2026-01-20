package fr.enssat.sharemybook.lewisgillian.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class BorrowsUiState(
    val borrowedBooks: List<Book> = emptyList(),
    val currentUserUid: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class BorrowsViewModel(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BorrowsUiState())
    val uiState: StateFlow<BorrowsUiState> = _uiState.asStateFlow()

    init {
        loadBorrowedBooks()
    }

    private fun loadBorrowedBooks() {
        viewModelScope.launch {
            userRepository.observeCurrentUser()
                .filterNotNull()
                .flatMapLatest { user ->
                    _uiState.value = _uiState.value.copy(currentUserUid = user.uid)
                    bookRepository.getBorrowedBooks(user.uid)
                }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        error = "Erreur de chargement: ${e.message}",
                        isLoading = false
                    )
                }
                .collectLatest { books ->
                    _uiState.value = _uiState.value.copy(
                        borrowedBooks = books,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
