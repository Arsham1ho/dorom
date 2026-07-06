package com.arsham.dorom.ui.auth

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.arsham.dorom.BuildConfig
import com.arsham.dorom.ui.LocalAppContainer
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/**
 * Optional Google Sign-In, reached from Settings — not a hard gate. The app is fully usable
 * offline either way; signing in only turns on background sync.
 */
@Composable
fun SignInScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Sync across devices", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Sign in with Google to back up your plans, goals, and gym data and see them on another device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        Button(
            enabled = !loading,
            onClick = {
                scope.launch {
                    loading = true
                    error = null
                    runCatching {
                        val option = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID).build()
                        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
                        val response = CredentialManager.create(context).getCredential(context as Activity, request)
                        val googleCredential = GoogleIdTokenCredential.createFrom(response.credential.data)
                        container.authRepository.signInWithGoogleIdToken(googleCredential.idToken)
                        container.syncEngine.syncAll()
                    }.onFailure { e -> error = e.message ?: "Sign-in failed" }
                    loading = false
                    if (error == null) onBack()
                }
            },
        ) { Text(if (loading) "Signing in…" else "Continue with Google") }
        error?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}
