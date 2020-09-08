package com.ezike.tobenna.starwarssearch.character_search.views.detail

import androidx.core.view.isVisible
import com.ezike.tobenna.starwarssearch.character_search.databinding.FilmViewLayoutBinding
import com.ezike.tobenna.starwarssearch.character_search.model.FilmModel
import com.ezike.tobenna.starwarssearch.character_search.ui.characterDetail.adapter.FilmAdapter
import com.ezike.tobenna.starwarssearch.presentation.mvi.DispatchIntent
import com.ezike.tobenna.starwarssearch.presentation.mvi.UIComponent
import com.ezike.tobenna.starwarssearch.presentation.mvi.ViewIntent
import com.ezike.tobenna.starwarssearch.presentation.mvi.ViewState

data class FilmViewState private constructor(
    val films: List<FilmModel> = emptyList(),
    val errorMessage: String? = null,
    val isLoading: Boolean = true,
    val isVisible: Boolean = true
) : ViewState {

    inline fun state(transform: Factory.() -> FilmViewState): FilmViewState =
        transform(Factory(this))

    companion object Factory {

        private lateinit var state: FilmViewState

        operator fun invoke(viewState: FilmViewState): Factory {
            state = viewState
            return this
        }

        val init: FilmViewState
            get() = FilmViewState()

        val loading: FilmViewState
            get() = state.copy(isLoading = true, isVisible = true, errorMessage = null)

        val hide: FilmViewState
            get() = state.copy(isLoading = false, isVisible = false, errorMessage = null)

        fun error(message: String): FilmViewState =
            state.copy(isLoading = false, isVisible = true, errorMessage = message)

        fun success(films: List<FilmModel>): FilmViewState =
            state.copy(films = films, isLoading = false, isVisible = true, errorMessage = null)
    }
}

data class RetryFetchFilmIntent(val url: String) : ViewIntent

class FilmView(
    private val binding: FilmViewLayoutBinding,
    private val characterUrl: String,
    action: DispatchIntent
) : UIComponent<FilmViewState>() {

    private val filmAdapter: FilmAdapter by lazy(LazyThreadSafetyMode.NONE) { FilmAdapter() }

    init {
        binding.filmList.adapter = filmAdapter
        binding.filmErrorState.onRetry { action(RetryFetchFilmIntent(characterUrl)) }
    }

    override fun render(state: FilmViewState) {
        filmAdapter.submitList(state.films)
        binding.run {
            root.isVisible = state.isVisible
            if (state.isVisible) {
                emptyView.isVisible =
                    state.films.isEmpty() && !state.isLoading && state.errorMessage == null
                filmLoadingView.root.isVisible = state.isLoading
                filmErrorState.isVisible = state.errorMessage != null
                filmErrorState.setCaption(state.errorMessage)
            }
        }
    }
}