package `in`.saveo.repository

import `in`.saveo.model.MovieDetails
import `in`.saveo.retrofit.MovieRetroInterface
import androidx.lifecycle.LiveData
import io.reactivex.disposables.CompositeDisposable

class MovieDetailRepository (private val movieRetroInterface: MovieRetroInterface) {

    lateinit var movieDetailDataSource: MovieDetailDataSource
    fun fetchMovieDetails(
        compositeDisposable: CompositeDisposable,
        movieId: Int
    ): LiveData<MovieDetails> {

        movieDetailDataSource =
            MovieDetailDataSource(movieRetroInterface, compositeDisposable)
        movieDetailDataSource.fetchMovieDetails(movieId)

        return movieDetailDataSource.downloadedMovieResponse
    }

    fun getMovieNetworkState(): LiveData<NetworkState> {
        return movieDetailDataSource.networkState
    }

}