package com.malekaltaf.smsapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.malekaltaf.smsapplication.ui.theme.SMSApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SmsViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[Manifest.permission.READ_SMS] ?: false
        viewModel.updatePermissionStatus(readGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check initial permission status
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        
        viewModel.updatePermissionStatus(hasPermission)

        setContent {
            SMSApplicationTheme {
                SmsListScreen(
                    viewModel = viewModel,
                    onRequestPermission = { requestSmsPermissions() }
                )
            }
        }
    }

    private fun requestSmsPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS
            )
        )
    }
}