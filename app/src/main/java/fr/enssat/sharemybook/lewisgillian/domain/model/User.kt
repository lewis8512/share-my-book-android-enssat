package fr.enssat.sharemybook.lewisgillian.domain.model

data class User(
    val uid: String,
    val fullName: String,
    val tel: String,
    val email: String,
    val isCurrentUser: Boolean = false
) {
    fun isValid(): Boolean {
        return fullName.isNotBlank() &&
                tel.isNotBlank() &&
                email.isNotBlank() &&
                email.contains("@")
    }
}
