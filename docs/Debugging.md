# Using the Appcues Debugger

The Appcues debugger is an in-app overlay that provides debug information in an accessible manner.

## Overview

The Appcues debugger can also be manually triggered apart from the custom URL scheme with a call to `appcues.debug()` from within your app.

## Floating Button

The Appcues debugger launches in its minimized state, represented as a floating button that initially appears in the bottom right corner of your screen. Tap the floating button to expand the debugger. Tap the floating button again to minimize.

The floating button can be dragged around the screen and docked in an unobtrusive location. While minimized, as events pass through the SDK, the event names are momentarily displayed next to the floating button with an icon representing the event type.

To dismiss the debugger entirely, drag the floating button to the center bottom of the screen where it will snap to the dismiss zone. Let it go and the debugger will close.

## Monitoring Status Details

The debugger include a section that provides an at-a-glimpse overview of Appcues status in your app.

### Device Info

The device info row includes the model identifier and Android version of the device. 

### SDK Info

The SDK version along with the Appcues Account ID and Appcues Application ID of the `Appcues` instance.

### Connection Status

Shows a checkmark if a connection has been made to the Appcues servers.

Tap the row to re-check the connection status.

> If there is a connection error, long-press the row to copy the detailed error information.

### Deeplink Configuration Status

Tap to check status. Shows a checkmark if the Appcues deeplink is properly configured. See <doc:URLSchemeConfiguring> for information about error messages.

### Screen Tracking Status

Shows a checkmark if a screen event has been observed since the debugger was launched. You may need to navigate to another screen in your app for the debugger to observe a screen event.

Tap the row to filter the Recent Events section to only show Screen events.

### User Identity Status

Shows a checkmark if there is a user identified for the current session.

Tap the row to filter the Recent Events section to only show User Profile events. There may not be an event to inspect if the user was identified before the debugger was launched.

> Long-press the row to copy the user ID value.

### Experience Status

If there is an experience currently showing in your app, the debugger will the name of the experience as well as the current step.

If an experience fails to show, the debugger will note it with "Content Omitted" and the error message describing why the experience was not presented.

## Inspecting Events

The Recent Events section of the debugger shows the list of all events that have passed through the Appcues SDK, with the most recent events at the top of the list. The list of events can be filtered by type by selecting the Filter icon in the header row and selecting an event type.

Session and Experience events are automatically tracked by the SDK. Screen (`appcues.screen(title, properties)` or `appcues.trackScreens()`), Custom (`appcues.track(name, properties)`), User Profile (`appcues.identify(userId, properties)` or `appcues.anonymous(properties)`), and Group (`appcues.group(groupId,properties)`) events are tracked by your app calling the Appcues SDK.

Tap an event row to see the details of that event including all the properties associated with it.
