# Appcues Android SDK
[![CircleCI](https://circleci.com/gh/appcues/appcues-android-sdk.svg?style=shield&circle-token=d80a6bd72e608b1b62c104c87bfcfe8ea3faa7c1)](https://circleci.com/gh/appcues/appcues-android-sdk)
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/appcues/appcues-android-sdk/blob/main/LICENSE)

>NOTE: This is a pre-release project for testing as a part of our mobile beta program. If you are interested in learning more about our mobile product and testing it before it is officially released, please [visit our site](https://www.appcues.com/mobile) and request early access.  
>
>If you have been contacted to be a part of our mobile beta program, we encourage you to try out this library and  provide feedback via Github issues and pull requests.  Please note this library will not operate if you are not part of the mobile beta program.

Appcues Android SDK allows you to integrate Appcues experiences into your native Android apps.

The SDK is a Kotlin library for sending user properties and events to the Appcues API and retrieving and rendering Appcues content based on those properties and events.

- [Appcues Android SDK](#appcues-android-sdk)
  - [🚀 Getting Started](#-getting-started)
    - [Installation](#installation)
      - [Standard](#standard-installation)
      - [Segment](#segment)
    - [One Time Setup](#one-time-setup)
      - [Initializing the SDK](#initializing-the-sdk)
  - [📝 Documentation](#-documentation)
  - [🎬 Samples](#-samples)
  - [👷 Contributing](#-contributing)
  - [📄 License](#-license)

## 🚀 Getting Started

### Installation

Add the Appcues Android SDK dependency to your application. There are options for a standard installation, or for usage through a Segment plugin.

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

Initializing the SDK requires you to provide two values: `APPCUES_ACCOUNT_ID` and `APPCUES_APPLICATION_ID`. These values can be obtained from your [Appcues settings](https://studio.appcues.com/settings/account).

#### Supporting Debugging and Experience Previewing

Supporting debugging and experience previewing is not required for the Appcues Android SDK to function, but it is necessary for the optimal Appcues builder experience. Refer to the [Debug Guide](https://github.com/appcues/appcues-android-sdk/blob/main/Debugging.md) for details.

### Identifying Users

In order to target content to the right users at the right time, you need to identify users and send Appcues data about them. A user is identified with a unique ID.

- `identify(userId)`

### Tracking Screens and Events

Events are the “actions” your users take in your application, which can be anything from clicking a certain button to viewing a specific screen. Once you’ve installed and initialized the Appcues Android SDK, you can start tracking screens and events using the following methods:

- `track(name)`
- `screen(title)`

Refer to the full [Getting Started Guide](https://github.com/appcues/appcues-android-sdk/blob/main/docs/GettingStarted.md) for more details.

## 🛠 Customization

Refer to the [Extending Guide](https://github.com/appcues/appcues-android-sdk/blob/main/docs/Extending.md) for details.

## 📝 Documentation

Full documentation is available at https://docs.appcues.com/

## 🎬 Samples

The [samples](https://github.com/appcues/appcues-android-sdk/tree/main/samples) directory in repository contains a full example Kotlin Android app providing references for usage of the Appcues API.

## 👷 Contributing

See the [contributing guide](https://github.com/appcues/appcues-android-sdk/blob/main/CONTRIBUTING.md) to learn how to get set up for development and how to contribute to the project.

## 📄 License

This project is licensed under the MIT License. See [LICENSE](https://github.com/appcues/appcues-android-sdk/blob/main/LICENSE) for more information.
