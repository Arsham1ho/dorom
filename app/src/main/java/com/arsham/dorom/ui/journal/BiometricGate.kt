package com.arsham.dorom.ui.journal

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** Wraps BiometricPrompt (fingerprint/face/device PIN fallback) behind a simple callback API. */
@Composable
fun rememberBiometricUnlock(onSuccess: () -> Unit, onFailure: () -> Unit = {}): () -> Unit {
    val context = LocalContext.current
    val activity = context as FragmentActivity

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Personal Journal")
            .setSubtitle("Only you should see this")
            .setAllowedAuthenticators(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            )
            .build()
    }

    val prompt = remember {
        BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onFailure()
                }

                override fun onAuthenticationFailed() {
                    onFailure()
                }
            },
        )
    }

    return {
        val manager = BiometricManager.from(context)
        val canAuth = manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        )
        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            prompt.authenticate(promptInfo)
        } else {
            // No biometrics/PIN configured on the device — fall back to open access rather than lock the user out.
            onSuccess()
        }
    }
}
