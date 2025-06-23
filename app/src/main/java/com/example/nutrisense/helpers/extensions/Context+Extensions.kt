package com.example.nutrisense.helpers.extensions

import android.content.Context
import android.widget.Toast

fun Context.showToast(message: String, isLong: Boolean = false) {
    val duration = if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(this, message, duration).show()
}

fun Context.showErrorToast(message: String) {
    Toast.makeText(this, "❌ $message", Toast.LENGTH_LONG).show()
}

fun Context.showSuccessToast(message: String) {
    Toast.makeText(this, "✅ $message", Toast.LENGTH_SHORT).show()
}

fun Context.showWarningToast(message: String) {
    Toast.makeText(this, "⚠️ $message", Toast.LENGTH_SHORT).show()
}