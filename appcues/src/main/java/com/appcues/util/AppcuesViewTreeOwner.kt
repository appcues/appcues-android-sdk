package com.appcues.util

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.findViewTreeOnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

internal class AppcuesViewTreeOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner, OnBackPressedDispatcherOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val onBackPressedDispatcher: OnBackPressedDispatcher = OnBackPressedDispatcher { }

    private val savedState = Bundle()

    private val savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore = ViewModelStore()

    fun init(view: ViewGroup, activity: Activity) {
        initRegistry()
        // this is the "official" decorView, that may contain registered Owners
        val decorView = activity.window.decorView

        if (view.findViewTreeLifecycleOwner() == null) {
            val viewOwner = decorView.findViewTreeLifecycleOwner()
            view.setViewTreeLifecycleOwner(viewOwner ?: this)
        }

        if (view.findViewTreeViewModelStoreOwner() == null) {
            val viewOwner = decorView.findViewTreeViewModelStoreOwner()
            view.setViewTreeViewModelStoreOwner(viewOwner ?: this)
        }

        if (view.findViewTreeSavedStateRegistryOwner() == null) {
            val viewOwner = decorView.findViewTreeSavedStateRegistryOwner()
            view.setViewTreeSavedStateRegistryOwner(viewOwner ?: this)
        }

        if (view.findViewTreeOnBackPressedDispatcherOwner() == null) {
            val viewOwner = decorView.findViewTreeOnBackPressedDispatcherOwner()
            view.setViewTreeOnBackPressedDispatcherOwner(viewOwner ?: this)
        }
    }

    private fun initRegistry() {
        if (savedStateRegistry.isRestored.not()) {
            savedStateRegistryController.performRestore(savedState)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }
}
