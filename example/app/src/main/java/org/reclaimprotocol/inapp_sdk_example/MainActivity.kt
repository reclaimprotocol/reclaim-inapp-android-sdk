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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import org.json.JSONObject
import org.reclaimprotocol.inapp_sdk.ReclaimOverrides
import org.reclaimprotocol.inapp_sdk.ReclaimVerification
import org.reclaimprotocol.inapp_sdk_example.ui.theme.ReclaimInAppSdkExampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = applicationContext
        enableEdgeToEdge()
        ReclaimVerification.preWarm(context)

        ReclaimVerification.setConsoleLogging(context = context, enable = true) { result ->
            result
                .onSuccess {
                    Log.d("MainActivity", "Reclaim setConsoleLogging set")
                }
                .onFailure { throwable ->
                    Log.e("MainActivity", "Could not set setConsoleLogging", throwable)
                    Toast.makeText(context, "Could not set setConsoleLogging", Toast.LENGTH_LONG).show()
                }
        }

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

enum class VerificationMode {
    PROVIDER_ID,
    JSON_CONFIG,
    URL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamplePage(
    context: Context
) {
    var selectedMode by remember { mutableStateOf(VerificationMode.PROVIDER_ID) }
    var expanded by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("example") }
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
            // Mode Selection Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.padding(16.dp)
            ) {
                TextField(
                    value = when (selectedMode) {
                        VerificationMode.PROVIDER_ID -> "Provider ID"
                        VerificationMode.JSON_CONFIG -> "JSON Config String"
                        VerificationMode.URL -> "URL"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Verification Mode") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true)
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Provider ID") },
                        onClick = {
                            selectedMode = VerificationMode.PROVIDER_ID
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("JSON Config String") },
                        onClick = {
                            selectedMode = VerificationMode.JSON_CONFIG
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("URL") },
                        onClick = {
                            selectedMode = VerificationMode.URL
                            expanded = false
                        }
                    )
                }
            }

            // Input Field
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = {
                    Text(
                        when (selectedMode) {
                            VerificationMode.PROVIDER_ID -> "Reclaim Provider Id"
                            VerificationMode.JSON_CONFIG -> "JSON Config String"
                            VerificationMode.URL -> "URL"
                        }
                    )
                },
                maxLines = 4,
                modifier = Modifier.padding(16.dp)
            )

            // Button
            Button(
                onClick = {
                    val handler = object : ReclaimVerification.ResultHandler {
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

                    when (selectedMode) {
                        VerificationMode.PROVIDER_ID -> {
                            ReclaimVerification.startVerification(
                                context = context,
                                request = ReclaimVerification.Request.fromManifestMetaData(
                                    context = context,
                                    providerId = inputText,
                                ),
                                handler = handler,
                            )
                        }
                        VerificationMode.JSON_CONFIG -> {
                            ReclaimVerification.startVerificationFromJson(
                                context = context,
                                template = jsonStringToMap(inputText),
                                handler = handler,
                            )
                        }
                        VerificationMode.URL -> {
                            ReclaimVerification.startVerificationFromUrl(
                                context = context,
                                requestUrl = inputText,
                                handler = handler,
                            )
                        }
                    }
                }
            ) {
                Text(
                    text = when (selectedMode) {
                        VerificationMode.PROVIDER_ID -> "Start Verification with Provider ID"
                        VerificationMode.JSON_CONFIG -> "Start Verification with JSON Config"
                        VerificationMode.URL -> "Start Verification with URL"
                    }
                )
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

fun jsonStringToMap(jsonString: String): Map<Any?, Any?> {
    val map = mutableMapOf<Any?, Any?>()
    val jsonObject = JSONObject(jsonString)
    val keys = jsonObject.keys()

    while (keys.hasNext()) {
        val key = keys.next()
        val value = jsonObject.get(key)

        when (value) {
            is JSONObject -> map[key] = jsonStringToMap(value.toString()) // Recursively convert nested objects
            // Add more cases for other JSON array/number/boolean types if needed
            else -> map[key] = value
        }
    }
    return map
}