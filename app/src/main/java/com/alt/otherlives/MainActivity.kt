package com.alt.otherlives

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alt.otherlives.app.AltApp
import com.alt.otherlives.core.diagnostics.AppCrashDiagnostics

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCrashDiagnostics.install(applicationContext)
        enableEdgeToEdge()
        setContent { AltApp() }
    }
}
