package fr.enssat.sharemybook.lewisgillian.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.enssat.sharemybook.lewisgillian.R
import fr.enssat.sharemybook.lewisgillian.ui.components.ErrorDialog
import fr.enssat.sharemybook.lewisgillian.ui.components.PhoneNumberField
import fr.enssat.sharemybook.lewisgillian.ui.components.PhoneNumberValidator
import fr.enssat.sharemybook.lewisgillian.ui.viewmodel.ProfileViewModel

data class OnboardingStep(
    val titleResId: Int,
    val subtitleResId: Int,
    val icon: ImageVector
)

val onboardingSteps = listOf(
    OnboardingStep(
        titleResId = R.string.onboarding_name_title,
        subtitleResId = R.string.onboarding_name_subtitle,
        icon = Icons.Default.Person
    ),
    OnboardingStep(
        titleResId = R.string.onboarding_phone_title,
        subtitleResId = R.string.onboarding_phone_subtitle,
        icon = Icons.Default.Phone
    ),
    OnboardingStep(
        titleResId = R.string.onboarding_email_title,
        subtitleResId = R.string.onboarding_email_subtitle,
        icon = Icons.Default.Email
    )
)

@Composable
fun OnboardingScreen(viewModel: ProfileViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    val totalSteps = onboardingSteps.size
    
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    fun isValidName(name: String): Boolean {
        val regex = Regex("^[A-Za-zÀ-ÖØ-öø-ÿ -]{2,}")
        return name.isNotBlank() && regex.matches(name)
    }

    fun isValidPhone(phone: String): Boolean {
        return PhoneNumberValidator.isValidFullNumber(phone)
    }

    fun isValidEmail(email: String): Boolean {
        val regex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
        return email.isNotBlank() && regex.matches(email)
    }
    
    val isStep0Valid = isValidName(uiState.fullName)
    val isStep1Valid = isValidPhone(uiState.tel)
    val isStep2Valid = isValidEmail(uiState.email)

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(currentStep) {
        focusRequester.requestFocus()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = (currentStep + 1).toFloat() / totalSteps,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.step_of, currentStep + 1, totalSteps),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    } else {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    }
                },
                label = "step_animation"
            ) { step ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = onboardingSteps[step].icon,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(onboardingSteps[step].titleResId),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(onboardingSteps[step].subtitleResId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    when (step) {
                        0 -> {
                            val showNameError = uiState.fullName.isNotBlank() && !isValidName(uiState.fullName)
                            OutlinedTextField(
                                value = uiState.fullName,
                                onValueChange = {
                                    val filtered = it.filter { c -> c.isLetter() || c == '-' || c == ' ' }
                                    viewModel.updateFullName(filtered)
                                },
                                label = { Text(stringResource(R.string.full_name)) },
                                placeholder = { Text(stringResource(R.string.name_placeholder)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                singleLine = true,
                                isError = showNameError,
                                supportingText = if (showNameError) {
                                    { Text(stringResource(R.string.name_error)) }
                                } else null,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { if (isStep0Valid) currentStep++ }
                                )
                            )
                        }
                        1 -> {
                            val phoneErrorMessage = stringResource(R.string.phone_invalid_country)
                            PhoneNumberField(
                                value = uiState.tel,
                                onValueChange = { viewModel.updateTel(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                label = stringResource(R.string.phone),
                                isError = uiState.tel.isNotBlank() && !isValidPhone(uiState.tel),
                                errorMessage = if (uiState.tel.isNotBlank() && !isValidPhone(uiState.tel)) phoneErrorMessage else null
                            )
                        }
                        2 -> {
                            val showEmailError = uiState.email.isNotBlank() && !isValidEmail(uiState.email)
                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = {
                                    val filtered = it.filter { c ->
                                        c.isLetterOrDigit() || c == '@' || c == '-' || c == '.' || c == '_' || c == '%'
                                    }
                                    viewModel.updateEmail(filtered)
                                },
                                label = { Text(stringResource(R.string.email)) },
                                placeholder = { Text(stringResource(R.string.email_placeholder)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                singleLine = true,
                                isError = showEmailError,
                                supportingText = if (showEmailError) {
                                    { Text(stringResource(R.string.email_invalid)) }
                                } else null,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (isStep2Valid) {
                                            focusManager.clearFocus()
                                            viewModel.saveProfile()
                                        }
                                    }
                                )
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(stringResource(R.string.back), modifier = Modifier.padding(start = 8.dp))
                    }
                }
                
                Button(
                    onClick = {
                        if (currentStep < totalSteps - 1) {
                            currentStep++
                        } else {
                            viewModel.saveProfile()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = when (currentStep) {
                        0 -> isStep0Valid
                        1 -> isStep1Valid
                        2 -> isStep2Valid && !uiState.isSaving
                        else -> true
                    }
                ) {
                    if (uiState.isSaving && currentStep == totalSteps - 1) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else if (currentStep < totalSteps - 1) {
                        Text(stringResource(R.string.next))
                        Icon(
                            Icons.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(18.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(stringResource(R.string.finish), modifier = Modifier.padding(start = 8.dp))
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
