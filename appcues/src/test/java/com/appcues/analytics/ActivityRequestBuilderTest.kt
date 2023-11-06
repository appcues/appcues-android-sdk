package com.appcues.analytics

import com.appcues.AppcuesConfig
import com.appcues.Storage
import com.appcues.data.remote.appcues.request.ActivityRequest
import com.appcues.data.remote.appcues.request.EventRequest
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verifySequence
import org.junit.Test
import java.util.UUID

internal class ActivityRequestBuilderTest {

    private val activityRequestSlot = slot<ActivityRequest>()
    private val eventRequestSlot = slot<EventRequest>()

    private val config: AppcuesConfig = mockk<AppcuesConfig>(relaxed = true).apply {
        every { accountId } returns "accountId"
    }

    private val storage: Storage = mockk<Storage>(relaxed = true).apply {
        every { userId } returns "userId"
        every { groupId } returns "groupId"
        every { userSignature } returns "user-signature"
    }

    private val autoPropertyDecorator: AutoPropertyDecorator = mockk<AutoPropertyDecorator>().apply {
        every { decorateIdentify(capture(activityRequestSlot)) } returns mockk()
        every { decorateTrack(capture(eventRequestSlot)) } returns mockk()
        every { autoProperties } returns hashMapOf("auto" to "properties")
        every { decorateGroup(capture(activityRequestSlot)) } returns mockk()
    }

    private val activityRequestBuilder = ActivityRequestBuilder(
        config = config,
        storage = storage,
        decorator = autoPropertyDecorator,
    )

    @Test
    fun `identify SHOULD call decorator WITH proper ActivityRequest`() {
        // given
        val properties = hashMapOf("_test" to "test")
        val sessionId = UUID.randomUUID()
        // when
        activityRequestBuilder.identify(sessionId, properties)
        // then
        with(activityRequestSlot.captured) {
            assertThat(userId).isEqualTo("userId")
            assertThat(groupId).isEqualTo("groupId")
            assertThat(accountId).isEqualTo("accountId")
            assertThat(this.sessionId).isEqualTo(sessionId)
            assertThat(profileUpdate).hasSize(1)
            assertThat(profileUpdate).containsEntry("_test", "test")
            assertThat(userSignature).isEqualTo("user-signature")
        }
    }

    @Test
    fun `group SHOULD return ActivityRequest WITH proper properties`() {
        // given
        val properties = hashMapOf("_test" to "test")
        val sessionId = UUID.randomUUID()
        // when
        activityRequestBuilder.group(sessionId, properties)
        // then
        with(activityRequestSlot.captured) {
            assertThat(userId).isEqualTo("userId")
            assertThat(groupId).isEqualTo("groupId")
            assertThat(accountId).isEqualTo("accountId")
            assertThat(this.sessionId).isEqualTo(sessionId)
            assertThat(groupUpdate).hasSize(1)
            assertThat(groupUpdate).containsEntry("_test", "test")
            assertThat(userSignature).isEqualTo("user-signature")
        }
    }

    @Test
    fun `track SHOULD decorate track before getting autoProperties`() {
        // given
        val properties = hashMapOf("_test" to "test")
        // when
        activityRequestBuilder.track(UUID.randomUUID(), "event1", properties)
        // then
        verifySequence {
            autoPropertyDecorator.decorateTrack(eventRequestSlot.captured)
            autoPropertyDecorator.autoProperties
        }
        with(eventRequestSlot.captured) {
            assertThat(name).isEqualTo("event1")
            assertThat(attributes).isEqualTo(properties)
        }
    }

    @Test
    fun `track SHOULD return ActivityRequest`() {
        // given
        val properties = hashMapOf("_test" to "test")
        val sessionId = UUID.randomUUID()
        // when
        with(activityRequestBuilder.track(sessionId, "event1", properties)) {
            // then
            assertThat(userId).isEqualTo("userId")
            assertThat(accountId).isEqualTo("accountId")
            assertThat(groupId).isEqualTo("groupId")
            assertThat(this.sessionId).isEqualTo(sessionId)
            assertThat(profileUpdate).hasSize(1)
            assertThat(profileUpdate).containsEntry("auto", "properties")
            assertThat(events).hasSize(1)
            assertThat(userSignature).isEqualTo("user-signature")
        }
    }

    @Test
    fun `screen SHOULD decorate track before getting autoProperties`() {
        // given
        val properties = mutableMapOf<String, Any>("_test" to "test")
        // when
        activityRequestBuilder.screen(UUID.randomUUID(), "screen2", properties)
        // then
        verifySequence {
            autoPropertyDecorator.decorateTrack(eventRequestSlot.captured)
            autoPropertyDecorator.autoProperties
        }
        with(eventRequestSlot.captured) {
            assertThat(name).isEqualTo("appcues:screen_view")
            assertThat(attributes).containsEntry("_test", "test")
            assertThat(attributes).containsEntry("screenTitle", "screen2")
            assertThat(context).containsEntry("screen_title", "screen2")
        }
    }

    @Test
    fun `screen SHOULD return ActivityRequest`() {
        // given
        val properties = mutableMapOf<String, Any>("_test" to "test")
        val sessionId = UUID.randomUUID()
        // when
        with(activityRequestBuilder.screen(sessionId, "screen2", properties)) {
            // then
            assertThat(userId).isEqualTo("userId")
            assertThat(accountId).isEqualTo("accountId")
            assertThat(groupId).isEqualTo("groupId")
            assertThat(this.sessionId).isEqualTo(sessionId)
            assertThat(profileUpdate).hasSize(1)
            assertThat(profileUpdate).containsEntry("auto", "properties")
            assertThat(events).hasSize(1)
            assertThat(userSignature).isEqualTo("user-signature")
        }
    }
}
