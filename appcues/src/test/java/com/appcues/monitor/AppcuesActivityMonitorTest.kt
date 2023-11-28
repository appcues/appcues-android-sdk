package com.appcues.monitor

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.appcues.monitor.AppcuesActivityMonitor.ActivityMonitorListener
import com.google.common.truth.Truth.assertThat
import io.mockk.called
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Test

internal class AppcuesActivityMonitorTest {

    @Test
    fun `SHOULD be instance of ActivityLifecycleCallbacks`() {
        // GIVEN
        val monitor = AppcuesActivityMonitor
        // THEN
        assertThat(monitor).isInstanceOf(Application.ActivityLifecycleCallbacks::class.java)
    }

    @Test
    fun `initialize SHOULD register monitor`() {
        // GIVEN
        val application = mockk<Application>(relaxed = true)
        val monitor = AppcuesActivityMonitor
        // WHEN
        monitor.initialize(application)
        // THEN
        verify { application.registerActivityLifecycleCallbacks(monitor) }
    }

    @Test
    fun `onActivityResumed and onActivityPaused SHOULD toggle isPaused `() {
        // GIVEN
        val activity = mockk<Activity>(relaxed = true)
        val monitor = AppcuesActivityMonitor
        // WHEN
        monitor.onActivityResumed(activity)
        // THEN
        assertThat(monitor.isPaused).isFalse()
        // WHEN
        monitor.onActivityPaused(activity)
        // THEN
        assertThat(monitor.isPaused).isTrue()
        // WHEN
        monitor.onActivityResumed(activity)
        // THEN
        assertThat(monitor.isPaused).isFalse()
    }

    @Test
    fun `subscribe SHOULD add listener and and use that when reporting activity change`() {
        // GIVEN
        val activity = mockk<Activity>(relaxed = true)
        val listener = mockk<ActivityMonitorListener>(relaxed = true)
        val monitor = AppcuesActivityMonitor
        // WHEN
        monitor.subscribe(listener)
        monitor.onActivityCreated(activity, null)
        monitor.onActivityResumed(activity)
        // THEN
        verify { listener.onActivityChanged(activity) }
    }

    @Test
    fun `unsubscribe SHOULD remove listener from list`() {
        // GIVEN
        val activity = mockk<Activity>(relaxed = true)
        val listener = mockk<ActivityMonitorListener>(relaxed = true)
        val monitor = AppcuesActivityMonitor
        // WHEN
        monitor.subscribe(listener)
        monitor.unsubscribe(listener)
        monitor.onActivityResumed(activity)
        // THEN
        verify(exactly = 0) { listener.onActivityChanged(activity) }
    }

    @Test
    fun `onActivityResume SHOULD report when changing activity instances`() {
        // GIVEN
        val activity1 = mockk<Activity>(relaxed = true)
        val activity2 = mockk<Activity>(relaxed = true)
        val listener = mockk<ActivityMonitorListener>(relaxed = true)
        val monitor = AppcuesActivityMonitor

        monitor.subscribe(listener)
        // used when adding item to listener map
        excludeRecords { listener.hashCode() }
        // WHEN
        monitor.onActivityResumed(activity1)
        monitor.onActivityCreated(activity2, null)
        monitor.onActivityResumed(activity2)
        // THEN
        verifySequence {
            listener.onActivityChanged(activity1)
            listener.onActivityChanged(activity2)
        }
    }

    @Test
    fun `onActivityResume SHOULD not report same activity`() {
        // GIVEN
        val activity1 = mockk<Activity>(relaxed = true)
        val listener = mockk<ActivityMonitorListener>(relaxed = true)
        val monitor = AppcuesActivityMonitor

        monitor.subscribe(listener)
        // used when adding item to listener map
        excludeRecords { listener.hashCode() }
        // WHEN
        monitor.onActivityResumed(activity1)
        monitor.onActivityResumed(activity1)
        // THEN
        verify(exactly = 1) { listener.onActivityChanged(activity1) }
    }

    @Test
    fun `onActivityCreated SHOULD not call do nothing`() {
        // GIVEN
        val activity1 = mockk<Activity>(relaxed = true)
        val listener = mockk<ActivityMonitorListener>(relaxed = true)
        val monitor = AppcuesActivityMonitor
        monitor.subscribe(listener)
        // used when adding item to listener map
        excludeRecords { listener.hashCode() }

        // WHEN
        monitor.onActivityCreated(activity1, null)
        // THEN
        verify { listener wasNot called }
    }

    @Test
    fun `onActivityStarted SHOULD not call do nothing`() {
        // GIVEN
        val activity1 = mockk<Activity>(relaxed = true)
        val listener = mockk<ActivityMonitorListener>(relaxed = true)
        val monitor = AppcuesActivityMonitor
        monitor.subscribe(listener)
        // used when adding item to listener map
        excludeRecords { listener.hashCode() }

        // WHEN
        monitor.onActivityStarted(activity1)
        // THEN
        verify { listener wasNot called }
    }

    @Test
    fun `onActivityStopped SHOULD not call do nothing`() {
        // GIVEN
        val activity1 = mockk<Activity>(relaxed = true)
        val listener = mockk<ActivityMonitorListener>(relaxed = true)
        val monitor = AppcuesActivityMonitor
        monitor.subscribe(listener)
        // used when adding item to listener map
        excludeRecords { listener.hashCode() }

        // WHEN
        monitor.onActivityStopped(activity1)
        // THEN
        verify { listener wasNot called }
    }

    @Test
    fun `onActivitySaveInstanceState SHOULD not call do nothing`() {
        // GIVEN
        val activity1 = mockk<Activity>(relaxed = true)
        val listener = mockk<ActivityMonitorListener>(relaxed = true)
        val monitor = AppcuesActivityMonitor
        monitor.subscribe(listener)
        // used when adding item to listener map
        excludeRecords { listener.hashCode() }

        // WHEN
        monitor.onActivitySaveInstanceState(activity1, Bundle())
        // THEN
        verify { listener wasNot called }
    }

    @Test
    fun `onActivityDestroyed SHOULD not call do nothing`() {
        // GIVEN
        val activity1 = mockk<Activity>(relaxed = true)
        val listener = mockk<ActivityMonitorListener>(relaxed = true)
        val monitor = AppcuesActivityMonitor
        monitor.subscribe(listener)
        // used when adding item to listener map
        excludeRecords { listener.hashCode() }

        // WHEN
        monitor.onActivityDestroyed(activity1)
        // THEN
        verify { listener wasNot called }
    }
}
