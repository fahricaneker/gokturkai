package com.gokturk.ai

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private fun available() = FirebaseApp.getApps(App.instance).isNotEmpty()
    suspend fun ensureUser(): String? {
        if (!available()) return null
        val auth = FirebaseAuth.getInstance()
        return auth.currentUser?.uid ?: auth.signInAnonymously().await().user?.uid
    }
    suspend fun save(message: ChatMessage) {
        val uid = ensureUser() ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid).collection("messages").document(message.id).set(message).await()
    }
}
