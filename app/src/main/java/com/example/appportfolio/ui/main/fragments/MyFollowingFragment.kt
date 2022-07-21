package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.*
import android.widget.AbsListView
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.adapters.PersonAdapter
import com.example.appportfolio.databinding.FragmentSearchpersonBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.BasePersonViewModel
import com.example.appportfolio.ui.main.viewmodel.MyFollowingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyFollowingFragment:BasePersonFragment(R.layout.fragment_searchperson),MenuProvider {
    lateinit var binding: FragmentSearchpersonBinding
    private lateinit var searchedadapter: PersonAdapter
    lateinit var followingadapter:PersonAdapter
    private var firstLoad=true
    private var mRootView:View?=null
    override val basePersonViewModel: BasePersonViewModel
        get() {
            return ViewModelProvider(requireActivity())[MyFollowingViewModel::class.java]
        }
    override val searchedAdapter: PersonAdapter
        get() = searchedadapter

    override val edtSearch: EditText
        get() = binding.edtNick
    override val loadfirstprogress: ProgressBar
        get() = binding.firstloadprogress
    override val rootView: View
        get() = binding.root

    private val viewModel: MyFollowingViewModel
        get() = basePersonViewModel as MyFollowingViewModel

    private var followlast=false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null) {
            binding = DataBindingUtil.inflate<FragmentSearchpersonBinding>(
                inflater,
                R.layout.fragment_searchperson, container, false
            )
            (activity as MainActivity).setToolBarVisible("myFollowingFragment")
            curfrag = "myFollowingFragment"
            searchedadapter = PersonAdapter()
            followingadapter = PersonAdapter()
            followingadapter.setOnPersonClickListener { person->
                clickperson(person)

            }
            followingadapter.setOnFollowClickListener { person->
                clickfollow(person)
            }
            setupPersonRv(binding.rvSearchedPerson,searchedadapter,searchedscrollListener)
            setupPersonRv(binding.rvFollowedPerson,followingadapter,followingscrollListener)
            hideSearchedRv()



            setView()

            firstloading=true
            init()


            mRootView=binding.root
        }
        subscribeToObserver()

        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(firstLoad)
        {
            firstLoad=false
            if((activity as MainActivity).isConnected!!){
                viewModel.getFollowingPersons(null,null,api)
            }
            else SocialApplication.showError(
                requireView(),
                requireContext(),
                false,
                "",
                "다시로드",

                ) { viewModel.getFollowingPersons(null, null, api) }
        }

    }
    override fun onResume() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="팔로잉"
        super.onResume()

    }

    override fun hideSearchedRv() {
        super.hideSearchedRv()
        binding.rvFollowedPerson.visibility=View.VISIBLE
        binding.rvSearchedPerson.visibility=View.GONE
    }

    override fun showSearchedRv() {
        super.showSearchedRv()
        binding.rvFollowedPerson.visibility=View.GONE
        binding.rvSearchedPerson.visibility=View.VISIBLE
    }

    override fun applyFollowingState() {
        var alerted=false
        var togglingindex:Int
        var followingstate:Int?=null
        searchedadapter.currentList.find{ person-> person.userid==curTogglinguser }?.let {
            togglingindex=searchedadapter.currentList.lastIndexOf(it)
            followingstate=0
            searchedadapter.currentList[togglingindex].apply {
                if (this.following == 1) {
                    this.following = 0
                    followingstate = 0
                } else {
                    this.following = 1
                    followingstate = 1
                }
                searchedAdapter.notifyItemChanged(togglingindex)
                if(followingstate==1)
                    Toast.makeText(requireContext(), this.nickname + "님을 팔로우했습니다", Toast.LENGTH_SHORT)
                        .show()
                else
                    Toast.makeText(requireContext(), this.nickname + "님 팔로우를 해제했습니다", Toast.LENGTH_SHORT)
                        .show()
                alerted=true
            }
        }
        followingadapter.currentList.find{ person-> person.userid==curTogglinguser }?.let {
            togglingindex=followingadapter.currentList.lastIndexOf(it)
            followingadapter.currentList[togglingindex].apply {
                if (this.following == 1) {
                    this.following = 0
                    followingstate = 0
                } else {
                    this.following = 1
                    followingstate = 1
                }
                followingadapter.notifyItemChanged(togglingindex)
                if(!alerted)
                {
                    if(followingstate==1)
                        Toast.makeText(requireContext(), this.nickname + "님을 팔로우했습니다", Toast.LENGTH_SHORT)
                            .show()
                    else
                        Toast.makeText(requireContext(), this.nickname + "님 팔로우를 해제했습니다", Toast.LENGTH_SHORT)
                            .show()
                }

            }
        }
        followingstate?.let{
            vmToggle.setcurtoggle(curTogglinguser,it)
        }

    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
    }
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            android.R.id.home->{
                parentFragmentManager.popBackStack()
                true
            }
            else->false
        }
    }
    private val followingscrollListener= object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            val totalItemCount = recyclerView.adapter!!.itemCount - 1
            followingadapter.currentList.last().userid?.let{
                if(!recyclerView.canScrollVertically(1)&&(lastVisibleItemPosition == totalItemCount)&&isScrolling&&!followlast){
                    isScrolling=false
                    viewModel.getFollowingPersons(it,null,api)
                }
            }

        }
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                isScrolling=true
        }
    }

    override fun subscribeToObserver() {
        super.subscribeToObserver()
        viewModel.getfollowingPersonResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError={
                getPersonError(followingadapter,it)
            },
            onLoading={
                getPersonLoading(followingadapter)
            }
        ){
            getPersonSuccess(followingadapter,it)
        })
        vmToggle.curtoggling.observe(viewLifecycleOwner){togglestates->
            togglestates.map { togglestate ->
                searchedadapter.currentList.find{person -> person.userid==togglestate.toggleuser  }?.let{
                    val togglingindex=searchedadapter.currentList.lastIndexOf(it)
                    searchedadapter.currentList[togglingindex].following=togglestate.following
                    searchedadapter.notifyItemChanged(togglingindex)
                }
                followingadapter.currentList.find{person -> person.userid==togglestate.toggleuser  }?.let{
                    val togglingindex=followingadapter.currentList.lastIndexOf(it)
                    followingadapter.currentList[togglingindex].following=togglestate.following
                    followingadapter.notifyItemChanged(togglingindex)
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}