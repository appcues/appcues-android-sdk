# Appcues Android SDK

Appcues Android SDK allows you to integrate Appcues experiences into your native Android apps.

The SDK is a Kotlin library for sending user properties and events to the Appcues API and retrieving and rendering Appcues content based on those properties and events.

- [Appcues Android SDK](#appcues-android-sdk)
  - [🚀 Getting Started](#-getting-started)
    - [Installation](#installation)
      - [Segment](#segment)
      - [Gradle](#gradle)
    - [One Time Setup](#one-time-setup)
      - [Initializing the SDK](#initializing-the-sdk)
  - [📝 Documentation](#-documentation)
  - [🎬 Samples](#-samples)
  - [👷 Contributing](#-contributing)
  - [📄 License](#-license)

## 🚀 Getting Started

### Installation

Add the Appcues Android SDK package to your app. There are several supported installation options.

#### Segment

Will be implemented soon

#### Gradle

Will be implemented soon
 
### One Time Setup
 
After installing the package, you can reference Appcues Android SDK by importing the package with `import com.appcues.*`.

#### Initializing the SDK

An instance of the Appcues Android SDK should be initialized when your app launches. A lifecycle method such as the Application onCreate in would be a common location:

```kotlin
override fun onCreate() {
    super.onCreate()

    appcues = Appcues.Builder(this, APPCUES_ACCOUNT_ID, APPCUES_APPLICATION_ID)
        .logging(Appcues.LoggingLevel.BASIC)
        .build()
}
```

Initializing the SDK requires you to provide two values: `APPCUES_ACCOUNT_ID` and `APPCUES_APPLICATION_ID`. These values can be obtained from your [Appcues settings](https://studio.appcues.com/settings/account).

## 📝 Documentation

Full documentation is available at https://docs.appcues.com/

## 🎬 Examples

The [samples](https://github.com/appcues/appcues-android-sdk/tree/main/samples) directory in repository contains full example Android apps providing references for usage of the Appcues API.

## 👷 Contributing

See the [contributing guide](https://github.com/appcues/appcues-android-sdk/blob/main/CONTRIBUTING.md) to learn how to get set up for development and how to contribute to the project.

## 📄 License

This project is licensed under the MIT License. See [LICENSE](https://github.com/appcues/appcues-android-sdk/blob/main/LICENSE) for more information.
