# Reclaim Compose Example - Android

This is a complete example of how to use the Reclaim InApp SDK in an Android application. This example is also using Jetpack Compose.

## Prerequisites

- You have an app in Reclaim Devtools.
- You have added a provider to your app in Reclaim Devtools.
- You have the appId and secret from Reclaim Devtools.

## Setup

1. The example project has placeholder values for app id and secret in the metadata element of the AndroidManifest.xml file. Update them with the correct values as per your project from Reclaim Devtools.

```xml
<meta-data android:name="org.reclaimprotocol.inapp_sdk.APP_ID"
    android:value="<YOUR_RECLAIM_APP_ID>" />
<meta-data android:name="org.reclaimprotocol.inapp_sdk.APP_SECRET"
    android:value="<YOUR_RECLAIM_APP_SECRET>" />
```
2. Build the project and run on a device or simulator.
