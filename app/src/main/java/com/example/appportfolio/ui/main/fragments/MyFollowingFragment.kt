package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.PersonAdapter
import com.example.appportfolio.data.entities.Person
import com.example.appportfolio.databinding.FragmentSearchpersonBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.BasePersonViewModel
import com.example.appportfolio.ui.main.viewmodel.MyFollowingViewModel
import com.example.appportfolio.ui.main.viewmodel.SearchPersonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyFollowingFragment:BasePersonFragment(R.layout.fragment_searchperson) {
    lateinit var binding: FragmentSearchpersonBinding
    lateinit var searchedadapter: PersonAdapter
    lateinit var followingadapter:PersonAdapter
    private var mRootView:View?=null
    override val basePersonViewModel: BasePersonViewModel
        get() {
            val vm= ViewModelProvider(requireActivity()).get(MyFollowingViewModel::class.java)
        return vm
    }
    override val searchedAdapter: PersonAdapter
        get() = searchedadapter
    override val followingAdapter: PersonAdapter?
        get() =followingadapter
    override val rvSearched: RecyclerView
        get() = binding.rvSearchedPerson

    override val edtSearch: EditText
        get() = binding.edtNick
    override val rvFollowed: RecyclerView?
        get() = binding.rvFollowedPerson
    override val loadfirstprogress: ProgressBar
        get() = binding.firstloadprogress

    protected val viewModel: MyFollowingViewModel
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
            setupFollowingRcv()
            showFollowedRv()



            setView()

            firstloading=true
            init()
            viewModel.getFollowingPersons(null,api)
            mRootView=binding.root
        }
        subscribeToObserver()

        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="팔로잉"
        super.onResume()

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home->{
                parentFragmentManager.popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setupFollowingRcv()=rvFollowed?.apply {
        adapter=followingAdapter
        layoutManager= LinearLayoutManager(requireContext())
        itemAnimator=null
        addOnScrollListener(this@MyFollowingFragment.followingscrollListener)
    }
    private val followingscrollListener= object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            val totalItemCount = recyclerView.adapter!!.itemCount - 1
            followingadapter.currentList.last().userid?.let{
                if(!recyclerView.canScrollVertically(1)&&(lastVisibleItemPosition == totalItemCount)&&isScrolling&&!followlast){
                    isScrolling=false
                    viewModel.getFollowingPersons(it,api)
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
                isLoading=false
                if(firstloading)
                    loadfirstprogress.visibility=View.GONE
                else
                {
                    var currentlist=followingadapter!!.currentList.toMutableList()
                    currentlist.removeLast()
                    followingadapter!!.submitList(currentlist)
                }
                firstloading=false
                snackbar(it)
            },
            onLoading={
                isLoading=true
                if(firstloading)
                    loadfirstprogress.visibility=View.VISIBLE
                else
                {
                    if(followingadapter!!.currentList.size>0)
                    {
                        var templist=followingadapter.currentList.toList()
                        templist+=listOf(Person(null,"","","",0))
                        followingadapter.submitList(templist)
                    }

                }
            }
        ){
            var currentlist=followingadapter.currentList.toMutableList()
            if(firstloading)
                loadfirstprogress.visibility=View.GONE
            else if(currentlist.size>0)
                currentlist.removeLast()
            firstloading=false
            handleResponse(requireContext(),it.resultCode) {
                var persons=currentlist.toList()
                when (it.resultCode) {
                    400 -> {
                        snackbar("서버 에러 발생")
                    }
                    300 -> {
                        followlast=true
                        followingadapter.submitList(persons)
                        if(persons.isNotEmpty())
                            snackbar("더이상 표시할 목록이 없습니다")
                    }
                    200 -> {
                        showFollowedRv()
                        if(firstloading)
                            persons=listOf()
                        persons+= it.persons
                        followingadapter.submitList(persons)
                    }
                    else -> null
                }
            }
        })

    }
    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}