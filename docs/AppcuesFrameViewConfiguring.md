# Configuring an AppcuesFrameView

Using `AppcuesFrameView` instances in your layouts will allow you to embed inline Appcues experience content in your app. This style of pattern is non-modal in the user's experience, differing from modals and tooltips used in mobile flows. Any number of desired embedded experiences can be rendered in the application at any given time.

## Using an AppcuesFrameView with XML Layouts

When using XML layouts, insert `com.appcues.AppcuesFrameView` instances wherever you would like Appcues embedded experience content to potentially appear. By default, these views will not take up any space in the rendered layout. Only when qualified experience content is targeted to these frames will they actually be visible. You can think of this process as reserving placeholder locations in your application UI for potential additional content. An `AppcuesFrameView` can also be used directly in your code, if programmatic view construction is required.

```xml
<com.appcues.AppcuesFrameView
    android:id="@+id/appcuesFrame1"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

Provide a distinct `android:id` for each `AppcuesFrameView` instance. This will be used later find the view instance, and register the frame with the Appcues SDK.

### Registering an AppcuesFrameView with the Appcues SDK

Once the frame has been added to the layout, register the view instance with the Appcues SDK, so that qualified experience content can be injected. Each frame should use a unique `frameId` (String). This identifier is used when building embedded experiences, informing Appcues the exact location in your app that the content should be targeted. This `frameId` can match the `android:id`, but it is not required.

Call the `appcues.registerEmbed(frameId: String, frame: AppcuesFrameView)` function to register each frame instance, when that view is loaded in you application.

```kotlin
appcues.registerEmbed("frame1", appcuesFrame1)
```

Once the frame views are registered, the integration is complete.

## Using AppcuesFrame with Jetpack Compose

The Appcues SDK also supports embedded experience content in layouts using Jetpack Compose. The `AppcuesFrame` Composable can be used to place a frame in your composition, passing the Appcues SDK instance and `frameId` values to register the view. No additional frame registration step is needed when using Jetpack Compose.

```kotlin
Column {
    ...
    AppcuesFrame(appcues = appcues, frameId = "frame1")
    ...
}
```

## Other Considerations

- The `frameId` registered with Appcues for each frame should ideally be globally unique in the application, but at least must be unique on the screen where experience content may be targeted.
- Some `AppcuesFrameView` instances may not be visible on the screen when it first loads, if they are lower down on a scrolling page, for instance. However, when they scroll into view, any qualified content on that screen will then render into that position.
- Using `AppcuesFrameView` in views with element re-use (ex. `RecyclerView`) is supported, but may require re-registering with a new `frameId` when elements are re-used, depending on your use case.
- If you are re-registering a frame in a non cell re-use situation where you don't need the content to re-render, setting the `retainContent` property to `false` on the `AppcuesFrameView` instance prevents having the same content re-render.
- When configuring settings for triggering embedded experience content, make sure that the experience is triggered on the same screen where the target `frameId` exists.
- To preview embedded content from the mobile builder inside your application, you may need to initiate the preview and then navigate to the screen where the target `frameId` exists.
