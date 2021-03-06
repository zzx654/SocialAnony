package com.example.appportfolio.ui.main.fragments
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.adapters.PostAdapter
import com.example.appportfolio.databinding.FragmentPostsBinding
import com.example.appportfolio.other.Constants.RG_HEADER
import com.example.appportfolio.ui.main.viewmodel.BasePostViewModel
import com.example.appportfolio.ui.main.viewmodel.nearPostViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NearFragment:BasePostFragment(R.layout.fragment_posts) {
    lateinit var binding: FragmentPostsBinding
    private lateinit var nearpostAdapter: PostAdapter
    private var mRootView:View?=null
    override val basePostViewModel: BasePostViewModel
        get() {
            val vm:nearPostViewModel by viewModels()
            return vm
        }
    override val postAdapter: PostAdapter
        get() = nearpostAdapter
    private val viewModel:nearPostViewModel
    get() = basePostViewModel as nearPostViewModel

    override val scrollTool: FloatingActionButton
        get() = binding.fbScrollTool
    override val rvPosts: RecyclerView
        get() = binding.rvPosts
    override val loadProgressBar: ProgressBar
        get() = binding.loadProgressBar
    override val srLayout: SwipeRefreshLayout
        get() = binding.sr
    override val tvWarn: TextView
        get() = binding.tvWarn
    override val retry: TextView
        get() = binding.retry

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null)
        {
            (parentFragment as HomeFragment).binding.vp
            binding = DataBindingUtil.inflate<FragmentPostsBinding>(
                inflater,
                R.layout.fragment_posts, container, false
            )
            nearpostAdapter = PostAdapter()
            nearpostAdapter.setOnDistanceChangedListener { checkedDistance,checkedId->
                isLast=false
                srLayout.isRefreshing=true
                if(checkedDistance!=nearpostAdapter.checkedDistance){
                    nearpostAdapter.checkedDistance=checkedDistance
                    nearpostAdapter.checkedChip=checkedId
                    refreshPosts()
                }

            }
            nearpostAdapter.setheadertype(RG_HEADER)
            setView()

            mRootView=binding.root
            refreshPosts()

        }
        return mRootView
    }
    //???????????????????????????????????? ?????? ??????????????????????????????

    override fun loadNewPosts() {
        getPosts()
    }

    override fun refreshPosts() {
        getPosts(true)
    }

    fun getPosts(refresh:Boolean=false)
    {
        if(SocialApplication.checkGeoPermission(requireContext()))
        {
            val distance=postAdapter.checkedDistance
            var lastpostnum:Int?=null
            var lastpostdate:String?=null
            val curPosts=postAdapter.currentList
            if(!refresh)
            {
                if(curPosts.isNotEmpty())
                {
                    val lastPost=curPosts.last()
                    lastpostnum=lastPost.postnum
                    lastpostdate=lastPost.date
                }
            }
            if(gpsTracker.latitude!=null)
            {
                viewModel.getNearPosts(lastpostnum,lastpostdate,distance!!,gpsTracker.latitude!!,gpsTracker.longitude!!,api)
                    binding.tvWarn.visibility=View.GONE
            }
            else
            {
                if(srLayout.isRefreshing)
                    srLayout.isRefreshing=false
                Toast.makeText(requireContext(),"?????? ???????????? ????????? ????????????",Toast.LENGTH_SHORT).show()
                binding.tvWarn.visibility=View.VISIBLE
                binding.tvWarn.text=requireContext().getString(R.string.gpsdisabled)
            }
        }
        else
        {
            if(srLayout.isRefreshing)
                srLayout.isRefreshing=false
            binding.tvWarn.text=requireContext().getString(R.string.gpsdisabled)
            binding.tvWarn.visibility=View.VISIBLE
        }
    }
}