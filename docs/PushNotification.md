# 📲 Push Notification

The Appcues Android SDK supports receiving push notifications, so you can reach your users whenever the moment is right.

## Prerequisites

[Configure your Android push settings in Appcues Studio](https://docs.appcues.com/en_US/push-notifications/push-notification-settings) before configuring push notifications in your app, to allow you quickly test your configuration end to end.

## Setting up

This guide assumes this is the first time code is being added to your project related to setting up push notifications using Google services (Firebase Cloud Messaging). In the case your project is already configured for push, proceed to Step 2.

### Step 1. Add Firebase

Follow the steps in the official Google documentation on [How to add Firebase to your project](https://firebase.google.com/docs/android/setup).

### Step 2. Add the Appcues Firebase Messaging Service

Firebase connects to your app through a `<service>`. Go to your Manifest file and add:

```xml
 <service
    android:name="com.appcues.AppcuesFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>


```

If your project has already added a child of FirebaseMessagingService, you can leave it as is, and instead plug in AppcuesFirebaseMessagingService to your service class.

```kotlin
import com.appcues.AppcuesFirebaseMessagingService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CustomFirebaseMessageService : FirebaseMessagingService() {
override fun onMessageReceived(message: RemoteMessage) {
	if (AppcuesFirebaseMessagingService.handleMessage(baseContext, message)) {
		// handled as Appcues message
		return
	}

	// not Appcues message
	super.onMessageReceived(message)
}

	override fun onNewToken(token: String) {
		// sets new token from the callback
		AppcuesFirebaseMessagingService.setToken(token)

		super.onNewToken(token)
	}
}


```

### Step 3. Customizing

The Appcues SDK allows for some customizations that will change how your messages will be delivered to your end users. These properties are set as `<resources>` properties. In order change those properties create a file under `res/values` and set it to desired values.

```xml
<resources>
	<!-- notification icon -->
	<drawable name="appcues_notification_small_icon" />
	<!-- notification primary color -->
	<color name="appcues_notification_color" />
	<!-- notification channel (different id means new channel in app's settings) -->
	<string name="appcues_notification_channel_id" />
	<!-- notinotification name (text will be displayed in app's settings -->
	<string name="appcues_notification_channel_name" />
	<!-- notification description (text will be displayed in app's settings -->
	<string name="appcues_notification_channel_description" />
	 <!-- int ranging from 0 to 5, where 0 means NONE, and 5 is MAX importance -->
	 <integer name="appcues_notification_channel_importance" />
</resources>


```

To check default values see [appcues.xml](https://github.com/appcues/appcues-android-sdk/blob/main/appcues/src/main/res/values/appcues.xml)

## Debugging Push Notifications

The Appcues Debugger can validate your push notification setup end-to-end.

Tap the **Appcues Push** row in the Appcues Debugger to check your configuration and send a test push notification. When the configuration is completed you should see an Appcues test push and when tapping the debugger will green check (✅) this row

### Debugger Error Codes

|Error Code |Fix|
|-----------|---|
|1          |The Appcues SDK did not find the required FirebaseMessagingService from reading your manifest file.|
|2          |Permission to show notification is not enabled, you can tap Appcues Push row and change apps permission in app's settings page.|
|3          |The Appcues SDK did not receive push token (re-check your installation to and make sure Firebase service is properly setup).|
|4          |Something happened on the server side, try again later.|
|5          |Usually this is just a warning when the validation push is ignored or dismissed. In case your notification did not show, re-check your installation, make sure that when using a custom FirebaseMessagingService you must include AppcuesFirebaseMessagingService.handleMessage(..) call properly.|
|`<3-digit>`|3-digit error codes indicate an error from the server sending a test push and usually are not caused by incorrect configuration of the Appcues SDK. If a server issue is suspected, wait a few minutes and try again or contact Appcues support.|
