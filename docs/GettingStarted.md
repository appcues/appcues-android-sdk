# Getting Started with Appcues Android SDK

Initialize the SDK and track events.

## Initializing the SDK

An instance of the Appcues Android SDK ([`Appcues`](https://github.com/appcues/appcues-android-sdk/blob/main/appcues/src/main/java/com/appcues/Appcues.kt)) should be initialized when your app launches. A lifecycle method such as the Application onCreate would be a common location:

```kotlin
override fun onCreate() {
    super.onCreate()
    appcues = Appcues(this, APPCUES_ACCOUNT_ID, APPCUES_APPLICATION_ID)
}
```

Initializing the SDK requires you to provide two values, an Appcues account ID, and an Appcues mobile application ID. These values can be obtained from your [Appcues settings](https://studio.appcues.com/settings/account).

An additional optional parameter can also be provided to customize the behavior of the SDK using the `AppcuesConfig`. For example, setting the logging level:

```kotlin
override fun onCreate() {
    super.onCreate()
    appcues = Appcues(this, APPCUES_ACCOUNT_ID, APPCUES_APPLICATION_ID) {
        loggingLevel = LogginLevel.DEBUG
    }
}
```

## Managing Users

In order to target content to the right users at the right time, you need to identify users and send Appcues data about them. A user is identified with a unique ID.

```kotlin
// identify a known user
appcues.identify(userId, properties)
```

For more detail about session management and anonymous user tracking, refer to [Identifying and Managing Users](https://github.com/appcues/appcues-android-sdk/blob/main/docs/Identifying.md).

## Tracking Screens and Events

Events are the “actions” your users take in your application. Screens are a special type of event to capture a user viewing a specific screen. Once you’ve installed and initialized the Appcues Android SDK, you can start tracking screens and events using the following methods:

```kotlin
// track a custom event
appcues.track(name, properties)

// track a screen view
appcues.screen(title, properties)
```

A screen should be tracked each time the screen appears to the user, for example in an Activity or Fragment: 
```kotlin
override fun onResume() {
    super.onResume()
    appcues.screen("Screen Name")
}
```

The Appcues Android SDK supports basic automatic screen tracking for Activities.  This will report a screen view using the Activity `label` value as the screen title, each time a new Activity starts.  To enable this automatic screen tracking, call `trackScreens()`.

## Debugging

See [Configuring the Appcues URL Scheme](https://github.com/appcues/appcues-android-sdk/blob/main/docs/URLSchemeConfiguring.md) for setup instructions and then refer to Refer to the [Debug Guide](https://github.com/appcues/appcues-android-sdk/blob/main/docs/Debugging.md) for usage details.

## Observing Analytics

See [Observing Analytics](https://github.com/appcues/appcues-android-sdk/blob/main/docs/AnalyticObserving.md) for information about how to listen to analytics tracking data being reported by the Appcues SDK, including uses cases for integrating with other Analytics tracking packages.

## Google Play Dependencies

The Appcues Android SDK includes a dependency on the [Google Play In-App Review](https://developer.android.com/guide/playcore/in-app-review) libraries. This dependency allows for building experiences that can request a Play Store in-app review. If your application is not distributed through Google Play, or you otherwise want to opt out of this dependency and the in-app review capability in Appcues, you can update your build.gradle dependency as shown below.

```kotlin
implementation('com.appcues:appcues:<latest_version>') {
    exclude group: 'com.google.android.play', module: 'review'
    exclude group: 'com.google.android.play', module: 'review-ktx'
}
```