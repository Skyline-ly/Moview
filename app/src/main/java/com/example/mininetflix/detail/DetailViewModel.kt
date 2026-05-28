package com.example.mininetflix.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.mininetflix.network.TmdbApi
import com.example.mininetflix.BuildConfig
import kotlinx.coroutines.launch


class DetailViewModel : ViewModel() {

    private val _trailerKey = MutableLiveData<String?>(null)
    val trailerKey: LiveData<String?> = _trailerKey

    // Make sure we only hit the network once for this movie, even if onCreateView
    // runs again (e.g. after a rotation).
    private var hasFetched = false

    fun fetchTrailer(movieId: Int) {
        if (hasFetched) return
        hasFetched = true

        viewModelScope.launch {
            try {
                val response = TmdbApi.retrofitService.getMovieVideos(
                    movieId = movieId,
                    apiKey = BuildConfig.TMDB_API_KEY
                )
                // Prefer an OFFICIAL YouTube Trailer; fall back to any YouTube Trailer.
                val trailer = response.results.firstOrNull {
                    it.site == "YouTube" && it.type == "Trailer" && it.official
                } ?: response.results.firstOrNull {
                    it.site == "YouTube" && it.type == "Trailer"
                }
                _trailerKey.value = trailer?.key
            } catch (e: Exception) {
                _trailerKey.value = null
            }
        }
    }
}
