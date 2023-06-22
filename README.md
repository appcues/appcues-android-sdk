# Appcues Android SDK
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/appcues/appcues-android-sdk/tree/main.svg?style=shield)](https://dl.circleci.com/status-badge/redirect/gh/appcues/appcues-android-sdk/tree/main)
[![maven](https://img.shields.io/maven-central/v/com.appcues/appcues)](https://repo1.maven.org/maven2/com/appcues/appcues/)
[![](https://img.shields.io/badge/-documentation-informational)](https://appcues.github.io/appcues-android-sdk)
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/appcues/appcues-android-sdk/blob/main/LICENSE)

Appcues Android SDK allows you to integrate Appcues experiences into your native Android apps.

The SDK is a Kotlin library for sending user properties and events to the Appcues API and retrieving and rendering Appcues content based on those properties and events.

- [Appcues Android SDK](#appcues-android-sdk)
  - [🚀 Getting Started](#-getting-started)
    - [Installation](#installation)
      - [Standard](#standard-installation)
      - [Segment](#segment)
    - [One Time Setup](#one-time-setup)
      - [Initializing the SDK](#initializing-the-sdk)
      - [Supporting Debugging and Experience Previewing](#supporting-debugging-and-experience-previewing)
    - [Identifying Users](#identifying-users)
    - [Tracking Screens and Events](#tracking-screens-and-events)
    - [Anchored Tooltips](#anchored-tooltips)
  - [🛠 Customization](#-customization)
  - [📝 Documentation](#-documentation)
  - [🎬 Samples](#-samples)
  - [👷 Contributing](#-contributing)
  - [📄 License](#-license)

## 🚀 Getting Started

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

#### Supporting Debugging and Experience Previewing

During installation, follow the steps outlined in [Configuring the Appcues URL Scheme](https://github.com/appcues/appcues-android-sdk/blob/main/docs/URLSchemeConfiguring.md). This is necessary for the optimal Appcues builder experience, to support debugging and experience preview. Refer to the [Debug Guide](https://github.com/appcues/appcues-android-sdk/blob/main/docs/Debugging.md) for details about using the Appcues debugger.

### Identifying Users

In order to target content to the right users at the right time, you need to identify users and send Appcues data about them. A user is identified with a unique ID.

- `identify(userId)`

### Tracking Screens and Events

Events are the “actions” your users take in your application, which can be anything from clicking a certain button to viewing a specific screen. Once you’ve installed and initialized the Appcues Android SDK, you can start tracking screens and events using the following methods:

- `track(name)`
- `screen(title)`

### Anchored Tooltips

Anchored tooltips use element targeting to point directly at specific views in your application. For more information about how to configure your application's views for element targeting, refer to the [Anchored Tooltips Guide](https://github.com/appcues/appcues-android-sdk/blob/main/docs/AnchoredTooltips.md).

Refer to the full [Getting Started Guide](https://github.com/appcues/appcues-android-sdk/blob/main/docs/GettingStarted.md) for more details.

## 🛠 Customization

Refer to the [Extending Guide](https://github.com/appcues/appcues-android-sdk/blob/main/docs/Extending.md) for details.

## 📝 Documentation

SDK Documentation is available at https://appcues.github.io/appcues-android-sdk and full Appcues documentation is available at https://docs.appcues.com/

## 🎬 Samples

The [samples](https://github.com/appcues/appcues-android-sdk/tree/main/samples) directory in repository contains a full example Kotlin Android app providing references for usage of the Appcues API.

## 👷 Contributing

See the [contributing guide](https://github.com/appcues/appcues-android-sdk/blob/main/CONTRIBUTING.md) to learn how to get set up for development and how to contribute to the project.

## 📄 License

This project is licensed under the MIT License. See [LICENSE](https://github.com/appcues/appcues-android-sdk/blob/main/LICENSE) for more information.
