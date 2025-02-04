package org.reclaimprotocol.reclaim_inapp_sdk_example

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.reclaimprotocol.reclaim_inapp_sdk.ReclaimVerification
import org.reclaimprotocol.reclaim_inapp_sdk_example.ui.theme.ReclaimInAppSdkExampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context =  applicationContext
        enableEdgeToEdge()
        setContent {
            ReclaimInAppSdkExampleTheme {
                ExamplePage(
                    context = context
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamplePage(
    context: Context
) {
    var providerIdText by remember { mutableStateOf("6d3f6753-7ee6-49ee-a545-62f1b1822ae5") }
    var resultText by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun showErrorInSnackbar(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Reclaim InApp SDK Example")
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            TextField(
                value = providerIdText,
                onValueChange = { providerIdText = it },
                label = { Text("Reclaim Provider Id") },
                modifier = Modifier.padding(16.dp)
            )
            Button(
                onClick = {
                    ReclaimVerification.startVerification(
                        context = context,
                        request = ReclaimVerification.Request.fromManifestMetaData(
                            context = context,
                            providerId = providerIdText
                        ),
                        handler = object : ReclaimVerification.ResultHandler {
                            override fun onException(exception: ReclaimVerification.ReclaimVerificationException) {
                                Log.e("MainActivity", "Something went wrong", exception)
                                val reason = when (exception) {
                                    is ReclaimVerification.ReclaimVerificationException.Failed -> "Failed because: ${exception.reason}"
                                    is ReclaimVerification.ReclaimVerificationException.Cancelled -> "Verification cancelled"
                                    is ReclaimVerification.ReclaimVerificationException.Dismissed -> "Dismissed by user"
                                    is ReclaimVerification.ReclaimVerificationException.SessionExpired -> "Session expired"
                                }
                                Log.d("MainActivity", "reason: $reason")
                                showErrorInSnackbar(reason)
                            }

                            override fun onResponse(response: ReclaimVerification.Response) {
                                Log.d("MainActivity", response.toString())
                                resultText = response.toString()
                            }
                        }
                    )
                }
            ) {
                Text(text = "Start Verification")
            }
            if (resultText.isNotEmpty()) {
                TextField(
                    value = resultText,
                    readOnly = true,
                    onValueChange = { resultText = it },
                    label = { Text("Reclaim Verification Result") },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}