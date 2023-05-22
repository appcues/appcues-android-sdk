# Android Permission Primers

Android applications must ask users for permission to use certain resources on the device, such as camera, storage or location. Sometimes, it is recommended that the application show an educational UI to the user that describes why a feature needs a particular permission. This guide will explain how to use Appcues to help with the permission rationale UI, also known as a permission primer. See the [Google guidelines](https://developer.android.com/training/permissions/requesting) for more information about requesting permissions.

The following example will show how an implementation with Appcues could work for an application that needs to request Camera permission. In this example, we'll follow the OS recommendations for when to show the permission primer, using the [`ActivityCompat.shouldShowRequestPermissionRationale`](https://developer.android.com/reference/androidx/core/app/ActivityCompat#shouldShowRequestPermissionRationale(android.app.Activity,java.lang.String)) function. 

## In-app configuration

1. Add the permission the application needs to the AndroidManifest.xml.

```xml
<manifest>
    <uses-permission android:name="android.permission.CAMERA" />
    <application>
        .
        .
    </application>
</manifest>
```

2. Update your existing `Appcues.identify()` call to set a user property with the current value for whether a permission rational should show. Having an up-to-date property allows for targeting an Appcues flow to the latest property value. This examples uses `showPermissionRationaleCamera` to track this value.

```kotlin
val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
appcues.identify(
    userId, 
    mapOf(
        // other user properties...
        "showPermissionRationaleCamera" to showRationale
    )
)
```

> 👉 If your application desires a different use case around when to show the primer, that can be configured as desired by simply adjusting the logic around setting the targeting property on the Appcues user in this step, and on step 4 below.

3. In the application code that needs the camera permission, define a permission request constant and then check for the permission status as shown.

```kotlin
const val CAMERA_PERMISSION_REQUEST_CODE = 1234

when {
    ContextCompat.checkSelfPermission(
        context, 
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED -> {
        // permission is granted, use camera feature
        performAction()
    }

    ActivityCompat.shouldShowRequestPermissionRationale(
        activity, 
        Manifest.permission.CAMERA
    ).not() -> {
        // permission is not granted yet && shouldShowRequestPermissionRationale returns false
        // request permission
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
    }

    // otherwise, permission is not granted, but we should show the rationale UI - we'll do nothing here,
    // as the Appcues flow targeted to this use case will show our rationale and then trigger the native
    // request prompt upon completion
}
```

> ⚠️ `ContextCompat` and `ActivityCompat` functions are only supported on API 23+. If your application targets devices on lower API levels, ensure this code is inside a conditional check based on the API level running on the device.

4. Implement the `OnRequestPermissionsResultCallback` interface, and check for the return of permission where the request code is `CAMERA_PERMISSION_REQUEST_CODE`. Update the Appcues user property `showPermissionRationaleCamera` accordingly. This ensures that the Appcues user profile is updated for targeting a permission primer flow, in the case of making a subsequent permission request after an initial ask was denied.

```kotlin
override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, use camera feature
            performAction() 
            // update the appcues user set the permission rational property to false - no need to ask any longer
            appcues.identify(userId, mapOf("showPermissionRationaleCamera" to false))
        } else {
            // permission was denied - check if we should show rationale on a subsequent ask            
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
            // update the appcues user set the permission rational property based on the result above
            appcues.identify(userId, mapOf("showPermissionRationaleCamera" to showRationale))
        }
    }
}
```

5. Register an AnalyticsListener on the Appcues instance. This listener will check for a specific event, `request-permission-camera`, which will be triggered by our permission primer flow. This event instructs our application code to proceed with the native permission prompt, after the primer flow has completed.

```kotlin
appcues.analyticsListener = object : AnalyticsListener {
    override fun trackedAnalytic(type: AnalyticType, value: String?, properties: Map<String, Any>?, isInternal: Boolean) {
        if (type == AnalyticType.EVENT && value == "request-permission-camera") {
            // listen for "request-permission-camera" EVENT that will be sent by the permission primer flow
            // and continue by requesting the permission in this application
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }
}
```

> 👉 Note that “request-permission-camera” is the name we have chosen to use for our custom event, and this will match the custom event we trigger in the Appcues permission primer flow we build below.

## Create the flow in Appcues Mobile Builder

1. In the Appcues Mobile Builder, create a new flow explaining why this permission is important for your feature. In our example, we're requesting camera permissions for the application, and explain the rationale using the flow below.

<p align="center">
  <img src="flow_example.png" width="400">
</p>

2. The `Continue` button on this flow will dismiss the flow. It should be updated to also track the `request-permission-camera` event we declared above in the Appcues `analyticsListener`. This is what will instruct the application code to make the native permission request.

<p align="center">
  <img src="flow_button_actions.png" width="400">
</p>

## Target the flow in Appcues Studio

1. Set the Audience targeting for the flow to “Specific users”. Select “User property”, “Show Permission Rationale Camera”, “equals”, true. This ensures only users who have previously been determined as eligible for seeing the permission rationale will see the flow. It will not show for users who have denied multiple times already and cannot be asked again. It will also not show for first time requests, in this use case, where the Android application does not consider the user eligible to see the rationale UI. The logic around when to show the rationale can be adjusted to meet your application's use cases.

<p align="center">
  <img src="flow_audience_target.png" width="800">
</p>

2. Configure the Trigger and Screen targeting rules for your use case.

3. Publish!
