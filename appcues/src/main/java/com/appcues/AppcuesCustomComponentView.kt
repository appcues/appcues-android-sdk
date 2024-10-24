package com.appcues

import android.view.ViewGroup

/**
 * AppcuesCustomComponentView is an interface to be implemented when registering a new custom component linked to an identifier.
 *
 * (static call) Appcues.registerCustomComponent("identifier", appcuesCustomComponentViewImpl)
 */
public interface AppcuesCustomComponentView {

    /**
     * Returns a ViewGroup that will be inflated in the experience block for custom component matching the registered identifier.
     *
     * Its possible to use Compose by inflating a ComposeAndroidView and working with compose from there.
     *
     * @param actionsController provides control over experience and appcues related actions.
     * @param config Map containing properties filled when specifying this custom component in builder.
     *
     * @return ViewGroup with custom layout
     */
    public fun getView(actionsController: AppcuesExperienceActions, config: Map<String, Any>?): ViewGroup
}
