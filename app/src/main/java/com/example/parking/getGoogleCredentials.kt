package com.example.parking

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import java.io.InputStream

fun getGoogleCredentials(context: Context): GoogleCredentials {
    return try {
        val inputStream: InputStream = context.assets.open("service-account-key.json")
        Log.d("GoogleCredentialsTest", "Nyckeln laddades korrekt!")
        GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/cloud-vision"))
    } catch (e: Exception) {
        Log.e("GoogleCredentialsTest", "Kunde inte ladda nyckeln: ${e.message}")
        throw e
    }
}
