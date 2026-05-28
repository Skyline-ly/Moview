package com.example.mininetflix.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.mininetflix.network.Movie
import com.example.mininetflix.databinding.FragmentOverviewBinding
import com.example.mininetflix.overview.MoviePosterAdapter
import com.example.mininetflix.overview.OverviewViewModel
import com.example.mininetflix.overview.TmdbApiStatus

class OverviewFragment : Fragment() {

    private var _binding: FragmentOverviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OverviewViewModel by lazy {
        ViewModelProvider(this)[OverviewViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOverviewBinding.inflate(inflater, container, false)

        // One click handler for every poster AND the hero — all navigate to Detail.
        val onMovieClick: (Movie) -> Unit = { movie ->
            findNavController().navigate(
                OverviewFragmentDirections.actionOverviewFragmentToDetailFragment(movie)
            )
        }


        // 4 adapter INSTANCES — same class, different lists.
        val popularAdapter = MoviePosterAdapter(onMovieClick)
        val topRatedAdapter = MoviePosterAdapter(onMovieClick)
        val nowPlayingAdapter = MoviePosterAdapter(onMovieClick)
        val upcomingAdapter = MoviePosterAdapter(onMovieClick)

        // Bind each row with a HORIZONTAL LinearLayoutManager.
        setupRow(binding.popularRow, popularAdapter)
        setupRow(binding.topRatedRow, topRatedAdapter)
        setupRow(binding.nowPlayingRow, nowPlayingAdapter)
        setupRow(binding.upcomingRow, upcomingAdapter)

        // Each row observes its own LiveData.
        viewModel.popular.observe(viewLifecycleOwner) { popularAdapter.submitList(it) }
        viewModel.topRated.observe(viewLifecycleOwner) { topRatedAdapter.submitList(it) }
        viewModel.nowPlaying.observe(viewLifecycleOwner) { nowPlayingAdapter.submitList(it) }
        viewModel.upcoming.observe(viewLifecycleOwner) { upcomingAdapter.submitList(it) }

        // Hero — load backdrop + title; tap navigates same as a poster.
        viewModel.featured.observe(viewLifecycleOwner) { movie ->
            if (movie != null) {
                binding.heroTitle.text = movie.title
                val imagePath = movie.backdropPath ?: movie.posterPath
                if (imagePath != null) {
                    Glide.with(binding.heroImage.context)
                        .load("https://image.tmdb.org/t/p/w780$imagePath")
                        .into(binding.heroImage)
                }
                binding.heroContainer.setOnClickListener { onMovieClick(movie) }
            }
        }
        // Sprint 8 — search icon on the hero opens the Search screen.
        binding.searchButton.setOnClickListener {
            findNavController().navigate(
                OverviewFragmentDirections.actionOverviewFragmentToSearchFragment()
            )
        }

        // Status: hide scroll content during loading/error to avoid a blank hero flash.
        viewModel.statusMessage.observe(viewLifecycleOwner) { binding.statusText.text = it }
        viewModel.status.observe(viewLifecycleOwner) { status ->
            binding.loadingSpinner.visibility =
                if (status == TmdbApiStatus.LOADING) View.VISIBLE else View.GONE
            binding.errorImage.visibility =
                if (status == TmdbApiStatus.ERROR) View.VISIBLE else View.GONE
            binding.scrollContainer.visibility =
                if (status == TmdbApiStatus.DONE) View.VISIBLE else View.GONE
        }

        return binding.root
    }

    private fun setupRow(rv: RecyclerView, adapter: MoviePosterAdapter) {
        rv.adapter = adapter
        rv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}