package com.example.nutrisense.helpers.extensions

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView

/**
 * Extension functions pentru management-ul visual al stărilor de UI
 * (Loading, Error, Empty State)
 */

// Loading State Management
fun ProgressBar.show() {
    this.visibility = View.VISIBLE
}

fun ProgressBar.hide() {
    this.visibility = View.GONE
}

fun ProgressBar.setLoading(isLoading: Boolean) {
    if (isLoading) show() else hide()
}

// Error Message Management
fun TextView.showError(message: String) {
    this.text = message
    this.visibility = View.VISIBLE
}

fun TextView.hideError() {
    this.visibility = View.GONE
    this.text = ""
}

fun TextView.showErrorIfNotEmpty(message: String?) {
    if (!message.isNullOrBlank()) {
        showError(message)
    } else {
        hideError()
    }
}

// Empty State Management
fun TextView.showEmptyState(message: String) {
    this.text = message
    this.visibility = View.VISIBLE
}

fun TextView.hideEmptyState() {
    this.visibility = View.GONE
    this.text = ""
}

fun TextView.showEmptyStateIfEmpty(message: String, isEmpty: Boolean) {
    if (isEmpty) {
        showEmptyState(message)
    } else {
        hideEmptyState()
    }
}

// Combined State Management Helper
fun manageLoadingErrorState(
    progressBar: ProgressBar,
    errorTextView: TextView,
    isLoading: Boolean,
    errorMessage: String? = null
) {
    progressBar.setLoading(isLoading)
    if (isLoading) {
        errorTextView.hideError()
    } else {
        errorTextView.showErrorIfNotEmpty(errorMessage)
    }
}

fun manageContentState(
    progressBar: ProgressBar,
    errorTextView: TextView,
    emptyStateTextView: TextView,
    isLoading: Boolean,
    errorMessage: String? = null,
    isEmpty: Boolean = false,
    emptyMessage: String = "No data available"
) {
    progressBar.setLoading(isLoading)

    if (isLoading) {
        errorTextView.hideError()
        emptyStateTextView.hideEmptyState()
    } else if (!errorMessage.isNullOrBlank()) {
        errorTextView.showError(errorMessage)
        emptyStateTextView.hideEmptyState()
    } else if (isEmpty) {
        emptyStateTextView.showEmptyState(emptyMessage)
        errorTextView.hideError()
    } else {
        errorTextView.hideError()
        emptyStateTextView.hideEmptyState()
    }
}
