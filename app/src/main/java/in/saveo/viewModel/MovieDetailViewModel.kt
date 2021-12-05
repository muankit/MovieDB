package `in`.saveo.viewModel

import `in`.saveo.model.MovieDetails
import `in`.saveo.repository.MovieDetailRepository
import `in`.saveo.repository.NetworkState
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

class MovieDetailViewModel (private val movieRepository :MovieDetailRepository,movieId:Int): ViewModel(){

    private val compositeDisposable = CompositeDisposable()

    val movieDetails : LiveData<MovieDetails> by lazy{
        movieRepository.fetchMovieDetails(compositeDisposable,movieId)
    }

    val networkState : LiveData<NetworkState> by lazy {
        movieRepository.getMovieNetworkState()
    }


    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()

    }
}