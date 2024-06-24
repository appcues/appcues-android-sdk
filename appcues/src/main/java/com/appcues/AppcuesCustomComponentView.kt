package com.appcues

import android.view.ViewGroup

/**
 * TODO document this
 */
public interface AppcuesCustomComponentView {

    /**
     * TODO document this
     */
    public fun getView(actionsController: AppcuesExperienceActions, config: Map<String, Any>?): ViewGroup
}
