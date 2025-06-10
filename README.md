# Appcues Android SDK
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/appcues/appcues-android-sdk/tree/main.svg?style=shield)](https://dl.circleci.com/status-badge/redirect/gh/appcues/appcues-android-sdk/tree/main)
[![maven](https://img.shields.io/maven-central/v/com.appcues/appcues)](https://repo1.maven.org/maven2/com/appcues/appcues/)
[![](https://img.shields.io/badge/-documentation-informational)](https://appcues.github.io/appcues-android-sdk)
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/appcues/appcues-android-sdk/blob/main/LICENSE)

Appcues Android SDK allows you to integrate Appcues experiences into your native Android apps.

The SDK is a Kotlin library for sending user properties and events to the Appcues API and retrieving and rendering Appcues content based on those properties and events.

- [Appcues Android SDK](#appcues-android-sdk)
  - [🚀 Getting Started](#-getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
      - [Standard](#standard-installation)
      - [Segment](#segment)
    - [One Time Setup](#one-time-setup)
      - [Initializing the SDK](#initializing-the-sdk)
      - [Supporting Builder Preview and Screen Capture](#supporting-builder-preview-and-screen-capture)
      - [Enabling Push Notifications](#enabling-push-notifications)
    - [Identifying Users](#identifying-users)
    - [Tracking Screens and Events](#tracking-screens-and-events)
    - [Anchored Tooltips](#anchored-tooltips)
    - [Embedded Experiences](#embedded-experiences)
    - [Custom Components](#custom-components)
  - [📝 Documentation](#-documentation)
  - [🎬 Samples](#-samples)
  - [👷 Contributing](#-contributing)
  - [📄 License](#-license)

## 🚀 Getting Started

### Prerequisites

Your application's `build.gradle` must have a `compileSdk` of 35+ and `minSdk` of 21+, and use Android Gradle Plugin (AGP) 8+.

```
android {
    compileSdk 35

    defaultConfig {
        minSdk 21
    }
}
```

Due to the SDK usage of Jetpack Compose, it is required to either:

1. apply `kotlin-android` plugin in app's build.gradle file.
   ```
   plugins {  
     id 'com.android.application' 
     id 'kotlin-android' 
   }
   ```
2. **OR** Update Android Gradle Plugin 8.4.0+

> [Related Google issue](https://issuetracker.google.com/issues/328687152) regarding usage of the Jetpack Compose dependency versions 1.6+

### Installation

Add the Appcues Android SDK dependency to your application. There are options for a standard installation, or for usage through a Segment plugin. An installation [tutorial video](https://appcues.wistia.com/medias/g6l6u32cjh) is also available for reference.

#### Standard Installation

The library is distributed through Maven Central. Add the appcues module to your build.gradle as a dependency as shown in the code sample below, and replace the `<latest_version>` with the [latest release version](https://github.com/appcues/appcues-android-sdk/releases) listed in this repository.

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.appcues:appcues:<latest_version>'
}
```

#### Segment

Appcues supports integration with Segment's [analytics-kotlin](https://github.com/segmentio/analytics-kotlin) library.  To install with Segment, you'll  use the [Segment Appcues plugin](https://github.com/appcues/segment-appcues-android).

 
### One Time Setup
 
After installing the package, you can reference Appcues Android SDK by importing the package with `import com.appcues.*`.

#### Initializing the SDK

An instance of the Appcues Android SDK should be initialized when your app launches. A lifecycle method such as the Application onCreate would be a common location:

```kotlin
override fun onCreate() {
    super.onCreate()
    appcues = Appcues(this, APPCUES_ACCOUNT_ID, APPCUES_APPLICATION_ID)
}
```

Initializing the SDK requires you to provide two values: `APPCUES_ACCOUNT_ID` and `APPCUES_APPLICATION_ID`. These values can be obtained from your [Appcues settings](https://studio.appcues.com/settings/account). Refer to the help documentation on [Registering your mobile app in Studio](https://docs.appcues.com/article/848-registering-your-mobile-app-in-studio) for more information.

#### Supporting Builder Preview and Screen Capture

During installation, follow the steps outlined in [Configuring the Appcues URL Scheme](https://github.com/appcues/appcues-android-sdk/blob/main/docs/URLSchemeConfiguring.md). This is necessary for the complete Appcues builder experience, supporting experience preview, screen capture and debugging. Refer to the [Debug Guide](https://github.com/appcues/appcues-android-sdk/blob/main/docs/Debugging.md) for details about using the Appcues debugger.

#### Enabling Push Notifications

During installation, follow the steps outlined in [Push Notification](https://github.com/appcues/appcues-android-sdk/blob/main/docs/PushNotification.md).

### Identifying Users

In order to target content to the right users at the right time, you need to identify users and send Appcues data about them. A user is identified with a unique ID.

- `identify(userId)`

After identifying a user, you can optionally associate that user with group.

- `group(groupId)`

### Tracking Screens and Events

Events are the “actions” your users take in your application, which can be anything from clicking a certain button to viewing a specific screen. Once you’ve installed and initialized the Appcues Android SDK, you can start tracking screens and events using the following methods:

- `track(name)`
- `screen(title)`

### Anchored Tooltips

Anchored tooltips use element targeting to point directly at specific views in your application. For more information about how to configure your application's views for element targeting, refer to the [Anchored Tooltips Guide](https://github.com/appcues/appcues-android-sdk/blob/main/docs/AnchoredTooltips.md).

### Embedded Experiences

Add `AppcuesFrameView` instances in your application layouts to support embedded experience content, with a non-modal presentation. For more information about how to configure your application layouts to use frame views, refer to the guide on [Configuring an AppcuesFrameView](https://github.com/appcues/appcues-android-sdk/blob/main/docs/AppcuesFrameViewConfiguring.md).

### Custom Components

Implement `AppcuesCustomComponentView` instances and register them with Appcues to take advantage of building your own components and using them in experiences. For more information refer to the guide on [Configuring an AppcuesCustomComponentView](https://github.com/appcues/appcues-android-sdk/blob/main/docs/AppcuesCustomComponentConfiguring.md)

Refer to the full [Getting Started Guide](https://github.com/appcues/appcues-android-sdk/blob/main/docs/GettingStarted.md) for more details.

## 📝 Documentation

SDK Documentation is available at https://appcues.github.io/appcues-android-sdk and full Appcues documentation is available at https://docs.appcues.com/

## 🎬 Samples

The [samples](https://github.com/appcues/appcues-android-sdk/tree/main/samples) directory in repository contains a full example Kotlin Android app providing references for usage of the Appcues API.

## 👷 Contributing

See the [contributing guide](https://github.com/appcues/appcues-android-sdk/blob/main/CONTRIBUTING.md) to learn how to get set up for development and how to contribute to the project.

## 📄 License

This project is licensed under the MIT License. See [LICENSE](https://github.com/appcues/appcues-android-sdk/blob/main/LICENSE) for more information.
