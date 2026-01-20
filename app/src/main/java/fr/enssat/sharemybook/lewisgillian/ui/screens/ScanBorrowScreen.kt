package fr.enssat.sharemybook.lewisgillian.ui.screens

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.enssat.sharemybook.lewisgillian.R
import fr.enssat.sharemybook.lewisgillian.ui.ScannerActivity
import fr.enssat.sharemybook.lewisgillian.ui.viewmodel.TransactionState
import fr.enssat.sharemybook.lewisgillian.ui.viewmodel.TransactionViewModel
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanBorrowScreen(
    viewModel: TransactionViewModel,
    onNavigateBack: () -> Unit,
    onTransactionComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val qrContent = result.data?.getStringExtra(ScannerActivity.SCAN_RESULT_KEY)
            val scanType = result.data?.getStringExtra(ScannerActivity.SCAN_TYPE_KEY)
            
            if (qrContent != null && scanType == ScannerActivity.SCAN_TYPE_QR) {
                try {
                    val jsonObject = JSONObject(qrContent)
                    val shareId = jsonObject.getString("shareId")
                    viewModel.acceptTransaction(shareId)
                } catch (e: Exception) {
                    viewModel.acceptTransaction(qrContent)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.borrow_book)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = uiState.state) {
                is TransactionState.Idle -> {
                    Icon(
                        imageVector = Icons.Outlined.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.scan_lender_qr),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.scan_lender_instructions),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val intent = Intent(context, ScannerActivity::class.java)
                            scannerLauncher.launch(intent)
                        }
                    ) {
                        Icon(Icons.Outlined.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(stringResource(R.string.scan_qr_button))
                    }
                }

                is TransactionState.Confirming -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.confirming_transaction))
                }

                is TransactionState.Success -> {
                    val bookTitle = state.transactionData.book.title
                    val ownerName = state.transactionData.owner.fullName
                    val isReturn = state.transactionData.action == "RETURN"
                    val successMessage = if (isReturn) {
                        stringResource(R.string.book_returned_message, bookTitle, ownerName)
                    } else {
                        stringResource(R.string.book_borrowed_message, bookTitle, ownerName)
                    }
                    LaunchedEffect(Unit) {
                        Toast.makeText(
                            context,
                            successMessage,
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.reset()
                        onTransactionComplete()
                    }

                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.redirecting))
                }

                is TransactionState.Error -> {
                    val errorMessage = stringResource(R.string.error_prefix, state.message)
                    LaunchedEffect(state.message) {
                        Toast.makeText(
                            context,
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.reset()
                    }

                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.resetting))
                }

                else -> {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
