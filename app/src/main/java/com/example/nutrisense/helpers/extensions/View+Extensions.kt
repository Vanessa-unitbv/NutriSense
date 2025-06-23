package com.example.nutrisense.helpers.extensions

import android.view.View
import android.widget.EditText

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.isVisible(): Boolean {
    return visibility == View.VISIBLE
}

fun View.isHidden(): Boolean {
    return visibility == View.GONE
}

fun View.isInvisible(): Boolean {
    return visibility == View.INVISIBLE
}

fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

fun EditText.clearErrorAndFocus() {
    error = null
    clearFocus()
}

fun EditText.setErrorAndFocus(errorMessage: String) {
    error = errorMessage
    requestFocus()
}

fun EditText.getTextString(): String {
    return text.toString().trim()
}