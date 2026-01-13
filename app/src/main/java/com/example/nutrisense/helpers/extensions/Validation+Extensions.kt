package com.example.nutrisense.helpers.extensions

import android.widget.EditText

/**
 * Extension functions pentru validări fără popup
 * Afișează eroare direct pe EditText și setează focus
 */

/**
 * Validează dacă text este gol
 * @return true dacă validarea a trecut, false dacă nu
 */
fun EditText.validateNotEmpty(fieldName: String): Boolean {
    val text = this.text.toString().trim()
    if (text.isEmpty()) {
        this.error = "$fieldName cannot be empty!"
        this.requestFocus()
        return false
    }
    this.error = null
    return true
}

/**
 * Validează email
 * @return true dacă email este valid, false dacă nu
 */
fun EditText.validateEmail(): Boolean {
    val email = this.text.toString().trim()
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    if (email.isEmpty()) {
        this.error = "Email cannot be empty!"
        this.requestFocus()
        return false
    }

    if (!email.matches(emailPattern.toRegex())) {
        this.error = "Please enter a valid email address!"
        this.requestFocus()
        return false
    }

    this.error = null
    return true
}

/**
 * Validează parolă
 * @param minLength lungimea minimă a parolei
 * @return true dacă parola este validă, false dacă nu
 */
fun EditText.validatePassword(minLength: Int = 6): Boolean {
    val password = this.text.toString().trim()

    if (password.isEmpty()) {
        this.error = "Password cannot be empty!"
        this.requestFocus()
        return false
    }

    if (password.length < minLength) {
        this.error = "Password must be at least $minLength characters!"
        this.requestFocus()
        return false
    }

    this.error = null
    return true
}

/**
 * Validează un număr
 * @return true dacă valoarea este un număr valid, false dacă nu
 */
fun EditText.validateNumber(fieldName: String, min: Float = 0f, max: Float = Float.MAX_VALUE): Boolean {
    val text = this.text.toString().trim()

    if (text.isEmpty()) {
        this.error = "$fieldName cannot be empty!"
        this.requestFocus()
        return false
    }

    val number = text.toFloatOrNull()
    if (number == null) {
        this.error = "$fieldName must be a valid number!"
        this.requestFocus()
        return false
    }

    if (number < min || number > max) {
        this.error = "$fieldName must be between $min and $max!"
        this.requestFocus()
        return false
    }

    this.error = null
    return true
}

/**
 * Validează lungime de text
 * @return true dacă lungimea este validă, false dacă nu
 */
fun EditText.validateMinLength(fieldName: String, minLength: Int): Boolean {
    val text = this.text.toString().trim()

    if (text.isEmpty()) {
        this.error = "$fieldName cannot be empty!"
        this.requestFocus()
        return false
    }

    if (text.length < minLength) {
        this.error = "$fieldName must be at least $minLength characters!"
        this.requestFocus()
        return false
    }

    this.error = null
    return true
}

/**
 * Validează dacă EditText este gol
 * @return true dacă validarea a trecut, false dacă nu
 */
fun EditText.validateFieldNotEmpty(fieldName: String): Boolean {
    if (this.getTextString().isEmpty()) {
        this.error = "$fieldName cannot be empty!"
        this.requestFocus()
        return false
    }
    this.error = null
    return true
}

/**
 * Validează un număr cu mesaj de eroare specific
 * @return true dacă validarea a trecut, false dacă nu
 */
fun EditText.validateNumberField(fieldName: String, min: Float = 0f, max: Float = Float.MAX_VALUE): Boolean {
    val text = this.getTextString()

    if (text.isEmpty()) {
        this.error = "$fieldName cannot be empty!"
        this.requestFocus()
        return false
    }

    val number = text.toFloatOrNull()
    if (number == null) {
        this.error = "$fieldName must be a valid number!"
        this.requestFocus()
        return false
    }

    if (number < min || number > max) {
        this.error = "$fieldName must be between $min and $max!"
        this.requestFocus()
        return false
    }

    this.error = null
    return true
}

/**
 * Validează email cu mesaj specific
 * @return true dacă email este valid, false dacă nu
 */
fun EditText.validateEmailField(): Boolean {
    val email = this.getTextString()
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    if (email.isEmpty()) {
        this.error = "Email cannot be empty!"
        this.requestFocus()
        return false
    }

    if (!email.matches(emailPattern.toRegex())) {
        this.error = "Invalid email format!"
        this.requestFocus()
        return false
    }

    this.error = null
    return true
}

/**
 * Validează parolă cu mesaj specific
 * @param minLength lungimea minimă a parolei
 * @return true dacă parola este validă, false dacă nu
 */
fun EditText.validatePasswordField(minLength: Int = 6): Boolean {
    val password = this.getTextString()

    if (password.isEmpty()) {
        this.error = "Password cannot be empty!"
        this.requestFocus()
        return false
    }

    if (password.length < minLength) {
        this.error = "Password must be at least $minLength characters!"
        this.requestFocus()
        return false
    }

    this.error = null
    return true
}

/**
 * Șterge eroarea de pe EditText
 */
fun EditText.clearValidationError() {
    this.error = null
}
