package com.appcues

import android.net.Uri

/**
 * An handler that can be supplied by the host application to control behaviors around link navigation.
 */
public interface NavigationHandler {

    /**
     * Navigates to the given uri, suspending until navigation has completed. The navigation path
     * value in `uri` may be a scheme link (deep link) inside of the current application, or an
     * external web or application link.
     *
     * @param uri the uri for the destination to navigate.
     * @return true if the navigation completed successfully, false if not.
     */
    public suspend fun navigateTo(uri: Uri): Boolean
}
