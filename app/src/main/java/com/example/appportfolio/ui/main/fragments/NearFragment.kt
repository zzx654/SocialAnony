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
class NearFragment:BasePostFragment() {
    override val basePostViewModel: BasePostViewModel
        get() {
            val vm:nearPostViewModel by viewModels()
            return vm
        }
    private val viewModel:nearPostViewModel
    get() = basePostViewModel as nearPostViewModel


    //라디오버튼선택변했을때는 일단 리스트비우고시작하기

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(mRootView==null)
        {
            postAdapter?.setOnDistanceChangedListener { checkedDistance,checkedId->
                isLast=false
                srLayout.isRefreshing=true
                if(checkedDistance!=postAdapter?.checkedDistance){
                    postAdapter?.checkedDistance=checkedDistance
                    postAdapter?.checkedChip=checkedId
                    refreshPosts()
                }
            }
            postAdapter?.setheadertype(RG_HEADER)
        }
    }
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
            val distance=postAdapter?.checkedDistance
            var lastpostnum:Int?=null
            var lastpostdate:String?=null
            val curPosts=postAdapter?.currentList
            if(!refresh)
            {
                if (curPosts != null) {
                    if(curPosts.isNotEmpty()) {
                        val lastPost= curPosts.last()
                        lastpostnum=lastPost?.postnum
                        lastpostdate=lastPost?.date
                    }
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
                Toast.makeText(requireContext(),"위치 서비스를 활성화 해주세요",Toast.LENGTH_SHORT).show()
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