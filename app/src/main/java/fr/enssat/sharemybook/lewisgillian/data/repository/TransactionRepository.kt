package fr.enssat.sharemybook.lewisgillian.data.repository

import android.util.Log
import fr.enssat.sharemybook.lewisgillian.data.remote.api.ShareMyBookApiService
import fr.enssat.sharemybook.lewisgillian.data.remote.dto.TransactionAcceptRequest
import fr.enssat.sharemybook.lewisgillian.data.remote.dto.TransactionBook
import fr.enssat.sharemybook.lewisgillian.data.remote.dto.TransactionInitRequest
import fr.enssat.sharemybook.lewisgillian.data.remote.dto.TransactionUser
import fr.enssat.sharemybook.lewisgillian.domain.model.Book
import fr.enssat.sharemybook.lewisgillian.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class TransactionRepository(
    private val shareMyBookApi: ShareMyBookApiService
) {

    suspend fun initLoan(book: Book, owner: User): String = withContext(Dispatchers.IO) {
        Log.d("TransactionRepo", "Init loan: ${book.title}")

        val request = TransactionInitRequest(
            action = "LOAN",
            book = TransactionBook(
                uid = book.uid,
                isbn = book.isbn,
                title = book.title,
                authors = book.authors,
                covers = book.coverUrl
            ),
            owner = TransactionUser(
                uid = owner.uid,
                fullName = owner.fullName,
                tel = owner.tel,
                email = owner.email
            )
        )

        val response = shareMyBookApi.initTransaction(request)

        if (response.isSuccessful && response.body() != null) {
            val shareId = response.body()!!.shareId
            Log.d("TransactionRepo", "ShareId: $shareId")
            shareId
        } else {
            throw Exception("Erreur serveur: ${response.code()}")
        }
    }

    suspend fun initReturn(book: Book, owner: User): String = withContext(Dispatchers.IO) {
        Log.d("TransactionRepo", "Init return: ${book.title}")

        val request = TransactionInitRequest(
            action = "RETURN",
            book = TransactionBook(
                uid = book.uid,
                isbn = book.isbn,
                title = book.title,
                authors = book.authors,
                covers = book.coverUrl
            ),
            owner = TransactionUser(
                uid = owner.uid,
                fullName = owner.fullName,
                tel = owner.tel,
                email = owner.email
            )
        )

        val response = shareMyBookApi.initTransaction(request)

        if (response.isSuccessful && response.body() != null) {
            val shareId = response.body()!!.shareId
            Log.d("TransactionRepo", "ShareId: $shareId")
            shareId
        } else {
            throw Exception("Erreur serveur: ${response.code()}")
        }
    }

    suspend fun acceptTransaction(shareId: String, borrower: User): TransactionData =
        withContext(Dispatchers.IO) {
            Log.d("TransactionRepo", "Accept transaction: $shareId par ${borrower.fullName}")

            val request = TransactionAcceptRequest(
                borrower = TransactionUser(
                    uid = borrower.uid,
                    fullName = borrower.fullName,
                    tel = borrower.tel,
                    email = borrower.email
                )
            )

            Log.d("TransactionRepo", "Envoi accept request...")
            val response = shareMyBookApi.acceptTransaction(shareId, request)

            Log.d("TransactionRepo", "Reponse accept: code=${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val transactionResponse = response.body()!!

                Log.d("TransactionRepo", "Accept reussi: action=${transactionResponse.action}")

                TransactionData(
                    action = transactionResponse.action,
                    book = TransactionBookData(
                        uid = transactionResponse.book.uid,
                        isbn = transactionResponse.book.isbn,
                        title = transactionResponse.book.title,
                        authors = transactionResponse.book.authors,
                        covers = transactionResponse.book.covers
                    ),
                    owner = TransactionUserData(
                        uid = transactionResponse.owner.uid,
                        fullName = transactionResponse.owner.fullName,
                        tel = transactionResponse.owner.tel,
                        email = transactionResponse.owner.email
                    ),
                    borrower = TransactionUserData(
                        uid = borrower.uid,
                        fullName = borrower.fullName,
                        tel = borrower.tel,
                        email = borrower.email
                    )
                )
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("TransactionRepo", "Erreur accept: code=${response.code()}, error=$errorBody")
                throw Exception("Erreur serveur: ${response.code()} - $errorBody")
            }
        }

    suspend fun getTransactionResult(shareId: String): TransactionData =
        withContext(Dispatchers.IO) {
            Log.d("TransactionRepo", "Get result: $shareId")
            val response = shareMyBookApi.getTransactionResult(shareId)

            Log.d("TransactionRepo", "Reponse HTTP: code=${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val transactionResponse = response.body()!!

                Log.d("TransactionRepo", "Transaction: action=${transactionResponse.action}, borrower=${transactionResponse.borrower?.fullName ?: "null"}")

                TransactionData(
                    action = transactionResponse.action,
                    book = TransactionBookData(
                        uid = transactionResponse.book.uid,
                        isbn = transactionResponse.book.isbn,
                        title = transactionResponse.book.title,
                        authors = transactionResponse.book.authors,
                        covers = transactionResponse.book.covers
                    ),
                    owner = TransactionUserData(
                        uid = transactionResponse.owner.uid,
                        fullName = transactionResponse.owner.fullName,
                        tel = transactionResponse.owner.tel,
                        email = transactionResponse.owner.email
                    ),
                    borrower = transactionResponse.borrower?.let {
                        Log.d("TransactionRepo", "Emprunteur detecte: ${it.fullName}")
                        TransactionUserData(
                            uid = it.uid,
                            fullName = it.fullName,
                            tel = it.tel,
                            email = it.email
                        )
                    }
                )
            } else {
                Log.e("TransactionRepo", "Erreur serveur: code=${response.code()}")
                throw Exception("Erreur serveur: ${response.code()}")
            }
        }

    suspend fun waitForAcceptance(
        shareId: String,
        maxAttempts: Int = 60,
        delayMs: Long = 2000
    ): TransactionData = withContext(Dispatchers.IO) {
        var lastValidData: TransactionData? = null
        var hadSuccessfulResponse = false

        repeat(maxAttempts) { attempt ->
            Log.d("TransactionRepo", "Verification ${attempt + 1}/$maxAttempts...")

            try {
                val data = getTransactionResult(shareId)
                hadSuccessfulResponse = true
                lastValidData = data

                if (data.borrower != null) {
                    Log.d("TransactionRepo", "Transaction acceptee!")
                    return@withContext data
                }
            } catch (e: Exception) {
                if (hadSuccessfulResponse && e.message?.contains("404") == true) {
                    Log.d("TransactionRepo", "Transaction acceptee (404 apres 200)")
                    lastValidData?.let { data ->
                        return@withContext data
                    }
                }
                Log.w("TransactionRepo", "Tentative echouee: ${e.message}")
            }

            delay(delayMs)
        }

        throw Exception("Delai d'attente depasse (2 min). Assurez-vous que l'emprunteur a bien scanne le QR code et que les deux telephones sont connectes a Internet.")
    }
}

data class TransactionData(
    val action: String,
    val book: TransactionBookData,
    val owner: TransactionUserData,
    val borrower: TransactionUserData?
)

data class TransactionBookData(
    val uid: String,
    val isbn: String,
    val title: String,
    val authors: String,
    val covers: String?
)

data class TransactionUserData(
    val uid: String,
    val fullName: String,
    val tel: String,
    val email: String
)
