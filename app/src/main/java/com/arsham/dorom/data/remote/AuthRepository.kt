package com.arsham.dorom.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

/** Thin wrapper around Supabase Auth — Google Sign-In only for now. */
class AuthRepository(private val client: SupabaseClient) {
    val sessionStatus: StateFlow<SessionStatus> get() = client.auth.sessionStatus

    fun currentUserEmail(): String? = client.auth.currentUserOrNull()?.email

    suspend fun signInWithGoogleIdToken(idToken: String) {
        client.auth.signInWith(IDToken) {
            this.idToken = idToken
            this.provider = Google
        }
    }

    suspend fun signOut() {
        client.auth.signOut()
    }
}
