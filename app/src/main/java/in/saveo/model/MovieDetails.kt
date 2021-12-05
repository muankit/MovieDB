package `in`.saveo.model

import com.google.gson.annotations.SerializedName

data class MovieDetails(
    val id: Int,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String,
    @SerializedName("release_date")
    val releaseDate: String,
    @SerializedName("genres") var genres: List<Genre> = arrayListOf(),
    val runtime: Int,
    val status: String,
    val tagline: String,
    val title: String,
    val video: Boolean,
    @SerializedName("vote_average")
    val rating: Double
)
