package com.example.mininetflix.overview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.android.mininetflix.network.Movie
import com.example.android.mininetflix.network.TmdbApi
import com.example.mininetflix.BuildConfig
import kotlinx.coroutines.launch
import kotlin.collections.take

enum class TmdbApiStatus { LOADING, ERROR, DONE }


class OverviewViewModel : ViewModel() {

    // Internal (changeable) vs. external (read-only) LiveData — a common pattern.
    private val _status = MutableLiveData<TmdbApiStatus>()
    val status: LiveData<TmdbApiStatus> get() = _status

    // The real list of movies we got from TMDB.
    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> get() = _movies

    // A friendly message shown while loading or on error (empty when DONE).
    val statusMessage: LiveData<String> = status.map { state ->
        when (state) {
            TmdbApiStatus.LOADING -> "Loading movies…"
            TmdbApiStatus.ERROR -> "⚠ Couldn't load movies.\nCheck your Internet connection."
            else -> ""
        }
    }


    // The first 10 titles as plain text — just to prove we have real data.
    val moviesText: LiveData<String> = movies.map { list ->
        list.take(10).joinToString("\n") { it.title }
    }
    //Delete these lines — the grid replaces the text list:



    // Fetch immediately when the ViewModel is created.
    init {
        getPopularMovies()
    }

    private fun getPopularMovies() {
        viewModelScope.launch {
            _status.value = TmdbApiStatus.LOADING
            try {
                val response = TmdbApi.retrofitService.getPopular(BuildConfig.TMDB_API_KEY)
                _movies.value = response.results
                _status.value = TmdbApiStatus.DONE
            } catch (e: Exception) {
                _movies.value = listOf()
                _status.value = TmdbApiStatus.ERROR
            }
        }
    }
}

