package com.connect.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoachChatViewModel @Inject constructor(
    private val smartCoachAI: SmartCoachAI
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(text = "Bonjour ! Je suis votre coach personnel. Comment puis-je vous aider aujourd'hui concernant votre santé ou vos entraînements ?", isFromUser = false)
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // 1. Ajouter le message de l'utilisateur
        val userMsg = ChatMessage(text = text, isFromUser = true)
        _messages.value = _messages.value + userMsg

        // 2. Ajouter un message IA temporaire (Loading)
        val loadingMsg = ChatMessage(text = "...", isFromUser = false, isLoading = true)
        _messages.value = _messages.value + loadingMsg

        // 3. Appeler l'API Gemini
        viewModelScope.launch {
            val responseText = smartCoachAI.sendMessage(text)

            // Remplacer le message de chargement par la vraie réponse
            val currentList = _messages.value.toMutableList()
            currentList.removeLast() // Retire le loading
            currentList.add(ChatMessage(text = responseText, isFromUser = false))

            _messages.value = currentList
        }
    }
}
