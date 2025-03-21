package com.xa.inventorymaster

import android.app.Application
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner

class ItemViewModelFactory(
    private val application: Application,
    owner: SavedStateRegistryOwner
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(ItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ItemViewModel(application, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}