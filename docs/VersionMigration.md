# 2.x to 3.0 Migration Guide

## Overview

Updating to this release will not require any code changes for most SDK installations.  Code changes would only be required if the app implemented custom experience actions or traits.

## Custom Action and Trait Changes

### Removed

- To simplify the public API, `ExperienceAction` and `ExperienceTrait` related classes and functions no longer have public visibility. There no current use cases for extensibility that require these to be public. This change impacts items in the `com.appcues.action` and `com.appcues.trait` packages.
