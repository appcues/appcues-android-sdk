# Configuring the Appcues URL Scheme

The Appcues Android SDK includes support for a custom URL scheme that supports previewing Appcues experiences in-app prior to publishing and launching the Appcues debugger.

## Overview

Configuring the Appcues URL scheme involves adding an `<intent-filter>` with a specific scheme value in the `AndroidManifest.xml` and then directing the incoming Intent to the Appcues Android SDK.

> It is **strongly** recommended that you configure the custom URL scheme. It allows non-developer users of your Appcues instance to test Appcues experiences in a real setting. It's also valuable for future troubleshooting and support from Appcues via the debugger.

## Register the Custom URL Scheme

Update your `AndroidManifest.xml` to add an intent filter with the custom URL scheme inside the desired `<activity>` element.  Typically, the scheme would be registered on the main Activity in your application, denoted by another intent filter with `<action android:name="android.intent.action.MAIN" />`. Replace `APPCUES_APPLICATION_ID` in the snippet below with your app's Appcues Application ID. This value can be obtained from your [Appcues settings](https://studio.appcues.com/settings/installation).

For example, if your Appcues Application ID is `123-xyz` your url scheme value would be `appcues-123-xyz`.

```xml
<activity
    android:name="..."
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
    <intent-filter>
        <data android:scheme="appcues-APPCUES_APPLICATION_ID" />
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
    </intent-filter>
</activity>
```

## Handle the Custom URL Scheme

Custom URL's should be handled with a call to `appcues.onNewIntent(activity, intent)`. If the URL being opened is an Appcues URL, the URL will be handled and a result of `true` will be returned. Otherwise, `false` is returned and the application should handle the Intent.

A new Intent may be sent to the Activity through either the `onCreate` or `onNewIntent` functions.  The Activity should override these functions and provide an opportunity to forward the Intent on to the Appcues instance, and handle the result depending on whether it was an Appcues link or not.  Example usage in an Activity:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleLinkIntent(intent)
}

override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    handleLinkIntent(intent)
}

private fun handleLinkIntent(intent: Intent?) {
    val appcuesHandled = appcues.onNewIntent(this, intent)
    if (appcuesHandled) return
    
    // otherwise, it was not an Appcues link, the application should handle
}
```

## Verifying the Custom URL Scheme

Test that the URL scheme handling is set up correctly by navigating to `appcues-APPCUES_APPLICATION_ID://debugger` in your browser on the device with the app installed.

See the [Debug Guide](https://github.com/appcues/appcues-android-sdk/blob/main/Debugging.md) for details on the functionality of the debugger.