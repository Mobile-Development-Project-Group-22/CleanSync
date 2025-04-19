package com.example.cleansync.ui.notifications

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.jvm.java

data class NotificationPreferences(
    val email: Boolean = true,
    val push: Boolean = true
)


class NotificationSettingsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _preferences = MutableStateFlow(NotificationPreferences())
    val preferences: StateFlow<NotificationPreferences> = _preferences

    fun loadPreferences() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("notification_preferences").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                doc.toObject(NotificationPreferences::class.java)?.let {
                    _preferences.value = it
                }
            }
    }

    fun updatePreference(email: Boolean? = null, push: Boolean? = null) {
        val userId = auth.currentUser?.uid ?: return
        val updated = _preferences.value.copy(
            email = email ?: _preferences.value.email,
            push = push ?: _preferences.value.push
        )
        db.collection("notification_preferences").document(userId).set(updated)
        _preferences.value = updated
    }
}