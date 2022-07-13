# Observing Analytics

An [`AnalyticsListener`](https://appcues.github.io/appcues-android-sdk/appcues/com.appcues/-analytics-listener/index.html) can be registered to allow for the host application to see all of the analytics that are being tracked by the Appcues Android SDK.

## Registering the AnalyticsListener

One of the types in your application can implement the [`AnalyticsListener`](https://appcues.github.io/appcues-android-sdk/appcues/com.appcues/-analytics-listener/index.html) interface. In the usage example below, this is implemented on the main `Application` subclass. This interface defines a single function [`trackedAnalytic(type, value, properties, isInternal)`](https://appcues.github.io/appcues-android-sdk/appcues/com.appcues/-analytics-listener/tracked-analytic.html), which provides access to the analytics tracking being done by the SDK.

```kotlin
class ExampleApplication : Application(), AnalyticsListener {
    override fun trackedAnalytic(type: AnalyticType, value: String?, properties: Map<String, Any>?, isInternal: Boolean) {
        // process and use the analytics tracking information
    }
}
```

Set the listener for the Appcues Android SDK to use. In the usage example below, this is done in the ExampleApplication `onCreate()`. The listener can be provided during SDK initialization as a configuration property, as shown below, or set later after the SDK initialization completes.

```kotlin
override fun onCreate() {
    super.onCreate()

    appcues = Appcues(this, "APPCUES_ACCOUNT_ID", "APPCUES_APPLICATION_ID") {
        analyticsListener = this@ExampleApplication
    }
}
```

## Using the Analytics Tracking Data

There are two key types of Appcues analytics tracking data to distinguish.

The first type is the internal SDK events - these capture anything that is generated automatically inside of the Appcues SDK, including flow events, session events, and any automatically tracked screens.

The second type is all other analytics - these are all the screens, events, user or group identities that are passed into the SDK from the host application.

These two types are distinguished using the `isInternal` parameter on [`trackedAnalytic(type, value, properties, isInternal)`](https://appcues.github.io/appcues-android-sdk/appcues/com.appcues/-analytics-listener/tracked-analytic.html).

### Amplitude Integration Example

In this example use case, an application would like to observe and track all of the internal Appcues SDK events and send them to Amplitude as well. The other events that originate in the main application codebase are already integrated elsewhere in the codebase. The primary goal is to be able to analyze Appcues flow events, using Amplitude.

In this example, the [`AnalyticsListener`](https://appcues.github.io/appcues-android-sdk/appcues/com.appcues/-analytics-listener/index.html) implementation will filter out tracking items so that it is only tracking internal analytics of the `EVENT` [`AnalyticType`](https://appcues.github.io/appcues-android-sdk/appcues/com.appcues/-analytic-type/index.html).

```kotlin
override fun trackedAnalytic(type: AnalyticType, value: String?, properties: Map<String, Any>?, isInternal: Boolean) {

    // filter out any analytics we're not interested in passing along
    if (value != null && type == EVENT && isInternal) {
        
        // Amplitude requires translating properties to a JSONObject
        val eventProperties = properties?.let { map ->
            JSONObject().apply {
                map.forEach() {
                    put(it.key, it.value)
                }
            }
        }

        // log the event with Amplitude
        Amplitude.getInstance().logEvent(value, eventProperties)
    }
}
```

Now, all of the internal events from the Appcues SDK will also be sent to Amplitude, using the `logEvent` function call.