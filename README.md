# Reclaim InApp SDK

This SDK allows you to integrate Reclaim's in-app verification process into your Android application.

## Prerequisites

- An Android application source code (Support for Android 5.0 or later).
- An Android device or emulator running Android 5.0 or later.
- A Reclaim account where you've created an app and have the app id, app secret.
- A provider id that you've added to your app in Reclaim Devtools.

## Example

- See the [Reclaim Compose Example - Android](example/README.md) for a complete example of how to use the SDK in an Android application.

## Installation

Add the following repositories to your `settings.gradle` file's repositories block or at the end of settings.gradle:

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    String flutterStorageUrl = System.env.FLUTTER_STORAGE_BASE_URL ?: "https://storage.googleapis.com"
    String reclaimStorageUrl = System.env.RECLAIM_STORAGE_BASE_URL ?: "https://reclaim-inapp-sdk.s3.ap-south-1.amazonaws.com/android/0.3.0/repo"
    repositories {
        google()
        mavenCentral()
        maven {
            url "$reclaimStorageUrl"
        }
        maven {
            url "$flutterStorageUrl/download.flutter.io"
        }
    }
}
```

Some projects may require you to add the repositories to the root `build.gradle` file or your app-level `build.gradle` file's allprojects section.

Next, add the following to your app level `build.gradle` file:

```groovy
implementation "org.reclaimprotocol:inapp_sdk:0.3.0"
```

Add the following to your app level `AndroidManifest.xml` file under the `<application>` tag:

```xml
<activity
    android:name="org.reclaimprotocol.inapp_sdk.ReclaimActivity"
    android:theme="@style/Theme.ReclaimInAppSdk.LaunchTheme"
    android:configChanges="orientation|keyboardHidden|keyboard|screenSize|locale|layoutDirection|fontScale|screenLayout|density|uiMode"
    android:hardwareAccelerated="true"
    android:windowSoftInputMode="adjustResize"
    />
<meta-data android:name="org.reclaimprotocol.inapp_sdk.APP_ID"
    android:value="<YOUR_RECLAIM_APP_ID>" />
<meta-data android:name="org.reclaimprotocol.inapp_sdk.APP_SECRET"
    android:value="<YOUR_RECLAIM_APP_SECRET>" />
```

## Usage

To use ReclaimInAppSdk in your project, follow these steps:

1. Import the ReclaimInAppSdk module into your Kotlin/Java file.

```kotlin
import org.reclaimprotocol.inapp_sdk.ReclaimVerification
```

2. Create a request object.

```kotlin
val request = ReclaimVerification.Request(
        appId = "YOUR_APP_ID",
        secret = "YOUR_APP_SECRET",
        providerId = "YOUR_PROVIDER_ID"
    )
```

Or if you have added the APP_ID and APP_SECRET metadata to your AndroidManifest.xml file, you can create the request object using the `ReclaimVerification.Request.fromManifestMetaData` method.

```kotlin
    val request = ReclaimVerification.Request.fromManifestMetaData(
        context = context,
        providerId = "YOUR_PROVIDER_ID"
    )
```

3. Start the verification flow.


```kotlin
ReclaimVerification.startVerification(
    context = context,
    request = request,
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
        }

        override fun onResponse(response: ReclaimVerification.Response) {
            Log.d("MainActivity", response.toString())
        }
    }
)
```

The returned result `ReclaimVerification.ResultHandler.onResponse` in is a `ReclaimVerification.Response` object. This object contains a response that has proofs, exception, and the sessionId if the verification is successful.

If the verification is cancelled or failed, the handler's `ReclaimVerification.ResultHandler.onException` method is called with a `ReclaimVerification.ReclaimVerificationException` object.

For a complete example, see the [Reclaim Compose Example - Android](example/README.md).

