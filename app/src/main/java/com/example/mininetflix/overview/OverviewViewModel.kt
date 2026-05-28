package com.example.mininetflix.overview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.android.mininetflix.network.Movie
import com.example.android.mininetflix.network.TmdbApi
import com.example.mininetflix.BuildConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.collections.take
enum class TmdbApiStatus { LOADING, ERROR, DONE }

class OverviewViewModel : ViewModel() {

    private val _status = MutableLiveData<TmdbApiStatus>()
    val status: LiveData<TmdbApiStatus> get() = _status

    val statusMessage: LiveData<String> = status.map { state ->
        when (state) {
            TmdbApiStatus.LOADING -> "Loading movies…"
            TmdbApiStatus.ERROR -> "⚠ Couldn't load movies.\nCheck your Internet connection."
            else -> ""
        }
    }

    // One LiveData per row.
    private val _popular = MutableLiveData<List<Movie>>()
    val popular: LiveData<List<Movie>> get() = _popular

    private val _topRated = MutableLiveData<List<Movie>>()
    val topRated: LiveData<List<Movie>> get() = _topRated

    private val _nowPlaying = MutableLiveData<List<Movie>>()
    val nowPlaying: LiveData<List<Movie>> get() = _nowPlaying

    private val _upcoming = MutableLiveData<List<Movie>>()
    val upcoming: LiveData<List<Movie>> get() = _upcoming

    // Featured movie shown in the hero card — first item of Popular.
    private val _featured = MutableLiveData<Movie?>()
    val featured: LiveData<Movie?> get() = _featured

    init { loadHome() }

    private fun loadHome() {
        viewModelScope.launch {
            _status.value = TmdbApiStatus.LOADING
            try {
                val key = BuildConfig.TMDB_API_KEY

                // Wrap the parallel `async` block in `coroutineScope { }`. This is CRITICAL —
                // read the "Why coroutineScope?" lesson below before you ask "can I skip it?".
                coroutineScope {
                    // Fire all four requests at once — they run in PARALLEL.
                    val popularDeferred    = async { TmdbApi.retrofitService.getPopular(key) }
                    val topRatedDeferred   = async { TmdbApi.retrofitService.getTopRated(key) }
                    val nowPlayingDeferred = async { TmdbApi.retrofitService.getNowPlaying(key) }
                    val upcomingDeferred   = async { TmdbApi.retrofitService.getUpcoming(key) }

                    // Wait once — for whichever finishes last.
                    val popular    = popularDeferred.await().results
                    val topRated   = topRatedDeferred.await().results
                    val nowPlaying = nowPlayingDeferred.await().results
                    val upcoming   = upcomingDeferred.await().results

                    _popular.value    = popular
                    _topRated.value   = topRated
                    _nowPlaying.value = nowPlaying
                    _upcoming.value   = upcoming
                    _featured.value   = popular.firstOrNull()
                }

                _status.value = TmdbApiStatus.DONE
            } catch (e: Exception) {
                _popular.value    = emptyList()
                _topRated.value   = emptyList()
                _nowPlaying.value = emptyList()
                _upcoming.value   = emptyList()
                _featured.value   = null
                _status.value = TmdbApiStatus.ERROR
            }
        }
    }
}