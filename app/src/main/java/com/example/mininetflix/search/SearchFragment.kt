package com.example.mininetflix.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.mininetflix.network.Movie
import com.example.mininetflix.databinding.FragmentSearchBinding
import com.example.mininetflix.overview.MoviePosterAdapter
import com.example.mininetflix.overview.TmdbApiStatus

class SearchFragment : Fragment(){
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by lazy {
        ViewModelProvider(this)[SearchViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        val adapter = MoviePosterAdapter { movie -> openDetail(movie) }
        binding.searchResults.adapter = adapter

        binding.searchInput.doOnTextChanged { text, _, _, _ ->
            viewModel.onQueryChanged(text?.toString().orEmpty())
        }

        viewModel.results.observe(viewLifecycleOwner) { adapter.submitList(it) }

        viewModel.status.observe(viewLifecycleOwner) { status ->
            binding.searchLoading.visibility =
                if (status == TmdbApiStatus.LOADING) View.VISIBLE else View.GONE
        }

        viewModel.statusMessage.observe(viewLifecycleOwner) { msg ->
            binding.searchStatusText.text = msg
            binding.searchStatusText.visibility =
                if (msg.isNullOrBlank()) View.GONE else View.VISIBLE
        }

        return binding.root
    }

    private fun openDetail(movie: Movie) {
       findNavController().navigate(
            SearchFragmentDirections.actionSearchFragmentToDetailFragment(movie)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}