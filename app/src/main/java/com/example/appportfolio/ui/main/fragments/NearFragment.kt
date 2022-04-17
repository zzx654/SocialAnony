package com.example.appportfolio.ui.main.fragments
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.adapters.PostAdapter
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.databinding.FragmentNearBinding
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.BasePostViewModel
import com.example.appportfolio.ui.main.viewmodel.nearPostViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class NearFragment: BasePostFragment(R.layout.fragment_near) {
    lateinit var binding: FragmentNearBinding
    lateinit var nearpostAdapter: PostAdapter
    override val basePostViewModel: BasePostViewModel
        get() {
            val vm:nearPostViewModel by viewModels()
            return vm
        }
    override val postAdapter: PostAdapter
        get() = nearpostAdapter
    protected val viewModel:nearPostViewModel
    get() = basePostViewModel as nearPostViewModel

    override val scrollView: NestedScrollView
        get() = binding.scrollView
    override val scrollTool: FloatingActionButton
        get() = binding.fbScrollTool
    override val rvPosts: RecyclerView
        get() = binding.rvPosts
    override val loadMoreProgressBar: ProgressBar
        get() = binding.loadMoreProgressbar
    override val loadProgressBar: ProgressBar
        get() = binding.loadProgressBar
    override val srLayout: SwipeRefreshLayout
        get() = binding.sr

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate<FragmentNearBinding>(inflater,
            R.layout.fragment_near,container,false)
        binding.rgDistance.setOnCheckedChangeListener { group, checkedId ->
            postAdapter.differ.submitList(listOf())
            viewModel.clearposts()
        }
        nearpostAdapter=PostAdapter()
        nearpostAdapter.setOntagClickListener { tag->
            findNavController().navigate(HomeFragmentDirections.actionGlobalTagPostsFragment(tag))

        }
        nearpostAdapter.setOnPostClickListener { post->
            findNavController().navigate(HomeFragmentDirections.actionGlobalPostFragment(post))
        }
        return binding.root
    }
    //라디오버튼선택변했을때는 일단 리스트비우고시작하기

    override fun loadNewPosts() {
        getPosts()
    }

    override fun refreshPosts() {
        getPosts(true)
    }
    override fun navigateToPostFragment(post: Post) {
        (activity as MainActivity).navHostFragment.navController.navigate(HomeFragmentDirections.actionGlobalPostFragment(post))
    }

    fun getPosts(refresh:Boolean=false)
    {
        if(SocialApplication.checkGeoPermission(requireContext()))
        {
            val distance=when(binding.rgDistance.checkedRadioButtonId){
                R.id.rb5->5
                R.id.rb10->10
                R.id.rb15->15
                R.id.rb20->20
                R.id.rb25->25
                else->null
            }
            var lastpostnum:Int?=null
            var lastpostdate:String?=null
            val curPosts=postAdapter.differ.currentList
            if(!refresh)
            {
                if(!curPosts.isNullOrEmpty())
                {
                    val lastPost=curPosts.last()
                    lastpostnum=lastPost.postnum
                    lastpostdate=lastPost.date
                }
            }
            if(gpsTracker.latitude!=null)
            {
                viewModel.getNearPosts(lastpostnum,lastpostdate,distance!!,gpsTracker.latitude!!,gpsTracker.longitude!!,api)
                if(scrollView.visibility==View.GONE)
                {
                    scrollView.visibility=View.VISIBLE
                    binding.tvWarn.visibility=View.GONE
                }

            }
            else
            {
                Toast.makeText(requireContext(),"위치 서비스를 활성화 해주세요",Toast.LENGTH_SHORT).show()
                srLayout.isRefreshing=false
                scrollView.visibility=View.GONE
                binding.tvWarn.visibility=View.VISIBLE
            }
        }
        else
        {
            srLayout.isRefreshing=false
            scrollView.visibility=View.GONE
            binding.tvWarn.visibility=View.VISIBLE
        }
    }
}