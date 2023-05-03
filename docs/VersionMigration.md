# 1.x to 2.0 Migration Guide

## Overview

Updating to this release will not require any code changes for most SDK installations.  Code changes would only be required if your app made anonymous calls with user properties, or if the app implemented custom experience traits or actions.

## General Changes

### Removed

- Properties are no longer supported for anonymous users. `Appcues.anonymous(properties: Map<String, Any>?)` has been superseded by `Appcues.anonymous()`. If you require properties, use `Appcues.identify(userId: String, properties: Map<String, Any>? = null)`.

## Custom Action and Trait Changes

### Added

- `MetadataSettingTrait` interface introduced in 2.0, is responsible for `fun produceMetadata(): Map<String, Any?>` to share information between traits.

### Changed

- `PresentingTrait` can now throws `AppcuesTraitException::class`
- `AppcuesTraitException` is added to provide more information regarding exceptions related to the trait system.
- `BackdropDecoratingTrait` method `fun BoxScope.Backdrop()` changed to `fun BoxScope.BackdropDecorate(content: @Composable BoxScope.() -> Unit)` and now one BackdropDecorate wraps the next one instead of each drawing on top of the next.
- `ContainerDecoratingTrait` method `fun BoxScope.DecorateContainer()` changed to `fun BoxScope.DecorateContainer(containerPadding: PaddingValues, safeAreaInsets: PaddingValues)`.
- `ContentWrappingTrait` method `fun WrapContent(content: @Composable (hasFixedHeight: Boolean, contentPadding: PaddingValues?) -> Unit)` changed to `fun WrapContent(content: @Composable (modifier: Modifier, containerPadding: PaddingValues, safeAreaInsets: PaddingValues) -> Unit)`.
- `StepDecoratingTrait` method `fun BoxScope.DecorateStep(stepDecoratingPadding: StepDecoratingPadding)` changed to `fun BoxScope.DecorateStep(containerPadding: PaddingValues, safeAreaInsets: PaddingValues, stickyContentPadding: StickyContentPadding)`.
- `StepDecoratingPadding` renamed to `StickyContentPadding`.
- `ModalTrait` is now `PresentingTrait`. it is responsible for presenting the container that will hold the modal type presentation.
- `ExperienceAction` method `suspend fun execute(appcues: Appcues)` changed to `suspend fun execute()`.
