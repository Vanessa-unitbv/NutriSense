package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nutrisense.ui.screens.NotificationSettingsScreen
import com.example.nutrisense.ui.theme.NutriSenseTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment that hosts the NotificationSettingsScreen Composable.
 * Provides advanced notification settings for water and meal reminders.
 */
@AndroidEntryPoint
class NotificationSettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                NutriSenseTheme {
                    NotificationSettingsScreen(
                        onBackClick = {
                            findNavController().navigateUp()
                        },
                        onSaveClick = { settings ->
                        }
                    )
                }
            }
        }
    }
}

