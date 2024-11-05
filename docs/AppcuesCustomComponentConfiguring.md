# Configuring an AppcuesCustomComponent

An `AppcuesCustomComponentView` allows your app to define a custom component that can be rendered in an experience containing a custom component block using the registered `identifier` for the component.

## Implementing AppcuesCustomViewComponentView

```kotlin
internal class ExampleCustomComponentView(private val context: Context) : AppcuesCustomComponentView {

    override val debugConfig: Map<String, Any> = mapOf("user_name" to "John")
    
    override fun getView(actionsController: AppcuesExperienceActions, config: Map<String, Any>?): ViewGroup {
        val userName = config?.get("user_name") as? String
        val textView = TextView(context).apply { text = "Hello World $userName" } 
        
        // define a viewGroup and its children
        return LinearLayout(context).apply {
            addView(textView)
        }
    }
}
```

> When working with compose its possible to inflate an AndroidComposeView and compose your view from there.

### Registering an AppcuesCustomViewComponentView with the Appcues SDK

Registering a custom view is made statically through the call of `Appcues.registerCustomComponent(identifier, customView)`, make sure the identifier is an unique string when registering multiple custom components

```kotlin
val customView = ExampleCustomComponentView(context)

Appcues.registerCustomComponent("customView1", customView)
```

## Other Considerations

* All registered custom view are listed in the `Debugger` under the `Plugins` section.
* When config options are used to the custom view, filling the `debugConfig` helps non-developers in the team to validate how the custom component works.
