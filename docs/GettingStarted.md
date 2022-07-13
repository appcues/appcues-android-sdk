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

> Appcues recommends choosing opaque and hard to guess user IDs, such as a UUID. See the [FAQ for Developers](https://docs.appcues.com/article/159-faq#choosing-a-user-id) for more details about how to choose a User ID.

The inverse of identifying is resetting.  For example, if a user logs out of your app calling `reset()` will disable tracking of screens and events until a user is identified again.

You can target users during anonymous usage in your application using `anonymous()` instead of `identify()`.  This can have implications on your billing based on active user counts.  The format of anonymous IDs can be customized during initialization with the `AppcuesConfig`, using the `anonymousIdFactory` property.


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
