package com.appcues.analytics

import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceCompleted
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceDismissed
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceError
import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceStarted
import com.appcues.analytics.ExperienceLifecycleEvent.StepCompleted
import com.appcues.analytics.ExperienceLifecycleEvent.StepError
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.BUTTON_TAPPED
import com.appcues.analytics.ExperienceLifecycleEvent.StepRecovered
import com.appcues.analytics.ExperienceLifecycleEvent.StepSeen
import com.appcues.mocks.mockExperience
import com.appcues.statemachine.Error
import com.appcues.util.appcuesFormatted
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class ExperienceLifecycleEventTest {

    private val experience = mockExperience()

    @Test
    fun `StepSeen event testing`() {
        // given
        val event = StepSeen(experience, 1)
        // then
        assertThat(event).isInstanceOf(ExperienceLifecycleEvent::class.java)
        assertThat(event.name).isEqualTo("appcues:v2:step_seen")
        assertThat(event.properties).containsEntry("stepIndex", "0,1")
    }

    @Test
    fun `StepInteraction event testing`() {
        // given
        val interactionProperties = hashMapOf<String, Any>("a" to 1, "b" to 2)
        val event = StepInteraction(experience, 2, BUTTON_TAPPED, interactionProperties)
        // then
        assertThat(event).isInstanceOf(ExperienceLifecycleEvent::class.java)
        assertThat(event.name).isEqualTo("appcues:v2:step_interaction")
        assertThat(event.properties).containsEntry("stepIndex", "0,2")
        assertThat(event.properties).containsEntry("interactionType", "Button Tapped")
        assertThat(event.properties).containsEntry("interactionData", interactionProperties)
    }

    @Test
    fun `StepCompleted event testing`() {
        // given
        val event = StepCompleted(experience, 1)
        // then
        assertThat(event).isInstanceOf(ExperienceLifecycleEvent::class.java)
        assertThat(event.name).isEqualTo("appcues:v2:step_completed")
        assertThat(event.properties).containsEntry("stepIndex", "0,1")
    }

    @Test
    fun `StepError event testing`() {
        // given
        val error = Error.StepError(experience, 1, "test error", true)
        val event = StepError(error, experience)
        // then
        assertThat(event).isInstanceOf(ExperienceLifecycleEvent::class.java)
        assertThat(event.name).isEqualTo("appcues:v2:step_error")
        assertThat(event.properties).containsEntry("stepIndex", "0,1")
        assertThat(event.properties).containsEntry("message", "test error")
        assertThat(event.properties).containsEntry("errorId", error.id.appcuesFormatted())
    }

    @Test
    fun `StepRecovered event testing`() {
        // given
        val event = StepRecovered(experience, 1)
        // then
        assertThat(event).isInstanceOf(ExperienceLifecycleEvent::class.java)
        assertThat(event.name).isEqualTo("appcues:v2:step_recovered")
        assertThat(event.properties).containsEntry("stepIndex", "0,1")
    }

    @Test
    fun `ExperienceStarted event testing`() {
        // given
        val event = ExperienceStarted(experience)
        // then
        assertThat(event).isInstanceOf(ExperienceLifecycleEvent::class.java)
        assertThat(event.name).isEqualTo("appcues:v2:experience_started")
        assertThat(event.properties).doesNotContainKey("stepIndex")
    }

    @Test
    fun `ExperienceCompleted event testing`() {
        // given
        val event = ExperienceCompleted(experience)
        // then
        assertThat(event).isInstanceOf(ExperienceLifecycleEvent::class.java)
        assertThat(event.name).isEqualTo("appcues:v2:experience_completed")
        assertThat(event.properties).doesNotContainKey("stepIndex")
    }

    @Test
    fun `ExperienceDismissed event testing`() {
        // given
        val event = ExperienceDismissed(experience, 1)
        // then
        assertThat(event).isInstanceOf(ExperienceLifecycleEvent::class.java)
        assertThat(event.name).isEqualTo("appcues:v2:experience_dismissed")
        assertThat(event.properties).containsEntry("stepIndex", "0,1")
    }

    @Test
    fun `ExperienceError event testing`() {
        // given
        val error = Error.ExperienceError(experience, "test error")
        val event = ExperienceError(error, experience)
        // then
        assertThat(event).isInstanceOf(ExperienceLifecycleEvent::class.java)
        assertThat(event.name).isEqualTo("appcues:v2:experience_error")
        assertThat(event.properties).doesNotContainKey("stepIndex")
        assertThat(event.properties).containsEntry("message", "test error")
        assertThat(event.properties).containsEntry("errorId", error.id.appcuesFormatted())
    }

//    @Test
//    fun `ExperienceLifecycleEvent general testing`() {
//        // given
//    }
}
