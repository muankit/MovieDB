package `in`.saveo

import `in`.saveo.repository.MovieRepository
import `in`.saveo.repository.NetworkState
import `in`.saveo.retrofit.MovieRetroInterface
import `in`.saveo.retrofit.MovieRetrofitClient
import `in`.saveo.viewModel.MainViewModel
import android.content.Context
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.annotation.Px
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Math.abs
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    lateinit var movieRepository: MovieRepository

    // slide adapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: MovieSliderRecyclerAdapter
    private lateinit var snapHelper: SnapHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val apiService : MovieRetroInterface = MovieRetrofitClient.getClient()

        movieRepository = MovieRepository(apiService)

        viewModel = getViewModel()

        val movieAdapter = MovieRecyclerAdapter(this, this)

        val gridLayoutManager = GridLayoutManager(this, 3)

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val viewType = movieAdapter.getItemViewType(position)
                return if (viewType == movieAdapter.TYPE_MOVIE) 1
                else 3
            }
        };

        movie_rv.layoutManager = gridLayoutManager
        movie_rv.setHasFixedSize(true)
        movie_rv.adapter = movieAdapter


        // top carousel
        layoutManager = ProminentLayoutManager(this)
        adapter = MovieSliderRecyclerAdapter()
        snapHelper = PagerSnapHelper()

        movie_slider.setItemViewCacheSize(4)
        movie_slider.layoutManager = layoutManager
        movie_slider.adapter = adapter

        val spacing = resources.getDimensionPixelSize(R.dimen.carousel_spacing)
        movie_slider.addItemDecoration(LinearHorizontalSpacingDecoration(spacing))
        movie_slider.addItemDecoration(BoundsOffsetDecoration())
        snapHelper.attachToRecyclerView(movie_slider)



        viewModel.moviePagedList.observe(this, Observer {
            movieAdapter.submitList(it)
            adapter.submitList(it)
        })

        viewModel.networkState.observe(this, Observer {
            movie_err_msg.visibility = if (viewModel.listIsEmpty() && it == NetworkState.ERROR) View.VISIBLE else View.GONE

            if (!viewModel.listIsEmpty()) {
                movieAdapter.setNetworkState(it)
            }
        })

    }

    private fun getViewModel(): MainViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(movieRepository) as T
            }
        })[MainViewModel::class.java]
    }


    // custom classes for slide adapter
    class LinearHorizontalSpacingDecoration(@Px private val innerSpacing: Int) :
        RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)

            val itemPosition = parent.getChildAdapterPosition(view)

            outRect.left = if (itemPosition == 0) 0 else innerSpacing / 2
            outRect.right = if (itemPosition == state.itemCount - 1) 0 else innerSpacing / 2
        }
    }

    class BoundsOffsetDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)

            val itemPosition = parent.getChildAdapterPosition(view)

            val itemWidth = view.layoutParams.width
            val offset = (parent.width - itemWidth) / 2

            if (itemPosition == 0) {
                outRect.left = offset
            } else if (itemPosition == state.itemCount - 1) {
                outRect.right = offset
            }
        }
    }

    internal class ProminentLayoutManager(
        context: Context,

        private val minScaleDistanceFactor: Float = 1.5f,

        private val scaleDownBy: Float = 0.5f
    ) : LinearLayoutManager(context, HORIZONTAL, false) {

        private val prominentThreshold =
            context.resources.getDimensionPixelSize(R.dimen.prominent_threshold)

        override fun onLayoutCompleted(state: RecyclerView.State?) =
            super.onLayoutCompleted(state).also { scaleChildren() }

        override fun scrollHorizontallyBy(
            dx: Int,
            recycler: RecyclerView.Recycler,
            state: RecyclerView.State
        ) = super.scrollHorizontallyBy(dx, recycler, state).also {
            if (orientation == HORIZONTAL) scaleChildren()
        }

        private fun scaleChildren() {
            val containerCenter = width / 2f

            val scaleDistance = minScaleDistanceFactor * containerCenter

            var translationXForward = 0f

            for (i in 0 until childCount) {
                val child = getChildAt(i)!!

                val childCenter = (child.left + child.right) / 2f
                val distanceToCenter = abs(childCenter - containerCenter)

                child.isActivated = distanceToCenter < prominentThreshold

                val scaleDownAmount = (distanceToCenter / scaleDistance).coerceAtMost(1f)
                val scale = 1f - scaleDownBy * scaleDownAmount

                child.scaleX = scale
                child.scaleY = scale

                val translationDirection = if (childCenter > containerCenter) -1 else 1
                val translationXFromScale = translationDirection * child.width * (1 - scale) / 2f
                child.translationX = translationXFromScale + translationXForward

                translationXForward = 0f

                if (translationXFromScale > 0 && i >= 1) {
                    getChildAt(i - 1)!!.translationX += 2 * translationXFromScale

                } else if (translationXFromScale < 0) {
                    translationXForward = 2 * translationXFromScale
                }
            }
        }

        override fun getExtraLayoutSpace(state: RecyclerView.State): Int {
            return (width / (1 - scaleDownBy)).roundToInt()
        }
    }
}