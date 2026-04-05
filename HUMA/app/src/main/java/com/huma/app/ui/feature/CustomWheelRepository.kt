package com.huma.app.ui.feature

import androidx.compose.runtime.mutableStateListOf

object CustomWheelRepository {

    val customChallenges = mutableStateListOf<String>()

    fun addChallenge(text: String) {
        if (text.isNotBlank()) {
            customChallenges.add(text)
        }
    }

    fun removeChallenge(text: String) {
        customChallenges.remove(text)
    }
}