# Finding and using fonts

Appcues allows for customers to use system and custom fonts when rending flows. 

## Overview

Available fonts can be found using the Appcues debugger in the `Available fonts`, 
in some cases in order for Appcues to find those fonts customer may need to provide extra configuration property.

## How it works

Appcues will look for fonts in `assets/fonts` and `res/fonts`. but when placing fonts in `res/fonts` the Android compiler will create an ID linking that font.

Both during the scan (Debugger) or during usage in any flow, Appcues relies on the font name provided in the builder and it will try to find the corresponding font available in the app.

## Multi-module apps or custom App ids

One of the main issues with our approach is that those paths sometimes are tied to the appId (package) where the font is located, the only path we can automatically know is the one provided from `context.packageName`
And placing a font in a different package makes it hard for the SDK to find it, this is the reason why we provide with a config option `AppcuesConfig.packageNames` that allows customer to provide the locations we should scan for fonts.

```kotlin
appcues = Appcues(this, APPCUES_ACCOUNT_ID, APPCUES_APPLICATION_ID) {
    packageNames = listOf("com.appcues.samples.kotlin.module")
}
```
