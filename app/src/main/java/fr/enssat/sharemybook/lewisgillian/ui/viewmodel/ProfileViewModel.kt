package fr.enssat.sharemybook.lewisgillian.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.enssat.sharemybook.lewisgillian.R
import fr.enssat.sharemybook.lewisgillian.domain.model.User
import fr.enssat.sharemybook.lewisgillian.ui.components.PhoneNumberValidator
import fr.enssat.sharemybook.lewisgillian.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val fullName: String = "",
    val tel: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(
    application: Application,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private fun getString(resId: Int): String = getApplication<Application>().getString(resId)
    private fun getString(resId: Int, vararg args: Any): String = getApplication<Application>().getString(resId, *args)

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val user = userRepository.observeCurrentUser().first()

                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        fullName = user.fullName,
                        tel = user.tel,
                        email = user.email,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        user = null,
                        fullName = "",
                        tel = "",
                        email = "",
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = getString(R.string.error_loading, e.message ?: ""),
                    isLoading = false
                )
            }
        }
    }

    fun updateFullName(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value)
    }

    fun updateTel(value: String) {
        _uiState.value = _uiState.value.copy(tel = value)
    }

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                error = null,
                successMessage = null
            )

            val fullName = _uiState.value.fullName.trim()
            val tel = _uiState.value.tel.trim()
            val email = _uiState.value.email.trim()

            if (fullName.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = getString(R.string.error_name_required)
                )
                return@launch
            }

            if (tel.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = getString(R.string.error_phone_required)
                )
                return@launch
            }

            if (!PhoneNumberValidator.isValidFullNumber(tel)) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = getString(R.string.error_phone_invalid)
                )
                return@launch
            }

            if (email.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = getString(R.string.error_email_required)
                )
                return@launch
            }

            if (!email.contains("@") || !email.contains(".")) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = getString(R.string.error_email_invalid)
                )
                return@launch
            }

            val existingUid = _uiState.value.user?.uid ?: ""
            val uid = if (existingUid.isBlank()) {
                userRepository.generateUserUuid()
            } else {
                existingUid
            }

            val user = User(
                uid = uid,
                fullName = fullName,
                tel = tel,
                email = email,
                isCurrentUser = true
            )

            try {
                userRepository.saveCurrentUser(user)
                _uiState.value = _uiState.value.copy(
                    user = user,
                    isSaving = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: getString(R.string.error_unknown),
                    successMessage = null
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
