package `in`.saveo

import `in`.saveo.model.MovieDetails
import `in`.saveo.repository.MovieDetailRepository
import `in`.saveo.retrofit.MovieRetroInterface
import `in`.saveo.retrofit.MovieRetrofitClient
import `in`.saveo.utils.Constants.IMAGE_BASE_URL
import `in`.saveo.viewModel.MovieDetailViewModel
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_movie_detail.*
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: MovieDetailViewModel
    private lateinit var movieRepository: MovieDetailRepository

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        val movieId:Int=intent.getIntExtra("id",1)

        val apiService:MovieRetroInterface = MovieRetrofitClient.getClient()
        movieRepository= MovieDetailRepository(apiService)

        viewModel = getViewModel(movieId)

        viewModel.movieDetails.observe(this, Observer {
            bindViews(it)
        })

        viewModel.networkState.observe(this, Observer {
//            txt_error.visibility=if(it== NetworkState.ERROR) View.VISIBLE else View.GONE
        })
    }

    private fun getViewModel(movieId:Int): MovieDetailViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MovieDetailViewModel(movieRepository,movieId) as T
            }
        })[MovieDetailViewModel::class.java]
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun bindViews(movieDetails: MovieDetails){


        // converting date
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = formatter.parse(movieDetails.releaseDate)
        val formattedDate = DateTimeFormatter.ofPattern("dd MMM yyyy").format(date)

        // converting time
        val time = movieDetails.runtime
        val hour = time / 60
        val minutes = time % 60

        val movieTime : String = if (hour > 0) {
            "$hour h $minutes min"
        } else {
            "$minutes min"
        }

        val genres = movieDetails.genres
        for ((index, genre) in genres.withIndex()) {

            if (index == 2){
                break
            }

            val chip = layoutInflater.inflate(R.layout.custom_chip_layout, movie_detail_genre_chips, false) as Chip
            chip.text = genre.name
            movie_detail_genre_chips.addView(chip)
        }

        movie_detail_title.text = movieDetails.title
        movie_detail_meta_info.text = "$movieTime | $formattedDate"
        movie_detail_rating.rating = movieDetails.rating.toFloat()
        movie_detail_review_txt.text = movieDetails.status
        movie_detail_description.text = movieDetails.overview
        movie_detail_rating_txt.text = movieDetails.rating.toString()
        Log.e("TAG", "bindViews:  " + movieDetails.rating)

        val moviePosterURL = IMAGE_BASE_URL + movieDetails.posterPath
        Glide.with(this)
            .load(moviePosterURL)
            .into(movie_detail_image);

    }
}