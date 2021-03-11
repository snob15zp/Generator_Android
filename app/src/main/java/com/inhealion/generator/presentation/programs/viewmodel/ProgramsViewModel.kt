package com.inhealion.generator.presentation.programs.viewmodel

import androidx.lifecycle.viewModelScope
import com.inhealion.generator.model.State
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.api.model.Folder
import com.inhealion.generator.networking.api.model.Program
import com.inhealion.generator.presentation.main.viewmodel.BaseViewModel
import com.inhealion.generator.utils.ApiErrorStringProvider
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProgramsViewModel(
    val folder: Folder,
    private val generatorApiCoroutinesClient: GeneratorApiCoroutinesClient,
    private val apiErrorStringProvider: ApiErrorStringProvider
) : BaseViewModel<List<Program>>() {

    fun load() {
        viewModelScope.launch {
            postState(State.InProgress())

            generatorApiCoroutinesClient.fetchPrograms(folder.id)
                .catch { postState(State.Failure(apiErrorStringProvider.getErrorMessage(it), it)) }
                .collect { postState(State.Success(it)) }
        }
    }
}
