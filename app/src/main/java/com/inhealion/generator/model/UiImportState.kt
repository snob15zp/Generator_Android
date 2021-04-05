package com.inhealion.generator.model

sealed class UiImportState {
    object InProgress : UiImportState()
    object Success : UiImportState()
    data class Failed(val message: String) : UiImportState()
}
