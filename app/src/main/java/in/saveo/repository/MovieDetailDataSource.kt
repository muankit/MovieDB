package `in`.saveo.repository

import `in`.saveo.model.MovieDetails
import `in`.saveo.retrofit.MovieRetroInterface
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.Exception

class MovieDetailDataSource (
    private val apiService: MovieRetroInterface,
    private val compositeDisposable: CompositeDisposable
) {

    private val mutableNetworkState = MutableLiveData<NetworkState>()

    val networkState: LiveData<NetworkState>
        get() = mutableNetworkState

    private val _downloadedMovieDetailsResponse = MutableLiveData<MovieDetails>()
    val downloadedMovieResponse: LiveData<MovieDetails>
        get() = _downloadedMovieDetailsResponse

    fun fetchMovieDetails(movieId: Int) {
        mutableNetworkState.postValue(NetworkState.LOADING)


        try {
            compositeDisposable.add(
                apiService.getMovieDetails(movieId)
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        {
                            _downloadedMovieDetailsResponse.postValue(it)
                            mutableNetworkState.postValue(NetworkState.LOADED)
                        },
                        {
                            mutableNetworkState.postValue(NetworkState.ERROR)
                            it.message?.let { it1 -> Log.e("MovieDetailsDataSource", it1) }
                        }
                    )
            )

        } catch (e: Exception) {
            e.message?.let { Log.e("MovieDetailsDataSource", it) }
        }

    }


}