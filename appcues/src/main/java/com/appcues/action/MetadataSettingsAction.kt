package com.appcues.action

/**
 * Whenever an ExperienceAction class implements this interface
 * the SDK will consider that action when getting the information
 * to track a step_interaction event.
 *
 * Note: the SDK consider the last MetadataSettingsAction class from
 *       the action list to capture the interaction for that
 *       primitive, meaning all other MetadataSettingsActions in the list
 *       of actions will be ignored.
 */
internal interface MetadataSettingsAction {

    val category: String
    val destination: String
}
