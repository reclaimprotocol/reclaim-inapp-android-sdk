package org.reclaimprotocol.inapp_sdk_example

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import org.reclaimprotocol.inapp_sdk.ReclaimOverrides
import org.reclaimprotocol.inapp_sdk.ReclaimVerification
import org.reclaimprotocol.inapp_sdk_example.ui.theme.ReclaimInAppSdkExampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = applicationContext
        enableEdgeToEdge()
        ReclaimVerification.preWarm(context)

        // Advanced Usage: Use ReclaimVerification.setOverrides for overriding sdk
        ReclaimVerification.setOverrides(
            context = context,
            appInfo = ReclaimOverrides.ReclaimAppInfo(
                appName = "Overriden Example",
                appImageUrl = "https://placehold.co/400x400/png"
            )
        ) { result ->
            result
                .onSuccess {
                    Log.d("MainActivity", "Reclaim Overrides set")
                }
                .onFailure { throwable ->
                    Log.e("MainActivity", "Could not set overrides", throwable)
                    Toast.makeText(context, "Could not set overrides", Toast.LENGTH_LONG).show()
                }
        }

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
    var providerIdText by remember { mutableStateOf("example") }
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
                            providerId = providerIdText,
                        ),
                        handler = object : ReclaimVerification.ResultHandler {
                            override fun onException(exception: ReclaimVerification.ReclaimVerificationException) {
                                Log.e("MainActivity", "Something went wrong.\nreason: ${exception.reason}\ncause: ${exception.cause}", exception)
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