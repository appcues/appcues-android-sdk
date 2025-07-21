# Configuring Views for Anchored Tooltips

The Appcues Android SDK supports anchored tooltips targeting any `android.view.View`, typically used in XML layouts, as well as `@Composable` views built with Jetpack Compose.

For many common elements like top level navigation tabs or buttons, the view may already be selectable using the resource name defined in the XML layout with no additional changes necessary. For other views, it may be necessary to make very simple updates in the code to make that view identifiable.

The identifiers described below are used to create a mobile view selector when a screen layout is captured for usage in the Mobile Builder. This selector is used by the Appcues Mobile Builder to create and target anchored tooltips. When a user qualifies for a flow, this selector is used to render the anchored tooltip content.

## Instrumenting Android Views

The following `android.view.View` properties are used to identify elements, in order of precedence:

* [`android:id`](https://developer.android.com/reference/android/view/View#attr_android:id) - specifically, the name of the identifier is used, not the potentially changing integer value. This value is only captured for View elements that are considered interactive - defined by having [`clickable`](https://developer.android.com/reference/android/view/View#attr_android:clickable) set to true.
* [`android:tag`](https://developer.android.com/reference/android/view/View#attr_android:tag) - allows for setting any arbitrary String value to associate with this view.
* [`android:contentDescription`](https://developer.android.com/reference/android/view/View#attr_android:contentDescription) - a property used in assistive applications, such as TalkBack, to convey information to users with disabilities to help them use the app. In addition to being a good practice to use to support core accessibility features on Android, it can also help with Appcues element targeting. It is considered a lower priority property for selector usage, however, due to the fact that the descriptive strings can often be non-unique throughout the application and they may also be localized to the user's preferred language and not maintain consistency with the text used when the flow was built.

At least one identifiable property must be set. Not all are required. The best way to ensure great performance of Android anchored tooltips in Appcues is to set a unique resource `android:id` on each interactive `View` element that may be targeted, in the XML layout. For non-interactive elements, use a unique `android:tag` value.

These properties are available on any `android.view.View` derived type, and can be set in layout XML as follows (omitting other unrelated attributes typically set in XML for brevity):

```xml
<Button
    android:id="@+id/btnSaveProfile"
    ...
    android:contentDescription="Save"
    android:tag="btnSaveProfile" />
```

## Instrumenting Jetpack Compose Views

Android Applications using Jetpack Compose to render UI can also identify Composable view elements. A custom Modifier, `.appcuesView(tag)`, is provided by the Appcues Android SDK to support this use case. Chain this modifier onto any views that should be eligible for targeting anchored tooltips in your application.

The `tag` String value must be unique on the screen where an anchored tooltip may be targeted.

The following example shows how this would be added to a `androidx.compose.material.Button` Composable:

```kotlin
Button(
    modifier = Modifier
        .padding(8.dp)
        .width(50.dp)
        .background(Color.Blue)
        .appcuesView("btnSaveProfile"), 
    onClick = { }
) {
    Text("Save Profile")
}
```

Note that Applications using Compose can also embed additional `android.view.View` content inside of Composables, using the provided [interop features](https://developer.android.com/jetpack/compose/migrate/interoperability-apis/views-in-compose). Those Views should be identified just like any other `android.view.View` as described in the section above, and they will also be available for targeting anchored tooltips.

## Instrumenting Web Views

HTML content loaded in a `WebView` is eligible for use with anchored tooltips. Any visible element with an `id` attribute or a `data-appcues-id` attribute will be selectable.

```html
<button id="my-button" type="button">My Button</button>
<div data-appcues-id="some-id">...</div>
```


## Other Considerations

### Selector Uniqueness
Ensure that view identifiers used for selectors are unique within the visible views on the screen at the time an anchored tooltip is attempting to render. If no unique match is found, the Appcues flow will terminate with an error. It is not required that selectors are globally unique across the application, but they must be on any given screen layout.

Using multiple selector properties is another way to ensure uniqueness. For instance, if two views in a layout have the same resource `android:id`, but different `android:tag` values, a selector will be able to find the unique match by finding the element that matches both properties exactly.

### Consistent View Identifiers
Maintain consistency with view identifiers as new versions of the app are released. For example, if a key navigation tab was using an identifier like "Home Tab" in several versions of the application, then changed to "Home" - this would break the ability for selectors using "Home Tab" to be able to find that view and target a tooltip in the newer versions of the app. You could build multiple flows targeting different versions of the application, but it helps keep things simplest if consistent view identifiers can be maintained over time.
