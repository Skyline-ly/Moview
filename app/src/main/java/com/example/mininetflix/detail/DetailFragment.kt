package com.example.mininetflix.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide

import com.example.mininetflix.databinding.FragmentDetailBinding
class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    // SafeArgs: the Movie passed from OverviewFragment.
    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        val movie = args.selectedMovie

        binding.detailTitle.text = movie.title
        binding.detailRating.text = "★ %.1f".format(movie.voteAverage)
        binding.detailOverview.text = movie.overview

        // Show only the year (first 4 chars of "2026-04-15"); hide the dot if there is no date.
        val year = movie.releaseDate?.take(4)
        binding.detailRelease.text = year ?: ""
        binding.detailDot.visibility = if (year.isNullOrBlank()) View.GONE else View.VISIBLE

        // Prefer the wide backdrop; fall back to the poster.
        val imagePath = movie.backdropPath ?: movie.posterPath
        if (imagePath != null) {
            Glide.with(binding.detailImage.context)
                .load("https://image.tmdb.org/t/p/w780$imagePath")
                .into(binding.detailImage)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}