package fr.enssat.sharemybook.lewisgillian.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.enssat.sharemybook.lewisgillian.R
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil

data class Country(
    val code: String,
    val name: String,
    val dialCode: String,
    val flag: String
)

val commonCountries = listOf(
    Country("FR", "France", "+33", "🇫🇷"),
    Country("BE", "Belgique", "+32", "🇧🇪"),
    Country("CH", "Suisse", "+41", "🇨🇭"),
    Country("LU", "Luxembourg", "+352", "🇱🇺"),
)

object PhoneNumberValidator {
    private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
    
    fun isValid(phoneNumber: String, countryCode: String): Boolean {
        return try {
            val number = phoneUtil.parse(phoneNumber, countryCode)
            phoneUtil.isValidNumber(number)
        } catch (e: NumberParseException) {
            false
        }
    }
    
    fun isValidFullNumber(fullPhoneNumber: String): Boolean {
        return try {
            val number = phoneUtil.parse(fullPhoneNumber, null)
            phoneUtil.isValidNumber(number)
        } catch (e: NumberParseException) {
            false
        }
    }

    fun detectCountry(fullPhoneNumber: String): String? {
        return try {
            val number = phoneUtil.parse(fullPhoneNumber, null)
            phoneUtil.getRegionCodeForNumber(number)
        } catch (e: NumberParseException) {
            null
        }
    }
}

@Composable
fun PhoneNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val phoneLabel = if (label.isNotBlank()) label else stringResource(R.string.phone)
    var selectedCountry by remember { 
        mutableStateOf(
            if (value.isNotBlank()) {
                val detectedCode = PhoneNumberValidator.detectCountry(value)
                commonCountries.find { it.code == detectedCode } ?: commonCountries[0]
            } else {
                commonCountries[0]
            }
        )
    }
    
    fun shouldShowZero(country: Country): Boolean {
        return country.code in listOf("FR", "BE", "LU")
    }

    fun formatLocalNumber(input: String, country: Country): String {
        val digits = input.filter { it.isDigit() }
        val withZero = if (shouldShowZero(country)) {
            if (digits.startsWith("0")) digits else "0$digits"
        } else digits
        return if (shouldShowZero(country)) {
            withZero.chunked(2).joinToString(" ")
        } else withZero
    }

    var localNumber by remember {
        mutableStateOf(
            if (value.startsWith("+")) {
                value.removePrefix(selectedCountry.dialCode).trim()
            } else {
                value
            }
        )
    }
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }

    androidx.compose.runtime.LaunchedEffect(localNumber, selectedCountry) {
        val formatted = formatLocalNumber(localNumber, selectedCountry)
        textFieldValue = TextFieldValue(text = formatted, selection = androidx.compose.ui.text.TextRange(formatted.length))
    }
    
    var showCountryPicker by remember { mutableStateOf(false) }
    
    val isValidNumber = remember(localNumber, selectedCountry) {
        if (localNumber.isBlank()) true
        else PhoneNumberValidator.isValid(localNumber, selectedCountry.code)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top
    ) {
        OutlinedTextField(
            value = "${selectedCountry.flag} ${selectedCountry.dialCode}",
            onValueChange = { },
            readOnly = true,
            modifier = Modifier
                .width(120.dp)
                .clickable { showCountryPicker = true },
            label = { Text(stringResource(R.string.country)) },
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(R.string.choose_country),
                    modifier = Modifier.clickable { showCountryPicker = true }
                )
            },
            singleLine = true
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        val phonePlaceholder = if (shouldShowZero(selectedCountry)) stringResource(R.string.phone_placeholder_fr) else stringResource(R.string.phone_placeholder_generic)
        val phoneInvalidText = stringResource(R.string.phone_invalid)
        val validText = stringResource(R.string.valid)
        val invalidText = stringResource(R.string.invalid)
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { tfv: TextFieldValue ->
                val digits = tfv.text.filter { it.isDigit() }
                val withZero = if (shouldShowZero(selectedCountry)) {
                    if (digits.startsWith("0")) digits else "0$digits"
                } else digits
                localNumber = withZero
                val formatted = formatLocalNumber(withZero, selectedCountry)
                textFieldValue = TextFieldValue(formatted, selection = androidx.compose.ui.text.TextRange(formatted.length))
                val fullNumber = "${selectedCountry.dialCode}${withZero}"
                onValueChange(fullNumber)
            },
            modifier = Modifier.weight(1f),
            label = { Text(phoneLabel) },
            placeholder = { Text(phonePlaceholder) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            isError = isError || (!isValidNumber && localNumber.isNotBlank()),
            supportingText = {
                if (!isValidNumber && localNumber.isNotBlank()) {
                    Text(phoneInvalidText, color = MaterialTheme.colorScheme.error)
                } else if (errorMessage != null) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
            },
            trailingIcon = {
                if (localNumber.isNotBlank()) {
                    if (isValidNumber) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = validText,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = invalidText,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )
    }
    
    if (showCountryPicker) {
        CountryPickerDialog(
            countries = commonCountries,
            selectedCountry = selectedCountry,
            onCountrySelected = { country ->
                selectedCountry = country
                showCountryPicker = false
                val fullNumber = "${country.dialCode}${localNumber.filter { it.isDigit() }}"
                onValueChange(fullNumber)
            },
            onDismiss = { showCountryPicker = false }
        )
    }
}

@Composable
private fun CountryPickerDialog(
    countries: List<Country>,
    selectedCountry: Country,
    onCountrySelected: (Country) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isBlank()) countries
        else countries.filter { 
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.dialCode.contains(searchQuery)
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_country_title)) },
        text = {
            Box(modifier = Modifier.height(400.dp)) {
                LazyColumn {
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(stringResource(R.string.search)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            singleLine = true
                        )
                    }

                    items(filteredCountries) { country ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCountrySelected(country) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = country.flag,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = country.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = country.dialCode,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (country == selectedCountry) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(R.string.selected),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
