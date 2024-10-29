package com.appcues

/**
 * AppcuesExperienceActions is the controller that allows custom component access useful appcues and experience calls.
 */
// Suppressing until figure out a way to make this class more concise
@Suppress("LongParameterList")
public interface AppcuesExperienceActions {

    /**
     * Manually trigger all actions defined in studio for this custom block
     */
    public fun triggerBlockActions()

    /**
     * Move to the next step or completes the experience if called from the last step
     */
    public fun nextStep()

    /**
     * Move to a previous step if any
     */
    public fun previousStep()

    /**
     * Closes the experience
     *
     * @param markComplete if set to true signals that the experience completed successfully.
     *                     leaving it as false (default) means that this experience is not completed
     *                     analytics wise, like when it was dismissed by tapping on a "X" close
     */
    public fun close(markComplete: Boolean = false)

    /**
     * Track an action taken by a user.
     *
     * @param name Name of the event.
     * @param properties Optional properties that provide additional context about the event.
     */
    public fun track(name: String, properties: Map<String, Any>? = null)

    /**
     * Updates user profile.
     *
     * @param properties properties that provide additional context about the user.
     */
    public fun updateProfile(properties: Map<String, String>)
}
