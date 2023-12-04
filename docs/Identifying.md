# Identifying and Managing Users

In order to target content to the right users at the right time, you need to identify users and send Appcues data about them.

## Identifying Known Users

> Appcues recommends choosing opaque and hard to guess user IDs, such as a UUID. See the [FAQ for Developers](https://docs.appcues.com/article/159-faq#choosing-a-user-id) for more details about how to choose a User ID.

```kotlin
appcues.identify(userId, properties)
```

It is recommended that the application identify a user at moments such as sign in, and also when the app starts up on a cold launch with existing log in credentials. This will ensure that any user properties tied to this user can be kept accurately up to date.

The inverse of identifying is resetting. For example, if a user logs out of your app. Calling `reset()` will disable tracking of screens and events until a user is identified again.

### Identity Verification
If your Appcues account is configured for identity verification, pass the user signature in the properties included on the `identify(userId, properties)` call. Use the key "appcues:user_id_signature" and the string value of the signature.

```kotlin
appcues.identify(userId, mapOf("appcues:user_id_signature" to signature))
```

This signature will be used in an Authorization header on network requests from the SDK. The Appcues API will use this signature to verify that the requests from the client are authorized using an SDK key configured in Appcues Studio.

## Identifying Anonymous Users

You can target users during anonymous usage in your application using `anonymous()` instead of `identify()`.  This can have implications on your billing based on active user counts. The format of anonymous IDs can be customized during initialization with the `AppcuesConfig`, using the `anonymousIdFactory` property. Anonymous IDs will always be prefixed with `anon:` by the SDK.

## Grouping Users

Associating a user with a group allows you to additionally capture analytics at the group level, and target content to show based upon group membership and properties.

```kotlin
appcues.group(groupId, properties)
```
