# Extending Appcues Experiences

Appcues Experiences are designed to be flexible and powerful without requiring any customization. However, customization is possible as part of the following patterns.

## Listening and Intercepting Experiences

An [`ExperienceListener`](https://github.com/appcues/appcues-android-sdk/blob/main/appcues/src/main/java/com/appcues/ExperienceListener.kt) can be registered with an `Appcues` instance, using the `AppcuesConfig` property `experienceListener(listener)`.  This listener will be informed whenever an experience will start or when an experience finishes.

An [`AppcuesInterceptor`](https://github.com/appcues/appcues-android-sdk/blob/main/appcues/src/main/java/com/appcues/AppcuesInterceptor.kt) can be registered with an `Appcues` instance, using the `AppcuesConfig` property `interceptor(interceptor)`.  This interceptor will be notified when an experience is being prepared to be shown, and can inform the Appcues SDK whether it is allowed to continue (return `true`) or not (return `false`).  A suspend function is used so that any other preparation the application needs to make to show the experience can be done asynchronously.
