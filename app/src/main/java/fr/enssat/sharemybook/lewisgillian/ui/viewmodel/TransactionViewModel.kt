package fr.enssat.sharemybook.lewisgillian.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.enssat.sharemybook.lewisgillian.R
import fr.enssat.sharemybook.lewisgillian.domain.model.User
import fr.enssat.sharemybook.lewisgillian.data.repository.BookRepository
import fr.enssat.sharemybook.lewisgillian.data.repository.TransactionData
import fr.enssat.sharemybook.lewisgillian.data.repository.TransactionRepository
import fr.enssat.sharemybook.lewisgillian.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.glxn.qrgen.android.QRCode

enum class TransactionAction {
    LOAN,
    RETURN
}

sealed class TransactionState {
    object Idle : TransactionState()
    object Initializing : TransactionState()
    data class WaitingForScan(val shareId: String, val qrBitmap: Bitmap) : TransactionState()
    object Confirming : TransactionState()
    data class Success(val transactionData: TransactionData) : TransactionState()
    data class Error(val message: String) : TransactionState()
}

data class TransactionUiState(
    val state: TransactionState = TransactionState.Idle,
    val action: TransactionAction? = null,
    val bookUid: String? = null
)

class TransactionViewModel(
    application: Application,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    private fun getString(resId: Int): String = getApplication<Application>().getString(resId)

    fun initLoan(bookUid: String) {
        viewModelScope.launch {
            _uiState.value = TransactionUiState(
                state = TransactionState.Initializing,
                action = TransactionAction.LOAN,
                bookUid = bookUid
            )

            try {
                val user = validateUserAndBook(bookUid, isLoan = true) ?: return@launch
                val book = bookRepository.getBookByUid(bookUid) ?: run {
                    setError(getString(R.string.error_book_not_found))
                    return@launch
                }

                val shareId = transactionRepository.initLoan(book, user)
                val qrBitmap = generateQRCode(shareId)

                _uiState.value = _uiState.value.copy(
                    state = TransactionState.WaitingForScan(shareId, qrBitmap)
                )

                startPolling(shareId, bookUid)
            } catch (e: Exception) {
                setError(e.message ?: getString(R.string.error_unknown))
            }
        }
    }

    fun initReturn(bookUid: String) {
        viewModelScope.launch {
            _uiState.value = TransactionUiState(
                state = TransactionState.Initializing,
                action = TransactionAction.RETURN,
                bookUid = bookUid
            )

            try {
                val user = validateUserAndBook(bookUid, isLoan = false) ?: return@launch
                val book = bookRepository.getBookByUid(bookUid) ?: run {
                    setError(getString(R.string.error_book_not_found))
                    return@launch
                }

                if (!book.isLent()) {
                    setError(getString(R.string.error_not_lent))
                    return@launch
                }

                val shareId = transactionRepository.initReturn(book, user)
                val qrBitmap = generateQRCode(shareId)

                _uiState.value = _uiState.value.copy(
                    state = TransactionState.WaitingForScan(shareId, qrBitmap)
                )

                startPolling(shareId, bookUid)
            } catch (e: Exception) {
                setError(e.message ?: getString(R.string.error_unknown))
            }
        }
    }

    fun acceptTransaction(shareId: String) {
        viewModelScope.launch {
            _uiState.value = TransactionUiState(state = TransactionState.Confirming)

            try {
                val user = userRepository.getCurrentUser()
                if (user == null) {
                    setError(getString(R.string.error_profile_required))
                    return@launch
                }

                if (!user.isValid()) {
                    setError(getString(R.string.error_profile_incomplete))
                    return@launch
                }

                val transactionData = transactionRepository.acceptTransaction(shareId, user)

                val ownerContact = User(
                    uid = transactionData.owner.uid,
                    fullName = transactionData.owner.fullName,
                    tel = transactionData.owner.tel,
                    email = transactionData.owner.email,
                    isCurrentUser = false
                )
                userRepository.saveContact(ownerContact)

                when (transactionData.action) {
                    "LOAN" -> {
                        bookRepository.addBorrowedBook(
                            uid = transactionData.book.uid,
                            isbn = transactionData.book.isbn,
                            title = transactionData.book.title,
                            authors = transactionData.book.authors,
                            coverUrl = transactionData.book.covers,
                            ownerUid = transactionData.owner.uid,
                            borrowerUid = user.uid
                        )
                    }
                    "RETURN" -> {
                        bookRepository.deleteBook(transactionData.book.uid)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    state = TransactionState.Success(transactionData)
                )
            } catch (e: Exception) {
                setError(e.message ?: getString(R.string.error_unknown))
            }
        }
    }

    private fun startPolling(shareId: String, bookUid: String) {
        viewModelScope.launch {
            try {
                val transactionData = transactionRepository.waitForAcceptance(shareId)

                if (transactionData.borrower != null) {
                    _uiState.value = _uiState.value.copy(state = TransactionState.Confirming)

                    val borrower = transactionData.borrower
                    val borrowerContact = User(
                        uid = borrower.uid,
                        fullName = borrower.fullName,
                        tel = borrower.tel,
                        email = borrower.email,
                        isCurrentUser = false
                    )
                    userRepository.saveContact(borrowerContact)

                    when (transactionData.action) {
                        "LOAN" -> bookRepository.markAsLent(bookUid, borrower.uid)
                        "RETURN" -> bookRepository.markAsReturned(bookUid)
                    }

                    _uiState.value = _uiState.value.copy(
                        state = TransactionState.Success(transactionData)
                    )
                } else {
                    setError(getString(R.string.error_transaction_borrower_missing))
                }
            } catch (e: Exception) {
                val errorMessage = if (e.message?.contains("Delai") == true) {
                    getString(R.string.error_timeout)
                } else {
                    e.message ?: getString(R.string.error_unknown)
                }
                setError(errorMessage)
            }
        }
    }

    private suspend fun validateUserAndBook(bookUid: String, isLoan: Boolean): User? {
        val user = userRepository.getCurrentUser()
        if (user == null) {
            setError(getString(R.string.error_profile_required))
            return null
        }

        if (!user.isValid()) {
            setError(getString(R.string.error_profile_incomplete))
            return null
        }

        val book = bookRepository.getBookByUid(bookUid)
        if (book == null) {
            setError(getString(R.string.error_book_not_found))
            return null
        }

        if (!book.isOwnedBy(user.uid)) {
            setError(getString(R.string.error_not_owner))
            return null
        }

        if (isLoan && book.isLent()) {
            setError(getString(R.string.error_already_lent))
            return null
        }

        return user
    }

    private fun generateQRCode(shareId: String): Bitmap {
        val qrContent = """{"shareId":"$shareId"}"""
        return QRCode.from(qrContent)
            .withSize(512, 512)
            .bitmap()
    }

    private fun setError(message: String) {
        _uiState.value = _uiState.value.copy(
            state = TransactionState.Error(message)
        )
    }

    fun reset() {
        _uiState.value = TransactionUiState()
    }
}
