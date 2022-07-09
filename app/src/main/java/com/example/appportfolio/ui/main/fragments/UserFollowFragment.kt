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
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.adapters.PersonAdapter
import com.example.appportfolio.databinding.FragmentUsersBinding
import com.example.appportfolio.other.Constants.FOLLOWING
import com.example.appportfolio.other.Event
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.BasePersonViewModel
import com.example.appportfolio.ui.main.viewmodel.UserFollowPersonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class UserFollowFragment:BasePersonFragment(R.layout.fragment_users) {
    lateinit var binding:FragmentUsersBinding
    private var mRootView: View?=null
    private lateinit var usersAdapter: PersonAdapter
    private var userid:Int?=null
    private var firstLoad=true
    private val getInfoType
    get() = arguments?.getInt("getInfoType")
    override val basePersonViewModel: BasePersonViewModel
        get() {
            return ViewModelProvider(requireActivity())[UserFollowPersonViewModel::class.java]
        }
    protected val viewModel: UserFollowPersonViewModel
        get() = basePersonViewModel as UserFollowPersonViewModel
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
        usersAdapter.currentList.find{ person-> person.userid==curTogglinguser }?.let {
            togglingindex=usersAdapter.currentList.lastIndexOf(it)
            followingstate=0
            usersAdapter.currentList[togglingindex].apply {
                if (this.following == 1) {
                    this.following = 0
                    followingstate = 0
                } else {
                    this.following = 1
                    followingstate = 1
                }
                usersAdapter.notifyItemChanged(togglingindex)
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
            binding= DataBindingUtil.inflate(inflater,R.layout.fragment_users,container,false)
            (activity as MainActivity).setToolBarVisible("userFollowFragment")
            userid=arguments?.getInt("userid")
            usersAdapter= PersonAdapter()
            usersAdapter.setOnPersonClickListener { person->
                clickperson(person)

            }
            usersAdapter.setOnFollowClickListener { person->
                clickfollow(person)
            }
            setupPersonRv(binding.rvHotUsers,usersAdapter,usersscrollListener)
            firstloading=true
            init()
            if(userid==0)
                userid=null


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
                if(getInfoType==FOLLOWING)
                    viewModel.getFollowingPersons(null,userid,api)
                else
                    viewModel.getFollowerPersons(null,userid,api)
            }
            else{
                SocialApplication.showError(
                    requireView(),
                    requireContext(),
                    false,
                    "",
                    "다시로드",

                    ) {
                    if (getInfoType == FOLLOWING)
                        viewModel.getFollowingPersons(null, userid, api)
                    else
                        viewModel.getFollowerPersons(null, userid, api)
                }

            }
        }

    }
    private val usersscrollListener= object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            val totalItemCount = recyclerView.adapter!!.itemCount - 1
            usersAdapter.currentList.last().userid?.let{
                if(!recyclerView.canScrollVertically(1)&&(lastVisibleItemPosition == totalItemCount)&&isScrolling){
                    isScrolling=false

                    if(getInfoType==FOLLOWING)
                        viewModel.getFollowingPersons(it,userid,api)
                    else
                        viewModel.getFollowerPersons(it,userid,api)
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
        if(getInfoType== FOLLOWING)
            (activity as MainActivity).binding.title.text="팔로잉"
        else
            (activity as MainActivity).binding.title.text="팔로워"
        super.onResume()

    }
    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{
                parentFragmentManager.popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun subscribeToObserver() {
        super.subscribeToObserver()
        viewModel.getfollowPersonResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError={
                getPersonError(usersAdapter,it)
            },
            onLoading={
                getPersonLoading(usersAdapter)
            }
        ){
            getPersonSuccess(usersAdapter,it)
        })
        vmToggle.curtoggling.observe(viewLifecycleOwner){togglestates->
            togglestates.map { togglestate ->
                usersAdapter.currentList.find{person -> person.userid==togglestate.toggleuser  }?.let{
                    val togglingindex=usersAdapter.currentList.lastIndexOf(it)
                    usersAdapter.currentList[togglingindex].following=togglestate.following
                    usersAdapter.notifyItemChanged(togglingindex)
                }
                usersAdapter.currentList.find{person -> person.userid==togglestate.toggleuser  }?.let{
                    val togglingindex=usersAdapter.currentList.lastIndexOf(it)
                    usersAdapter.currentList[togglingindex].following=togglestate.following
                    usersAdapter.notifyItemChanged(togglingindex)
                }
            }
        }
    }

}