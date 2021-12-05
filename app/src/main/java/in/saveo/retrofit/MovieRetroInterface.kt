package `in`.saveo.retrofit

import `in`.saveo.model.MovieDetails
import `in`.saveo.model.MovieResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieRetroInterface {

//    https://api.themoviedb.org/3/movie/popular?api_key=3b8c1d4b896f9a8797fa6e67752649bf&page=1

    @GET("movie/{movie_id}")
    fun getMovieDetails(@Path("movie_id")id:Int) : Single<MovieDetails>


    @GET("movie/popular")
    fun getPopularMovie(@Query("page")page:Int) : Single<MovieResponse>


}