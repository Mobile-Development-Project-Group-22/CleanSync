package com.example.cleansync.ui.auth

import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.example.cleansync.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun rememberGoogleSignInHandler(
    context: Context,
    onTokenReceived: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    val currentOnTokenReceived = rememberUpdatedState(onTokenReceived)
    val currentOnError = rememberUpdatedState(onError)

    val serverClientId = context.getString(R.string.Server_Client_ID)

    // Create a GoogleSignInClient instance
    val googleSignInClient = remember {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestEmail()
                .build()
        )
    }

    // Create a launcher for the sign-in intent
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data == null) {
            currentOnError.value("Google Sign-In cancelled")

        }
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            // Google Sign-In was successful, authenticate with Firebase
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                currentOnTokenReceived.value(idToken)
            } else {
                currentOnError.value("No ID token received")
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignInHandler", "Sign-in failed", e)
            currentOnError.value("Google Sign-In failed: ${e.statusCode}")
        }
    }

    return {
        // Launch the Google Sign-In intent when this function is called
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }
}