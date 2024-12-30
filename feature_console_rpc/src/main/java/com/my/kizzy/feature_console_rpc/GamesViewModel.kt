/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * GamesViewModel.kt is part of Kizzy
 *  *  * and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.feature_console_rpc

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.kizzy.domain.model.Game
import com.my.kizzy.domain.model.Resource
import com.my.kizzy.domain.use_case.get_games.GetGamesUseCase
import com.my.kizzy.resources.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamesViewModel @Inject constructor(
    private val getGamesUseCase: GetGamesUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state: MutableState<GamesState> = mutableStateOf(GamesState.Loading)
    val state: State<GamesState> = _state
    private val games = mutableListOf<Game>()
    val isSearchBarVisible = mutableStateOf(false)

    private var searchJob: Job? = null
    private var currentSort: String? = null
    private var currentSearchQuery: String = ""

    init {
        getGames()
    }

    private fun getGames() {
        getGamesUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = GamesState.Success(games = result.data ?: emptyList())
                    games.clear()
                    games.addAll(result.data ?: emptyList())
                }

                is Resource.Error -> {
                    _state.value = GamesState.Error(
                        error = result.message ?: context.getString(R.string.unknown_error)
                    )
                }

                is Resource.Loading -> {
                    _state.value = GamesState.Loading
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onUiEvent(uiEvent: UiEvent) {
        when (uiEvent) {
            UiEvent.CloseSearchBar -> isSearchBarVisible.value = false
            UiEvent.OpenSearchBar -> isSearchBarVisible.value = true
            is UiEvent.Search -> onSearch(uiEvent.query)
            is UiEvent.SortBy -> onSort(uiEvent.platform)
            UiEvent.ClearSort -> clearSort()
            UiEvent.TryAgain -> getGames()
        }
    }

    private fun onSearch(query: String) {
        currentSearchQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            applyFilters()
        }
    }

    private fun onSort(platform: String) {
        currentSort = platform
        applyFilters()
    }

    private fun clearSort() {
        currentSort = null
        applyFilters()
    }

    private fun applyFilters() {
        val filteredGames = games.filter {
            (currentSort == null || it.platform.equals(currentSort, ignoreCase = true)) &&
            (currentSearchQuery.isBlank() || it.game_title.contains(currentSearchQuery, ignoreCase = true))
        }
        _state.value = GamesState.Success(filteredGames)
    }
}
