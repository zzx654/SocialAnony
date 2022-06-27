package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.showError
import com.example.appportfolio.adapters.PersonAdapter
import com.example.appportfolio.databinding.FragmentUsersBinding
import com.example.appportfolio.other.Constants
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.BasePersonViewModel
import com.example.appportfolio.ui.main.viewmodel.HotPersonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HotUsersFragment:BasePersonFragment(R.layout.fragment_users) {
    lateinit var binding:FragmentUsersBinding
    private var mRootView: View?=null
    private lateinit var hotusersAdapter: PersonAdapter
    private var firstLoad=true
    override val basePersonViewModel: BasePersonViewModel
        get(){
            val vm= ViewModelProvider(requireActivity()).get(HotPersonViewModel::class.java)
            return vm
        }
    protected val viewModel: HotPersonViewModel
        get() = basePersonViewModel as HotPersonViewModel
    override val searchedAdapter: PersonAdapter?
        get() = null
    override val edtSearch: EditText?
        get() = null
    override val loadfirstprogress: ProgressBar
        get() = binding.loadfirst
    override val rootView: View
        get() = binding.root

    override fun applyFollowingState() {
        var togglingindex:Int
        var followingstate:Int?=null
        hotusersAdapter.currentList.find{ person-> person.userid==curTogglinguser }?.let {
            togglingindex=hotusersAdapter.currentList.lastIndexOf(it)
            followingstate=0
            hotusersAdapter.currentList[togglingindex].apply {
                if (this.following == 1) {
                    this.following = 0
                    followingstate = 0
                } else {
                    this.following = 1
                    followingstate = 1
                }
                hotusersAdapter.notifyItemChanged(togglingindex)
                if(followingstate==1)
                    Toast.makeText(requireContext(), this.nickname + "님을 팔로우했습니다", Toast.LENGTH_SHORT)
                        .show()
                else
                    Toast.makeText(requireContext(), this.nickname + "님 팔로우를 해제했습니다", Toast.LENGTH_SHORT)
                        .show()
            }
        }

        followingstate?.let{
            vmToggle.setcurtoggle(curTogglinguser,it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null)
        {
            binding=DataBindingUtil.inflate(inflater,R.layout.fragment_users,container,false)
            (activity as MainActivity).setToolBarVisible("hotUsersFragment")
            hotusersAdapter= PersonAdapter()
            hotusersAdapter.setOnPersonClickListener { person->
                clickperson(person)

            }
            hotusersAdapter.setOnFollowClickListener { person->
                clickfollow(person)
            }
            setupPersonRv(binding.rvHotUsers,hotusersAdapter,hotusersscrollListener)
            firstloading=true
            init()


            mRootView=binding.root
        }
        subscribeToObserver()

        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(firstLoad){
            firstLoad=false
            if((activity as MainActivity).isConnected!!){
                viewModel.getHotUsers(null,null,api)
            }
            else{
                showError(requireView(),requireContext(),false,"","다시로드"){  viewModel.getHotUsers(null,null,api)}
            }
        }
    }
    private val hotusersscrollListener= object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            val totalItemCount = recyclerView.adapter!!.itemCount - 1
            hotusersAdapter.currentList.last().userid?.let{
                if(!recyclerView.canScrollVertically(1)&&(lastVisibleItemPosition == totalItemCount)&&isScrolling&&hotusersAdapter.currentList.size<100){
                    isScrolling=false
                    viewModel.getHotUsers(it,hotusersAdapter.currentList.last().followingcount,api)
                }
            }

        }
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                isScrolling=true
        }
    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="인기유저"
        super.onResume()

    }
    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home->{
                parentFragmentManager.popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun subscribeToObserver() {
        super.subscribeToObserver()
        viewModel.gethotUsersResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError={
                getPersonError(hotusersAdapter,it)
            },
            onLoading={
                getPersonLoading(hotusersAdapter)
            }
        ){
            getPersonSuccess(hotusersAdapter,it)
        })
        vmToggle.curtoggling.observe(viewLifecycleOwner){togglestates->
            togglestates.map { togglestate ->
                hotusersAdapter.currentList.find{person -> person.userid==togglestate.toggleuser  }?.let{
                    val togglingindex=hotusersAdapter.currentList.lastIndexOf(it)
                    hotusersAdapter.currentList[togglingindex].following=togglestate.following
                    hotusersAdapter.notifyItemChanged(togglingindex)
                }
                hotusersAdapter.currentList.find{person -> person.userid==togglestate.toggleuser  }?.let{
                    val togglingindex=hotusersAdapter.currentList.lastIndexOf(it)
                    hotusersAdapter.currentList[togglingindex].following=togglestate.following
                    hotusersAdapter.notifyItemChanged(togglingindex)
                }
            }
        }
    }

}