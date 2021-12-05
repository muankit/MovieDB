package `in`.saveo

import `in`.saveo.model.Movie
import `in`.saveo.repository.NetworkState
import `in`.saveo.utils.Constants.IMAGE_BASE_URL
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.movie_card.view.*
import kotlinx.android.synthetic.main.netwrok_state_item.view.*

class MovieRecyclerAdapter (private val context: Context, private val activity : Activity) : PagedListAdapter<Movie, RecyclerView.ViewHolder>(MovieDiffCallback()) {

    val TYPE_MOVIE = 1
    val TYPE_STATE = 2

    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View

        return if (viewType == TYPE_MOVIE) {
            view = layoutInflater.inflate(R.layout.movie_card, parent, false)
            MovieViewHolder(view)
        } else {
            view = layoutInflater.inflate(R.layout.netwrok_state_item, parent, false)
            NetworkStateViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_MOVIE) {
            (holder as MovieViewHolder).bind(getItem(position),context, activity)
        }
        else {
            (holder as NetworkStateViewHolder).bind(networkState)
        }
    }


    private fun hasExtraRow(): Boolean {
        return networkState != null && networkState != NetworkState.LOADED
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            TYPE_STATE
        } else {
            TYPE_MOVIE
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }

    }

    class MovieViewHolder (view: View) : RecyclerView.ViewHolder(view) {

        fun bind(movie: Movie?,context: Context, activity: Activity) {

            val moviePosterURL = IMAGE_BASE_URL + movie?.posterPath
            Glide.with(itemView.context)
                .load(moviePosterURL)
                .into(itemView.movie_poster);


            itemView.setOnClickListener{
                val intent = Intent(context, MovieDetailActivity::class.java)
                intent.putExtra("id", movie?.id)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, itemView.movie_poster, "movie_image")
                context.startActivity(intent, options.toBundle())
            }

        }

    }

    class NetworkStateViewHolder (view: View) : RecyclerView.ViewHolder(view) {

        fun bind(networkState: NetworkState?) {
            if (networkState != null && networkState == NetworkState.LOADING) {
                itemView.network_progress_bar.visibility = View.VISIBLE;
            }
            else  {
                itemView.network_progress_bar.visibility = View.GONE;
            }

            if (networkState != null && networkState == NetworkState.ERROR) {
                itemView.network_state_txt.visibility = View.VISIBLE;
                itemView.network_state_txt.text = networkState.msg;
            }
            else if (networkState != null && networkState == NetworkState.ENDOFLIST) {
                itemView.network_state_txt.visibility = View.VISIBLE;
                itemView.network_state_txt.text = networkState.msg;
            }
            else {
                itemView.network_state_txt.visibility = View.GONE;
            }
        }
    }


    fun setNetworkState(newNetworkState: NetworkState) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()

        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }

    }
}