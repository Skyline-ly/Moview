package com.example.mininetflix.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.android.mininetflix.network.Movie
import com.example.android.mininetflix.network.TmdbApi
import com.example.mininetflix.BuildConfig
import com.example.mininetflix.overview.TmdbApiStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.isNullOrEmpty

class SearchViewModel : ViewModel() {

    private val _results = MutableLiveData<List<Movie>>(emptyList())
    val results: LiveData<List<Movie>> get() = _results

    private val _status = MutableLiveData<TmdbApiStatus>(TmdbApiStatus.DONE)
    val status: LiveData<TmdbApiStatus> get() = _status

    // ⚠ MUST be declared BEFORE `statusMessage` below. See lesson at the end of this guide.
    private var _lastQuery: String = ""
    private var searchJob: Job? = null

    val statusMessage: LiveData<String> = status.map { state ->
        when (state) {
            TmdbApiStatus.LOADING -> "Searching…"
            TmdbApiStatus.ERROR -> "⚠ Couldn't search.\nCheck your Internet connection."
            TmdbApiStatus.DONE -> {
                val currentResults = _results.value
                val currentQuery = _lastQuery
                when {
                    currentQuery.isBlank() -> "Type to search movies."
                    currentResults.isNullOrEmpty() -> "No movies match \"$currentQuery\"."
                    else -> ""
                }
            }
        }
    }

    fun onQueryChanged(query: String) {
        _lastQuery = query.trim()

        // Cancel any pending search — we're typing a new character.
        searchJob?.cancel()

        if (_lastQuery.isBlank()) {
            _results.value = emptyList()
            _status.value = TmdbApiStatus.DONE
            return
        }

        searchJob = viewModelScope.launch {
            // Debounce: wait 300ms of silence before actually hitting the network.
            // If the user types another character within 300ms, this coroutine is
            // cancelled by the .cancel() call above before reaching the API call.
            delay(300)

            _status.value = TmdbApiStatus.LOADING
            try {
                val response = TmdbApi.retrofitService.searchMovies(
                    apiKey = BuildConfig.TMDB_API_KEY,
                    query = _lastQuery
                )
                _results.value = response.results
                _status.value = TmdbApiStatus.DONE
            } catch (e: Exception) {
                _results.value = emptyList()
                _status.value = TmdbApiStatus.ERROR
            }
        }
    }
}