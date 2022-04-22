# Extending Appcues Experiences

Appcues Experiences are designed to be flexible and powerful without requiring any customization. However, customization is possible as part of the following patterns.

## Custom Experience Actions

An [`ExperienceAction`](https://github.com/appcues/appcues-android-sdk/blob/main/appcues/src/main/java/com/appcues/action/ExperienceAction.kt) is a behavior triggered from an interaction with an experience, for example tapping a button.

An action can be registered with `appcues.registerAction(type, factory)`.

## Custom Experience Traits

An [`ExperienceTrait`](https://github.com/appcues/appcues-android-sdk/blob/main/appcues/src/main/java/com/appcues/trait/ExperienceTrait.kt) modifies the how an entire experience, or a particular step in an experience is displayed. A trait has capabilities that modify the way an experience is displayed to the user.

A trait can be registered with `appcues.registerTrait(type, factory)`.


## Listening and Intercepting Experiences

An [`ExperienceListener`](https://github.com/appcues/appcues-android-sdk/blob/main/appcues/src/main/java/com/appcues/ExperienceListener.kt) can be registered with an `Appcues` instance, using the `Appcues.Builder` function `experienceListener(listener)`.  This listener will be informed whenever an experience will start or when an experience finishes.

An [`AppcuesInterceptor`](https://github.com/appcues/appcues-android-sdk/blob/main/appcues/src/main/java/com/appcues/AppcuesInterceptor.kt) can be registered with an `Appcues` instance, using the `Appcues.Builder` function `interceptor(interceptor)`.  This interceptor will be notified when an experience is being prepared to be shown, and can inform the Appcues SDK whether it is allowed to continue (return `true`) or not (return `false`).  A suspend function is used so that any other preparation the application needs to make to show the experience can be done asynchronously.