# Understanding the Experience Trait System

Experiences displayed by the Appcues Android SDK are customizable and extensible via a flexible system of Experience Traits.

## Overview

An ``ExperienceTrait`` modifies the how an entire experience, or a particular step in an experience is displayed. 
Each type of ``ExperienceTrait`` has the capability to apply specific changes to the rendered content in an experience.
Also, some traits are applied to the whole experience, some to a set of steps, and others are specific to a step.

## Trait Capabilities

An experience trait must adopt at least one of the following capabilities to have any effect, and may adopt more than one for more complex functionality.

Each trait type is applied to a specific part of the [composition](https://developer.android.com/jetpack/compose). Some types of traits only allow a single instance to be applied to rendered content, for example a `PresentingTrait`. Other types, for example `StepDecorating`, can have multiple instances applied to the same rendered content, in which case they are applied in sequence.

<p align="center">
  <img src="traits_composition.png" width="400">
</p>

### Presenting

A ``PresentingTrait`` is the trait responsible for providing the view in which the experience will be shown. by default we are starting our `AppcuesActivity` that will orchestrate all other traits to ensure the proper order and placement of elements are correct.

> Only a single ``PresentingTrait`` will be applied in the process of displaying an experience step even if multiple are defined.

> In the current version is not viable to customize a PresentingTrait since there is a lot of internal dependencies that are needed for it to work properly.

### Backdrop Decorating

A  ``BackdropDecoratingTrait`` draws content behind the content wrapper.

> Not all experiences will include a backdrop, and a ``BackdropDecoratingTrait`` will not be invoked if the experience does not include a backdrop.
> Multiple ``BackdropDecoratingTrait`` will be applied in sequence.

### Content Wrapping

A ``ContentWrappingTrait`` is responsible for wrapping the content in a container that will be presented to the user. It is possible to create different types of containers for your content other than dialogs, bottom sheet, full screen like the ones we already have.

> Only a single ``ContentWrappingTrait`` will be applied in the process of displaying an experience step even if multiple are defined.

### Content Holder

A ``ContentHolderTrait`` is responsible for organizing all steps within that wrapped content. by default we will only show the first step and only by using an action user will be able to move to the next one, but in case of the carousel its possible to swipe through the steps in that wrapper content.

### Step Decorating

A ``StepDecoratingTrait`` draws content on top of the content in that particular step. For example, positioning sticky content overlaid on top.

### Container Decorating

A ``ContainerDecoratingTrait`` draws content on top of the content wrapper. It is used to overlay content on top of all steps within container, for example a close button.

## Experience-Level, Group-Level, and Step-Level Traits

The Appcues mobile experience data model allows for traits to be specified at the experience level, at the step-group level, or at the step level. Experience-level traits modify the entire experience and are applied when any step of the experience is being displayed. Group-level traits apply when any of child steps of the group is being displayed. Step-level traits are scoped to be applied only when the specific step is being displayed.

In practice this distinction looks like this in the experience data model:

```json
{
    ...
    "traits": [
        // Experience-level traits
    ],
    "steps": [
        {
            ...
            "traits": [
                // Group-level traits
            ],
            "children": [
                {
                    ...
                    "content": { ... },
                    "traits": [
                        // Step-level traits for the first step
                    ]
                }
            ]
        },
        {
            ...
            "content": { ... },
            "traits": [
                // Step-level traits for the second step
            ]
        }
    ]
}
```
